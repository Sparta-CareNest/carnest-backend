package com.carenest.business.caregiverservice.infrastructure.s3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.carenest.business.caregiverservice.config.AmazonConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AmazonS3Manager {

	private final AmazonS3Client amazonS3Client;
	private final AmazonConfig amazonConfig;

	// MultipartFile 리스트를 전달받아 S3에 업로드
	public List<String> upload(List<MultipartFile> multipartFiles, String dirName, Uuid uuid) throws IOException {
		List<String> uploadImageUrls = new ArrayList<>();
		for (MultipartFile multipartFile : multipartFiles) {
			File uploadFile = convert(multipartFile)
				.orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환 실패"));
			String uploadImageUrl = upload(uploadFile, dirName, uuid.getUuid());
			uploadImageUrls.add(uploadImageUrl);
		}
		return uploadImageUrls; // 업로드된 파일들의 S3 URL 주소 리스트 반환
	}

	private String upload(File uploadFile, String dirName, String uuid) {
		String fileName = dirName + "/" + uuid + uploadFile.getName();
		String uploadImageUrl = putS3(uploadFile, fileName);
		removeNewFile(uploadFile);  // 로컬에 생성된 File 삭제 (MultipartFile -> File 전환 하며 로컬에 파일 생성됨)
		return uploadImageUrl;      // 업로드된 파일의 S3 URL 주소 반환
	}

	private String putS3(File uploadFile, String fileName) {
		amazonS3Client.putObject(
			new PutObjectRequest(amazonConfig.getBucket(), fileName, uploadFile)
				.withCannedAcl(CannedAccessControlList.PublicRead) // PublicRead 권한으로 업로드 됨
		);
		return amazonS3Client.getUrl(amazonConfig.getBucket(), fileName).toString();
	}

	private void removeNewFile(File targetFile) {
		if (targetFile.delete()) {
			log.info("파일이 삭제되었습니다.");
		} else {
			log.info("파일이 삭제되지 못했습니다.");
		}
	}

	private Optional<File> convert(MultipartFile file) throws IOException {
		File convertFile = new File(Objects.requireNonNull(file.getOriginalFilename()));
		if (convertFile.createNewFile()) {
			try (FileOutputStream fos = new FileOutputStream(convertFile)) {
				fos.write(file.getBytes());
			}
			return Optional.of(convertFile);
		}
		return Optional.empty();
	}
}