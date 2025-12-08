package org.example.backendpos.service;

import org.example.backendpos.dto.StartOrderResponse;

public interface OrderService {
    StartOrderResponse startOrderResponse(int tableNumber, int amountOfGuests);
}
