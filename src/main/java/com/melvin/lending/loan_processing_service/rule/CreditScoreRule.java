package com.melvin.lending.loan_processing_service.rule;

import com.melvin.lending.loan_processing_service.domain.entity.LoanApplication;
import com.melvin.lending.loan_processing_service.domain.enums.RejectionReason;
import com.melvin.lending.loan_processing_service.domain.service.EligibilityRule;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CreditScoreRule implements EligibilityRule {

    private static final int MIN_CREDIT_SCORE = 600;

    @Override
    public void validate(LoanApplication app, List<RejectionReason> reasons) {
        if (app.getCreditScore() < MIN_CREDIT_SCORE) {
            reasons.add(RejectionReason.CREDIT_SCORE_BELOW_MINIMUM);
        }
    }
}
