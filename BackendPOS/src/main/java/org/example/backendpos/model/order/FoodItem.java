package org.example.backendpos.model.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FoodItem extends Item {
    private List<Condiment> condiments;
    private boolean isItMeat;

    public FoodItem(Long id, String name, Double price, List<Condiment> condiments, boolean isItMeat) {
        super(id, name, price);
        this.condiments = condiments;
        this.isItMeat = isItMeat;
    }
}
