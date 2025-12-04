package org.example.backendpos.service;

import org.example.backendpos.dto.RestaurantTableDto;

import java.util.List;

public interface RestaurantTableService {
    List<RestaurantTableDto> getAllTables();
}
