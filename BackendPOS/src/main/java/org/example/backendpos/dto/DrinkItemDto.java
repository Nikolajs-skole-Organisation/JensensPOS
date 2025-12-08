package org.example.backendpos.dto;

public record DrinkItemDto(
        Long id,
        String name,
        Double price
) {
}
