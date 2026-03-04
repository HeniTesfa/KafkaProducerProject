package com.kafka.rest;

import com.kafka.ProducerService;
import com.kafka.avro.Shipment;
import com.kafka.avro.ShipmentStatus;
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

@WebMvcTest(ShipmentController.class)
class ShipmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProducerService producerService;

    @Test
    void createShipment_withValidAvroJson_returns200andCreated() throws Exception {
        Shipment shipment = Shipment.newBuilder()
                .setShipmentId("shp-001")
                .setOrderId("ord-001")
                .setCarrier("FedEx")
                .setTrackingNumber("TRK123")
                .setStatus(ShipmentStatus.CREATED)
                .setShippedAt(null)
                .setDeliveredAt(null)
                .build();

        String avroJson = AvroTestHelper.toAvroJson(shipment);

        mockMvc.perform(post("/api/shipments")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(avroJson))
                .andExpect(status().isOk())
                .andExpect(content().string("created"));

        verify(producerService).sendAvro(eq("shipment"), eq("shp-001"), any(Shipment.class));
    }

    @Test
    void createShipment_withInvalidJson_returns400() throws Exception {
        mockMvc.perform(post("/api/shipments")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("{invalid}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createShipment_withEmptyBody_returns400() throws Exception {
        mockMvc.perform(post("/api/shipments")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(""))
                .andExpect(status().isBadRequest());
    }
}
