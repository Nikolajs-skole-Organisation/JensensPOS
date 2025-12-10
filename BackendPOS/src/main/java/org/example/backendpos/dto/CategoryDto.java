package org.example.backendpos.dto;

import java.util.List;

public record CategoryDto(
        Long id,
        String name,
        List<FoodItemDto> foodItems,
        List<DrinkItemDto> drinkItems
) {
}
