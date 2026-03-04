package com.kafka.model;

import lombok.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    private String orderId;

    @NotNull
    private String supplierId;

    @NotNull
    private String siteId;

    @NotNull
    private List<OrderLine> orderLines;

    @DecimalMin("0.0")
    private BigDecimal totalAmount;

    @NotBlank
    private String currency;

    @NotNull
    private OrderStatus orderStatus;

    @NotNull
    private Instant orderDate;

    public enum OrderStatus { CREATED, APPROVED, SHIPPED, COMPLETED }
}