package com.kafka.rest;

import com.kafka.ProducerService;
import java.time.Instant;
import com.kafka.avro.Order;
import com.kafka.avro.OrderLine;
import com.kafka.avro.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProducerService producerService;

    @Test
    void createOrder_withValidAvroJson_returns200andCreated() throws Exception {
        OrderLine line = OrderLine.newBuilder()
                .setTradeItemId("ti-001")
                .setQuantity(2)
                .setUnitPrice(ByteBuffer.wrap(new BigDecimal("10.00").unscaledValue().toByteArray()))
                .build();

        Order order = Order.newBuilder()
                .setOrderId("ord-001")
                .setSupplierId("sup-001")
                .setSiteId("site-001")
                .setOrderLines(List.of(line))
                .setTotalAmount(null)
                .setCurrency("USD")
                .setOrderStatus(OrderStatus.CREATED)
                .setOrderDate(Instant.ofEpochMilli(1709000000000L))
                .build();

        String avroJson = AvroTestHelper.toAvroJson(order);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(avroJson))
                .andExpect(status().isOk())
                .andExpect(content().string("created"));

        verify(producerService).sendAvro(eq("order"), eq("ord-001"), any(Order.class));
    }

    @Test
    void createOrder_withInvalidJson_returns400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("{invalid}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createOrder_withEmptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(""))
                .andExpect(status().isBadRequest());
    }
}
