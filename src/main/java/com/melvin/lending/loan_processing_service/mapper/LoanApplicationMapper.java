package com.melvin.lending.loan_processing_service.mapper;

import com.melvin.lending.loan_processing_service.config.LoanProcessingServiceMapperConfig;
import com.melvin.lending.loan_processing_service.domain.entity.LoanApplication;
import com.melvin.lending.loan_processing_service.domain.enums.RejectionReason;
import com.melvin.lending.loan_processing_service.domain.valueobject.LoanOffer;
import com.melvin.lending.loan_processing_service.dto.LoanApplicationRequest;
import com.melvin.lending.loan_processing_service.dto.LoanApplicationResponse;
import com.melvin.lending.loan_processing_service.dto.LoanOfferResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Mapper(config = LoanProcessingServiceMapperConfig.class)
public interface LoanApplicationMapper {

    // Request → Domain

    @Mapping(target = "id",             expression = "java(generateId())")
    @Mapping(target = "applicantName",  source = "request.applicant.name")
    @Mapping(target = "applicantAge",   source = "request.applicant.age")
    @Mapping(target = "monthlyIncome",  source = "request.applicant.monthlyIncome")
    @Mapping(target = "employmentType", source = "request.applicant.employmentType")
    @Mapping(target = "creditScore",    source = "request.applicant.creditScore")
    @Mapping(target = "loanAmount",     source = "request.loan.amount")
    @Mapping(target = "tenureMonths",   source = "request.loan.tenureMonths")
    @Mapping(target = "loanPurpose",    source = "request.loan.purpose")
    @Mapping(target = "appliedAt",      expression = "java(now())")
    @Mapping(target = "status",         ignore = true)
    @Mapping(target = "riskBand",       ignore = true)
    @Mapping(target = "offer",          ignore = true)
    @Mapping(target = "rejectionReasons", ignore = true)
    @Mapping(target = "decidedAt",      ignore = true)
    LoanApplication toDomain(LoanApplicationRequest request);

    // Domain → Response

    @Mapping(target = "rejectionReasons",
            expression = "java(mapRejectionReasons(application.getRejectionReasons()))")
    @Mapping(target = "offer", source = "offer")   // uses toOfferResponse(LoanOffer) below
    LoanApplicationResponse toResponse(LoanApplication application);

    LoanOfferResponse toOfferResponse(LoanOffer offer);

    // Helpers of this Mapper class

    default UUID generateId() {
        return UUID.randomUUID();
    }

    default Instant now() {
        return Instant.now();
    }

    default List<String> mapRejectionReasons(List<RejectionReason> reasons) {
        if (reasons == null) return null;
        return reasons.stream().map(Enum::name).toList();
    }
}
