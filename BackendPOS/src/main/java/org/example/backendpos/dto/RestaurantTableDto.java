package org.example.backendpos.dto;

public record RestaurantTableDto(
    Long id,
    int tableNumber,
    int rowStart,
    int colStart,
    int width,
    int height,
    String status
)
{}
