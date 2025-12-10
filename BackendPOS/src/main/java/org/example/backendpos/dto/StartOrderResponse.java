package org.example.backendpos.dto;

import java.util.List;

public record StartOrderResponse(
        Long orderId,
        int tableNumber,
        int amountOfGuests,
        String orderStatus,
        List<CategoryDto> categories
) {}
