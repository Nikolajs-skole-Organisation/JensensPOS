package org.example.backendpos.config;

import org.example.backendpos.model.RestaurantTable;
import org.example.backendpos.model.TableStatus;
import org.example.backendpos.repository.RestaurantTableRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InitData implements CommandLineRunner {

    private final RestaurantTableRepository restaurantTableRepository;

    public InitData(RestaurantTableRepository restaurantTableRepository) {
        this.restaurantTableRepository = restaurantTableRepository;
    }

    @Override
    public void run(String... args) {


        if (restaurantTableRepository.count() > 0) {
            return;
        }

        List<RestaurantTable> restaurantTables = List.of(
                // Pink area
                new RestaurantTable(null, 3, 2, 0, 3, 1, TableStatus.FREE),
                new RestaurantTable(null, 4, 0, 4, 1, 2, TableStatus.FREE),
                new RestaurantTable(null, 5, 0, 8, 1, 2, TableStatus.OCCUPIED),
                new RestaurantTable(null, 6, 0, 11, 1, 2, TableStatus.OCCUPIED),

                // Blue area
                new RestaurantTable(null, 9, 0, 14, 1, 1, TableStatus.RESERVED),
                new RestaurantTable(null, 8, 2, 14, 1, 1, TableStatus.RESERVED),
                new RestaurantTable(null, 7, 4, 14, 1, 2, TableStatus.FREE),

                new RestaurantTable(null, 12, 0, 18, 1, 1, TableStatus.FREE),
                new RestaurantTable(null, 11, 2, 18, 1, 1, TableStatus.OCCUPIED),
                new RestaurantTable(null, 10, 4, 18, 1, 2, TableStatus.FREE),

                new RestaurantTable(null, 16, 0, 21, 1, 1, TableStatus.FREE),
                new RestaurantTable(null, 15, 1, 21, 1, 1, TableStatus.RESERVED),
                new RestaurantTable(null, 14, 3, 21, 1, 1, TableStatus.FREE),
                new RestaurantTable(null, 13, 5, 21, 1, 1, TableStatus.FREE),

                new RestaurantTable(null, 20, 0, 24, 1, 1, TableStatus.FREE),
                new RestaurantTable(null, 19, 1, 24, 1, 1, TableStatus.OCCUPIED),
                new RestaurantTable(null, 18, 3, 24, 1, 1, TableStatus.RESERVED),
                new RestaurantTable(null, 17, 5, 24, 1, 1, TableStatus.FREE),

                new RestaurantTable(null, 23, 0, 27, 1, 1, TableStatus.FREE),
                new RestaurantTable(null, 22, 2, 27, 1, 1, TableStatus.FREE),
                new RestaurantTable(null, 21, 4, 27, 1, 2, TableStatus.FREE),

                new RestaurantTable(null, 30, 8, 19, 1, 1, TableStatus.FREE),
                new RestaurantTable(null, 31, 8, 20, 1, 1, TableStatus.OCCUPIED),

                new RestaurantTable(null, 32, 8, 22, 1, 1, TableStatus.FREE),
                new RestaurantTable(null, 33, 8, 23, 1, 1, TableStatus.OCCUPIED),
                new RestaurantTable(null, 34, 8, 24, 1, 1, TableStatus.OCCUPIED),
                new RestaurantTable(null, 35, 8, 25, 1, 1, TableStatus.FREE),

                new RestaurantTable(null, 36, 8, 27, 1, 1, TableStatus.FREE),
                new RestaurantTable(null, 37, 8, 28, 1, 1, TableStatus.FREE),

                // Green area
                new RestaurantTable(null, 42, 1, 30, 6, 1, TableStatus.FREE),
                new RestaurantTable(null, 41, 4, 30, 6, 1, TableStatus.FREE),
                new RestaurantTable(null, 40, 7, 33, 3, 1, TableStatus.FREE),

                // Locked tables
                new RestaurantTable(null, 0, 7, 14, 4, 1, TableStatus.BLOCKED),
                new RestaurantTable(null, 0, 3, 8, 4, 1, TableStatus.BLOCKED),
                new RestaurantTable(null, 0, 4, 8, 1, 2, TableStatus.BLOCKED),
                new RestaurantTable(null, 0, 4, 11, 1, 3, TableStatus.BLOCKED),
                new RestaurantTable(null, 0, 8, 3, 4, 1, TableStatus.BLOCKED),
                new RestaurantTable(null, 0, 4, 4, 2, 2, TableStatus.BLOCKED),
                new RestaurantTable(null, 0, 7, 29, 1, 2, TableStatus.BLOCKED)




        );
        restaurantTableRepository.saveAll(restaurantTables);
    }

    }


