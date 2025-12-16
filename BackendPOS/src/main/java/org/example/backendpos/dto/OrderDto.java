package org.example.backendpos.dto;

import org.example.backendpos.model.order.OrderItem;

import java.util.List;

public record OrderDto(
        int tableNumber,
        List<OrderItem> Items
) {
}
