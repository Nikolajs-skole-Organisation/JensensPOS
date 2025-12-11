package org.example.backendpos.service;

import org.example.backendpos.dto.*;
import org.example.backendpos.exception.DrinkItemNotFoundException;
import org.example.backendpos.exception.FoodItemNotFoundException;
import org.example.backendpos.exception.MissingMeatTemperatureException;
import org.example.backendpos.exception.OrderNotFoundException;
import org.example.backendpos.model.RestaurantTable;
import org.example.backendpos.model.TableStatus;
import org.example.backendpos.model.order.*;
import org.example.backendpos.repository.CategoryRepository;
import org.example.backendpos.repository.DrinkItemRepository;
import org.example.backendpos.repository.FoodItemRepository;
import org.example.backendpos.repository.OrderRepository;
import org.example.backendpos.repository.RestaurantTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final DrinkItemRepository drinkItemRepository;
    private final FoodItemRepository foodItemRepository;
    private final AddItemResponseMapper addItemResponseMapper;
    private final RestaurantTableRepository restaurantTableRepository;

    public OrderServiceImpl(OrderRepository orderRepository, CategoryRepository categoryRepository,
                            DrinkItemRepository drinkItemRepository, FoodItemRepository foodItemRepository,
                            AddItemResponseMapper addItemResponseMapper, RestaurantTableRepository restaurantTableRepository) {
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
        this.drinkItemRepository = drinkItemRepository;
        this.foodItemRepository = foodItemRepository;
        this.addItemResponseMapper = addItemResponseMapper;
        this.restaurantTableRepository = restaurantTableRepository;
    }

    public StartOrderResponse startOrderResponse(int tableNumber, int amountOfGuests) {

        Order order = orderRepository.findByTableNumberAndOrderStatus(tableNumber, OrderStatus.OPEN)
                .orElseGet(() -> {
                    Order newOrder = new Order();
                    newOrder.setTableNumber(tableNumber);
                    newOrder.setAmountOfGuests(amountOfGuests);
                    newOrder.setOrderStatus(OrderStatus.OPEN);
                    return orderRepository.save(newOrder);
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
    @Override
    public AddItemResponse addItemToOrder(Long orderId, AddOrderItemRequest request) {
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

        } else { // DRINK
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

        Order saved = orderRepository.save(order);
        return addItemResponseMapper.toDto(saved);
    }


    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with Id: " + orderId));
    }

    @Override
    public AddItemResponse getOpenOrderForTable(int tableNumber) {
        Order order = orderRepository.findByTableNumberAndOrderStatus(tableNumber, OrderStatus.OPEN)
                .orElseThrow(() -> new OrderNotFoundException(
                        "No open order for table " + tableNumber
                ));

        return addItemResponseMapper.toDto(order);
    }

    @Override
    public AddItemResponse getOrderDetails(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with Id: " + orderId));

        return addItemResponseMapper.toDto(order);
    }
    @Override
    public ReceiptDto calculateReceipt(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with Id: " + orderId));

        List<OrderItem> items = order.getItems();
        List<ReceiptLineDto> lines = new java.util.ArrayList<>();

        double total = 0.0;

        for (OrderItem oi : items) {
            String name;
            double unitPrice;

            if (oi.getFoodItem() != null) {
                name = oi.getFoodItem().getName();
                unitPrice = oi.getFoodItem().getPrice();
            } else {
                name = oi.getDrinkItem().getName();
                unitPrice = oi.getDrinkItem().getPrice();
            }

            double lineTotal = unitPrice * oi.getQuantity();
            total += lineTotal;

            ReceiptLineDto line = new ReceiptLineDto(
                    name,
                    oi.getQuantity(),
                    unitPrice,
                    lineTotal
            );
            lines.add(line);
        }

        return new ReceiptDto(
                order.getId(),
                order.getTableNumber(),
                lines,
                total
        );
    }

    @Transactional
    @Override
    public ReceiptDto payOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with Id: " + orderId));

        ReceiptDto receipt = calculateReceipt(orderId);

        order.setOrderStatus(OrderStatus.PAID);
        orderRepository.save(order);

        RestaurantTable restaurantTable = restaurantTableRepository
                .findByTableNumber(order.getTableNumber())
                .orElse(null);

        if (restaurantTable != null) {
            restaurantTable.setStatus(TableStatus.FREE);
            restaurantTableRepository.save(restaurantTable);
        }

        return receipt;
    }
}
