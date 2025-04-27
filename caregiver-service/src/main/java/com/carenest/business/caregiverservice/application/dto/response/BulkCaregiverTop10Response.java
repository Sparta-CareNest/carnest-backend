package com.carenest.business.caregiverservice.application.dto.response;

import java.util.List;

public record BulkCaregiverTop10Response(List<CaregiverGetTop10ResponseServiceDTO> responseDTOS) {
}
