package com.melvin.lending.loan_processing_service.service;

import com.melvin.lending.loan_processing_service.exception.EmiCalculationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmiCalculationService")
class EmiCalculationServiceTest {
    private final EmiCalculationService service = new EmiCalculationService();



    //  Formula correctness
    @Nested
    @DisplayName("Formula correctness — reference EMIs")
    class FormulaCorrectness {

        @ParameterizedTest(name = "P={0}, rate={1}%, tenure={2}m → EMI≈{3}")
        @CsvSource({
                "100000,12.00,12",
                "500000,10.50,60",
                "1000000,14.00,120",
                "250000,9.00,24",
                "300000,12.00,36",
                "750000,13.50,84"
        })
        @DisplayName("EMI matches service calculation exactly")
        void matchesService(BigDecimal principal, BigDecimal rate, int tenure) {
            BigDecimal expected = calculateExpectedEmi(principal, rate, tenure);
            BigDecimal actual = service.calculate(principal, rate, tenure);
            assertThat(actual).isEqualByComparingTo(expected);
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCases {

        @Test
        @DisplayName("zero rate → EMI = principal ÷ tenure")
        void zeroRateDividesPrincipalByTenure() {
            BigDecimal principal = new BigDecimal("120000");
            int tenure = 12;
            BigDecimal expected = principal.divide(BigDecimal.valueOf(tenure), 2, RoundingMode.HALF_UP);
            BigDecimal actual = calculate("120000", "0", tenure);
            assertThat(actual).isEqualByComparingTo(expected);
        }

        @Test
        @DisplayName("single-month tenure → EMI calculation correct")
        void singleMonthTenure() {
            BigDecimal expected = calculateExpectedEmi(new BigDecimal("100000"), new BigDecimal("12.00"), 1);
            BigDecimal actual = calculate("100000", "12.00", 1);
            assertThat(actual).isEqualByComparingTo(expected);
        }

        @Test
        @DisplayName("maximum tenure produces positive EMI")
        void maxTenureProducesPositiveEmi() {
            BigDecimal actual = calculate("5000000", "12.00", 360);
            assertThat(actual).isGreaterThan(BigDecimal.ZERO);
        }
    }

    // Scale & rounding
    @Nested
    @DisplayName("Scale and rounding")
    class ScaleAndRounding {

        @Test
        @DisplayName("EMI scale = 2 always")
        void resultScaleIsTwo() {
            BigDecimal actual = calculate("175000", "11.50", 36);
            assertThat(actual.scale()).isEqualTo(2);
        }

        @Test
        @DisplayName("zero-rate EMI also scale = 2")
        void zeroRateScaleIsTwo() {
            BigDecimal actual = calculate("100000", "0", 3);
            assertThat(actual.scale()).isEqualTo(2);
        }
    }

    //  Logical testing
    @Nested
    @DisplayName("Logical testing — EMI behavior")
    class LogicalTesting {

        @Test
        @DisplayName("longer tenure → lower EMI")
        void longerTenureProducesLowerEmi() {
            BigDecimal emi24 = calculate("500000", "12.00", 24);
            BigDecimal emi48 = calculate("500000", "12.00", 48);
            assertThat(emi24).isGreaterThan(emi48);
        }

        @Test
        @DisplayName("higher rate → higher EMI")
        void higherRateProducesHigherEmi() {
            BigDecimal emiLow = calculate("500000", "10.00", 36);
            BigDecimal emiHigh = calculate("500000", "15.00", 36);
            assertThat(emiHigh).isGreaterThan(emiLow);
        }

        @Test
        @DisplayName("higher principal → higher EMI")
        void higherPrincipalProducesHigherEmi() {
            BigDecimal emiSmall = calculate("200000", "12.00", 36);
            BigDecimal emiBig = calculate("500000", "12.00", 36);
            assertThat(emiBig).isGreaterThan(emiSmall);
        }
    }

    //  Guard clauses
    @Nested
    @DisplayName("Guard clauses — invalid inputs")
    class GuardClauses {

        @Test
        @DisplayName("null principal → throws EmiCalculationException")
        void nullPrincipalThrows() {
            assertThatThrownBy(() -> service.calculate(null, new BigDecimal("12.00"), 12))
                    .isInstanceOf(EmiCalculationException.class)
                    .hasMessage("Principal must be greater than zero");
        }

        @Test
        @DisplayName("zero or negative principal → throws EmiCalculationException")
        void zeroOrNegativePrincipalThrows() {
            assertThatThrownBy(() -> service.calculate(BigDecimal.ZERO, new BigDecimal("12.00"), 12))
                    .isInstanceOf(EmiCalculationException.class)
                    .hasMessage("Principal must be greater than zero");
            assertThatThrownBy(() -> service.calculate(new BigDecimal("-100000"), new BigDecimal("12.00"), 12))
                    .isInstanceOf(EmiCalculationException.class);
        }

        @Test
        @DisplayName("zero or negative tenure → throws EmiCalculationException")
        void invalidTenureThrows() {
            assertThatThrownBy(() -> service.calculate(new BigDecimal("100000"), new BigDecimal("12.00"), 0))
                    .isInstanceOf(EmiCalculationException.class)
                    .hasMessage("Tenure must be greater than zero months. Provided: 0");
            assertThatThrownBy(() -> service.calculate(new BigDecimal("100000"), new BigDecimal("12.00"), -6))
                    .isInstanceOf(EmiCalculationException.class);
        }

        @Test
        @DisplayName("null annual rate → throws EmiCalculationException")
        void nullRateThrows() {
            assertThatThrownBy(() -> service.calculate(new BigDecimal("100000"), null, 12))
                    .isInstanceOf(EmiCalculationException.class)
                    .hasMessageContaining("Annual interest rate cannot be null");
        }
    }


    // helper
    private BigDecimal calculateExpectedEmi(BigDecimal principal, BigDecimal annualRatePercent, int tenureMonths) {
        MathContext mc = new MathContext(20, RoundingMode.HALF_UP);
        BigDecimal one = BigDecimal.ONE;
        BigDecimal twelveHundred = new BigDecimal("1200"); // 12*100
        BigDecimal monthlyRate = annualRatePercent.divide(twelveHundred, mc);
        BigDecimal onePlusR = one.add(monthlyRate, mc);
        BigDecimal onePlusRpowN = onePlusR.pow(tenureMonths, mc);
        BigDecimal numerator = principal.multiply(monthlyRate, mc).multiply(onePlusRpowN, mc);
        BigDecimal denominator = onePlusRpowN.subtract(one, mc);
        BigDecimal emi = numerator.divide(denominator, 20, RoundingMode.HALF_UP); // high precision
        return emi.setScale(2, RoundingMode.HALF_UP); // round to 2 decimals like service
    }
    private BigDecimal calculate(String principal, String rate, int tenure) {
        return service.calculate(new BigDecimal(principal), new BigDecimal(rate), tenure);
    }


}