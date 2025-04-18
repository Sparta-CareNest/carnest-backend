package com.carenest.business.caregiverservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.carenest.business.caregiverservice.application.dto.request.CaregiverCreateRequestServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.BulkCaregiverTop10Response;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverCreateResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverReadResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverSearchResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverUpdateResponseServiceDTO;
import com.carenest.business.caregiverservice.presentation.dto.request.CaregiverUpdateRequestDTO;

public interface CaregiverService {

	CaregiverCreateResponseServiceDTO createCaregiver(CaregiverCreateRequestServiceDTO requestServiceDTO,
		List<MultipartFile> multipartFiles, UUID userId);

	CaregiverReadResponseServiceDTO getCaregiver(UUID userId);

	CaregiverUpdateResponseServiceDTO updateCaregiver(UUID userId, CaregiverUpdateRequestDTO dto);

	void deleteCaregiver(UUID caregiverId);

	Page<CaregiverSearchResponseServiceDTO> searchCaregiver(String location, String service, Pageable pageable);

	void updateCaregiverStatus(UUID id, boolean approvalStatusCheck);

	Boolean existsById(UUID id);

	Page<CaregiverReadResponseServiceDTO> getCaregiverAll(Pageable pageable);

	BulkCaregiverTop10Response getTop10Caregiver();

	CaregiverReadResponseServiceDTO getCaregiverDetailUser(UUID caregiverId);
}
