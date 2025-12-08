package org.example.backendpos.service;

import org.example.backendpos.dto.CategoryDto;
import org.example.backendpos.dto.DrinkItemDto;
import org.example.backendpos.dto.FoodItemDto;
import org.example.backendpos.dto.StartOrderResponse;
import org.example.backendpos.model.order.Category;
import org.example.backendpos.model.order.Order;
import org.example.backendpos.model.order.OrderStatus;
import org.example.backendpos.repository.CategoryRepository;
import org.example.backendpos.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;

    public OrderServiceImpl(OrderRepository orderRepository, CategoryRepository categoryRepository) {
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
    }

    public StartOrderResponse startOrderResponse(int tableNumber, int amountOfGuests) {
        Order order = new Order();
        order.setTableNumber(tableNumber);
        order.setAmountOfGuests(amountOfGuests);
        order.setOrderStatus(OrderStatus.OPEN);

        Order saved = orderRepository.save(order);

        List<Category> categories = categoryRepository.findAll();

        List<CategoryDto> categoryDtos = categories.stream()
                .map(category -> new CategoryDto(
                        category.getCategoryId(),
                        category.getName(),
                        category.getFoodItems().stream()
                                .map(food -> new FoodItemDto(
                                        food.getId(),
                                        food.getName(),
                                        food.getPrice(),
                                        food.isItMeat(),
                                        food.isAvailableForTakeaway(),
                                        food.isAvailableForPersonnel()
                                ))
                                .toList(),
                        category.getDrinkItems().stream()
                                .map(drink -> new DrinkItemDto(
                                        drink.getId(),
                                        drink.getName(),
                                        drink.getPrice()
                                ))
                                .toList()
                ))
                .toList();

        return new StartOrderResponse(
                saved.getId(),
                saved.getTableNumber(),
                saved.getAmountOfGuests(),
                saved.getOrderStatus().name(),
                categoryDtos
        );
    }
}
