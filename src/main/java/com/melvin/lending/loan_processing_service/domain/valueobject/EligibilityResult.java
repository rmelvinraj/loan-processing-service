package com.melvin.lending.loan_processing_service.domain.valueobject;

import com.melvin.lending.loan_processing_service.domain.enums.RejectionReason;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class EligibilityResult {

    private final boolean eligible;
    private final List<RejectionReason> reasons;

    public static EligibilityResult pass() {
        return new EligibilityResult(true, List.of());
    }

    public static EligibilityResult fail(List<RejectionReason> reasons) {
        if (reasons == null || reasons.isEmpty()) {
            throw new IllegalArgumentException("A failed eligibility result must carry at least one reason");
        }
        return new EligibilityResult(false, List.copyOf(reasons));
    }

    public boolean isRejected() {
        return !eligible;
    }
}