package com.melvin.lending.loan_processing_service.rule;

import com.melvin.lending.loan_processing_service.domain.entity.LoanApplication;
import com.melvin.lending.loan_processing_service.domain.enums.RejectionReason;
import com.melvin.lending.loan_processing_service.domain.service.EligibilityRule;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AgeTenureRule implements EligibilityRule {

    private static final int MAX_AGE_AT_LOAN_END = 65;

    @Override
    public void validate(LoanApplication app, List<RejectionReason> reasons) {

        double tenureYears = app.getTenureMonths() / 12.0;
        double ageAtEnd = app.getApplicantAge() + tenureYears;

        if (ageAtEnd > MAX_AGE_AT_LOAN_END) {
            reasons.add(RejectionReason.AGE_TENURE_LIMIT_EXCEEDED);
        }
    }
}
