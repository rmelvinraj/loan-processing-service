package com.melvin.lending.loan_processing_service.controller;

import com.melvin.lending.loan_processing_service.domain.enums.ApplicationStatus;
import com.melvin.lending.loan_processing_service.domain.enums.RiskBand;
import com.melvin.lending.loan_processing_service.dto.*;
import com.melvin.lending.loan_processing_service.exception.GlobalExceptionHandler;
import com.melvin.lending.loan_processing_service.service.LoanApplicationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;


import static org.mockito.Mockito.*;




import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanApplicationController.class)
@Import({GlobalExceptionHandler.class})
@DisplayName("LoanApplicationController")
class LoanApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LoanApplicationService loanApplicationService;

    private static final String URL = "/api/v1/loan-applications";

    private static final String VALID_BODY = """
            {
              "applicant": {
                "name": "Alice",
                "age": 30,
                "monthlyIncome": 80000,
                "employmentType": "SALARIED",
                "creditScore": 720
              },
              "loan": {
                "amount": 400000,
                "tenureMonths": 36,
                "purpose": "HOME"
              }
            }
            """;

    //  APPROVED APPLICATION → 201

    @Nested
    @DisplayName("APPROVED application → 201 Created")
    class ApprovedResponse {

        private LoanApplicationResponse approvedResponse() {
            return LoanApplicationResponse.builder()
                    .applicationId(UUID.randomUUID())
                    .status(ApplicationStatus.APPROVED)
                    .riskBand(RiskBand.LOW)
                    .offer(LoanOfferResponse.builder()
                            .interestRate(new BigDecimal("12.00"))
                            .tenureMonths(36)
                            .emi(new BigDecimal("13274.45"))
                            .totalPayable(new BigDecimal("477880.20"))
                            .build())
                    .build();
        }

        @Test
        @DisplayName("returns HTTP 201")
        void returns201() throws Exception {
            when(loanApplicationService.apply(any())).thenReturn(approvedResponse());

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("response body contains applicationId")
        void bodyContainsApplicationId() throws Exception {
            when(loanApplicationService.apply(any())).thenReturn(approvedResponse());

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(jsonPath("$.applicationId").exists());
        }

        @Test
        @DisplayName("response body has status APPROVED")
        void bodyHasStatusApproved() throws Exception {
            when(loanApplicationService.apply(any())).thenReturn(approvedResponse());

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(jsonPath("$.status").value("APPROVED"));
        }

        @Test
        @DisplayName("response body contains offer block with EMI")
        void bodyContainsOffer() throws Exception {
            when(loanApplicationService.apply(any())).thenReturn(approvedResponse());

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(jsonPath("$.offer.emi").value(13274.45))
                    .andExpect(jsonPath("$.offer.interestRate").value(12.00))
                    .andExpect(jsonPath("$.offer.tenureMonths").value(36))
                    .andExpect(jsonPath("$.offer.totalPayable").value(477880.20));
        }

        @Test
        @DisplayName("rejectionReasons is absent from approved response (@JsonInclude NON_NULL)")
        void rejectionReasonsAbsentFromApprovedResponse() throws Exception {
            when(loanApplicationService.apply(any())).thenReturn(approvedResponse());

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(jsonPath("$.rejectionReasons").doesNotExist());
        }
    }

    //  REJECTED APPLICATION → 422

    @Nested
    @DisplayName("REJECTED application → 422 Unprocessable Content")
    class RejectedResponse {

        private LoanApplicationResponse rejectedResponse() {
            return LoanApplicationResponse.builder()
                    .applicationId(UUID.randomUUID())
                    .status(ApplicationStatus.REJECTED)
                    .rejectionReasons(List.of(
                            "CREDIT_SCORE_BELOW_MINIMUM",
                            "AGE_TENURE_LIMIT_EXCEEDED"))
                    .build();
        }

        @Test
        @DisplayName("returns HTTP 422")
        void returns422() throws Exception {
            when(loanApplicationService.apply(any())).thenReturn(rejectedResponse());

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().is(HttpStatus.UNPROCESSABLE_CONTENT.value()));
        }

        @Test
        @DisplayName("response body has status REJECTED")
        void bodyHasStatusRejected() throws Exception {
            when(loanApplicationService.apply(any())).thenReturn(rejectedResponse());

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(jsonPath("$.status").value("REJECTED"));
        }

        @Test
        @DisplayName("response body contains rejectionReasons array")
        void bodyContainsRejectionReasons() throws Exception {
            when(loanApplicationService.apply(any())).thenReturn(rejectedResponse());

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(jsonPath("$.rejectionReasons").isArray())
                    .andExpect(jsonPath("$.rejectionReasons[0]").value("CREDIT_SCORE_BELOW_MINIMUM"));
        }

        @Test
        @DisplayName("offer block is absent from rejected response (@JsonInclude NON_NULL)")
        void offerAbsentFromRejectedResponse() throws Exception {
            when(loanApplicationService.apply(any())).thenReturn(rejectedResponse());

            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(jsonPath("$.offer").doesNotExist());
        }
    }

    // VALIDATION CONSTRAINTS

    @Nested
    @DisplayName("Bean validation constraints")
    class ValidationConstraints {

        @Test
        @DisplayName("blank applicant name → 400")
        void blankNameReturns400() throws Exception {
            String body = VALID_BODY.replace("\"Alice\"", "\"\"");
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("applicant age below 21 → 400")
        void ageBelowMinimumReturns400() throws Exception {
            String body = VALID_BODY.replace("\"age\": 30", "\"age\": 18");
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("applicant age above 60 → 400")
        void ageAboveMaximumReturns400() throws Exception {
            String body = VALID_BODY.replace("\"age\": 30", "\"age\": 65");
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("loan amount below ₹10,000 → 400")
        void loanAmountBelowMinReturns400() throws Exception {
            String body = VALID_BODY.replace("\"amount\": 400000", "\"amount\": 5000");
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("loan amount above ₹50,00,000 → 400")
        void loanAmountAboveMaxReturns400() throws Exception {
            String body = VALID_BODY.replace("\"amount\": 400000", "\"amount\": 6000000");
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("tenure below 6 months → 400")
        void tenureBelowMinReturns400() throws Exception {
            String body = VALID_BODY.replace("\"tenureMonths\": 36", "\"tenureMonths\": 3");
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("tenure above 360 months → 400")
        void tenureAboveMaxReturns400() throws Exception {
            String body = VALID_BODY.replace("\"tenureMonths\": 36", "\"tenureMonths\": 400");
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("credit score below 300 → 400")
        void creditScoreBelowMinReturns400() throws Exception {
            String body = VALID_BODY.replace("\"creditScore\": 720", "\"creditScore\": 200");
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("credit score above 900 → 400")
        void creditScoreAboveMaxReturns400() throws Exception {
            String body = VALID_BODY.replace("\"creditScore\": 720", "\"creditScore\": 950");
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("invalid employmentType enum value → 400")
        void invalidEnumReturns400() throws Exception {
            String body = VALID_BODY.replace("\"SALARIED\"", "\"FREELANCER\"");
            mockMvc.perform(post(URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }
}
