package com.kafka.model;

import lombok.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Shipment implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    private String shipmentId;

    @NotNull
    private String orderId;

    @NotBlank
    private String carrier;

    @NotBlank
    private String trackingNumber;

    @NotNull
    private ShipmentStatus status;

    private Instant shippedAt;

    private Instant deliveredAt;

    public enum ShipmentStatus { CREATED, DISPATCHED, IN_TRANSIT, DELIVERED }
}