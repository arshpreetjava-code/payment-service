package com.food.payment_service.controller;

import com.food.payment_service.kafka.consumers.ListenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    private final ListenerService listenerService;

    /**
     * Manually acknowledge/complete a payment for an order.
     * <p>
     * This endpoint is intended for manual approval of pending payments. It
     * delegates to the ListenerService which will publish a payment completed
     * or failed event depending on whether a pending payment exists.
     *
     * @param orderId the id of the order whose payment should be acknowledged
     * @return ResponseEntity with a textual confirmation message and HTTP 200
     */
    @PostMapping("/ack/{orderId}")
    public ResponseEntity<String> ack(@PathVariable String orderId) {
        return ResponseEntity.ok(listenerService.ackPayment(orderId));
    }
}
