package org.example.backendpos.model.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "drink_items")
@Getter
@Setter
@NoArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "drink_id")),
        @AttributeOverride(name = "name", column = @Column(name = "drink_name"))
}
)
public class DrinkItem extends Item {
    public DrinkItem(String name, Double price, Category category){
        super(name, price, category);
    }
}
