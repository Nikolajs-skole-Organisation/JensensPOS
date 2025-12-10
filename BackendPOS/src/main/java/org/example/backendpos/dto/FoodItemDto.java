package org.example.backendpos.dto;

public record FoodItemDto(
        Long id,
        String name,
        Double price,
        boolean isMeat,
        boolean availableForTakeaway,
        boolean availableForPersonnel
) {
}
