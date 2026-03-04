package com.kafka.integration;

import com.kafka.avro.Address;
import com.kafka.avro.Item;
import com.kafka.avro.ItemStatus;
import com.kafka.avro.Shipment;
import com.kafka.avro.ShipmentStatus;
import com.kafka.avro.Site;
import com.kafka.avro.SiteStatus;
import com.kafka.avro.SiteType;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(
        partitions = 1,
        topics = {"item", "order", "shipment", "sites", "supplier", "tradeItem"},
        brokerProperties = {
                "transaction.state.log.replication.factor=1",
                "transaction.state.log.min.isr=1"
        }
)
@TestPropertySource(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
@DirtiesContext
class KafkaProducerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @MockBean
    private AdminClient adminClient;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        CreateTopicsResult result = mock(CreateTopicsResult.class);
        KafkaFuture<Void> future = mock(KafkaFuture.class);
        when(future.get()).thenReturn(null);
        when(result.all()).thenReturn(future);
        when(adminClient.createTopics(any())).thenReturn(result);
    }

    private static <T extends SpecificRecordBase> String toAvroJson(T record) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonEncoder encoder = EncoderFactory.get().jsonEncoder(record.getSchema(), out);
        new SpecificDatumWriter<T>(record.getSchema()).write(record, encoder);
        encoder.flush();
        return out.toString(StandardCharsets.UTF_8);
    }

    private HttpHeaders textPlainHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        return headers;
    }

    @Test
    void postItem_returns200andPublishesToKafka() throws Exception {
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
                .setCreatedAt(java.time.Instant.ofEpochMilli(1709000000000L))
                .setUpdatedAt(java.time.Instant.ofEpochMilli(1709000000000L))
                .build();

        String avroJson = toAvroJson(item);

        Map<String, Object> props = KafkaTestUtils.consumerProps("test-grp-item", "true", embeddedKafka);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        DefaultKafkaConsumerFactory<String, byte[]> cf = new DefaultKafkaConsumerFactory<>(props);
        Consumer<String, byte[]> consumer = cf.createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "item");

        HttpEntity<String> request = new HttpEntity<>(avroJson, textPlainHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/items", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("created");

        ConsumerRecords<String, byte[]> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
        assertThat(records.count()).isGreaterThan(0);
        consumer.close();
    }

    @Test
    void postShipment_returns200andPublishesToKafka() throws Exception {
        Shipment shipment = Shipment.newBuilder()
                .setShipmentId("shp-001")
                .setOrderId("ord-001")
                .setCarrier("FedEx")
                .setTrackingNumber("TRK123")
                .setStatus(ShipmentStatus.CREATED)
                .setShippedAt(null)
                .setDeliveredAt(null)
                .build();

        String avroJson = toAvroJson(shipment);

        Map<String, Object> props = KafkaTestUtils.consumerProps("test-grp-shipment", "true", embeddedKafka);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        DefaultKafkaConsumerFactory<String, byte[]> cf = new DefaultKafkaConsumerFactory<>(props);
        Consumer<String, byte[]> consumer = cf.createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "shipment");

        HttpEntity<String> request = new HttpEntity<>(avroJson, textPlainHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/shipments", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("created");

        ConsumerRecords<String, byte[]> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
        assertThat(records.count()).isGreaterThan(0);
        consumer.close();
    }

    @Test
    void postSite_returns200andPublishesToKafka() throws Exception {
        Address addr = Address.newBuilder()
                .setLine1("123 Main St")
                .setLine2(null)
                .setCity("Springfield")
                .setState("IL")
                .setPostalCode("62701")
                .setCountry("US")
                .build();

        Site site = Site.newBuilder()
                .setSiteId("site-001")
                .setSiteName("Main Warehouse")
                .setSiteType(SiteType.WAREHOUSE)
                .setAddress(addr)
                .setStatus(SiteStatus.ACTIVE)
                .build();

        String avroJson = toAvroJson(site);

        Map<String, Object> props = KafkaTestUtils.consumerProps("test-grp-sites", "true", embeddedKafka);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        DefaultKafkaConsumerFactory<String, byte[]> cf = new DefaultKafkaConsumerFactory<>(props);
        Consumer<String, byte[]> consumer = cf.createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "sites");

        HttpEntity<String> request = new HttpEntity<>(avroJson, textPlainHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/sites", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("created");

        ConsumerRecords<String, byte[]> records = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10));
        assertThat(records.count()).isGreaterThan(0);
        consumer.close();
    }

    @Test
    void postItem_withInvalidJson_returns400() throws Exception {
        HttpEntity<String> request = new HttpEntity<>("{invalid}", textPlainHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/items", request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
