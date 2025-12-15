package org.example.backendpos.service;

import org.example.backendpos.dto.*;
import org.example.backendpos.exception.DrinkItemNotFoundException;
import org.example.backendpos.exception.FoodItemNotFoundException;
import org.example.backendpos.exception.MissingMeatTemperatureException;
import org.example.backendpos.exception.OrderNotFoundException;
import org.example.backendpos.model.order.*;
import org.example.backendpos.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;
    private final DrinkItemRepository drinkItemRepository;
    private final FoodItemRepository foodItemRepository;
    private final AddItemResponseMapper addItemResponseMapper;
    private final OrderItemRepository orderItemRepository;

    public OrderServiceImpl(OrderRepository orderRepository, CategoryRepository categoryRepository,
                            DrinkItemRepository drinkItemRepository, FoodItemRepository foodItemRepository,
                            AddItemResponseMapper addItemResponseMapper, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
        this.drinkItemRepository = drinkItemRepository;
        this.foodItemRepository = foodItemRepository;
        this.addItemResponseMapper = addItemResponseMapper;
        this.orderItemRepository = orderItemRepository;
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
                    .filter(oi ->
                            !oi.isHasBeenSent() &&
                                    oi.getFoodItem() != null &&
                                    oi.getFoodItem().getId().equals(foodItem.getId()) &&
                                    (foodItem.isItMeat()
                                            ? oi.getMeatTemperature() == request.meatTemperature()
                                            : true)
                    )
                    .findFirst()
                    .orElse(null);

            if (orderItem == null) {
                orderItem = new OrderItem();
                orderItem.setFoodItem(foodItem);
                orderItem.setQuantity(1);
                if (foodItem.isItMeat()) {
                    orderItem.setMeatTemperature(request.meatTemperature());
                }
                orderItem.setHasBeenSent(false);
                order.addItem(orderItem);
            } else {
                orderItem.incrementQuantity();
            }

        } else { // DRINK
            DrinkItem drinkItem = drinkItemRepository.findById(request.itemId())
                    .orElseThrow(() -> new DrinkItemNotFoundException("Drink item not found with id: " + request.itemId()));

            orderItem = order.getItems().stream()
                    .filter(oi ->
                            !oi.isHasBeenSent() &&
                                    oi.getDrinkItem() != null &&
                                    oi.getDrinkItem().getId().equals(drinkItem.getId())
                    )
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
    @Transactional
    public void sendToKitchenAndBar(int tableNumber) {
        Order order = orderRepository.findByTableNumberAndOrderStatus(tableNumber, OrderStatus.OPEN)
                .orElseThrow(() -> new OrderNotFoundException("No open order for table " + tableNumber));

        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        for (OrderItem item : order.getItems()) {
            if (!item.isHasBeenSent()) {
                item.setHasBeenSent(true);
                item.setSentAt(now);
            }
        }

        orderRepository.save(order);
    }


    @Override
    public List<KitchenOrderItemDto> getKitchenItems(Instant since, long lastId) {
        return orderItemRepository.findKitchenItemsAfter(since, lastId)
                .stream()
                .map(KitchenOrderItemDto::from)
                .toList();
    }

    @Transactional
    public void bumpKitchenTicket(int tableNumber) {
        Order order = orderRepository.findByTableNumberAndOrderStatus(tableNumber, OrderStatus.OPEN)
                .orElseThrow(() -> new OrderNotFoundException("No open order for table " + tableNumber));

        orderItemRepository.bumpKitchenItems(order.getId(), Instant.now().truncatedTo(ChronoUnit.MILLIS));
    }

}
