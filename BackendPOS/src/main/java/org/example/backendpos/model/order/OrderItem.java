package org.example.backendpos.model.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "order_item")
@Getter
@Setter
@NoArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_item_id")
    private FoodItem foodItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "drink_item_id")
    private DrinkItem drinkItem;

    @Column(nullable = false)
    int quantity = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "meat_temperature")
    private MeatTemperature meatTemperature;

    @Column(name = "has_been_sent")
    private boolean hasBeenSent;

    private Instant sentAt;

    private Instant bumpedAt;

    public void incrementQuantity(){
        quantity++;
    }
}
