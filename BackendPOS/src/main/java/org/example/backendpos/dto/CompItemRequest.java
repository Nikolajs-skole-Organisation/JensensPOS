package org.example.backendpos.dto;

import java.util.List;

public record CompItemRequest(
        String pin,
        String reason,
        List<Long> orderItemIds
) {
}
