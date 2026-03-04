package com.kafka.rest;

import org.apache.avro.io.EncoderFactory;
import org.apache.avro.io.JsonEncoder;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class AvroTestHelper {

    static <T extends SpecificRecordBase> String toAvroJson(T record) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonEncoder encoder = EncoderFactory.get().jsonEncoder(record.getSchema(), out);
        new SpecificDatumWriter<T>(record.getSchema()).write(record, encoder);
        encoder.flush();
        return out.toString(StandardCharsets.UTF_8);
    }
}
