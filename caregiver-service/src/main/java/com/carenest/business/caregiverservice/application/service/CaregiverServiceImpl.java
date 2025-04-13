package com.carenest.business.caregiverservice.application.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.carenest.business.caregiverservice.application.dto.mapper.CaregiverApplicationMapper;
import com.carenest.business.caregiverservice.application.dto.request.CaregiverCreateRequestServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverCreateResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverReadResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverSearchResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverUpdateResponseServiceDTO;
import com.carenest.business.caregiverservice.config.AmazonConfig;
import com.carenest.business.caregiverservice.domain.model.Caregiver;
import com.carenest.business.caregiverservice.domain.model.category.CategoryLocation;
import com.carenest.business.caregiverservice.domain.model.category.CategoryService;
import com.carenest.business.caregiverservice.domain.service.CaregiverDomainService;
import com.carenest.business.caregiverservice.exception.CaregiverException;
import com.carenest.business.caregiverservice.exception.ErrorCode;
import com.carenest.business.caregiverservice.infrastructure.repository.CaregiverRepository;
import com.carenest.business.caregiverservice.infrastructure.repository.CategoryLocationRepository;
import com.carenest.business.caregiverservice.infrastructure.repository.CategoryServiceRepository;
import com.carenest.business.caregiverservice.infrastructure.repository.UuidRepository;
import com.carenest.business.caregiverservice.infrastructure.s3.AmazonS3Manager;
import com.carenest.business.caregiverservice.infrastructure.s3.Uuid;
import com.carenest.business.caregiverservice.presentation.dto.request.CaregiverUpdateRequestDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaregiverServiceImpl implements CaregiverService {

	private final CaregiverRepository caregiverRepository;
	private final CategoryServiceRepository categoryServiceRepository;
	private final CategoryLocationRepository categoryLocationRepository;
	private final UuidRepository uuidRepository;
	private final CaregiverDomainService caregiverDomainService;
	private final AmazonS3Manager amazonS3Manager;
	private final AmazonConfig amazonConfig;


	@Qualifier("caregiverApplicationMapper")
	private final CaregiverApplicationMapper applicationMapper;

	@Override
	@Transactional
	public CaregiverCreateResponseServiceDTO createCaregiver(CaregiverCreateRequestServiceDTO requestServiceDTO,
		List<MultipartFile> multipartFiles) {

		// TODO: userId() 검증 로직을 추가해야함.

		List<String> uploadUrls = null;
		if (multipartFiles != null && !multipartFiles.isEmpty()) {
			try {
				// 예: S3 업로드 시 사용할 디렉토리와 고유 식별자 생성
				String dirName = amazonConfig.getReviewPath();
				Uuid saveUuid = uuidRepository.save(Uuid.builder().uuid(UUID.randomUUID().toString()).build());
				// 인프라 어댑터를 통해 파일 업로드 및 URL 리스트 반환
				uploadUrls = amazonS3Manager.upload(multipartFiles, dirName, saveUuid);

			} catch (IOException e) {
				// 업로드 실패에 대한 예외 처리
				log.error("업로드 실패: ",e);
				throw new CaregiverException(ErrorCode.UPLOAD_IMAGE_FAILED);
			}
		}
		if (uploadUrls != null && !uploadUrls.isEmpty()) {
			requestServiceDTO = requestServiceDTO.withImageUrls(uploadUrls);
		}

		// 카테고리 서비스/지역 검증
		List<CategoryService> categoryServices = categoryServiceRepository.findAllById(
			requestServiceDTO.categoryServiceIds());
		if (categoryServices.size() != requestServiceDTO.categoryServiceIds().size()) {
			throw new CaregiverException(ErrorCode.NOT_FOUND_SERVICES);
		}

		List<CategoryLocation> categoryLocations = categoryLocationRepository.findAllById(
			requestServiceDTO.categoryLocationIds());
		if (categoryLocations.size() != requestServiceDTO.categoryLocationIds().size()) {
			throw new CaregiverException(ErrorCode.NOT_FOUND_LOCATION);
		}

		Caregiver caregiver = caregiverDomainService.createCaregiverWithCategories(requestServiceDTO, categoryServices,
			categoryLocations,uploadUrls);

		Caregiver saveCaregiver = caregiverRepository.save(caregiver);

		return applicationMapper.toCreateResponseServiceDTO(saveCaregiver.getId());
	}


	@Override
	@Transactional(readOnly = true)
	public CaregiverReadResponseServiceDTO getCaregiver(UUID caregiverId) {
		// 1. N+1 문제로 fetchJoin 으로 가져옴
		Caregiver caregiver = caregiverRepository.findCaregiverWithCategories(caregiverId)
			.orElseThrow(() -> new CaregiverException(ErrorCode.NOT_FOUND));

		// 2. DTO 이름으로 변환하기 위해
		List<String> categoryServiceNames = caregiver.getCaregiverCategoryServices()
			.stream()
			.map(n -> n.getCategoryService().getName())
			.toList();

		List<String> categoryLocationNames = caregiver.getCaregiverCategoryLocations()
			.stream()
			.map(n -> n.getCategoryLocation().getName())
			.toList();

		return new CaregiverReadResponseServiceDTO(caregiver.getId(), caregiver.getUserId(), caregiver.getDescription(),
			caregiver.getRating(), caregiver.getExperienceYears(), caregiver.getPricePerHour(),
			caregiver.getPricePerDay(), caregiver.getApprovalStatus(), caregiver.getGender(), categoryServiceNames,
			categoryLocationNames);
	}

	@Override
	@Transactional
	public CaregiverUpdateResponseServiceDTO updateCaregiver(UUID caregiverId, CaregiverUpdateRequestDTO dto) {
		Caregiver caregiver = caregiverRepository.findById(caregiverId)
			.orElseThrow(() -> new CaregiverException(ErrorCode.NOT_FOUND));

		// 필드 수정
		if (dto.description() != null)
			caregiver.setDescription(dto.description());
		if (dto.experienceYears() != null)
			caregiver.setExperienceYears(dto.experienceYears());
		if (dto.pricePerHour() != null)
			caregiver.setPricePerHour(dto.pricePerHour());
		if (dto.pricePerDay() != null)
			caregiver.setPricePerDay(dto.pricePerDay());

		// category 조회
		List<CategoryService> categoryServices =
			dto.categoryServiceIds() != null ? categoryServiceRepository.findAllById(dto.categoryServiceIds()) : null;

		List<CategoryLocation> categoryLocations =
			dto.categoryLocationIds() != null ? categoryLocationRepository.findAllById(dto.categoryLocationIds()) :
				null;

		caregiverDomainService.updateCaregiverCategories(caregiver, categoryServices, categoryLocations);

		// 응답 변환
		List<String> categoryServiceNames = caregiver.getCaregiverCategoryServices()
			.stream()
			.map(n -> n.getCategoryService().getName())
			.toList();

		List<String> categoryLocationNames = caregiver.getCaregiverCategoryLocations()
			.stream()
			.map(n -> n.getCategoryLocation().getName())
			.toList();

		return new CaregiverUpdateResponseServiceDTO(caregiver.getId(), caregiver.getUserId(),
			caregiver.getDescription(), caregiver.getRating(), caregiver.getExperienceYears(),
			caregiver.getPricePerHour(), caregiver.getPricePerDay(), caregiver.getApprovalStatus(),
			caregiver.getGender(), categoryServiceNames, categoryLocationNames);
	}

	@Override
	@Transactional
	public void deleteCaregiver(UUID caregiverId) {

		Caregiver caregiver = caregiverRepository.findById(caregiverId)
			.orElseThrow(() -> new CaregiverException(ErrorCode.NOT_FOUND));
		caregiverDomainService.deleteCaregiverWithAssociations(caregiverId, caregiver);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<CaregiverSearchResponseServiceDTO> searchCaregiver(String location, String service, Pageable pageable) {
		Page<Caregiver> caregivers = caregiverRepository.searchByConditions(location, service, pageable);


		return caregivers.map(caregiver -> new CaregiverSearchResponseServiceDTO(
			caregiver.getId(),
			caregiver.getDescription(),
			caregiver.getRating(),
			caregiver.getExperienceYears(),
			caregiver.getPricePerHour(),
			caregiver.getPricePerDay(),
			caregiver.getGender()
		));
	}

	@Override
	@Transactional
	public void updateCaregiverStatus(UUID id, boolean approvalStatusCheck) {
		Caregiver caregiver = caregiverRepository.findById(id).orElseThrow(()->
			new CaregiverException(ErrorCode.NOT_FOUND));

		caregiverDomainService.caregiverApprovalStatusCheck(caregiver,approvalStatusCheck);
	}

	@Override
	public Boolean existsById(UUID id) {
		return caregiverRepository.existsById(id);
	}

	@Override
	public Page<CaregiverReadResponseServiceDTO> getCaregiverAll(Pageable pageable) {

		Page<Caregiver> caregivers = caregiverRepository.findAllCaregivers(pageable);

		return caregivers.map(caregiver -> new CaregiverReadResponseServiceDTO(
			caregiver.getId(),
			caregiver.getUserId(),
			caregiver.getDescription(),
			caregiver.getRating(),
			caregiver.getExperienceYears(),
			caregiver.getPricePerHour(),
			caregiver.getPricePerDay(),
			caregiver.getApprovalStatus(),
			caregiver.getGender(),
			caregiver.getCaregiverCategoryServices()
				.stream()
				.map(cs -> cs.getCategoryService().getName())
				.toList(),
			caregiver.getCaregiverCategoryLocations()
				.stream()
				.map(cl -> cl.getCategoryLocation().getName())
				.toList()
		));
	}

}
