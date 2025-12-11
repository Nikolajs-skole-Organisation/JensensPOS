package org.example.backendpos.dto;

    public record ReceiptLineDto(
            String name,
            int quantity,
            Double unitPrice,
            Double lineTotal
    ) { }

