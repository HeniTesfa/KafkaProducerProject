package com.kafka.rest;

import com.kafka.ProducerService;
import java.time.Instant;
import com.kafka.avro.Address;
import com.kafka.avro.Supplier;
import com.kafka.avro.SupplierStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SupplierController.class)
class SupplierControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProducerService producerService;

    @Test
    void createSupplier_withValidAvroJson_returns200andCreated() throws Exception {
        Address addr = Address.newBuilder()
                .setLine1("456 Business Ave")
                .setLine2(null)
                .setCity("Chicago")
                .setState("IL")
                .setPostalCode("60601")
                .setCountry("US")
                .build();

        Supplier supplier = Supplier.newBuilder()
                .setSupplierId("sup-001")
                .setSupplierName("Acme Corp")
                .setTaxId(null)
                .setContactEmail(null)
                .setContactPhone(null)
                .setAddress(addr)
                .setStatus(SupplierStatus.ACTIVE)
                .setCreatedAt(Instant.ofEpochMilli(1709000000000L))
                .build();

        String avroJson = AvroTestHelper.toAvroJson(supplier);

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(avroJson))
                .andExpect(status().isOk())
                .andExpect(content().string("created"));

        verify(producerService).sendAvro(eq("supplier"), eq("sup-001"), any(Supplier.class));
    }

    @Test
    void createSupplier_withInvalidJson_returns400() throws Exception {
        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("{invalid}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSupplier_withEmptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(""))
                .andExpect(status().isBadRequest());
    }
}
