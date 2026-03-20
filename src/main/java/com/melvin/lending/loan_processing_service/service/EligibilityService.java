package com.melvin.lending.loan_processing_service.service;

import com.melvin.lending.loan_processing_service.domain.entity.LoanApplication;
import com.melvin.lending.loan_processing_service.domain.enums.RejectionReason;
import com.melvin.lending.loan_processing_service.domain.service.EligibilityRule;
import com.melvin.lending.loan_processing_service.domain.valueobject.EligibilityResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class EligibilityService {

    private final List<EligibilityRule> rules;

    public EligibilityResult evaluate(LoanApplication application) {

        log.debug("Evaluating eligibility for applicationId={}", application.getId());

        List<RejectionReason> reasons = new ArrayList<>();

        // Apply all rules
        for (EligibilityRule rule : rules) {
            rule.validate(application, reasons);
        }

        if (reasons.isEmpty()) {
            log.debug("Eligibility PASSED for applicationId={}", application.getId());
            return EligibilityResult.pass();
        }

        log.info("Eligibility FAILED for applicationId={}, reasons={}",
                application.getId(), reasons);

        return EligibilityResult.fail(reasons);
    }
}
