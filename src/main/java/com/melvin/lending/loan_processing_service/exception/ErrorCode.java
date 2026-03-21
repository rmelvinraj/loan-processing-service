package com.melvin.lending.loan_processing_service.exception;

public enum ErrorCode {

    //  System or Validation Errors
    REQUEST_VALIDATION_FAILED,
    MALFORMED_REQUEST_BODY,
    INVALID_ARGUMENT,
    INTERNAL_SERVER_ERROR,
    APPLICATION_ALREADY_DECIDED,

    //Eligibility Rule
    CREDIT_SCORE_LOW
}
