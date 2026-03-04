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
public class Item implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    private String itemId;

    @NotBlank
    private String sku;

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String category;

    @NotBlank
    private String brand;

    @NotNull
    private ItemStatus status;

    private BigDecimal weight;
    private String weightUnit;

    @NotNull
    private Instant createdAt;

    @NotNull
    private Instant updatedAt;

    public enum ItemStatus { ACTIVE, DISCONTINUED }
}