package com.kafka.rest;

import com.kafka.ProducerService;
import com.kafka.avro.Site;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.JsonDecoder;
import org.apache.avro.specific.SpecificDatumReader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/sites")
public class SiteController {
    private final ProducerService producer;

    public SiteController(ProducerService producer) {
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<String> createSite(@RequestBody String json) {
        SpecificDatumReader<Site> reader = new SpecificDatumReader<>(Site.class);
        try {
            JsonDecoder decoder = DecoderFactory.get().jsonDecoder(Site.getClassSchema(), json);
            Site site = reader.read(null, decoder);
            producer.sendAvro("sites", site.getSiteId().toString(), site);
            return ResponseEntity.ok("created");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("invalid schema: " + e.getMessage());
        }
    }
}
