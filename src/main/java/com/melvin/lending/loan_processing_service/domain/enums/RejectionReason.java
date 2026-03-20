package com.melvin.lending.loan_processing_service.domain.enums;

public enum RejectionReason {
    CREDIT_SCORE_BELOW_MINIMUM,
    AGE_TENURE_LIMIT_EXCEEDED,
    EMI_EXCEEDS_60_PERCENT_OF_INCOME,

    // Used by Offer Generation
    EMI_EXCEEDS_50_PERCENT_OF_INCOME
}
