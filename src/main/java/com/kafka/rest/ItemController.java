package com.kafka.rest;

import com.kafka.ProducerService;
import com.kafka.avro.Item;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/items")
public class ItemController {
    private final ProducerService producer;

    public ItemController(ProducerService producer) {
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<String> createItem(@RequestBody String json) {
        SpecificDatumReader<Item> reader = new SpecificDatumReader<>(Item.class);
        try {
            JsonDecoder decoder = DecoderFactory.get().jsonDecoder(Item.getClassSchema(), json);
            Item item = reader.read(null, decoder);
            producer.sendAvro("item", item.getItemId().toString(), item);
            return ResponseEntity.ok("created");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("invalid schema: " + e.getMessage());
        }
    }
}
