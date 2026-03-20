package com.melvin.lending.loan_processing_service.dto;

import com.melvin.lending.loan_processing_service.domain.enums.EmploymentType;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Immutable DTO for loan applicant data - using record for:
 * - Immutable fields with auto-generated getters, equals(), hashCode(), toString()
 * - Boundary validation: @NotBlank, @NotNull, @Min/@Max, @DecimalMin reject invalid data early
 */
@Builder
public record ApplicantRequest(

        @NotBlank(message = "Applicant name must not be blank")
        String name,

        @NotNull(message = "Age is required")
        @Min(value = 21, message = "Applicant must be at least 21 years old")
        @Max(value = 60, message = "Applicant must not be older than 60 years")
        Integer age,

        @NotNull(message = "Monthly income is required")
        @DecimalMin(value = "0.01", message = "Monthly income must be greater than 0")
        BigDecimal monthlyIncome,

        @NotNull(message = "Employment type is required (SALARIED or SELF_EMPLOYED)")
        EmploymentType employmentType,

        @NotNull(message = "Credit score is required")
        @Min(value = 300, message = "Credit score must be at least 300")
        @Max(value = 900, message = "Credit score must not exceed 900")
        Integer creditScore
) {}
