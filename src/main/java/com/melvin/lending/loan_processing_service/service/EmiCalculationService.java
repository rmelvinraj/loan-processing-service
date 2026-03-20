package com.melvin.lending.loan_processing_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Calculates the Equated Monthly Instalment (EMI) using the formula:
 *
 *   EMI = P × r × (1 + r)^n / ((1 + r)^n − 1)
 *
 * Where:
 *   P = principal (loan amount)
 *   r = monthly interest rate = annualRate / 12 / 100
 *   n = tenure in months
 */

@Slf4j
@Service
public class EmiCalculationService {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    // High precision for intermediate calculations
    private static final MathContext MC = new MathContext(20, ROUNDING);

    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final BigDecimal TWELVE = new BigDecimal("12");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    public BigDecimal calculate(BigDecimal principal, BigDecimal annualRatePercent, int tenureMonths) {

        log.debug("Calculating EMI: principal={}, annualRate={}%, tenure={}m",
                principal, annualRatePercent, tenureMonths);


        // Handle zero interest rate (edge case)
        if (annualRatePercent.compareTo(BigDecimal.ZERO) == 0) {
            BigDecimal emi = principal.divide(BigDecimal.valueOf(tenureMonths), SCALE, ROUNDING);
            log.debug("Zero interest rate detected, EMI={}", emi);
            return emi;
        }

        // Monthly rate: r = annualRate / 12 / 100
        BigDecimal monthlyRate = annualRatePercent
                .divide(HUNDRED.multiply(TWELVE), MC);

        // (1 + r)^n
        BigDecimal onePlusR = ONE.add(monthlyRate, MC);
        BigDecimal onePlusRpowN = onePlusR.pow(tenureMonths, MC);

        // EMI formula EMI = P × r × (1 + r)^n / (1 + r)^n − 1
        BigDecimal numerator = principal
                .multiply(monthlyRate, MC)
                .multiply(onePlusRpowN, MC);

        BigDecimal denominator = onePlusRpowN.subtract(ONE, MC);

        BigDecimal emi = numerator.divide(denominator, SCALE, ROUNDING);

        log.debug("Calculated EMI={}", emi);
        return emi;
    }

}
