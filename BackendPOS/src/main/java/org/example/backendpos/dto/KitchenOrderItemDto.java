package org.example.backendpos.dto;

import org.example.backendpos.model.order.OrderItem;

import java.time.Instant;

public record KitchenOrderItemDto(
        int tableNumber,
        Instant sentAt,
        OrderItemDto item
) {
    public static KitchenOrderItemDto from (OrderItem oi){
        return new KitchenOrderItemDto(
                oi.getOrder().getTableNumber(),
                oi.getSentAt(),
                OrderItemDto.from(oi)
        );
    }
}
