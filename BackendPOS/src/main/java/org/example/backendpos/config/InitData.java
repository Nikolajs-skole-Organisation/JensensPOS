package org.example.backendpos.config;

import org.example.backendpos.model.RestaurantTable;
import org.example.backendpos.model.TableStatus;
import org.example.backendpos.model.order.Category;
import org.example.backendpos.model.order.DrinkItem;
import org.example.backendpos.model.order.FoodItem;
import org.example.backendpos.repository.CategoryRepository;
import org.example.backendpos.repository.DrinkItemRepository;
import org.example.backendpos.repository.FoodItemRepository;
import org.example.backendpos.repository.RestaurantTableRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class InitData implements CommandLineRunner {

    private final RestaurantTableRepository restaurantTableRepository;
    private final CategoryRepository categoryRepository;
    private final FoodItemRepository foodItemRepository;
    private final DrinkItemRepository drinkItemRepository;

    public InitData(RestaurantTableRepository restaurantTableRepository, CategoryRepository categoryRepository,
                    FoodItemRepository foodItemRepository, DrinkItemRepository drinkItemRepository) {
        this.restaurantTableRepository = restaurantTableRepository;
        this.categoryRepository = categoryRepository;
        this.foodItemRepository = foodItemRepository;
        this.drinkItemRepository = drinkItemRepository;
    }

    @Override
    public void run(String... args) {

        initTables();
        initMenu();
    }

    private void initTables(){
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

    private void initMenu() {
        // If categories already exist, assume menu is seeded
        if (categoryRepository.count() > 0) {
            return;
        }

        // ----- Categories -----
        Category forretter      = new Category();
        forretter.setName("FORRETTER");

        Category hovedretter    = new Category();
        hovedretter.setName("HOVEDRETTER");

        Category desserter      = new Category();
        desserter.setName("DESSERTER");

        Category drinks         = new Category();
        drinks.setName("DRINKS");

        Category gratis         = new Category();
        gratis.setName("GRATIS");

        Category tilbud         = new Category();
        tilbud.setName("TILBUD");

        Category personaleRetter = new Category();
        personaleRetter.setName("PERSONALE RETTER");

        categoryRepository.saveAll(List.of(
                forretter, hovedretter, desserter, drinks, gratis, tilbud, personaleRetter
        ));

        // ----- Food items -----
        FoodItem garlicBread = new FoodItem(
                "Hvidløgsbrød",
                39.0,
                forretter,
                null,
                false,
                true,
                false
        );

        FoodItem nachos = new FoodItem(
                "Nachos med ost",
                69.0,
                forretter,
                null,
                false,
                true,
                false
        );

        FoodItem steak = new FoodItem(
                "Oksebøf 250g",
                199.0,
                hovedretter,
                null,
                true,   // isItMeat
                false,  // availableForTakeaway
                false   // availableForPersonnel
        );

        FoodItem burger = new FoodItem(
                "Jensens Burger",
                149.0,
                hovedretter,
                null,
                true,
                true,
                false
        );

        FoodItem kidsPasta = new FoodItem(
                "Børnepasta",
                79.0,
                hovedretter,
                null,
                false,
                true,
                false
        );

        FoodItem iceCream = new FoodItem(
                "Is dessert",
                59.0,
                desserter,
                null,
                false,
                true,
                false
        );

        FoodItem brownie = new FoodItem(
                "Chokolade brownie",
                69.0,
                desserter,
                null,
                false,
                true,
                false
        );

        // Gratis / personale / tilbud examples
        FoodItem brødOgSmør = new FoodItem(
                "Brød og smør",
                0.0,
                gratis,
                null,
                false,
                false,
                false
        );

        FoodItem personaleRet = new FoodItem(
                "Dagens personaleret",
                0.0,
                personaleRetter,
                null,
                true,
                false,
                true
        );

        FoodItem dagensTilbud = new FoodItem(
                "Dagens tilbudsret",
                99.0,
                tilbud,
                null,
                true,
                false,
                false
        );

        foodItemRepository.saveAll(List.of(
                garlicBread,
                nachos,
                steak,
                burger,
                kidsPasta,
                iceCream,
                brownie,
                brødOgSmør,
                personaleRet,
                dagensTilbud
        ));

        // ----- Drink items -----
        DrinkItem cola = new DrinkItem(
                "Coca Cola 0,5L",
                32.0,
                drinks
        );

        DrinkItem colaZero = new DrinkItem(
                "Coca Cola Zero 0,5L",
                32.0,
                drinks
        );

        DrinkItem fadøl = new DrinkItem(
                "Fadøl 0,5L",
                49.0,
                drinks
        );

        DrinkItem vand = new DrinkItem(
                "Kildevand",
                29.0,
                drinks
        );

        DrinkItem husetsVin = new DrinkItem(
                "Husets rødvin glas",
                59.0,
                drinks
        );

        drinkItemRepository.saveAll(List.of(
                cola,
                colaZero,
                fadøl,
                vand,
                husetsVin
        ));
    }
}


