package org.example.backendpos.service;

import org.example.backendpos.model.order.Order;

import java.util.List;

public interface OrderService {
    List<String> getAllCategories();
    public void startOrder(Order order);
}
