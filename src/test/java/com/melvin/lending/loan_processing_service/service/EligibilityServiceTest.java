package com.melvin.lending.loan_processing_service.service;

import com.melvin.lending.loan_processing_service.domain.entity.LoanApplication;
import com.melvin.lending.loan_processing_service.domain.enums.EmploymentType;
import com.melvin.lending.loan_processing_service.domain.enums.LoanPurpose;
import com.melvin.lending.loan_processing_service.domain.enums.RejectionReason;
import com.melvin.lending.loan_processing_service.domain.service.EligibilityRule;
import com.melvin.lending.loan_processing_service.domain.valueobject.EligibilityResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EligibilityService — evaluation of loan applications")
class EligibilityServiceTest {

    @Mock
    private EligibilityRule rule1;

    @Mock
    private EligibilityRule rule2;

    private EligibilityService service;

    @BeforeEach
    void setUp() {
        List<EligibilityRule> rules = List.of(rule1, rule2);
        service = new EligibilityService(rules);
    }

    //
    // Passing scenario — all rules pass
    //
    @Nested
    @DisplayName("Passing applications")
    class PassingApplications {

        @Test
        @DisplayName("no rejection reasons added → EligibilityResult passes")
        void noRejectionReasons_passes() {
            // Arrange
            LoanApplication application = defaultApplication();

            // Mocks do nothing by default, which simulates a "Pass"
            // They do not add anything to the rejection list

            // Act
            EligibilityResult result = service.evaluate(application);

            // Assert
            assertThat(result.isEligible()).isTrue();
            assertThat(result.isRejected()).isFalse();
            assertThat(result.getReasons()).isEmpty();

            // Verify interactions
            verify(rule1).validate(any(), anyList());
            verify(rule2).validate(any(), anyList());
        }
    }

    //
    // Failing scenario — rule adds rejection
    //
    @Nested
    @DisplayName("Failing applications")
    class FailingApplications {

        @Test
        @DisplayName("rejection reason added → EligibilityResult fails")
        void rejectionReasonAdded_fails() {
            // Arrange
            LoanApplication application = defaultApplication();

            //  Rule 1 failing by adding a reason to the list
            doAnswer(invocation -> {
                List<RejectionReason> reasons = invocation.getArgument(1);
                reasons.add(RejectionReason.CREDIT_SCORE_BELOW_MINIMUM);
                return null;
            }).when(rule1).validate(any(), any());

            // Act
            EligibilityResult result = service.evaluate(application);

            // Assert
            assertThat(result.isEligible()).isFalse();
            assertThat(result.isRejected()).isTrue();
            assertThat(result.getReasons())
                    .hasSize(1)
                    .containsExactly(RejectionReason.CREDIT_SCORE_BELOW_MINIMUM);
        }
    }
    //  Single rule rejects
    @Nested
    @DisplayName("Single rule rejects")
    class SingleRuleRejects {

        @Test
        @DisplayName("rejection reason added → EligibilityResult fails")
        void singleReason_fails() {
            // Arrange
            LoanApplication app = defaultApplication();
            willReject(rule1, RejectionReason.CREDIT_SCORE_BELOW_MINIMUM);

            // Act
            EligibilityResult result = service.evaluate(app);

            // Assert
            assertThat(result.isEligible()).isFalse();
            assertThat(result.isRejected()).isTrue();
            assertThat(result.getReasons())
                    .containsExactly(RejectionReason.CREDIT_SCORE_BELOW_MINIMUM);
        }
    }

    //  Multiple rules reject
    @Nested
    @DisplayName("Multiple rules reject")
    class MultipleRulesReject {

        @Test
        @DisplayName("aggregates rejection reasons from all failing rules")
        void aggregatesAllReasons() {
            // Arrange
            LoanApplication app = defaultApplication();
            willReject(rule1, RejectionReason.CREDIT_SCORE_BELOW_MINIMUM);
            willReject(rule2, RejectionReason.AGE_TENURE_LIMIT_EXCEEDED);

            // Act
            EligibilityResult result = service.evaluate(app);

            // Assert
            assertThat(result.isEligible()).isFalse();
            assertThat(result.getReasons())
                    .containsExactlyInAnyOrder(
                            RejectionReason.CREDIT_SCORE_BELOW_MINIMUM,
                            RejectionReason.AGE_TENURE_LIMIT_EXCEEDED
                    );
        }
    }

    //  Invocation discipline
    @Nested
    @DisplayName("Rule invocation discipline")
    class RuleInvocation {

        @Test
        @DisplayName("every registered rule is called exactly once per evaluation")
        void allRulesCalledOnce() {
            // Arrange
            LoanApplication app = defaultApplication();

            // Act
            service.evaluate(app);

            // Assert
            verify(rule1, times(1)).validate(eq(app), anyList());
            verify(rule2, times(1)).validate(eq(app), anyList());
        }

        @Test
        @DisplayName("evaluation is fail-all — continues past first failing rule")
        void doesNotShortCircuitAfterFirstFailure() {
            // Arrange
            LoanApplication app = defaultApplication();
            willReject(rule1, RejectionReason.CREDIT_SCORE_BELOW_MINIMUM);

            // Act
            service.evaluate(app);

            // Assert
            verify(rule2).validate(eq(app), anyList());
        }

        @Test
        @DisplayName("no rules registered → always eligible")
        void noRulesRegisteredAlwaysEligible() {
            EligibilityResult result = new EligibilityService(List.of()).evaluate(defaultApplication());
            assertThat(result.isEligible()).isTrue();
            assertThat(result.getReasons()).isEmpty();
        }
    }

    //  Application reference
    @Nested
    @DisplayName("Application reference forwarded correctly")
    class ApplicationForwarding {

        @Test
        @DisplayName("the same application instance is passed to each rule")
        void sameInstanceForwarded() {
            // Arrange
            LoanApplication app = defaultApplication();

            // Act
            service.evaluate(app);

            // Assert
            verify(rule1).validate(eq(app), anyList());
            verify(rule2).validate(eq(app), anyList());
        }
    }

    //
    // Helper Methods
    //

    private LoanApplication defaultApplication() {
        return new LoanApplication(
                UUID.randomUUID(),
                "Kiran Raj",
                31,
                new BigDecimal("75000.00"),
                EmploymentType.SALARIED,
                750,
                new BigDecimal("50000.00"),
                12,
                LoanPurpose.HOME,
                Instant.now()
        );
    }
    private void willReject(EligibilityRule rule, RejectionReason reason) {
        doAnswer(inv -> {
            List<RejectionReason> reasons = inv.getArgument(1);
            reasons.add(reason);
            return null;
        }).when(rule).validate(any(), anyList());
    }
}