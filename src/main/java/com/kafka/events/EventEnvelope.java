package com.kafka.events;

import lombok.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@With
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EventEnvelope implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull UUID eventId;
    @NotNull String eventType;
    @NotNull int eventVersion;
    @NotNull String aggregateId;
    @NotNull String aggregateType;
    @NotNull Instant eventTime;
    @NotNull String sourceService;
    String correlationId;
    String causationId;
    String traceId;
    int schemaVersion;
    String tenantId;
    @NotNull byte[] payload;
    @NotNull Map<String, String> metadata;
}
