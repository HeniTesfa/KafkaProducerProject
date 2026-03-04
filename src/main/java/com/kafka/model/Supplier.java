package com.kafka.model;

import lombok.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;

@Data
@With
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Supplier implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    private String supplierId;

    @NotBlank
    private String supplierName;

    private String taxId;
    private String contactEmail;
    private String contactPhone;

    @NotNull
    private Address address;

    @NotNull
    private SupplierStatus status;

    @NotNull
    private Instant createdAt;

    public enum SupplierStatus { ACTIVE, SUSPENDED }
}