package org.example.backendpos.controller;

import org.example.backendpos.dto.AddOrderItemRequest;
import org.example.backendpos.dto.StartOrderResponse;
import org.example.backendpos.model.order.MeatTemperature;
import org.example.backendpos.model.order.Order;
import org.example.backendpos.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/service")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/tableview/{tableNumber}")
    public StartOrderResponse startOrder(
            @PathVariable int tableNumber,
            @RequestParam int amountOfGuests
    ){
        return orderService.startOrderResponse(tableNumber, amountOfGuests);
    }

    @PostMapping("/orders/{orderId}/items")
    public Order addItemToOrder(@PathVariable Long orderId,
                                @RequestBody AddOrderItemRequest request){
        return orderService.addItemToOrder(orderId, request);
    }

    @GetMapping("/orders/meat-temperatures")
    public MeatTemperature[] getMeatTemperatures() {
        return MeatTemperature.values();
    }
}
