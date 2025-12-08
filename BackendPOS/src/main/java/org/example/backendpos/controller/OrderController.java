package org.example.backendpos.controller;

import org.example.backendpos.dto.AddOrderItemRequest;
import org.example.backendpos.dto.StartOrderResponse;
import org.example.backendpos.model.order.MeatTemperature;
import org.example.backendpos.model.order.Order;
import org.example.backendpos.service.OrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public StartOrderResponse startOrder(
            @RequestParam int tableNumber,
            @RequestParam int amountOfGuests
    ){
        return orderService.startOrderResponse(tableNumber, amountOfGuests);
    }

    public Order addItemToOrder(@PathVariable Long orderId,
                                @RequestBody AddOrderItemRequest request){
        return orderService.addItemToOrder(orderId, request);
    }

    public MeatTemperature[] getMeatTemperatures() {
        return MeatTemperature.values();
    }
}
