package com.melvin.lending.loan_processing_service.domain.service;

import com.melvin.lending.loan_processing_service.domain.entity.LoanApplication;
import com.melvin.lending.loan_processing_service.domain.enums.RejectionReason;

import java.util.List;

public interface EligibilityRule {

    void validate(LoanApplication application, List<RejectionReason> reasons);
}
