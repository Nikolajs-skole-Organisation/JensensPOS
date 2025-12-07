package org.example.backendpos.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Condiment extends Item{
    private long id;
    private String name;
    private double price;
    private boolean isGratis;

    public Condiment(long id, String name, double price, boolean isGratis) {
        super(id, name, price);
        this.isGratis = isGratis;
    }
}
