package org.example.backendpos.dto;

import org.example.backendpos.model.order.DrinkItem;

public record DrinkItemDto(
        Long id,
        String name,
        Double price
) {
    public static DrinkItemDto from(DrinkItem drink) {
        return new DrinkItemDto(
                drink.getId(),
                drink.getName(),
                drink.getPrice()
        );
    }
}
