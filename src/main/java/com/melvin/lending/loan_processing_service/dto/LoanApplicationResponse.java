package com.melvin.lending.loan_processing_service.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.melvin.lending.loan_processing_service.domain.enums.ApplicationStatus;
import com.melvin.lending.loan_processing_service.domain.enums.RiskBand;
import lombok.Builder;

import java.util.List;
import java.util.UUID;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoanApplicationResponse(
        UUID applicationId,
        ApplicationStatus status,
        RiskBand riskBand,
        LoanOfferResponse offer,
        List<String> rejectionReasons
) {}
