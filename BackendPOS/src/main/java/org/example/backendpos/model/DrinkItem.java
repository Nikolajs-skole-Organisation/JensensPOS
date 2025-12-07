package org.example.backendpos.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DrinkItem extends Item{
    private Boolean includeIce;

    public DrinkItem(Long id, String name, Double price, Boolean includeIce) {
        super(id, name, price);
        this.includeIce = includeIce;
    }
}
