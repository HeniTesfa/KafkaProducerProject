package com.kafka.rest;

import com.kafka.ProducerService;
import com.kafka.avro.TradeItem;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/trade-items")
public class TradeItemController {
    private final ProducerService producer;

    public TradeItemController(ProducerService producer) {
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody String json) {
        SpecificDatumReader<TradeItem> reader = new SpecificDatumReader<>(TradeItem.class);
        try {
            JsonDecoder decoder = DecoderFactory.get().jsonDecoder(TradeItem.getClassSchema(), json);
            TradeItem tradeItem = reader.read(null, decoder);
            producer.sendAvro("tradeItem", tradeItem.getTradeItemId().toString(), tradeItem);
            return ResponseEntity.ok("created");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("invalid schema: " + e.getMessage());
        }
    }
}
