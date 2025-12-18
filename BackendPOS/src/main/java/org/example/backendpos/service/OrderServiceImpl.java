package org.example.backendpos.service;

import org.example.backendpos.dto.*;
import org.example.backendpos.exception.*;
import org.example.backendpos.model.EmployeeRole;
import org.example.backendpos.model.RestaurantTable;
import org.example.backendpos.model.TableStatus;
import org.example.backendpos.model.order.*;
import org.example.backendpos.repository.CategoryRepository;
import org.example.backendpos.repository.DrinkItemRepository;
import org.example.backendpos.repository.FoodItemRepository;
import org.example.backendpos.repository.OrderRepository;
import org.example.backendpos.repository.RestaurantTableRepository;
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
    private final RestaurantTableRepository restaurantTableRepository;
    private final OrderItemRepository orderItemRepository;
    private final EmployeeRepository employeeRepository;

    public OrderServiceImpl(OrderRepository orderRepository, CategoryRepository categoryRepository,
                            DrinkItemRepository drinkItemRepository, FoodItemRepository foodItemRepository,
                            AddItemResponseMapper addItemResponseMapper, RestaurantTableRepository restaurantTableRepository,
                            OrderItemRepository orderItemRepository, EmployeeRepository employeeRepository) {
        this.orderRepository = orderRepository;
        this.categoryRepository = categoryRepository;
        this.drinkItemRepository = drinkItemRepository;
        this.foodItemRepository = foodItemRepository;
        this.addItemResponseMapper = addItemResponseMapper;
        this.restaurantTableRepository = restaurantTableRepository;
        this.orderItemRepository = orderItemRepository;
        this.employeeRepository = employeeRepository;
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

        restaurantTableRepository.findByTableNumber(tableNumber)
                .ifPresent(table -> {
                    if (table.getStatus() != TableStatus.BLOCKED) {
                        table.setStatus(TableStatus.OCCUPIED);
                        restaurantTableRepository.save(table);
                    }
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
    public ReceiptDto calculateReceipt(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with Id: " + orderId));

        boolean comped = order.isComped();

        List<OrderItem> items = order.getItems();
        List<ReceiptLineDto> lines = new java.util.ArrayList<>();

        double total = 0.0;

        for (OrderItem oi : items) {
            String name;
            double unitPrice;

            if (oi.getFoodItem() != null) {
                name = oi.getFoodItem().getName();
                unitPrice = comped ? 0.0 : oi.getFoodItem().getPrice();
            } else {
                name = oi.getDrinkItem().getName();
                unitPrice = comped ? 0.0 : oi.getDrinkItem().getPrice();
            }

            boolean lineComped = comped || oi.isComped();
            if (lineComped) {
                unitPrice = 0.0;
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
        return orderItemRepository.findItemsAfterForStation(since, lastId, "KITCHEN")
                .stream()
                .map(KitchenOrderItemDto::from)
                .toList();
    }

    @Override
    public List<BarOrderItemDto> getBarItems(Instant since, long lastId){
        return orderItemRepository.findItemsAfterForStation(since, lastId, "BAR")
                .stream()
                .map(BarOrderItemDto::from)
                .toList();
    }

    @Transactional
    @Override
    public void bumpKitchenTicket(int tableNumber) {
        bumpStation(tableNumber, "KITCHEN");
    }

    @Transactional
    @Override
    public void bumpBarTicket(int tableNumber) {
        bumpStation(tableNumber, "BAR");
    }

    @Transactional
    @Override
    public ReceiptDto compOrder(Long orderId, String pin, String reason) {

        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("Den skal bruge chief kode");
        }

        var employee = employeeRepository.findByPinCode(pin)
                .orElseThrow(() -> new IllegalArgumentException("Den skal bruge chief kode"));

        if (employee.getRole() != EmployeeRole.CHIEF) {
            throw new IllegalArgumentException("Den skal bruge chief kode");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Du skal angive en årsag");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Ordre ikke fundet med id: " + orderId));

        if (order.getOrderStatus() != OrderStatus.OPEN) {
            throw new IllegalStateException("Kun åbne orders kan compes");
        }

        order.setComped(true);
        order.setOrderStatus(OrderStatus.PAID);
        orderRepository.save(order);

        restaurantTableRepository.findByTableNumber(order.getTableNumber())
                .ifPresent(t -> {
                    t.setStatus(TableStatus.FREE);
                    restaurantTableRepository.save(t);
                });

        return calculateReceipt(orderId);
    }

    @Override
    public void validateChiefPin(String pin) {
        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("Den skal bruge chief kode");
        }

        var employee = employeeRepository.findByPinCode(pin)
                .orElseThrow(() -> new IllegalArgumentException("Den skal bruge chief kode"));

        if (employee.getRole() != EmployeeRole.CHIEF) {
            throw new IllegalArgumentException("Den skal bruge chief kode");
        }
    }

    private void bumpStation(int tableNumber, String station){
        Order order = orderRepository.findByTableNumberAndOrderStatus(tableNumber, OrderStatus.OPEN)
                .orElseThrow(() -> new OrderNotFoundException("Ordre ikke fundet for bor nummer: " +tableNumber));

        orderItemRepository.bumpItemsForStation(
                order.getId(),
                Instant.now().truncatedTo(ChronoUnit.MILLIS),
                station
        );
    }

    @Transactional
    @Override
    public ReceiptDto compOrderItems(Long orderId, String pin, String reason, List<Long> orderItemIds) {

        if (pin == null || !pin.matches("\\d{4}")) {
            throw new IllegalArgumentException("Der skal bruges chief kode");
        }

        var employee = employeeRepository.findByPinCode(pin)
                .orElseThrow(() -> new IllegalArgumentException("Der skal bruges chief kode"));

        if (employee.getRole() != EmployeeRole.CHIEF) {
            throw new IllegalArgumentException("Der skal bruges chief kode");
        }

        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Du skal angive en årsag");
        }

        if (orderItemIds == null || orderItemIds.isEmpty()) {
            throw new IllegalArgumentException("Du skal vælge mindst 1");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with Id: " + orderId));

        if (order.getOrderStatus() != OrderStatus.OPEN) {
            throw new IllegalStateException("Kun åbne orders kan compes");
        }

        if (order.isComped()) {
            throw new IllegalStateException("Ordren er allerede comped");
        }

        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);

        for (OrderItem oi : order.getItems()) {
            if (orderItemIds.contains(oi.getId())) {
                oi.setComped(true);
                oi.setCompedAt(now);
                oi.setCompReason(reason);
            }
        }

        orderRepository.save(order);

        return calculateReceipt(orderId);
    }
}
