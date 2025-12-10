// src/main/java/org/example/backendpos/dto/AddItemResponse.java
package org.example.backendpos.dto;

import org.example.backendpos.model.order.OrderStatus;

import java.util.List;

public record AddItemResponse(
        Long orderId,
        int tableNumber,
        int amountOfGuests,
        OrderStatus orderStatus,
        List<OrderItemDto> items
) {}
