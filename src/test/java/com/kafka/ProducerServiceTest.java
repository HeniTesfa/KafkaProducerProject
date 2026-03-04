package com.kafka;

import com.kafka.avro.Item;
import com.kafka.avro.ItemStatus;
import org.apache.avro.specific.SpecificRecordBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProducerServiceTest {

    @Mock
    private KafkaTemplate<String, String> template;

    @Mock
    private KafkaTemplate<String, byte[]> byteTemplate;

    private ProducerService producerService;

    @BeforeEach
    void setUp() {
        producerService = new ProducerService(template, byteTemplate, "test-tx-", "avro-test-tx-");
    }

    @Test
    void send_delegatesToStringTemplate() {
        producerService.send("my-topic", "key1", "value1");

        verify(template).send("my-topic", "key1", "value1");
        verifyNoInteractions(byteTemplate);
    }

    @Test
    void sendAvro_serializesAndSendsBytesToByteTemplate() {
        Item item = Item.newBuilder()
                .setItemId("item-001")
                .setSku("SKU-001")
                .setName("Widget")
                .setDescription(null)
                .setCategory("Electronics")
                .setBrand("Acme")
                .setStatus(ItemStatus.ACTIVE)
                .setWeight(null)
                .setWeightUnit(null)
                .setCreatedAt(Instant.ofEpochMilli(1709000000000L))
                .setUpdatedAt(Instant.ofEpochMilli(1709000000000L))
                .build();

        producerService.sendAvro("item", "item-001", item);

        ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
        verify(byteTemplate).send(eq("item"), eq("item-001"), bytesCaptor.capture());
        assertThat(bytesCaptor.getValue()).isNotNull().isNotEmpty();
    }

    @Test
    void sendAvro_withIOException_doesNotPropagate() {
        Item item = Item.newBuilder()
                .setItemId("item-001")
                .setSku("SKU-001")
                .setName("Widget")
                .setDescription(null)
                .setCategory("Electronics")
                .setBrand("Acme")
                .setStatus(ItemStatus.ACTIVE)
                .setWeight(null)
                .setWeightUnit(null)
                .setCreatedAt(Instant.ofEpochMilli(1709000000000L))
                .setUpdatedAt(Instant.ofEpochMilli(1709000000000L))
                .build();

        doThrow(new RuntimeException("Kafka send failed")).when(byteTemplate)
                .send(any(String.class), any(String.class), any(byte[].class));

        assertThatCode(() -> producerService.sendAvro("item", "item-001", item))
                .doesNotThrowAnyException();
    }

    @Test
    void sendEnvelope_buildsEnvelopeAndSendsViaTransaction() {
        byte[] payload = new byte[]{1, 2, 3};

        producerService.sendEnvelope("my-topic", "key1", payload, "ItemCreated",
                "item-001", "Item");

        verify(byteTemplate).executeInTransaction(any());
    }
}
