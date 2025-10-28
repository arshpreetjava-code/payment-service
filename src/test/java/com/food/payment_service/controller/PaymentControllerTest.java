package com.food.payment_service.controller;

import com.food.payment_service.kafka.consumers.ListenerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListenerService listenerService;

    @Test
    @DisplayName("POST /api/payment/ack/{orderId} returns service response")
    void ack_ReturnsServiceResponse() throws Exception {
        String orderId = "order-123";
        Mockito.when(listenerService.ackPayment(eq(orderId)))
                .thenReturn("Payment acknowledged for orderId " + orderId);

        mockMvc.perform(post("/api/payment/ack/" + orderId)
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Payment acknowledged for orderId " + orderId)));
    }
}


