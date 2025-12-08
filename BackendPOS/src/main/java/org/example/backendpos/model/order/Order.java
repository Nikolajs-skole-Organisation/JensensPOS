package org.example.backendpos.model.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Order {

    private Long id;
    private List<FoodItem> foodItems;
    private List<DrinkItem> drinkItems;
    private String nameOfServer;
    private int tableNumber;
    private int amountOfGuests;
    private boolean hasBeenSent;
    private OrderStatus orderStatus;

    public Order(List<FoodItem> foodItems, List<DrinkItem> drinkItems, String nameOfServer, int tableNumber,
                 int amountOfGuests, boolean hasBeenSent, OrderStatus orderStatus) {
        this.foodItems = foodItems;
        this.drinkItems = drinkItems;
        this.nameOfServer = nameOfServer;
        this.tableNumber = tableNumber;
        this.amountOfGuests = amountOfGuests;
        this.hasBeenSent = hasBeenSent;
        this.orderStatus = orderStatus;
    }
}
