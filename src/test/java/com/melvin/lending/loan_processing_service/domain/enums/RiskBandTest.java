package com.melvin.lending.loan_processing_service.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;


import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Testing classification of credit score into a risk band.
 * Credit Score Risk Band
 *   750+ LOW
 *   650–749 MEDIUM
 *   600–649 HIGH
 * Scores below 600 are ineligible and handled upstream by the eligibility rules.
 */

@DisplayName("RiskBand — credit score classification")
class RiskBandTest {

    // LOW risk band (score ≥ 750)

    @Nested
    @DisplayName("LOW risk band (score ≥ 750)")
    class Low {

        @ParameterizedTest(name = "score {0} → LOW")
        @ValueSource(ints = {750, 800, 850, 900})
        @DisplayName("Scores ≥ 750 should be classified as LOW risk")
        void scoresAtOrAbove750AreLow(int score) {
            // Act
            RiskBand result = RiskBand.fromCreditScore(score);

            // Assert
            assertThat(result).isEqualTo(RiskBand.LOW);
        }

        @Test
        @DisplayName("exact boundary 750 → LOW")
        void exactBoundary750() {
            RiskBand result = RiskBand.fromCreditScore(750);
            assertThat(result).isEqualTo(RiskBand.LOW);
        }

        @Test
        @DisplayName("LOW premium is 0.00%")
        void lowPremiumIsZero() {
            BigDecimal premium = RiskBand.LOW.getRatePremiumPercent();
            assertThat(premium).usingComparator(BigDecimal::compareTo)
                    .isEqualByComparingTo(new BigDecimal("0.00"));
        }
    }

    // MEDIUM risk band (650 ≤ score ≤ 749)

    @Nested
    @DisplayName("MEDIUM risk band (650 ≤ score ≤ 749)")
    class Medium {

        @ParameterizedTest(name = "score {0} → MEDIUM")
        @ValueSource(ints = {650, 700, 720, 749})
        void scoresBetween650And749AreMedium(int score) {
            RiskBand result = RiskBand.fromCreditScore(score);
            assertThat(result).isEqualTo(RiskBand.MEDIUM);
        }

        @Test
        @DisplayName("lower boundary 650 → MEDIUM")
        void lowerBoundary650() {
            RiskBand result = RiskBand.fromCreditScore(650);
            assertThat(result).isEqualTo(RiskBand.MEDIUM);
        }

        @Test
        @DisplayName("upper boundary 749 (one below LOW) → MEDIUM")
        void upperBoundary749() {
            RiskBand result = RiskBand.fromCreditScore(749);
            assertThat(result).isEqualTo(RiskBand.MEDIUM);
        }

        @Test
        @DisplayName("MEDIUM premium is 1.50%")
        void mediumPremiumIsOnePointFive() {
            BigDecimal premium = RiskBand.MEDIUM.getRatePremiumPercent();
            assertThat(premium).usingComparator(BigDecimal::compareTo)
                    .isEqualByComparingTo(new BigDecimal("1.50"));
        }
    }

    // HIGH risk band (score < 650)

    @Nested
    @DisplayName("HIGH risk band (600 ≤ score ≤ 649)")
    class High {

        @ParameterizedTest(name = "score {0} → HIGH")
        @ValueSource(ints = {600, 620, 640, 649})
        @DisplayName("Scores 600–649 should be classified as HIGH risk")
        void scoresBetween600And649AreHigh(int score) {
            RiskBand result = RiskBand.fromCreditScore(score);
            assertThat(result).isEqualTo(RiskBand.HIGH);
        }

        @Test
        @DisplayName("HIGH premium is 3.00%")
        void highPremiumIsThreePercent() {
            BigDecimal premium = RiskBand.HIGH.getRatePremiumPercent();
            assertThat(premium).usingComparator(BigDecimal::compareTo)
                    .isEqualByComparingTo(new BigDecimal("3.00"));
        }
    }

    // Premium ordering invariant
    @Nested
    @DisplayName("Premium ordering invariant")
    class PremiumOrdering {

        @Test
        @DisplayName("LOW < MEDIUM < HIGH premiums")
        void premiumsAscendWithRisk() {
            BigDecimal low = RiskBand.LOW.getRatePremiumPercent();
            BigDecimal medium = RiskBand.MEDIUM.getRatePremiumPercent();
            BigDecimal high = RiskBand.HIGH.getRatePremiumPercent();

            assertThat(low).usingComparator(BigDecimal::compareTo).isLessThan(medium);
            assertThat(medium).usingComparator(BigDecimal::compareTo).isLessThan(high);
        }
    }

    // Arrange -> Complete boundary sweep
    @ParameterizedTest(name = "score {0} → {1}")
    @CsvSource({
            "600, HIGH",
            "649, HIGH",
            "650, MEDIUM",
            "749, MEDIUM",
            "750, LOW",
            "900, LOW"
    })
    @DisplayName("complete boundary sweep matches specification for valid scores")
    void completeBoundarySweep(int score, RiskBand expected) {
        RiskBand result = RiskBand.fromCreditScore(score);
        assertThat(result).isEqualTo(expected);
    }
}