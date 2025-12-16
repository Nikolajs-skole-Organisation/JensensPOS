package org.example.backendpos.controller;

import org.example.backendpos.dto.KitchenOrderItemDto;
import org.example.backendpos.dto.OrderItemDto;
import org.example.backendpos.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/kitchen")
public class KitchenController {

    private final OrderService orderService;

    public KitchenController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/updates")
    public List<KitchenOrderItemDto> getUpdates(
            @RequestParam(defaultValue = "0") long since,
            @RequestParam(defaultValue = "0") long lastId
    ) {
        return orderService.getKitchenItems(
                Instant.ofEpochMilli(since),
                lastId
        );
    }

    @PostMapping("/tables/{tableNumber}/bump")
    public void bump(@PathVariable int tableNumber) {
        orderService.bumpKitchenTicket(tableNumber);
    }


}
