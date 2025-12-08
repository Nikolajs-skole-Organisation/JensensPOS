package org.example.backendpos.model.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "food_items")
@Getter
@Setter
@NoArgsConstructor
@AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "food_id")),
        @AttributeOverride(name = "name", column = @Column(name = "food_name"))
})
public class FoodItem extends Item {

    @Column(name = "is_meat", nullable = false)
    private boolean isItMeat;

    @Column(name = "available_for_takeaway", nullable = false)
    private boolean availableForTakeaway;

    @Column(name = "available_for_personnel", nullable = false)
    private boolean availableForPersonnel;

    @Transient
    private List<Condiment> condiments;

    public FoodItem(String name, Double price, Category category, List<Condiment> condiments, boolean isItMeat,
                    boolean availableForTakeaway, boolean availableForPersonnel){
        super(name, price, category);
        this.condiments = condiments;
        this.isItMeat = isItMeat;
        this.availableForTakeaway = availableForTakeaway;
        this.availableForPersonnel = availableForPersonnel;
    }
}
