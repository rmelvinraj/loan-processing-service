package com.melvin.lending.loan_processing_service.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record LoanOfferResponse(
        BigDecimal interestRate,
        int        tenureMonths,
        BigDecimal emi,
        BigDecimal totalPayable
) {}
