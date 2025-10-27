package com.food.payment_service.kafka.consumers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.food.payment_service.kafka.events.OrderEvent;
import com.food.payment_service.kafka.events.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.food.payment_service.kafka.topics.KafkaTopics.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ListenerService {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    // Stores pending acknowledgments for manual approval
    private final Map<String, Acknowledgment> pendingAcks = new ConcurrentHashMap<>();
    private final Map<String, OrderEvent> pendingOrders = new ConcurrentHashMap<>();

    @KafkaListener(topics = ORDER_CREATED, groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderCreated(String message, Acknowledgment ack) throws JsonProcessingException {
        OrderEvent orderEvent = objectMapper.readValue(message, OrderEvent.class);

        log.info("Received order.created for orderId {}", orderEvent.getOrderId());

        // Store pending ack and order for manual approval
        pendingAcks.put(orderEvent.getOrderId(), ack);
        pendingOrders.put(orderEvent.getOrderId(), orderEvent);

        log.info("Payment pending for orderId {}. Awaiting manual approval.", orderEvent.getOrderId());
    }

    public String ackPayment(String orderId) {
        Acknowledgment ack = pendingAcks.remove(orderId);
        OrderEvent orderEvent = pendingOrders.remove(orderId);

        if (ack != null && orderEvent != null) {
            // Simulate payment processing
            PaymentEvent paymentEvent = PaymentEvent.builder()
                    .orderId(orderEvent.getOrderId())
                    .status("COMPLETED")
                    .amount(orderEvent.getTotalAmount())
                    .build();

            kafkaTemplate.send(PAYMENT_COMPLETED, paymentEvent);
            log.info("Payment completed for orderId {}", orderId);

            // Manually acknowledge the message
            ack.acknowledge();
            return "Payment acknowledged for orderId " + orderId;
        } else {
            kafkaTemplate.send(PAYMENT_FAILED, PaymentEvent.builder()
                    .orderId(orderId)
                    .status("FAILED")
                    .amount(0)
                    .build());
            return "No pending payment found for orderId " + orderId;
        }
    }
}
