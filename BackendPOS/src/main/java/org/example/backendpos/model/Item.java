package org.example.backendpos.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Item {

    private Long id;
    private String name;
    private double price;

    public Item(Long id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
}
