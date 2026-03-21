package com.melvin.lending.loan_processing_service.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

/**
 * Standardized error response envelope returned for every error.
 * Structure ->
 *
 *  "timestamp":
 *  "status":
 *  "error":
 *  "errorCode":
 *  "message":
 *  "path":
 *  "traceId":
 *  "fieldErrors": [{
 *        "field":
 *        "rejectedValue":
 *        "message":
 *     }]
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private final Instant timestamp;

    private final int status;
    private final String error;
    private final String errorCode;
    private final String message;
    private final String path;
    private final String traceId;
    private final List<FieldError> fieldErrors;

    private ErrorResponse(Builder builder) {
        this.timestamp = builder.timestamp;
        this.status = builder.status;
        this.error = builder.error;
        this.errorCode = builder.errorCode;
        this.message = builder.message;
        this.path = builder.path;
        this.traceId = builder.traceId;
        this.fieldErrors = builder.fieldErrors == null ? null : List.copyOf(builder.fieldErrors);
    }

    /**
     * Validation error detail.
     */
    public record FieldError(
            String field,
            Object rejectedValue,
            String message
    ) {
    }

    // Builder

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Instant timestamp = Instant.now();
        private int status;
        private String error;
        private String errorCode;
        private String message;
        private String path;
        private String traceId;
        private List<FieldError> fieldErrors;

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder status(int status) {
            this.status = status;
            return this;
        }

        public Builder error(String error) {
            this.error = error;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public Builder traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        public Builder fieldErrors(List<FieldError> fieldErrors) {
            this.fieldErrors = fieldErrors;
            return this;
        }

        public ErrorResponse build() {
            return new ErrorResponse(this);
        }
    }
}
