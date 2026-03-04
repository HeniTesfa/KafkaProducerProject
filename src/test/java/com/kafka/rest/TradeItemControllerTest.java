package com.kafka.rest;

import com.kafka.ProducerService;
import java.time.Instant;
import com.kafka.avro.TradeItem;
import com.kafka.avro.TradeItemStatus;
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

@WebMvcTest(TradeItemController.class)
class TradeItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProducerService producerService;

    @Test
    void createTradeItem_withValidAvroJson_returns200andCreated() throws Exception {
        TradeItem tradeItem = TradeItem.newBuilder()
                .setTradeItemId("ti-001")
                .setItemId("item-001")
                .setSupplierId("sup-001")
                .setCostPrice(null)
                .setRetailPrice(null)
                .setCurrency("USD")
                .setMinimumOrderQuantity(10)
                .setStatus(TradeItemStatus.ACTIVE)
                .setEffectiveFrom(Instant.ofEpochMilli(1709000000000L))
                .setEffectiveTo(null)
                .build();

        String avroJson = AvroTestHelper.toAvroJson(tradeItem);

        mockMvc.perform(post("/api/trade-items")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(avroJson))
                .andExpect(status().isOk())
                .andExpect(content().string("created"));

        verify(producerService).sendAvro(eq("tradeItem"), eq("ti-001"), any(TradeItem.class));
    }

    @Test
    void createTradeItem_withInvalidJson_returns400() throws Exception {
        mockMvc.perform(post("/api/trade-items")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("{invalid}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTradeItem_withEmptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/trade-items")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(""))
                .andExpect(status().isBadRequest());
    }
}
