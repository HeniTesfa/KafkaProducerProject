package com.kafka.rest;

import com.kafka.ProducerService;
import com.kafka.avro.Order;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final ProducerService producer;

    public OrderController(ProducerService producer) {
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<String> createOrder(@RequestBody String json) {
        SpecificDatumReader<Order> reader = new SpecificDatumReader<>(Order.class);
        try {
            JsonDecoder decoder = DecoderFactory.get().jsonDecoder(Order.getClassSchema(), json);
            Order order = reader.read(null, decoder);
            producer.sendAvro("order", order.getOrderId().toString(), order);
            return ResponseEntity.ok("created");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("invalid schema: " + e.getMessage());
        }
    }
}
