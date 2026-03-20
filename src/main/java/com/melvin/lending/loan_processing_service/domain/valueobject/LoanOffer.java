package com.melvin.lending.loan_processing_service.domain.valueobject;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class LoanOffer {

    BigDecimal interestRate;
    int        tenureMonths;
    BigDecimal emi;
    BigDecimal totalPayable;
}
