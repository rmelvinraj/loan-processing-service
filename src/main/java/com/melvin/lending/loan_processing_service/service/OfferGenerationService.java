package com.melvin.lending.loan_processing_service.service;

import com.melvin.lending.loan_processing_service.domain.entity.LoanApplication;
import com.melvin.lending.loan_processing_service.domain.enums.RejectionReason;
import com.melvin.lending.loan_processing_service.domain.valueobject.LoanOffer;
import com.melvin.lending.loan_processing_service.result.OfferResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OfferGenerationService {

    private static final BigDecimal EMI_TO_INCOME_OFFER_LIMIT = new BigDecimal("0.50");

    private final InterestRateService interestRateService;
    private final EmiCalculationService emiCalculationService;

    /**
     * Generate loan offer or return rejection result.
     */
    public OfferResult generate(LoanApplication application) {

        BigDecimal annualRate = interestRateService.calculateAnnualRate(application);

        BigDecimal emi = emiCalculationService.calculate(
                application.getLoanAmount(),
                annualRate,
                application.getTenureMonths()
        );

        BigDecimal emiRatio = emi.divide(
                application.getMonthlyIncome(),
                4,
                RoundingMode.HALF_UP
        );

        //  Offer rejection condition
        if (emiRatio.compareTo(EMI_TO_INCOME_OFFER_LIMIT) > 0) {

            BigDecimal percentage = emiRatio
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(1, RoundingMode.HALF_UP);

            log.info("Offer rejected for applicationId={}: EMI {} ({}%) exceeds 50% income cap",
                    application.getId(), emi, percentage);

            return OfferResult.rejected(
                    List.of(RejectionReason.EMI_EXCEEDS_50_PERCENT_OF_INCOME)
            );
        }

        //  Build offer
        BigDecimal totalPayable = emi
                .multiply(BigDecimal.valueOf(application.getTenureMonths()))
                .setScale(2, RoundingMode.HALF_UP);

        LoanOffer offer = LoanOffer.builder()
                .interestRate(annualRate)
                .tenureMonths(application.getTenureMonths())
                .emi(emi)
                .totalPayable(totalPayable)
                .build();

        log.debug("Offer generated for applicationId={}: rate={}%, EMI={}, total={}",
                application.getId(), annualRate, emi, totalPayable);

        return OfferResult.approved(offer);
    }
}
