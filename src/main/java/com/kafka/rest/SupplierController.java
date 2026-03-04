package com.kafka.rest;

import com.kafka.ProducerService;
import com.kafka.avro.Supplier;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
    private final ProducerService producer;

    public SupplierController(ProducerService producer) {
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<String> createSupplier(@RequestBody String json) {
        SpecificDatumReader<Supplier> reader = new SpecificDatumReader<>(Supplier.class);
        try {
            JsonDecoder decoder = DecoderFactory.get().jsonDecoder(Supplier.getClassSchema(), json);
            Supplier supplier = reader.read(null, decoder);
            producer.sendAvro("supplier", supplier.getSupplierId().toString(), supplier);
            return ResponseEntity.ok("created");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("invalid schema: " + e.getMessage());
        }
    }
}
