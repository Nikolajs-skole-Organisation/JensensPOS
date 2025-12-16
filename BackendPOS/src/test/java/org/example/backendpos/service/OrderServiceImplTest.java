package org.example.backendpos.service;

import org.example.backendpos.dto.AddItemResponseMapper;
import org.example.backendpos.dto.ReceiptDto;
import org.example.backendpos.dto.ReceiptLineDto;
import org.example.backendpos.exception.OrderNotFoundException;
import org.example.backendpos.model.RestaurantTable;
import org.example.backendpos.model.TableStatus;
import org.example.backendpos.model.order.DrinkItem;
import org.example.backendpos.model.order.FoodItem;
import org.example.backendpos.model.order.Order;
import org.example.backendpos.model.order.OrderItem;
import org.example.backendpos.model.order.OrderStatus;
import org.example.backendpos.repository.CategoryRepository;
import org.example.backendpos.repository.DrinkItemRepository;
import org.example.backendpos.repository.FoodItemRepository;
import org.example.backendpos.repository.OrderRepository;
import org.example.backendpos.repository.RestaurantTableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;

    @Mock private RestaurantTableRepository restaurantTableRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void calculateReceipt_returnsCorrectLinesAndTotal() {
        // Arrange
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

        // Act
        ReceiptDto receipt = orderService.calculateReceipt(100L);

        // Assert
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

        assertThrows(OrderNotFoundException.class, () -> orderService.calculateReceipt(999L));

        verify(orderRepository).findById(999L);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(restaurantTableRepository);
    }

    @Test
    void payOrder_setsOrderPaid_freesTable_andReturnsReceipt() {
        // Arrange
        Order order = new Order();
        order.setId(200L);
        order.setTableNumber(7);
        order.setOrderStatus(OrderStatus.OPEN);

        FoodItem burger = new FoodItem("Jensens Burger", 149.0, null, null, true, true, false);
        OrderItem burgerItem = new OrderItem();
        burgerItem.setFoodItem(burger);
        burgerItem.setQuantity(2); // 298
        order.setItems(List.of(burgerItem));

        RestaurantTable table = new RestaurantTable(null, 7, 0, 0, 1, 1, TableStatus.OCCUPIED);

        when(orderRepository.findById(200L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        when(restaurantTableRepository.findByTableNumber(7)).thenReturn(Optional.of(table));
        when(restaurantTableRepository.save(any(RestaurantTable.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        ReceiptDto receipt = orderService.payOrder(200L);

        // Assert
        assertNotNull(receipt);
        assertEquals(200L, receipt.orderId());
        assertEquals(7, receipt.tableNumber());
        assertEquals(298.0, receipt.total(), 0.0001);
        assertTrue(containsLine(receipt.lines(), "Jensens Burger", 2, 149.0, 298.0));

        assertEquals(OrderStatus.PAID, order.getOrderStatus());

        assertEquals(TableStatus.FREE, table.getStatus());

        verify(orderRepository, times(2)).findById(200L);
        verify(orderRepository).save(order);

        verify(restaurantTableRepository).findByTableNumber(7);
        verify(restaurantTableRepository).save(table);
    }

    @Test
    void payOrder_doesNotFailIfTableNotFound_stillPaysAndReturnsReceipt() {
        // Arrange
        Order order = new Order();
        order.setId(300L);
        order.setTableNumber(9);
        order.setOrderStatus(OrderStatus.OPEN);

        DrinkItem vand = new DrinkItem("Kildevand", 29.0, null);
        OrderItem vandItem = new OrderItem();
        vandItem.setDrinkItem(vand);
        vandItem.setQuantity(1); // 29
        order.setItems(List.of(vandItem));

        when(orderRepository.findById(300L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        when(restaurantTableRepository.findByTableNumber(9)).thenReturn(Optional.empty());

        // Act
        ReceiptDto receipt = orderService.payOrder(300L);

        // Assert
        assertNotNull(receipt);
        assertEquals(29.0, receipt.total(), 0.0001);
        assertEquals(OrderStatus.PAID, order.getOrderStatus());

        verify(orderRepository, times(2)).findById(300L);
        verify(orderRepository).save(order);

        verify(restaurantTableRepository).findByTableNumber(9);
        verify(restaurantTableRepository, never()).save(any(RestaurantTable.class));
    }

    @Test
    void payOrder_throwsWhenOrderNotFound() {
        when(orderRepository.findById(400L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.payOrder(400L));

        verify(orderRepository).findById(400L);
        verifyNoMoreInteractions(orderRepository);
        verifyNoInteractions(restaurantTableRepository);
    }

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