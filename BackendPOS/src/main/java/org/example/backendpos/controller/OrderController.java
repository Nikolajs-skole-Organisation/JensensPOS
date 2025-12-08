package org.example.backendpos.controller;

import org.example.backendpos.model.order.Order;
import org.example.backendpos.model.order.OrderStatus;
import org.example.backendpos.service.OrderService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    public void startOrder(int tableNumber, int amountOfGuests){
        Order order = new Order();
        order.setTableNumber(tableNumber);
        order.setAmountOfGuests(amountOfGuests);
        order.setOrderStatus(OrderStatus.OPEN);
        orderService.startOrder(order);
    }
}
