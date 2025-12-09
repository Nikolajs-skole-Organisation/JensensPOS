// src/main/java/org/example/backendpos/dto/OrderItemDto.java
package org.example.backendpos.dto;

import org.example.backendpos.model.order.MeatTemperature;

public record OrderItemDto(
        Long id,
        int quantity,
        MeatTemperature meatTemperature,
        FoodItemDto foodItem,
        DrinkItemDto drinkItem
) {}
