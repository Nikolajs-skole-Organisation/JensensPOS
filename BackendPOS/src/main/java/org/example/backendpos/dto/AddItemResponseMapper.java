package org.example.backendpos.dto;

import org.example.backendpos.model.order.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AddItemResponseMapper {

    public AddItemResponse toDto(Order order) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(oi -> new OrderItemDto(
                        oi.getId(),
                        oi.getQuantity(),
                        oi.getMeatTemperature(),
                        oi.getFoodItem() != null
                                ? new FoodItemDto(
                                oi.getFoodItem().getId(),
                                oi.getFoodItem().getName(),
                                oi.getFoodItem().getPrice(),
                                oi.getFoodItem().isItMeat(),
                                oi.getFoodItem().isAvailableForTakeaway(),
                                oi.getFoodItem().isAvailableForPersonnel()
                        )
                                : null,
                        oi.getDrinkItem() != null
                                ? new DrinkItemDto(
                                oi.getDrinkItem().getId(),
                                oi.getDrinkItem().getName(),
                                oi.getDrinkItem().getPrice()
                        )
                                : null
                ))
                .toList();

        return new AddItemResponse(
                order.getId(),
                order.getTableNumber(),
                order.getAmountOfGuests(),
                order.getOrderStatus(),
                itemDtos
        );
    }
}
