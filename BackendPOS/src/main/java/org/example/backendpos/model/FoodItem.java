package org.example.backendpos.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FoodItem {
    private Long id;
    private String name;
    private Double price;
    private List<Condiment> condiments;

    public FoodItem(Long id, String name, Double price, List<Condiment> condiments) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.condiments = condiments;
    }
}
