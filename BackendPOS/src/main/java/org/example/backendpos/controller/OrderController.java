package org.example.backendpos.controller;

import org.example.backendpos.dto.StartOrderResponse;
import org.example.backendpos.model.order.Category;
import org.example.backendpos.model.order.Order;
import org.example.backendpos.model.order.OrderStatus;
import org.example.backendpos.service.OrderService;
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
}
