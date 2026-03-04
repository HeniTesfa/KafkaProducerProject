package com.kafka.rest;

import com.kafka.ProducerService;
import com.kafka.avro.Address;
import com.kafka.avro.Site;
import com.kafka.avro.SiteStatus;
import com.kafka.avro.SiteType;
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

@WebMvcTest(SiteController.class)
class SiteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProducerService producerService;

    @Test
    void createSite_withValidAvroJson_returns200andCreated() throws Exception {
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

        String avroJson = AvroTestHelper.toAvroJson(site);

        mockMvc.perform(post("/api/sites")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(avroJson))
                .andExpect(status().isOk())
                .andExpect(content().string("created"));

        verify(producerService).sendAvro(eq("sites"), eq("site-001"), any(Site.class));
    }

    @Test
    void createSite_withInvalidJson_returns400() throws Exception {
        mockMvc.perform(post("/api/sites")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("{invalid}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSite_withEmptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/sites")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(""))
                .andExpect(status().isBadRequest());
    }
}
