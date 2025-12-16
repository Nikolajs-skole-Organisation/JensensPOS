// src/main/java/org/example/backendpos/dto/OrderItemDto.java
package org.example.backendpos.dto;

import org.example.backendpos.model.order.MeatTemperature;
import org.example.backendpos.model.order.OrderItem;

public record OrderItemDto(
        Long id,
        int quantity,
        MeatTemperature meatTemperature,
        FoodItemDto foodItem,
        DrinkItemDto drinkItem
) {
    public static OrderItemDto from(OrderItem item) {
        return new OrderItemDto(
                item.getId(),
                item.getQuantity(),
                item.getMeatTemperature(),
                item.getFoodItem() != null
                        ? FoodItemDto.from(item.getFoodItem())
                        : null,
                item.getDrinkItem() != null
                        ? DrinkItemDto.from(item.getDrinkItem())
                        : null
        );
    }
}
