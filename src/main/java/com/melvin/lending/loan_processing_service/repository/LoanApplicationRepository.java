package com.melvin.lending.loan_processing_service.repository;

import com.melvin.lending.loan_processing_service.domain.entity.LoanApplication;

import java.util.UUID;
/**
 * Domain-facing repository interface for loan application persistence.
 *
 * This ensures the domain never depends on JPA, JDBC or related to DB.
 */
public interface LoanApplicationRepository {

    LoanApplication save(LoanApplication application);

    java.util.Optional<LoanApplication> findById(UUID id);
}
