package org.example.backendpos.model.order;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Condiment extends Item {
    private boolean isGratis;


    public Condiment(long id, String name, double price, Long categoryId,boolean isGratis) {
        super(id, name, price, categoryId);
        this.isGratis = isGratis;
    }
}
