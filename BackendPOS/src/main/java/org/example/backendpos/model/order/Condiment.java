package org.example.backendpos.model.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Condiment extends Item {
    private boolean isGratis;


    public Condiment(long id, String name, double price, Category category,boolean isGratis) {
        super(name, price, category);
        this.isGratis = isGratis;
    }
}
