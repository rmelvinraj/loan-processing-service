package com.melvin.lending.loan_processing_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;


@Builder
public record LoanApplicationRequest(

        @NotNull(message = "Applicant details are required")
        @Valid
        ApplicantRequest applicant,

        @NotNull(message = "Loan details are required")
        @Valid
        LoanRequest loan
) {}
