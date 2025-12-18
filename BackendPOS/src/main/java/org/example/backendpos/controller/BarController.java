package org.example.backendpos.controller;

import org.example.backendpos.dto.BarOrderItemDto;
import org.example.backendpos.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/bar")
public class BarController {

    private final OrderService orderService;

    public BarController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/updates")
    public List<BarOrderItemDto> getUpdates(
            @RequestParam(defaultValue = "0") long since,
            @RequestParam(defaultValue = "0") long lastId
    ) {
        return orderService.getBarItems(Instant.ofEpochMilli(since), lastId);
    }

    @PostMapping("/tables/{tableNumber}/bump")
    public void bump(@PathVariable int tableNumber) {
        orderService.bumpBarTicket(tableNumber);
    }
}
