package org.example.backendpos.service;

import org.example.backendpos.dto.*;
import org.example.backendpos.model.order.Order;

import java.time.Instant;
import java.util.List;

public interface OrderService {

    StartOrderResponse startOrderResponse(int tableNumber, int amountOfGuests);

    AddItemResponse addItemToOrder(Long orderId, AddOrderItemRequest request);

    AddItemResponse getOpenOrderForTable(int tableNumber);

    Order getOrderById(Long orderId);

    AddItemResponse getOrderDetails(Long orderId);

    void sendToKitchenAndBar(int tableNumber);

    List<KitchenOrderItemDto> getKitchenItems(Instant since, long lastId);
    void bumpKitchenTicket(int tableNumber);
}
