package com.melvin.lending.loan_processing_service.domain.entity;

import com.melvin.lending.loan_processing_service.domain.enums.*;
import com.melvin.lending.loan_processing_service.domain.valueobject.LoanOffer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Getter
public class LoanApplication {

    private final UUID id;

    // Applicant
    private final String applicantName;
    private final int applicantAge;
    private final BigDecimal monthlyIncome;
    private final EmploymentType employmentType;
    private final int creditScore;

    // Loan request
    private final BigDecimal loanAmount;
    private final int tenureMonths;
    private final LoanPurpose loanPurpose;

    // Decision outcome
    private ApplicationStatus status;
    private RiskBand riskBand;
    private LoanOffer offer;
    private List<RejectionReason> rejectionReasons;

    // Auditing trail
    private final Instant appliedAt; //  application submission time
    private Instant decidedAt;  // Set when approve() or reject() called

    public LoanApplication(
            UUID id,
            String applicantName,
            int applicantAge,
            BigDecimal monthlyIncome,
            EmploymentType employmentType,
            int creditScore,
            BigDecimal loanAmount,
            int tenureMonths,
            LoanPurpose loanPurpose,
            Instant appliedAt){
        this.id = id;
        this.applicantName = applicantName;
        this.applicantAge = applicantAge;
        this.monthlyIncome = monthlyIncome;
        this.employmentType = employmentType;
        this.creditScore = creditScore;
        this.loanAmount = loanAmount;
        this.tenureMonths = tenureMonths;
        this.loanPurpose = loanPurpose;
        this.appliedAt = appliedAt;

        this.status = ApplicationStatus.PENDING;
    }

    public static LoanApplication create(
            String applicantName,
            int applicantAge,
            BigDecimal monthlyIncome,
            EmploymentType employmentType,
            int creditScore,
            BigDecimal loanAmount,
            int tenureMonths,
            LoanPurpose loanPurpose
    ) {
        return new LoanApplication(
                UUID.randomUUID(),
                applicantName,
                applicantAge,
                monthlyIncome,
                employmentType,
                creditScore,
                loanAmount,
                tenureMonths,
                loanPurpose,
                Instant.now()
        );
    }

    public void approve(LoanOffer offer, RiskBand riskBand) {
        requirePending();

        this.offer = Objects.requireNonNull(offer, "offer must not be null");
        this.riskBand = Objects.requireNonNull(riskBand, "riskBand must not be null");

        this.status = ApplicationStatus.APPROVED;
        this.decidedAt = Instant.now();
    }

    public void reject(List<RejectionReason> reasons) {
        requirePending();

        if (reasons == null || reasons.isEmpty()) {
            throw new IllegalArgumentException("Rejection must include at least one reason");
        }

        this.rejectionReasons = List.copyOf(reasons);
        this.status = ApplicationStatus.REJECTED;
        this.decidedAt = Instant.now();
    }

    public boolean isApproved() {
        return ApplicationStatus.APPROVED == status;
    }

    public boolean isRejected() {
        return ApplicationStatus.REJECTED == status;
    }
    private void requirePending() {
        if (status != ApplicationStatus.PENDING) {
            throw new IllegalStateException(
                    "Application %s already decided: %s".formatted(id, status)
            );
        }
    }
    // Domain validation - independent of infrastructure/services
    private void validate() {
        Objects.requireNonNull(applicantName, "applicantName must not be null");
        Objects.requireNonNull(monthlyIncome, "monthlyIncome must not be null");
        Objects.requireNonNull(employmentType, "employmentType must not be null");
        Objects.requireNonNull(loanAmount, "loanAmount must not be null");
        Objects.requireNonNull(loanPurpose, "loanPurpose must not be null");

        if (applicantAge < 21 || applicantAge > 60) {
            throw new IllegalArgumentException("Applicant age must be between 21 and 60");
        }

        if (creditScore < 300 || creditScore > 900) {
            throw new IllegalArgumentException("Credit score must be between 300 and 900");
        }

        if (monthlyIncome.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Monthly income must be greater than 0");
        }

        if (loanAmount.compareTo(new BigDecimal("10000")) < 0 ||
                loanAmount.compareTo(new BigDecimal("5000000")) > 0) {
            throw new IllegalArgumentException("Loan amount must be between 10,000 and 50,00,000 ");
        }

        if (tenureMonths < 6 || tenureMonths > 360) {
            throw new IllegalArgumentException("Tenure must be between 6 and 360 months");
        }
    }

}
