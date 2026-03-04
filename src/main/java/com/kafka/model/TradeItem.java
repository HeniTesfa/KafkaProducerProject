package com.kafka.model;

import lombok.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@With
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    private String tradeItemId;

    @NotNull
    private String itemId;

    @NotNull
    private String supplierId;

    @DecimalMin("0.0")
    private BigDecimal costPrice;

    @DecimalMin("0.0")
    private BigDecimal retailPrice;

    @NotBlank
    private String currency;

    @NotNull
    private Integer minimumOrderQuantity;

    @NotNull
    private TradeItemStatus status;

    @NotNull
    private Instant effectiveFrom;

    private Instant effectiveTo;

    public enum TradeItemStatus { ACTIVE, INACTIVE }
}