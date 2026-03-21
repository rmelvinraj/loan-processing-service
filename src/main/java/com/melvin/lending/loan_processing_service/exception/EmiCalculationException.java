package com.melvin.lending.loan_processing_service.exception;

import org.springframework.http.HttpStatus;

public class EmiCalculationException extends LoanProcessingException {

    private EmiCalculationException(String message, HttpStatus status, ErrorCode errorCode) {
        super(message, status, errorCode);
    }

    private EmiCalculationException(String message, HttpStatus status, ErrorCode errorCode, Throwable cause) {
        super(message, status, errorCode, cause);
    }

    // Factory methods for common EMI failure scenarios

    public static EmiCalculationException invalidPrincipal() {
        return new EmiCalculationException(
                "Principal must be greater than zero",
                HttpStatus.UNPROCESSABLE_CONTENT,
                ErrorCode.INVALID_ARGUMENT
        );
    }

    public static EmiCalculationException invalidTenure(int tenureMonths) {
        return new EmiCalculationException(
                "Tenure must be greater than zero months. Provided: " + tenureMonths,
                HttpStatus.UNPROCESSABLE_CONTENT,
                ErrorCode.INVALID_ARGUMENT
        );
    }

    public static EmiCalculationException zeroDenominator() {
        return new EmiCalculationException(
                "Invalid EMI calculation: denominator became zero",
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.EMI_CALCULATION_FAILED
        );
    }

    public static EmiCalculationException calculationError(Throwable cause) {
        return new EmiCalculationException(
                "Failed to calculate EMI: " + cause.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.EMI_CALCULATION_FAILED,
                cause
        );
    }
}
