package com.melvin.lending.loan_processing_service.repository.mapper;

import com.melvin.lending.loan_processing_service.config.LoanProcessingServiceMapperConfig;
import com.melvin.lending.loan_processing_service.domain.entity.LoanApplication;
import com.melvin.lending.loan_processing_service.domain.enums.RejectionReason;
import com.melvin.lending.loan_processing_service.domain.valueobject.LoanOffer;
import com.melvin.lending.loan_processing_service.repository.entity.LoanApplicationJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Objects;

@Mapper(config = LoanProcessingServiceMapperConfig.class)
public interface LoanApplicationPersistenceMapper {

    @Mapping(target = "interestRate",
            expression = "java(domain.getOffer() != null ? domain.getOffer().getInterestRate() : null)")
    @Mapping(target = "emi",
            expression = "java(domain.getOffer() != null ? domain.getOffer().getEmi() : null)")
    @Mapping(target = "totalPayable",
            expression = "java(domain.getOffer() != null ? domain.getOffer().getTotalPayable() : null)")
    @Mapping(target = "rejectionReasons",
            expression = "java(mapReasonsToStrings(domain.getRejectionReasons()))")
    LoanApplicationJpaEntity toJpaEntity(LoanApplication domain);

    @Mapping(target = "offer", expression = "java(mapOffer(entity))")
    @Mapping(target = "rejectionReasons",
            expression = "java(mapStringsToReasons(entity.getRejectionReasons()))")
    LoanApplication toDomain(LoanApplicationJpaEntity entity);

    default LoanOffer mapOffer(LoanApplicationJpaEntity entity) {
        if (entity.getInterestRate() == null || entity.getEmi() == null) {
            return null;
        }
        return LoanOffer.builder()
                .interestRate(entity.getInterestRate())
                .tenureMonths(entity.getTenureMonths())
                .emi(entity.getEmi())
                .totalPayable(entity.getTotalPayable())
                .build();
    }

    default List<String> mapReasonsToStrings(List<RejectionReason> reasons) {
        if (reasons == null) return null;
        return reasons.stream().map(Enum::name).toList();
    }

    default List<RejectionReason> mapStringsToReasons(List<String> reasons) {
        if (reasons == null) return null;

        return reasons.stream()
                .map(reason -> {
                    try {
                        return RejectionReason.valueOf(reason);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
