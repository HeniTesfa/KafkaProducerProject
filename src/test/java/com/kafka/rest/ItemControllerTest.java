package com.kafka.rest;

import com.kafka.ProducerService;
import com.kafka.avro.Item;
import com.kafka.avro.ItemStatus;
import org.junit.jupiter.api.Test;
import java.time.Instant;
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

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProducerService producerService;

    @Test
    void createItem_withValidAvroJson_returns200andCreated() throws Exception {
        Item item = Item.newBuilder()
                .setItemId("item-001")
                .setSku("SKU-001")
                .setName("Widget")
                .setDescription(null)
                .setCategory("Electronics")
                .setBrand("Acme")
                .setStatus(ItemStatus.ACTIVE)
                .setWeight(null)
                .setWeightUnit(null)
                .setCreatedAt(Instant.ofEpochMilli(1709000000000L))
                .setUpdatedAt(Instant.ofEpochMilli(1709000000000L))
                .build();

        String avroJson = AvroTestHelper.toAvroJson(item);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(avroJson))
                .andExpect(status().isOk())
                .andExpect(content().string("created"));

        verify(producerService).sendAvro(eq("item"), eq("item-001"), any(Item.class));
    }

    @Test
    void createItem_withInvalidJson_returns400() throws Exception {
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("{invalid}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItem_withEmptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(""))
                .andExpect(status().isBadRequest());
    }
}
