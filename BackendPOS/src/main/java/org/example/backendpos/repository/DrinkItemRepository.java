package org.example.backendpos.repository;

import org.example.backendpos.model.order.DrinkItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrinkItemRepository extends JpaRepository<DrinkItem, Long> {
}
