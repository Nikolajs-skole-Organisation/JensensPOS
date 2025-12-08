package org.example.backendpos.service;

import org.example.backendpos.model.order.Order;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Override
    public List<String> getAllCategories() {
        return List.of();
    }

    @Override
    public void startOrder(Order order) {

    }


}
