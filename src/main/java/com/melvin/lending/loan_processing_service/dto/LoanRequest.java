package com.melvin.lending.loan_processing_service.dto;

import com.melvin.lending.loan_processing_service.domain.enums.LoanPurpose;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

/**
 * Immutable DTO for loan request data - using record for:
 * - Immutable fields with auto-generated getters, equals(), hashCode(), toString()
 * - Boundary validation: @NotBlank, @NotNull, @Min/@Max, @DecimalMin reject invalid data early
 */

@Builder
public record LoanRequest(

        @NotNull(message = "Loan amount is required")
        @DecimalMin(value = "10000.00",    message = "Loan amount must be at least ₹10,000")
        @DecimalMax(value = "5000000.00",  message = "Loan amount must not exceed ₹50,00,000")
        BigDecimal amount,

        @NotNull(message = "Tenure is required")
        @Min(value = 6,   message = "Tenure must be at least 6 months")
        @Max(value = 360, message = "Tenure must not exceed 360 months")
        Integer tenureMonths,

        @NotNull(message = "Loan purpose is required (PERSONAL, HOME, AUTO)")
        LoanPurpose purpose
) {}
