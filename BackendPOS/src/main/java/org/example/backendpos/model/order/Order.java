package org.example.backendpos.model.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    private String nameOfServer;
    private int tableNumber;
    private int amountOfGuests;
    private boolean hasBeenSent;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    public void addItem(OrderItem item){
        items.add(item);
        item.setOrder(this);
    }
}
