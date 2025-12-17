package org.example.backendpos.dto;

import java.util.List;

public record ReceiptDto(
        Long orderId,
        int tableNumber,
        List<ReceiptLineDto> lines,
        double total
) { }
