package com.kafka.rest;

import com.kafka.ProducerService;
import com.kafka.avro.Shipment;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/shipments")
public class ShipmentController {
    private final ProducerService producer;

    public ShipmentController(ProducerService producer) {
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<String> createShipment(@RequestBody String json) {
        SpecificDatumReader<Shipment> reader = new SpecificDatumReader<>(Shipment.class);
        try {
            JsonDecoder decoder = DecoderFactory.get().jsonDecoder(Shipment.getClassSchema(), json);
            Shipment shipment = reader.read(null, decoder);
            producer.sendAvro("shipment", shipment.getShipmentId().toString(), shipment);
            return ResponseEntity.ok("created");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("invalid schema: " + e.getMessage());
        }
    }
}
