package org.example.backendpos.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DrinkItem {
    private Long id;
    private String name;
    private Double price;
    private Boolean includeIce;

    public DrinkItem(Long id, String name, Double price, Boolean includeIce) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.includeIce = includeIce;
    }
}
