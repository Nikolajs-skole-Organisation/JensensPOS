package org.example.backendpos.controller;

import org.example.backendpos.dto.*;
import org.example.backendpos.model.EmployeeRole;
import org.example.backendpos.model.order.MeatTemperature;
import org.example.backendpos.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
                                          @RequestBody AddOrderItemRequest request) {
        return orderService.addItemToOrder(orderId, request);
    }

    @GetMapping("/orders/meat-temperatures")
    public MeatTemperature[] getMeatTemperatures() {
        return MeatTemperature.values();
    }

    @GetMapping("/tables/{tableNumber}/open-order")
    public AddItemResponse getOpenOrderByTable(@PathVariable int tableNumber) {
        return orderService.getOpenOrderForTable(tableNumber);
    }

    @GetMapping("/orders/{orderId}")
    public AddItemResponse getOrder(@PathVariable Long orderId) {
        return orderService.getOrderDetails(orderId);
    }
    @GetMapping("/orders/{orderId}/receipt")
    public ReceiptDto getReceipt(@PathVariable Long orderId) {
        return orderService.calculateReceipt(orderId);
    }

    @PostMapping("/orders/{orderId}/pay")
    public ReceiptDto payOrder(@PathVariable Long orderId) {
        return orderService.payOrder(orderId);
    }

    @PostMapping("/tables/{tableNumber}/send")
    public void sendToKitchenAndBar(@PathVariable int tableNumber){
        orderService.sendToKitchenAndBar(tableNumber);
    }

    @PostMapping("/orders/comp/validate")
    public void validateChiefPin(@RequestBody PinRequest request) {
        orderService.validateChiefPin(request.pin());
    }

    @PostMapping("/orders/{orderId}/comp")
    public ReceiptDto compOrder(
            @PathVariable Long orderId,
            @RequestBody CompOrderRequest request
    ) {
        return orderService.compOrder(orderId, request.pin(), request.reason());
    }

    @PostMapping("/orders/{orderId}/items/comp")
    public ReceiptDto compItems(
            @PathVariable Long orderId,
            @RequestBody CompItemRequest request
    ) {
        return orderService.compOrderItems(
                orderId,
                request.pin(),
                request.reason(),
                request.orderItemIds()
        );
    }
}
