package org.example.backendpos.repository;

import org.example.backendpos.model.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query("""
              SELECT oi
              FROM OrderItem oi
              JOIN oi.order o
              WHERE oi.hasBeenSent = true
                AND oi.bumpedAt IS NULL
                AND oi.foodItem IS NOT NULL
                AND o.orderStatus = 'OPEN'
                AND (
                     oi.sentAt > :since
                  OR (oi.sentAt = :since AND oi.id > :lastId)
                )
              ORDER BY oi.sentAt ASC, oi.id ASC
            """)
    List<OrderItem> findKitchenItemsAfter(@Param("since") Instant since, @Param("lastId") Long lastId);

    @Modifying
    @Query("""
              UPDATE OrderItem oi
              SET oi.bumpedAt = :now
              WHERE oi.order.id = :orderId
                AND oi.hasBeenSent = true
                AND oi.bumpedAt IS NULL
                AND oi.foodItem IS NOT NULL
            """)
    int bumpKitchenItems(@Param("orderId") Long orderId, @Param("now") Instant now);


}
