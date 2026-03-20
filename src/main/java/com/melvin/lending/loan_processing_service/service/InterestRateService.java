package com.melvin.lending.loan_processing_service.service;

import com.melvin.lending.loan_processing_service.domain.entity.LoanApplication;
import com.melvin.lending.loan_processing_service.domain.enums.EmploymentType;
import com.melvin.lending.loan_processing_service.domain.enums.RiskBand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Slf4j
@Service
public class InterestRateService {

    private static final BigDecimal BASE_RATE = new BigDecimal("12.00");
    private static final BigDecimal SELF_EMPLOYED_PREMIUM = new BigDecimal("1.00");
    private static final BigDecimal LARGE_LOAN_PREMIUM = new BigDecimal("0.50");
    private static final BigDecimal LARGE_LOAN_THRESHOLD = new BigDecimal("1000000.00");

    public BigDecimal calculateAnnualRate(LoanApplication application) {

        Objects.requireNonNull(application, "application must not be null");

        RiskBand riskBand = RiskBand.fromCreditScore(application.getCreditScore());

        BigDecimal employmentPremium = employmentPremium(application.getEmploymentType());
        BigDecimal loanPremium = loanSizePremium(application.getLoanAmount());

        BigDecimal rate = BASE_RATE
                .add(riskBand.getRatePremiumPercent())
                .add(employmentPremium)
                .add(loanPremium);

        BigDecimal finalRate = rate.setScale(2, RoundingMode.HALF_UP);

        log.debug("Rate calculation: base={}, riskPremium={} ({}), employmentPremium={}, " +
                        "loanSizePremium={}, finalRate={}",
                BASE_RATE,
                riskBand.getRatePremiumPercent(),
                riskBand,
                employmentPremium,
                loanPremium,
                finalRate
        );

        return finalRate;
    }

    private BigDecimal employmentPremium(EmploymentType type) {
        return type == EmploymentType.SELF_EMPLOYED
                ? SELF_EMPLOYED_PREMIUM
                : BigDecimal.ZERO;
    }

    private BigDecimal loanSizePremium(BigDecimal loanAmount) {
        return loanAmount.compareTo(LARGE_LOAN_THRESHOLD) > 0
                ? LARGE_LOAN_PREMIUM
                : BigDecimal.ZERO;
    }
}
