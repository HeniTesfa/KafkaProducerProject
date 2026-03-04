package com.kafka.events;

import lombok.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import com.kafka.model.Shipment;

@Data
@With
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ShipmentEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull UUID eventId;
    @NotNull String eventType;
    @NotNull Instant eventTime;

    @NotNull String aggregateId;
    @NotNull String aggregateType;

    @NotNull com.kafka.model.Shipment payload;
}
