package com.kafka.events;

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

    @NotNull String tradeItemId;
    @NotNull Integer quantity;
    @DecimalMin("0.0") BigDecimal unitPrice;
}
