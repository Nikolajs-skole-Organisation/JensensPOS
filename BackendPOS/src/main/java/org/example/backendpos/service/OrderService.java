package org.example.backendpos.service;

import org.example.backendpos.dto.AddItemResponse;
import org.example.backendpos.dto.AddOrderItemRequest;
import org.example.backendpos.dto.ReceiptDto;
import org.example.backendpos.dto.StartOrderResponse;
import org.example.backendpos.dto.*;
import org.example.backendpos.model.EmployeeRole;
import org.example.backendpos.model.order.Order;

import java.time.Instant;
import java.util.List;

public interface OrderService {

    StartOrderResponse startOrderResponse(int tableNumber, int amountOfGuests);

    AddItemResponse addItemToOrder(Long orderId, AddOrderItemRequest request);

    AddItemResponse getOpenOrderForTable(int tableNumber);

    Order getOrderById(Long orderId);

    AddItemResponse getOrderDetails(Long orderId);

    ReceiptDto calculateReceipt(Long orderId);

    ReceiptDto payOrder(Long orderId);

    ReceiptDto compOrder(Long orderId, String pin, String reason);

    void validateChiefPin(String pin);

    void sendToKitchenAndBar(int tableNumber);

    List<KitchenOrderItemDto> getKitchenItems(Instant since, long lastId);

    List<BarOrderItemDto> getBarItems(Instant since, long lastId);

    void bumpKitchenTicket(int tableNumber);

    void bumpBarTicket(int tableNumber);
}
