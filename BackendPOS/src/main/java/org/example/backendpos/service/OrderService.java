package org.example.backendpos.service;

import org.example.backendpos.dto.AddOrderItemRequest;
import org.example.backendpos.dto.StartOrderResponse;
import org.example.backendpos.model.order.Order;

public interface OrderService {
    StartOrderResponse startOrderResponse(int tableNumber, int amountOfGuests);
    Order addItemToOrder(Long orderId, AddOrderItemRequest request);
}
