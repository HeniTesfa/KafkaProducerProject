# Kafka Production Demo — Producer Service

This project is a production-minded Apache Kafka **producer** application built with Spring Boot 3.1 and Java 17.
Consumer logic lives in a separate `KafkaConsumerProject` to mirror real-world microservice architecture.

## Features

- Six business topics: `item`, `tradeItem`, `supplier`, `order`, `shipment`, `sites`
- REST endpoint for each topic to post JSON payloads
- Avro schemas with Maven code generation (`avro-maven-plugin`)
- Producer configured for **exactly-once semantics**: idempotence + transactions
- `AdminClient` creates topics on startup (12 partitions, replication factor 3)
- SSL/SASL placeholders in `application.yml` for secured clusters
- Lombok for boilerplate-free model and event classes
- Unit and integration tests with embedded Kafka (`spring-kafka-test`)

## Project Structure

```
KafkaProducerProject/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── avro/                          # Avro schema definitions
│   │   │   ├── Envelope.avsc
│   │   │   ├── Item.avsc
│   │   │   ├── Order.avsc
│   │   │   ├── Shipment.avsc
│   │   │   ├── Site.avsc
│   │   │   ├── Supplier.avsc
│   │   │   └── TradeItem.avsc
│   │   ├── java/com/kafka/
│   │   │   ├── KafkaProductionApplication.java   # entry point
│   │   │   ├── KafkaConfig.java                  # producer & template config
│   │   │   ├── TopicCreator.java                 # AdminClient topic setup
│   │   │   ├── ProducerService.java              # core send logic
│   │   │   ├── model/                            # domain entity definitions
│   │   │   │   ├── Address.java
│   │   │   │   ├── Item.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderLine.java
│   │   │   │   ├── Shipment.java
│   │   │   │   ├── Site.java
│   │   │   │   ├── Supplier.java
│   │   │   │   └── TradeItem.java
│   │   │   ├── events/                           # event envelope + per-aggregate events
│   │   │   │   ├── EventEnvelope.java
│   │   │   │   ├── Address.java
│   │   │   │   ├── ItemEvent.java
│   │   │   │   ├── OrderEvent.java
│   │   │   │   ├── OrderLine.java
│   │   │   │   ├── ShipmentEvent.java
│   │   │   │   ├── SiteEvent.java
│   │   │   │   ├── SupplierEvent.java
│   │   │   │   └── TradeItemEvent.java
│   │   │   └── rest/                             # REST controllers
│   │   │       ├── ItemController.java
│   │   │       ├── OrderController.java
│   │   │       ├── ShipmentController.java
│   │   │       ├── SiteController.java
│   │   │       ├── SupplierController.java
│   │   │       └── TradeItemController.java
│   │   └── resources/
│   │       └── application.yml
│   └── test/
│       ├── java/com/kafka/
│       │   ├── ProducerServiceTest.java
│       │   ├── TopicCreatorTest.java
│       │   ├── integration/
│       │   │   └── KafkaProducerIntegrationTest.java
│       │   └── rest/
│       │       ├── AvroTestHelper.java
│       │       ├── ItemControllerTest.java
│       │       ├── OrderControllerTest.java
│       │       ├── ShipmentControllerTest.java
│       │       ├── SiteControllerTest.java
│       │       ├── SupplierControllerTest.java
│       │       └── TradeItemControllerTest.java
│       └── resources/
│           └── application.yml
```

## REST Endpoints

| Method | Path            | Topic     | Payload       |
|--------|-----------------|-----------|---------------|
| POST   | `/api/items`    | item      | Item JSON     |
| POST   | `/api/tradeitems`| tradeItem | TradeItem JSON|
| POST   | `/api/suppliers`| supplier  | Supplier JSON |
| POST   | `/api/orders`   | order     | Order JSON    |
| POST   | `/api/shipments`| shipment  | Shipment JSON |
| POST   | `/api/sites`    | sites     | Site JSON     |

## Building

```bash
cd "C:\......\KafkaProducerProject"
mvn clean package
```

The `avro-maven-plugin` generates Java classes from `src/main/avro/*.avsc` into `target/generated-sources/avro` during the build.

## Running

Set environment variables for your Kafka cluster, then:

```bash
java -jar target/kafka-production-1.0.0.jar
```

The application creates all required topics via `AdminClient` on startup and begins accepting REST requests.

## Testing

```bash
mvn test
```

Unit tests use Mockito; integration tests use an embedded Kafka broker provided by `spring-kafka-test`.

## Security

To enable SSL/SASL, copy your keystore/truststore files to `src/main/resources` and update `application.yml` with the correct paths and credentials. Placeholder properties are already present in the config.

## Producer Configuration

| Setting | Value | Purpose |
|---------|-------|---------|
| `enable.idempotence` | true | Prevents duplicate records on retry |
| `transactional.id` | set | Enables exactly-once semantics |
| `acks` | all | Waits for all in-sync replicas |
| `max.in.flight.requests.per.connection` | 1 | Preserves ordering with retries |

## High-Volume Considerations

- Run Kafka in a multi-node cluster with appropriate hardware.
- Enable compression (`compression.type=gzip/snappy/lz4`).
- Tune `batch.size` and `linger.ms` to balance latency vs. throughput.
- Scale horizontally by deploying multiple producer instances behind a load balancer.
- Monitor producer metrics (record-send-rate, request-latency-avg) via JMX or Micrometer.

---

Consumer functionality lives in `KafkaConsumerProject`. This project is intended as a starting point — integrate your business logic, monitoring, and deployment scripts as needed.
