package com.melvin.lending.loan_processing_service.rule;

import com.melvin.lending.loan_processing_service.domain.entity.LoanApplication;
import com.melvin.lending.loan_processing_service.domain.enums.RejectionReason;
import com.melvin.lending.loan_processing_service.domain.service.EligibilityRule;
import com.melvin.lending.loan_processing_service.service.EmiCalculationService;
import com.melvin.lending.loan_processing_service.service.InterestRateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * implementing EMI > 60% monthly income rejection rule
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmiToIncomeRule implements EligibilityRule {

    private static final BigDecimal EMI_TO_INCOME_LIMIT = new BigDecimal("0.60");

    private final EmiCalculationService emiCalculationService;
    private final InterestRateService interestRateService;

    @Override
    public void validate(LoanApplication app, List<RejectionReason> reasons) {

        BigDecimal rate = interestRateService.calculateAnnualRate(app);

        BigDecimal emi = emiCalculationService.calculate(
                app.getLoanAmount(),
                rate,
                app.getTenureMonths()
        );

        BigDecimal ratio = emi.divide(app.getMonthlyIncome(), 4, RoundingMode.HALF_UP);

        log.debug("EMI rule: emi={}, income={}, ratio={}", emi, app.getMonthlyIncome(), ratio);

        if (ratio.compareTo(EMI_TO_INCOME_LIMIT) > 0) {
            reasons.add(RejectionReason.EMI_EXCEEDS_60_PERCENT_OF_INCOME);
        }
    }
}
