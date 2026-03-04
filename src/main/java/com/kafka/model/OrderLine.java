package com.kafka.model;

import lombok.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderLine implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    private String tradeItemId;

    @NotNull
    private Integer quantity;

    @DecimalMin("0.0")
    private BigDecimal unitPrice;
}