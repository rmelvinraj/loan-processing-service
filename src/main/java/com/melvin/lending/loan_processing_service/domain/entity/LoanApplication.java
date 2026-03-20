package com.melvin.lending.loan_processing_service.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Getter
public class LoanApplication {

    private final UUID id;

    public LoanApplication(UUID id){
        this.id = id;
    }
}
