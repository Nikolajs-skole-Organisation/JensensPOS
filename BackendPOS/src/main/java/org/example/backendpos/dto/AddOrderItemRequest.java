package org.example.backendpos.dto;

import org.example.backendpos.model.order.ItemType;
import org.example.backendpos.model.order.MeatTemperature;

public record AddOrderItemRequest(
        Long itemId,
        ItemType itemType,
        MeatTemperature meatTemperature
) {
}
