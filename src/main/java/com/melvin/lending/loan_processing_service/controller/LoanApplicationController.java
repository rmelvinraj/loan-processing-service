package com.melvin.lending.loan_processing_service.controller;

import com.melvin.lending.loan_processing_service.domain.enums.ApplicationStatus;
import com.melvin.lending.loan_processing_service.dto.LoanApplicationRequest;
import com.melvin.lending.loan_processing_service.dto.LoanApplicationResponse;
import com.melvin.lending.loan_processing_service.service.LoanApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/loan-applications")
@RequiredArgsConstructor
public class LoanApplicationController {

    private final LoanApplicationService loanApplicationService;

    /**
     * Submits loan application for eligibility check and decision.
     * Returns 201 (APPROVED), 422 (REJECTED), or 400 (validation error).
     */
    @PostMapping
    public ResponseEntity<LoanApplicationResponse> apply(
            @Valid @RequestBody LoanApplicationRequest request) {

        log.info("Received loan application from applicant='{}', amount={}, tenure={}m",
                request.applicant().name(),
                request.loan().amount(),
                request.loan().tenureMonths());

        LoanApplicationResponse response = loanApplicationService.apply(request);

        HttpStatus httpStatus = response.status() == ApplicationStatus.APPROVED
                ? HttpStatus.CREATED
                : HttpStatus.UNPROCESSABLE_CONTENT;

        return ResponseEntity
                .status(httpStatus)
                .body(response);
    }
}
