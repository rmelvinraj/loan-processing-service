package com.melvin.lending.loan_processing_service.repository.entity;

import com.melvin.lending.loan_processing_service.domain.enums.ApplicationStatus;
import com.melvin.lending.loan_processing_service.domain.enums.EmploymentType;
import com.melvin.lending.loan_processing_service.domain.enums.LoanPurpose;
import com.melvin.lending.loan_processing_service.domain.enums.RiskBand;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "loan_applications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApplicationJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    // Applicant
    @Column(name = "applicant_name", nullable = false)
    private String applicantName;

    @Column(name = "applicant_age", nullable = false)
    private int applicantAge;

    @Column(name = "monthly_income", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyIncome;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false)
    private EmploymentType employmentType;

    @Column(name = "credit_score", nullable = false)
    private int creditScore;

    // Loan request
    @Column(name = "loan_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal loanAmount;

    @Column(name = "tenure_months", nullable = false)
    private int tenureMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_purpose", nullable = false)
    private LoanPurpose loanPurpose;

    // Decision
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ApplicationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_band")
    private RiskBand riskBand;

    // Offer (nullable — only present on APPROVED)
    @Column(name = "interest_rate", precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "emi", precision = 19, scale = 2)
    private BigDecimal emi;

    @Column(name = "total_payable", precision = 19, scale = 2)
    private BigDecimal totalPayable;

    // Rejection reasons (nullable — only present on REJECTED)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "loan_rejection_reasons", joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "reason")
    private List<String> rejectionReasons;

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt;

    @Column(name = "decided_at")
    private Instant decidedAt;
}
