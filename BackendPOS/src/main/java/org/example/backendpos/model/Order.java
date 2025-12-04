package org.example.backendpos.model;

import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class Order {

    private List<FoodItem> foodItems;
    private List<DrinkItem> drinkItems;
    private String nameOfServer;
    private int tableNumber;
    private int amountOfGuests;
    private boolean hasBeenSent;

    public Order(List<FoodItem> foodItems, List<DrinkItem> drinkItems, String nameOfServer, int tableNumber, int amountOfGuests, boolean hasBeenSent) {
        this.foodItems = foodItems;
        this.drinkItems = drinkItems;
        this.nameOfServer = nameOfServer;
        this.tableNumber = tableNumber;
        this.amountOfGuests = amountOfGuests;
        this.hasBeenSent = hasBeenSent;
    }


}
