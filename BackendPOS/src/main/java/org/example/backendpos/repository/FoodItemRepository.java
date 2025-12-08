package org.example.backendpos.repository;

import org.example.backendpos.model.order.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
}
