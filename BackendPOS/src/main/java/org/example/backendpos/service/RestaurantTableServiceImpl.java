package org.example.backendpos.service;

import org.example.backendpos.dto.RestaurantTableDto;
import org.example.backendpos.dto.RestaurantTableMapper;
import org.example.backendpos.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RestaurantTableServiceImpl implements RestaurantTableService {

    private final RestaurantTableRepository repository;
    private final RestaurantTableMapper restaurantTableMapper;

    public RestaurantTableServiceImpl(RestaurantTableRepository repository, RestaurantTableMapper restaurantTableMapper) {
        this.repository = repository;
        this.restaurantTableMapper = restaurantTableMapper;
    }

    @Override
    public List<RestaurantTableDto> getAllTables() {
        return repository.findAll()
                .stream()
                .map(restaurantTableMapper::toDto)
                .toList();
    }
}