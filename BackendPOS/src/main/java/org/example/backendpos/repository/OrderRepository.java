package org.example.backendpos.repository;

import org.example.backendpos.model.order.Order;
import org.example.backendpos.model.order.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order,Long> {

    Optional<Order> findByTableNumberAndOrderStatus(int tableNumber, OrderStatus orderStatus);
}
