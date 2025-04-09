package com.carenest.business.caregiverservice.application.service;

import java.util.UUID;

import com.carenest.business.caregiverservice.application.dto.request.CaregiverCreateRequestServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverCreateResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverReadResponseServiceDTO;
import com.carenest.business.caregiverservice.application.dto.response.CaregiverUpdateResponseServiceDTO;
import com.carenest.business.caregiverservice.presentation.dto.request.CaregiverUpdateRequestDTO;

public interface CaregiverService {

	CaregiverCreateResponseServiceDTO createCaregiver(CaregiverCreateRequestServiceDTO requestServiceDTO);

	CaregiverReadResponseServiceDTO getCaregiver(UUID caregiverId);

	CaregiverUpdateResponseServiceDTO updateCaregiver(UUID caregiverId, CaregiverUpdateRequestDTO dto);

	void deleteCaregiver(UUID caregiverId);
}
