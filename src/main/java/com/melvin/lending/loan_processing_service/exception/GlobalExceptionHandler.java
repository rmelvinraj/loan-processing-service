package com.melvin.lending.loan_processing_service.exception;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // custom exception for business logic
    @ExceptionHandler(EmiCalculationException.class)
    public ResponseEntity<ErrorResponse> handleEmiCalculation(
            EmiCalculationException ex, HttpServletRequest req) {

        log.error("EMI calculation failed for request on {}: {}", req.getRequestURI(), ex.getMessage(), ex);

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.UNPROCESSABLE_CONTENT.value())
                        .error(HttpStatus.UNPROCESSABLE_CONTENT.getReasonPhrase())
                        .errorCode(ErrorCode.EMI_CALCULATION_FAILED.name())
                        .message("Loan EMI calculation failed. Please check loan parameters.")
                        .path(req.getRequestURI())
                        .traceId(generateTraceId())
                        .build());
    }

    // Handle @Valid Request Body Errors

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> new ErrorResponse.FieldError(
                        fe.getField(),
                        fe.getRejectedValue(),
                        fe.getDefaultMessage()
                ))
                .toList();

        log.warn("Validation failed: {} field error(s)", fieldErrors.size());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .errorCode(ErrorCode.REQUEST_VALIDATION_FAILED.name())
                        .message("Request validation failed")
                        .path(req.getRequestURI())
                        .traceId(generateTraceId())
                        .fieldErrors(fieldErrors)
                        .build());
    }
    //  Malformed JSON (e.g., invalid JSON syntax)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableMessage(
            HttpMessageNotReadableException ex, HttpServletRequest req) {

        log.warn("Unreadable request body: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .errorCode(ErrorCode.MALFORMED_REQUEST_BODY.name())
                        .message("Malformed or missing request body. Please ensure JSON is valid")
                        .path(req.getRequestURI())
                        .traceId(generateTraceId())
                        .build());
    }
    // Domain guard exceptions
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest req) {

        log.warn("Illegal argument: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .errorCode(ErrorCode.INVALID_ARGUMENT.name())
                        .message(ex.getMessage())
                        .path(req.getRequestURI())
                        .traceId(generateTraceId())
                        .build());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex, HttpServletRequest req) {

        log.warn("Illegal state: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.CONFLICT.value())
                        .error(HttpStatus.CONFLICT.getReasonPhrase())
                        .errorCode(ErrorCode.APPLICATION_ALREADY_DECIDED.name())
                        .message(ex.getMessage())
                        .path(req.getRequestURI())
                        .traceId(generateTraceId())
                        .build());
    }
    // Catch-all (Safety Net)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(
            Exception ex, HttpServletRequest req) {

        log.error("Unhandled exception on {}", req.getRequestURI(), ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                        .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.name())
                        .message("An unexpected error occurred. Please contact support team.")
                        .path(req.getRequestURI())
                        .traceId(generateTraceId())
                        .build());
    }
    //helper class
    private String generateTraceId() {
        // In production, integrate with Sleuth/Micrometer or pass from headers
        return UUID.randomUUID().toString().substring(0, 8);
    }

}
