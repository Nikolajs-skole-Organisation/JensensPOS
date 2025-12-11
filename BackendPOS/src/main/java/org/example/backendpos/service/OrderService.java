package org.example.backendpos.service;

import org.example.backendpos.dto.AddItemResponse;
import org.example.backendpos.dto.AddOrderItemRequest;
import org.example.backendpos.dto.ReceiptDto;
import org.example.backendpos.dto.StartOrderResponse;
import org.example.backendpos.model.order.Order;

public interface OrderService {

    StartOrderResponse startOrderResponse(int tableNumber, int amountOfGuests);

    AddItemResponse addItemToOrder(Long orderId, AddOrderItemRequest request);

    AddItemResponse getOpenOrderForTable(int tableNumber);

    Order getOrderById(Long orderId);

    AddItemResponse getOrderDetails(Long orderId);

    ReceiptDto calculateReceipt(Long orderId);

    ReceiptDto payOrder(Long orderId);
}
