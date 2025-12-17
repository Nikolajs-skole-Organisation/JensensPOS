package org.example.backendpos.service;

import org.example.backendpos.dto.*;
import org.example.backendpos.exception.*;
import org.example.backendpos.model.RestaurantTable;
import org.example.backendpos.model.TableStatus;
import org.example.backendpos.model.order.*;
import org.example.backendpos.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private DrinkItemRepository drinkItemRepository;
    @Mock private FoodItemRepository foodItemRepository;
    @Mock private AddItemResponseMapper addItemResponseMapper;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private RestaurantTableRepository restaurantTableRepository;

    @InjectMocks
    private OrderServiceImpl service;

    // ---------- helpers ----------

    private Order openOrder(long id, int table) {
        Order o = new Order();
        // If your entity doesn't allow setId, remove this and rely on repo-returned IDs
        o.setId(id);
        o.setTableNumber(table);
        o.setAmountOfGuests(2);
        o.setOrderStatus(OrderStatus.OPEN);
        return o;
    }

    private FoodItem food(long id, String name, boolean isMeat) {
        FoodItem f = new FoodItem();
        f.setId(id);
        f.setName(name);
        f.setPrice(100.0);
        f.setItMeat(isMeat);
        f.setAvailableForTakeaway(true);
        f.setAvailableForPersonnel(true);
        return f;
    }

    private DrinkItem drink(long id, String name) {
        DrinkItem d = new DrinkItem();
        d.setId(id);
        d.setName(name);
        d.setPrice(50.0);
        return d;
    }

    private OrderItem foodItemLine(FoodItem f, int qty, boolean sent, MeatTemperature temp) {
        OrderItem oi = new OrderItem();
        oi.setFoodItem(f);
        oi.setQuantity(qty);
        oi.setHasBeenSent(sent);
        oi.setMeatTemperature(temp);
        return oi;
    }

    private OrderItem drinkItemLine(DrinkItem d, int qty, boolean sent) {
        OrderItem oi = new OrderItem();
        oi.setDrinkItem(d);
        oi.setQuantity(qty);
        oi.setHasBeenSent(sent);
        return oi;
    }

    // ---------------- startOrderResponse ----------------

    @Test
    void startOrderResponse_reusesExistingOpenOrder() {
        Order existing = openOrder(1L, 4);

        when(orderRepository.findByTableNumberAndOrderStatus(4, OrderStatus.OPEN))
                .thenReturn(Optional.of(existing));

        Category cat = new Category();
        cat.setCategoryId(10L);
        cat.setName("Starters");
        cat.setFoodItems(List.of(food(6L, "Bread", false)));
        cat.setDrinkItems(List.of(drink(2L, "Cola")));

        when(categoryRepository.findAll()).thenReturn(List.of(cat));

        StartOrderResponse res = service.startOrderResponse(4, 2);

        assertEquals(1L, res.orderId());
        assertEquals(4, res.tableNumber());
        assertEquals("OPEN", res.orderStatus());
        assertEquals(1, res.categories().size());
        assertEquals("Starters", res.categories().get(0).name());

        verify(orderRepository, never()).save(any());
    }

    @Test
    void startOrderResponse_createsNewOrderWhenNoneOpen() {
        when(orderRepository.findByTableNumberAndOrderStatus(7, OrderStatus.OPEN))
                .thenReturn(Optional.empty());

        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(99L);
            return o;
        });

        when(categoryRepository.findAll()).thenReturn(List.of());

        StartOrderResponse res = service.startOrderResponse(7, 5);

        assertEquals(99L, res.orderId());
        assertEquals(7, res.tableNumber());
        assertEquals(5, res.amountOfGuests());
        assertEquals("OPEN", res.orderStatus());

        verify(orderRepository).save(any(Order.class));
    }

    // ---------------- addItemToOrder ----------------

    @Test
    void addItemToOrder_food_nonMeat_incrementsExistingUnsentLine() {
        Order order = openOrder(1L, 4);
        FoodItem bread = food(10L, "Bread", false);

        OrderItem existing = foodItemLine(bread, 2, false, null);
        order.addItem(existing);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(bread));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        AddItemResponse dto = mock(AddItemResponse.class);
        when(addItemResponseMapper.toDto(order)).thenReturn(dto);

        AddOrderItemRequest req = new AddOrderItemRequest(10L, ItemType.FOOD, null);

        AddItemResponse res = service.addItemToOrder(1L, req);

        assertSame(dto, res);
        assertEquals(3, existing.getQuantity(), "should increment quantity on same unsent line");
        assertEquals(1, order.getItems().size(), "should not create a new row");

        verify(orderRepository).save(order);
    }

    @Test
    void addItemToOrder_food_nonMeat_createsNewLineIfExistingIsSent() {
        Order order = openOrder(1L, 4);
        FoodItem bread = food(10L, "Bread", false);

        OrderItem sentLine = foodItemLine(bread, 2, true, null);
        order.addItem(sentLine);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(foodItemRepository.findById(10L)).thenReturn(Optional.of(bread));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        when(addItemResponseMapper.toDto(order)).thenReturn(mock(AddItemResponse.class));

        service.addItemToOrder(1L, new AddOrderItemRequest(10L, ItemType.FOOD, null));

        assertEquals(2, order.getItems().size(), "should create a new row after sent");
        OrderItem newLine = order.getItems().get(1);
        assertFalse(newLine.isHasBeenSent());
        assertEquals(1, newLine.getQuantity());
        assertSame(bread, newLine.getFoodItem());
    }

    @Test
    void addItemToOrder_food_meat_requiresTemperature() {
        Order order = openOrder(1L, 4);
        FoodItem steak = food(20L, "Steak", true);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(foodItemRepository.findById(20L)).thenReturn(Optional.of(steak));

        assertThrows(MissingMeatTemperatureException.class,
                () -> service.addItemToOrder(1L, new AddOrderItemRequest(20L, ItemType.FOOD, null)));
    }

    @Test
    void addItemToOrder_food_meat_sameTemp_incrementsUnsentLine() {
        Order order = openOrder(1L, 4);
        FoodItem steak = food(20L, "Steak", true);

        OrderItem existing = foodItemLine(steak, 1, false, MeatTemperature.MEDIUM);
        order.addItem(existing);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(foodItemRepository.findById(20L)).thenReturn(Optional.of(steak));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(addItemResponseMapper.toDto(order)).thenReturn(mock(AddItemResponse.class));

        service.addItemToOrder(1L, new AddOrderItemRequest(20L, ItemType.FOOD, MeatTemperature.MEDIUM));

        assertEquals(2, existing.getQuantity());
        assertEquals(1, order.getItems().size(), "should not create new line for same temp, unsent");
    }

    @Test
    void addItemToOrder_food_meat_differentTemp_createsNewLine() {
        Order order = openOrder(1L, 4);
        FoodItem steak = food(20L, "Steak", true);

        OrderItem existing = foodItemLine(steak, 1, false, MeatTemperature.MEDIUM);
        order.addItem(existing);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(foodItemRepository.findById(20L)).thenReturn(Optional.of(steak));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(addItemResponseMapper.toDto(order)).thenReturn(mock(AddItemResponse.class));

        service.addItemToOrder(1L, new AddOrderItemRequest(20L, ItemType.FOOD, MeatTemperature.WELL_DONE));

        assertEquals(2, order.getItems().size(), "different temp should create new line");
        OrderItem newLine = order.getItems().get(1);
        assertEquals(MeatTemperature.WELL_DONE, newLine.getMeatTemperature());
        assertEquals(1, newLine.getQuantity());
    }

    @Test
    void addItemToOrder_drink_incrementsExistingUnsentLine() {
        Order order = openOrder(1L, 4);
        DrinkItem cola = drink(30L, "Cola");

        OrderItem existing = drinkItemLine(cola, 2, false);
        order.addItem(existing);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(drinkItemRepository.findById(30L)).thenReturn(Optional.of(cola));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(addItemResponseMapper.toDto(order)).thenReturn(mock(AddItemResponse.class));

        service.addItemToOrder(1L, new AddOrderItemRequest(30L, ItemType.DRINK, null));

        assertEquals(3, existing.getQuantity());
        assertEquals(1, order.getItems().size());
    }

    @Test
    void addItemToOrder_throwsIfOrderMissing() {
        when(orderRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> service.addItemToOrder(404L, new AddOrderItemRequest(1L, ItemType.FOOD, null)));
    }

    @Test
    void addItemToOrder_throwsIfFoodMissing() {
        Order order = openOrder(1L, 4);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(foodItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(FoodItemNotFoundException.class,
                () -> service.addItemToOrder(1L, new AddOrderItemRequest(999L, ItemType.FOOD, null)));
    }

    @Test
    void addItemToOrder_throwsIfDrinkMissing() {
        Order order = openOrder(1L, 4);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(drinkItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(DrinkItemNotFoundException.class,
                () -> service.addItemToOrder(1L, new AddOrderItemRequest(999L, ItemType.DRINK, null)));
    }

    // ---------------- getOrderById ----------------

    @Test
    void getOrderById_returnsOrder() {
        Order order = openOrder(1L, 4);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order res = service.getOrderById(1L);

        assertSame(order, res);
    }

    @Test
    void getOrderById_throwsIfMissing() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> service.getOrderById(1L));
    }

    // ---------------- calculateReceipt ----------------

    @Test
    void calculateReceipt_returnsCorrectLinesAndTotal() {
        Order order = new Order();
        order.setId(100L);
        order.setTableNumber(5);
        order.setOrderStatus(OrderStatus.OPEN);

        FoodItem steak = new FoodItem("Oksebøf 250g", 199.0, null, null, true, false, false);
        DrinkItem cola = new DrinkItem("Coca Cola 0,5L", 32.0, null);

        OrderItem steakItem = new OrderItem();
        steakItem.setFoodItem(steak);
        steakItem.setQuantity(2);

        OrderItem colaItem = new OrderItem();
        colaItem.setDrinkItem(cola);
        colaItem.setQuantity(3);

        order.setItems(List.of(steakItem, colaItem));

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

        ReceiptDto receipt = service.calculateReceipt(100L);

        assertNotNull(receipt);
        assertEquals(100L, receipt.orderId());
        assertEquals(5, receipt.tableNumber());
        assertNotNull(receipt.lines());
        assertEquals(2, receipt.lines().size());
        assertEquals(494.0, receipt.total(), 0.0001);

        assertTrue(containsLine(receipt.lines(), "Oksebøf 250g", 2, 199.0, 398.0));
        assertTrue(containsLine(receipt.lines(), "Coca Cola 0,5L", 3, 32.0, 96.0));

        verify(orderRepository).findById(100L);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(restaurantTableRepository);
    }

    @Test
    void calculateReceipt_throwsWhenOrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> service.calculateReceipt(999L));

        verify(orderRepository).findById(999L);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(restaurantTableRepository);
    }

    // ---------------- getOpenOrderForTable / getOrderDetails ----------------

    @Test
    void getOpenOrderForTable_mapsToDto() {
        Order order = openOrder(1L, 4);
        when(orderRepository.findByTableNumberAndOrderStatus(4, OrderStatus.OPEN))
                .thenReturn(Optional.of(order));

        AddItemResponse dto = mock(AddItemResponse.class);
        when(addItemResponseMapper.toDto(order)).thenReturn(dto);

        AddItemResponse res = service.getOpenOrderForTable(4);

        assertSame(dto, res);
    }

    @Test
    void getOrderDetails_mapsToDto() {
        Order order = openOrder(1L, 4);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        AddItemResponse dto = mock(AddItemResponse.class);
        when(addItemResponseMapper.toDto(order)).thenReturn(dto);

        AddItemResponse res = service.getOrderDetails(1L);

        assertSame(dto, res);
    }

    // ---------------- sendToKitchenAndBar ----------------

    @Test
    void sendToKitchenAndBar_marksOnlyUnsentItemsAsSentAndSetsSentAt() {
        Order order = openOrder(1L, 4);

        FoodItem bread = food(10L, "Bread", false);
        OrderItem unsent = foodItemLine(bread, 1, false, null);

        FoodItem steak = food(20L, "Steak", true);
        OrderItem alreadySent = foodItemLine(steak, 1, true, MeatTemperature.MEDIUM);
        alreadySent.setSentAt(Instant.parse("2025-01-01T00:00:00Z"));

        order.addItem(unsent);
        order.addItem(alreadySent);

        when(orderRepository.findByTableNumberAndOrderStatus(4, OrderStatus.OPEN))
                .thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        service.sendToKitchenAndBar(4);

        assertTrue(unsent.isHasBeenSent());
        assertNotNull(unsent.getSentAt(), "unsent should get sentAt");

        assertTrue(alreadySent.isHasBeenSent());
        assertEquals(Instant.parse("2025-01-01T00:00:00Z"), alreadySent.getSentAt(),
                "already sent should not be overwritten");

        verify(orderRepository).findByTableNumberAndOrderStatus(4, OrderStatus.OPEN);
        verify(orderRepository).save(order);
    }

    @Test
    void sendToKitchenAndBar_throwsIfNoOpenOrder() {
        when(orderRepository.findByTableNumberAndOrderStatus(4, OrderStatus.OPEN))
                .thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> service.sendToKitchenAndBar(4));
    }

    // ---------------- payOrder ----------------

    @Test
    void payOrder_setsOrderPaid_freesTable_andReturnsReceipt() {
        Order order = new Order();
        order.setId(200L);
        order.setTableNumber(7);
        order.setOrderStatus(OrderStatus.OPEN);

        FoodItem burger = new FoodItem("Jensens Burger", 149.0, null, null, true, true, false);
        OrderItem burgerItem = new OrderItem();
        burgerItem.setFoodItem(burger);
        burgerItem.setQuantity(2);
        order.setItems(List.of(burgerItem));

        RestaurantTable table = new RestaurantTable(null, 7, 0, 0, 1, 1, TableStatus.OCCUPIED);

        when(orderRepository.findById(200L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        when(restaurantTableRepository.findByTableNumber(7)).thenReturn(Optional.of(table));
        when(restaurantTableRepository.save(any(RestaurantTable.class))).thenAnswer(inv -> inv.getArgument(0));

        ReceiptDto receipt = service.payOrder(200L);

        assertNotNull(receipt);
        assertEquals(200L, receipt.orderId());
        assertEquals(7, receipt.tableNumber());
        assertEquals(298.0, receipt.total(), 0.0001);
        assertTrue(containsLine(receipt.lines(), "Jensens Burger", 2, 149.0, 298.0));

        assertEquals(OrderStatus.PAID, order.getOrderStatus());
        assertEquals(TableStatus.FREE, table.getStatus());

        verify(orderRepository).save(order);
        verify(restaurantTableRepository).findByTableNumber(7);
        verify(restaurantTableRepository).save(table);
    }

    @Test
    void payOrder_doesNotFailIfTableNotFound_stillPaysAndReturnsReceipt() {
        Order order = new Order();
        order.setId(300L);
        order.setTableNumber(9);
        order.setOrderStatus(OrderStatus.OPEN);

        DrinkItem vand = new DrinkItem("Kildevand", 29.0, null);
        OrderItem vandItem = new OrderItem();
        vandItem.setDrinkItem(vand);
        vandItem.setQuantity(1);
        order.setItems(List.of(vandItem));

        when(orderRepository.findById(300L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(restaurantTableRepository.findByTableNumber(9)).thenReturn(Optional.empty());

        ReceiptDto receipt = service.payOrder(300L);

        assertNotNull(receipt);
        assertEquals(29.0, receipt.total(), 0.0001);
        assertEquals(OrderStatus.PAID, order.getOrderStatus());

        verify(orderRepository).save(order);
        verify(restaurantTableRepository).findByTableNumber(9);
        verify(restaurantTableRepository, never()).save(any(RestaurantTable.class));
    }

    @Test
    void payOrder_throwsWhenOrderNotFound() {
        when(orderRepository.findById(400L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> service.payOrder(400L));

        verify(orderRepository).findById(400L);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(restaurantTableRepository);
    }

    // ---------------- getKitchenItems ----------------

    @Test
    void getKitchenItems_delegatesToRepository_andMaps() {
        Instant since = Instant.parse("2025-01-01T00:00:00Z");
        long lastId = 0L;

        Order order = openOrder(123L, 9);

        OrderItem oi = new OrderItem();
        oi.setId(1L);
        oi.setHasBeenSent(true);
        oi.setSentAt(Instant.parse("2025-01-02T00:00:00Z"));
        oi.setOrder(order);

        when(orderItemRepository.findKitchenItemsAfter(since, lastId)).thenReturn(List.of(oi));

        List<KitchenOrderItemDto> res = service.getKitchenItems(since, lastId);

        assertEquals(1, res.size());
        assertNotNull(res.get(0));
    }

    // ---------------- bumpKitchenTicket ----------------

    @Test
    void bumpKitchenTicket_callsRepositoryUpdate() {
        Order order = openOrder(123L, 9);

        when(orderRepository.findByTableNumberAndOrderStatus(9, OrderStatus.OPEN))
                .thenReturn(Optional.of(order));
        when(orderItemRepository.bumpKitchenItems(eq(123L), any(Instant.class))).thenReturn(3);

        service.bumpKitchenTicket(9);

        verify(orderItemRepository).bumpKitchenItems(eq(123L), any(Instant.class));
    }

    @Test
    void bumpKitchenTicket_throwsIfNoOpenOrder() {
        when(orderRepository.findByTableNumberAndOrderStatus(9, OrderStatus.OPEN))
                .thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> service.bumpKitchenTicket(9));
    }

    // ---------- assertion helper ----------

    private boolean containsLine(List<ReceiptLineDto> lines,
                                 String name,
                                 int quantity,
                                 double unitPrice,
                                 double lineTotal) {
        for (ReceiptLineDto l : lines) {
            if (name.equals(l.name())
                    && l.quantity() == quantity
                    && Math.abs(l.unitPrice() - unitPrice) < 0.0001
                    && Math.abs(l.lineTotal() - lineTotal) < 0.0001) {
                return true;
            }
        }
        return false;
    }
}