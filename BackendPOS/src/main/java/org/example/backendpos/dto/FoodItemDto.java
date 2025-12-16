package org.example.backendpos.dto;

import org.example.backendpos.model.order.FoodItem;

public record FoodItemDto(
        Long id,
        String name,
        Double price,
        boolean isMeat,
        boolean availableForTakeaway,
        boolean availableForPersonnel
) {
    public static FoodItemDto from(FoodItem food) {
        return new FoodItemDto(
                food.getId(),
                food.getName(),
                food.getPrice(),
                food.isItMeat(),
                food.isAvailableForTakeaway(),
                food.isAvailableForPersonnel()
        );
    }
}
