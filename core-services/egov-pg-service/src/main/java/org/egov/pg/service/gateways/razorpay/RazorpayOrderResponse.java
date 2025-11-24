package org.egov.pg.service.gateways.razorpay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class RazorpayOrderResponse {
    private String id;
    private String entity;
    private Integer amount;
    private String currency;
    private String receipt;
    private String status;
    private Integer attempts;

    @JsonProperty("created_at")
    private Long createdAt;

    private Map<String, String> notes;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
class RazorpayPaymentResponse {
    private String id;
    private String entity;
    private Integer amount;
    private String currency;
    private String status;

    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("invoice_id")
    private String invoiceId;

    private Boolean international;
    private String method;

    @JsonProperty("amount_refunded")
    private Integer amountRefunded;

    @JsonProperty("refund_status")
    private String refundStatus;

    private Boolean captured;
    private String description;

    @JsonProperty("card_id")
    private String cardId;

    private String bank;
    private String wallet;
    private String vpa;
    private String email;
    private String contact;

    private Map<String, String> notes;

    private Integer fee;
    private Integer tax;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_description")
    private String errorDescription;

    @JsonProperty("created_at")
    private Long createdAt;
}