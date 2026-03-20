package com.melvin.lending.loan_processing_service.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;
/**
 * MapStruct global configuration for loan processing service mappers.
 * Enforces strict mapping contracts:
 * - Spring integration for dependency injection
 * - ERROR on unmapped target properties (prevents silent data loss)
 * - WARN on unmapped source properties (allows extensions)
 */
@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.WARN
)
public interface LoanProcessingServiceMapperConfig {

}
