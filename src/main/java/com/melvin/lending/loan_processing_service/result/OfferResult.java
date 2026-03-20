package com.melvin.lending.loan_processing_service.result;

import com.melvin.lending.loan_processing_service.domain.enums.RejectionReason;
import com.melvin.lending.loan_processing_service.domain.valueobject.LoanOffer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class OfferResult {

    private final boolean approved;
    private final LoanOffer offer;
    private final List<RejectionReason> reasons;

    public static OfferResult approved(LoanOffer offer) {
        return new OfferResult(true, offer, List.of());
    }

    public static OfferResult rejected(List<RejectionReason> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            throw new IllegalArgumentException("Rejection must have at least one reason");
        }
        return new OfferResult(false, null, List.copyOf(reasons));
    }

    public boolean isRejected() {
        return !approved;
    }
}
