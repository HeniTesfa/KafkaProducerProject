

package com.kafka;

import com.kafka.events.EventEnvelope;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class ProducerService {
    private final KafkaTemplate<String,String> template;
    // binary producer for Avro or envelope payloads
    private final KafkaTemplate<String, byte[]> byteTemplate;

    public ProducerService(KafkaTemplate<String,String> template,
                           KafkaTemplate<String, byte[]> byteTemplate,
                           @Value("${kafka.transaction-id-prefix:prod-}") String txPrefix,
                           @Value("${kafka.avro-transaction-id-prefix:avro-prod-}") String avroTxPrefix) {
        this.template = template;
        this.byteTemplate = byteTemplate;
        // enable transactional if needed
        this.template.setTransactionIdPrefix(txPrefix);
        this.byteTemplate.setTransactionIdPrefix(avroTxPrefix);
    }

    public void send(String topic, String key, String value) {
        // synchronous send for demo (not recommended in high-speed env)
        template.send(topic, key, value);
    }

    public void sendAvro(String topic, String key, SpecificRecordBase record) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            SpecificDatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(record.getSchema());
            writer.write(record, encoder);
            encoder.flush();
            byteTemplate.send(topic, key, out.toByteArray());
        } catch (IOException e) {
            // log serialization error
        }
    }

    /**
     * Build and publish a generic envelope. Payload must be serialized to bytes
     * beforehand (e.g. Avro, JSON, etc.).
     */
    public void sendEnvelope(String topic, String key, byte[] payloadBytes, String eventType,
                             String aggregateId, String aggregateType) {
        EventEnvelope env = EventEnvelope.builder()
                .eventId(UUID.randomUUID())
                .eventType(eventType)
                .eventVersion(1)
                .aggregateId(aggregateId)
                .aggregateType(aggregateType)
                .eventTime(Instant.now())
                .sourceService("producer-service")
                .payload(payloadBytes)
                .metadata(Map.of())
                .schemaVersion(1)
                .build();
        // Here you would serialize Envelope (e.g. using Jackson/Avro) and send via kafkaTemplate
        try {
            byte[] bytes = serializeEnvelope(env);
            // transactional send example
            byteTemplate.executeInTransaction(t -> {
                t.send(topic, key, bytes);
                // additional operations could go here (e.g. DB writes)
                return null;
            });
        } catch (IOException e) {
            // log
        }
    }

    private byte[] serializeEnvelope(EventEnvelope env) throws IOException {
        // simple JSON serialization as example
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        return mapper.writeValueAsBytes(env);
    }
}
