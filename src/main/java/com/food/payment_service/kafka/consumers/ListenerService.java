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

    /**
     * Kafka listener for ORDER_CREATED events.
     * <p>
     * Parses the incoming message into an OrderEvent, stores the provided
     * Acknowledgment for later manual approval and keeps the order data in-memory
     * until it is acknowledged via {@link #ackPayment(String)}.
     *
     * @param message the raw JSON message received from Kafka
     * @param ack     the acknowledgment provided by the Kafka listener for manual acking
     * @throws JsonProcessingException if the message cannot be deserialized into OrderEvent
     */
    @KafkaListener(topics = ORDER_CREATED, groupId = "${spring.kafka.consumer.group-id}")
    public void handleOrderCreated(String message, Acknowledgment ack) throws JsonProcessingException {
        OrderEvent orderEvent = objectMapper.readValue(message, OrderEvent.class);

        log.info("Received order.created for orderId {}", orderEvent.getOrderId());

        // Store pending ack and order for manual approval
        pendingAcks.put(orderEvent.getOrderId(), ack);
        pendingOrders.put(orderEvent.getOrderId(), orderEvent);

        log.info("Payment pending for orderId {}. Awaiting manual approval.", orderEvent.getOrderId());
    }

    /**
     * Manually acknowledge a pending payment for an order.
     * <p>
     * If a pending acknowledgment exists for the provided orderId, this method
     * will publish a PAYMENT_COMPLETED event, acknowledge the Kafka message,
     * and return a success message. If no pending entry exists, it will publish
     * a PAYMENT_FAILED event and return a not-found message.
     *
     * @param orderId the id of the order to acknowledge payment for
     * @return a human readable message indicating the result
     */
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
