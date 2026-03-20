package com.melvin.lending.loan_processing_service.domain.valueobject;

import java.math.BigDecimal;

public class LoanOffer {

    BigDecimal interestRate;
    int        tenureMonths;
    BigDecimal emi;
    BigDecimal totalPayable;
}
