package org.example.backendpos.dto;

import org.example.backendpos.model.order.ItemType;

public record AddOrderItemRequest(
        Long itemId,
        ItemType itemType
) {
}
