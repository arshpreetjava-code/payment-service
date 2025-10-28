package com.food.payment_service.kafka.consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.food.payment_service.kafka.events.OrderEvent;
import com.food.payment_service.kafka.events.PaymentEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;

import java.time.LocalDateTime;

import static com.food.payment_service.kafka.topics.KafkaTopics.PAYMENT_COMPLETED;
import static com.food.payment_service.kafka.topics.KafkaTopics.PAYMENT_FAILED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ListenerServiceTest {

    private ObjectMapper objectMapper;
    private KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private ListenerService listenerService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        kafkaTemplate = mock(KafkaTemplate.class);
        listenerService = new ListenerService(objectMapper, kafkaTemplate);
    }

    @Test
    @DisplayName("handleOrderCreated stores ack and order for manual approval")
    void handleOrderCreated_StoresPending() throws JsonProcessingException {
        OrderEvent event = OrderEvent.builder()
                .orderId("o-1")
                .userId("u-1")
                .address("addr")
                .totalAmount(123)
                .createTime(LocalDateTime.now())
                .build();
        String json = objectMapper.writeValueAsString(event);
        Acknowledgment ack = mock(Acknowledgment.class);

        listenerService.handleOrderCreated(json, ack);

        // Now ack should succeed and send COMPLETED
        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);
        String response = listenerService.ackPayment("o-1");

        verify(kafkaTemplate, times(1)).send(eq(PAYMENT_COMPLETED), captor.capture());
        verify(ack, times(1)).acknowledge();
        assertThat(response).isEqualTo("Payment acknowledged for orderId o-1");
        PaymentEvent sent = captor.getValue();
        assertThat(sent.getOrderId()).isEqualTo("o-1");
        assertThat(sent.getStatus()).isEqualTo("COMPLETED");
        assertThat(sent.getAmount()).isEqualTo(123);
    }

    @Test
    @DisplayName("ackPayment on missing order sends FAILED event")
    void ackPayment_NoPending_SendsFailed() {
        ArgumentCaptor<PaymentEvent> captor = ArgumentCaptor.forClass(PaymentEvent.class);

        String response = listenerService.ackPayment("missing");

        verify(kafkaTemplate, times(1)).send(eq(PAYMENT_FAILED), captor.capture());
        assertThat(response).isEqualTo("No pending payment found for orderId missing");
        PaymentEvent sent = captor.getValue();
        assertThat(sent.getOrderId()).isEqualTo("missing");
        assertThat(sent.getStatus()).isEqualTo("FAILED");
        assertThat(sent.getAmount()).isEqualTo(0.0);
    }
}


