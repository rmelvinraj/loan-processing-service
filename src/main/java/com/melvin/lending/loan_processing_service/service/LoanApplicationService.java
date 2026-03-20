package com.melvin.lending.loan_processing_service.service;

import com.melvin.lending.loan_processing_service.domain.entity.LoanApplication;
import com.melvin.lending.loan_processing_service.domain.enums.RiskBand;
import com.melvin.lending.loan_processing_service.domain.valueobject.EligibilityResult;
import com.melvin.lending.loan_processing_service.dto.LoanApplicationRequest;
import com.melvin.lending.loan_processing_service.dto.LoanApplicationResponse;
import com.melvin.lending.loan_processing_service.mapper.LoanApplicationMapper;
import com.melvin.lending.loan_processing_service.repository.LoanApplicationRepository;
import com.melvin.lending.loan_processing_service.result.OfferResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanApplicationService {
    private final EligibilityService eligibilityService;
    private final OfferGenerationService offerGenerationService;
    private final LoanApplicationRepository repository;
    private final LoanApplicationMapper mapper;

    @Transactional
    public LoanApplicationResponse apply(LoanApplicationRequest request) {

        log.info("Processing loan application: applicant={}, amount={}, tenure={}m",
                request.applicant().name(),
                request.loan().amount(),
                request.loan().tenureMonths());

        // DTO → Domain
        LoanApplication application = mapper.toDomain(request);

        // Eligibility check
        EligibilityResult eligibility = eligibilityService.evaluate(application);

        if (!eligibility.isEligible()) {

            application.reject(eligibility.getReasons());

            LoanApplication saved = repository.save(application);

            log.info("Application {} REJECTED at eligibility stage: reasons={}",
                    saved.getId(), eligibility.getReasons());

            return mapper.toResponse(saved);
        }

        // Offer generation
        OfferResult offerResult = offerGenerationService.generate(application);

        if (offerResult.isRejected()) {

            application.reject(offerResult.getReasons());

            LoanApplication saved = repository.save(application);

            log.info("Application {} REJECTED at offer stage: reasons={}",
                    saved.getId(), offerResult.getReasons());

            return mapper.toResponse(saved);
        }

        // Approve application
        RiskBand riskBand = RiskBand.fromCreditScore(application.getCreditScore());

        application.approve(offerResult.getOffer(), riskBand);

        LoanApplication saved = repository.save(application);

        log.info("Application {} APPROVED: riskBand={}, rate={}%, EMI={}",
                saved.getId(),
                riskBand,
                offerResult.getOffer().getInterestRate(),
                offerResult.getOffer().getEmi());

        //  Return response
        return mapper.toResponse(saved);
    }
}
