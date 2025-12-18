package org.example.backendpos.dto;

import org.example.backendpos.model.order.OrderItem;

import java.time.Instant;

public record BarOrderItemDto (
    int tableNumber,
    Instant sentAt,
    OrderItemDto item
) {
    public static BarOrderItemDto from(OrderItem oi) {
        return new BarOrderItemDto(
                oi.getOrder().getTableNumber(),
                oi.getSentAt(),
                OrderItemDto.from(oi)
        );
    }
}