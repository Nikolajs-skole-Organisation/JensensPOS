package org.example.backendpos.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FoodItem extends Item{
    private List<Condiment> condiments;

    public FoodItem(Long id, String name, Double price, List<Condiment> condiments) {
        super(id, name, price);
        this.condiments = condiments;
    }
}
