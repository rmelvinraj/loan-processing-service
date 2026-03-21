package com.melvin.lending.loan_processing_service.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class LoanProcessingException extends RuntimeException {

  private final HttpStatus status;
  private final ErrorCode errorCode;

  protected LoanProcessingException(String message, HttpStatus status, ErrorCode errorCode) {
    super(message);
    this.status = status;
    this.errorCode = errorCode;
  }
  // Constructor for wrapping other exceptions (Throwable causes)
  protected LoanProcessingException(String message, HttpStatus status, ErrorCode errorCode, Throwable cause) {
    super(message, cause);
    this.status = status;
    this.errorCode = errorCode;
  }

}
