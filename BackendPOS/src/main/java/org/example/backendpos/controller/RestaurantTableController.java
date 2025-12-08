package org.example.backendpos.controller;

import org.example.backendpos.dto.RestaurantTableDto;
import org.example.backendpos.service.RestaurantTableService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/service/overview")
public class RestaurantTableController {

    private final RestaurantTableService service;

    public RestaurantTableController(RestaurantTableService service) {
        this.service = service;
    }

    @GetMapping
    public List<RestaurantTableDto> getAll() {
        return service.getAllTables();
    }
}
