package com.food.payment_service.kafka.events;

import com.food.payment_service.dto.FoodDto;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderEvent {
    private String orderId;
    private String userId;
    private String address;
    private FoodDto food;
    private long totalAmount;
    private LocalDateTime createTime;
}
