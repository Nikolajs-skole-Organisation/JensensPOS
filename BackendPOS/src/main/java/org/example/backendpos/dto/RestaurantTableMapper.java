package org.example.backendpos.dto;

import org.example.backendpos.model.RestaurantTable;
import org.springframework.stereotype.Component;

@Component
public class RestaurantTableMapper {

    public RestaurantTableDto toDto (RestaurantTable restaurantTable) {
        return new RestaurantTableDto(
                restaurantTable.getId(),
                restaurantTable.getTableNumber(),
                restaurantTable.getRowStart(),
                restaurantTable.getColStart(),
                restaurantTable.getWidth(),
                restaurantTable.getHeight(),
                restaurantTable.getStatus().name()
        );
    }
}
