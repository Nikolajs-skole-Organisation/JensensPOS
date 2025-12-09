package org.example.backendpos.controller;

import org.example.backendpos.dto.*;
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
    public AddItemResponse addItemToOrder(@PathVariable Long orderId,
                                          @RequestBody AddOrderItemRequest request){

        Order order = orderService.addItemToOrder(orderId, request);

        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(oi -> new OrderItemDto(
                        oi.getId(),
                        oi.getQuantity(),
                        oi.getMeatTemperature(),
                        oi.getFoodItem() != null
                                ? new FoodItemDto(
                                oi.getFoodItem().getId(),
                                oi.getFoodItem().getName(),
                                oi.getFoodItem().getPrice(),
                                oi.getFoodItem().isItMeat(),
                                oi.getFoodItem().isAvailableForTakeaway(),
                                oi.getFoodItem().isAvailableForPersonnel()
                        )
                                : null,
                        oi.getDrinkItem() != null
                                ? new DrinkItemDto(
                                oi.getDrinkItem().getId(),
                                oi.getDrinkItem().getName(),
                                oi.getDrinkItem().getPrice()
                        )
                                : null
                ))
                .toList();

        return new AddItemResponse(
                order.getId(),
                order.getTableNumber(),
                order.getAmountOfGuests(),
                order.getOrderStatus(),
                itemDtos
        );
    }

    @GetMapping("/orders/meat-temperatures")
    public MeatTemperature[] getMeatTemperatures() {
        return MeatTemperature.values();
    }
}
