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

    @PostMapping("/ack/{orderId}")
    public ResponseEntity<String> ack(@PathVariable String orderId) {
        return ResponseEntity.ok(listenerService.ackPayment(orderId));
    }
}
