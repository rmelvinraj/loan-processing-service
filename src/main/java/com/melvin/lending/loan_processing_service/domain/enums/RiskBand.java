package com.melvin.lending.loan_processing_service.domain.enums;

import java.math.BigDecimal;

public enum RiskBand {
    LOW(new BigDecimal("0.00")),
    MEDIUM(new BigDecimal("1.50")),
    HIGH(new BigDecimal("3.00"));

    private final BigDecimal ratePremiumPercent;

    RiskBand(BigDecimal ratePremiumPercent) {
        this.ratePremiumPercent = ratePremiumPercent;
    }

    public BigDecimal getRatePremiumPercent() {
        return ratePremiumPercent;
    }

    /**
     * Classify a credit score into a risk band.
     * Credit Score Risk Band
     *   750+ LOW
     *   650–749 MEDIUM
     *   600–649 HIGH
     * Scores below 600 are ineligible and handled upstream by the eligibility rules.
     */
    public static RiskBand fromCreditScore(int creditScore) {
        if (creditScore >= 750) return LOW;
        if (creditScore >= 650) return MEDIUM;
        return HIGH;
    }
}
