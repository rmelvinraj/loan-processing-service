package com.melvin.lending.loan_processing_service.repository;

import com.melvin.lending.loan_processing_service.domain.entity.LoanApplication;
import com.melvin.lending.loan_processing_service.repository.mapper.LoanApplicationPersistenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanApplicationRepositoryAdapter implements LoanApplicationRepository {

    private final LoanApplicationJpaRepository jpaRepository;
    private final LoanApplicationPersistenceMapper mapper;

    @Override
    public LoanApplication save(LoanApplication application) {
        log.debug("Persisting loan application id={} status={}", application.getId(), application.getStatus());
        var entity = mapper.toJpaEntity(application);
        var saved  = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<LoanApplication> findById(UUID id) {
        log.debug("Fetching loan application id={}", id);
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
}
