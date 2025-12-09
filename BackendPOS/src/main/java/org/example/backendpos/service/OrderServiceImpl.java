package org.example.backendpos.service;

import org.example.backendpos.dto.*;
import org.example.backendpos.exception.DrinkItemNotFoundException;
import org.example.backendpos.exception.FoodItemNotFoundException;
import org.example.backendpos.exception.MissingMeatTemperatureException;
import org.example.backendpos.exception.OrderNotFoundException;
import org.example.backendpos.model.order.*;
import org.example.backendpos.repository.CategoryRepository;
import org.example.backendpos.repository.DrinkItemRepository;
import org.example.backendpos.repository.FoodItemRepository;
import org.example.backendpos.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final DrinkItemRepository drinkItemRepository;
    private final FoodItemRepository foodItemRepository;

    public OrderServiceImpl(OrderRepository orderRepository, CategoryRepository categoryRepository,
                            DrinkItemRepository drinkItemRepository, FoodItemRepository foodItemRepository) {
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
        this.drinkItemRepository = drinkItemRepository;
        this.foodItemRepository = foodItemRepository;
    }

    public StartOrderResponse startOrderResponse(int tableNumber, int amountOfGuests) {

        Order order = orderRepository.findByTableNumberAndOrderStatus(tableNumber, OrderStatus.OPEN)
                .orElseGet(() -> {
                    Order newOrder = new Order();
                    newOrder.setTableNumber(tableNumber);
                    newOrder.setAmountOfGuests(amountOfGuests);
                    newOrder.setOrderStatus(OrderStatus.OPEN);
                    return  orderRepository.save(newOrder);
                });

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
                order.getId(),
                order.getTableNumber(),
                order.getAmountOfGuests(),
                order.getOrderStatus().name(),
                categoryDtos
        );
    }

    @Transactional
    public Order addItemToOrder(Long orderId, AddOrderItemRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with Id: " + orderId));

        OrderItem orderItem;

        if (request.itemType() == ItemType.FOOD) {
            FoodItem foodItem = foodItemRepository.findById(request.itemId())
                    .orElseThrow(() -> new FoodItemNotFoundException("Food item not found with id: " + request.itemId()));

            if (foodItem.isItMeat() && request.meatTemperature() == null) {
                throw new MissingMeatTemperatureException("Meat item requires a meat temperature");
            }

            orderItem = order.getItems().stream()
                    .filter(oi -> oi.getFoodItem() != null &&
                            oi.getFoodItem().getId().equals(foodItem.getId()))
                    .findFirst()
                    .orElse(null);

            if (orderItem == null) {
                orderItem = new OrderItem();
                orderItem.setFoodItem(foodItem);
                orderItem.setQuantity(1);
                if (foodItem.isItMeat()) {
                    orderItem.setMeatTemperature(request.meatTemperature());
                }
                order.addItem(orderItem);
            } else {
                orderItem.incrementQuantity();
            }
        } else {
            DrinkItem drinkItem = drinkItemRepository.findById(request.itemId())
                    .orElseThrow(() -> new DrinkItemNotFoundException("Drink item not found with id: " + request.itemId()));

            orderItem = order.getItems().stream()
                    .filter(oi -> oi.getDrinkItem() != null &&
                            oi.getDrinkItem().getId().equals(drinkItem.getId()))
                    .findFirst()
                    .orElse(null);

            if (orderItem == null) {
                orderItem = new OrderItem();
                orderItem.setDrinkItem(drinkItem);
                orderItem.setQuantity(1);
                order.addItem(orderItem);
            } else {
                orderItem.incrementQuantity();
            }
        }

        return orderRepository.save(order);
    }
}
