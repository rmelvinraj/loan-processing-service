package com.melvin.lending.loan_processing_service.repository;

import com.melvin.lending.loan_processing_service.repository.entity.LoanApplicationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LoanApplicationJpaRepository
        extends JpaRepository<LoanApplicationJpaEntity, UUID> {
}
