package com.food.payment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FoodDto {
    private String type;
    private String name;
    private String[] toppings;
    private int quantity;
    private long price;
}
