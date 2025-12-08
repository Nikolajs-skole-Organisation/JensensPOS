package org.example.backendpos.service;

import org.example.backendpos.dto.RestaurantTableDto;
import org.example.backendpos.dto.RestaurantTableMapper;
import org.example.backendpos.model.RestaurantTable;
import org.example.backendpos.model.TableStatus;
import org.example.backendpos.repository.RestaurantTableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class RestaurantServiceImplTest {

    @Mock
    private RestaurantTableRepository repository;

    @Mock
    private RestaurantTableMapper mapper;

    @InjectMocks
    private RestaurantTableServiceImpl service;

    @Test
    void getAllTables_returnsMappedList() {
        // Arrange
        RestaurantTable T1 = new RestaurantTable();
        T1.setId(1L);
        RestaurantTable T2 = new RestaurantTable();
        T2.setId(2L);

        RestaurantTableDto dto1 = new RestaurantTableDto(1L, 1, 4, 2,1,1, TableStatus.FREE.name());
        RestaurantTableDto dto2 = new RestaurantTableDto(2L, 2, 2, 3,5,1,TableStatus.OCCUPIED.name());

        when(repository.findAll()).thenReturn(List.of(T1, T2));
        when(mapper.toDto(T1)).thenReturn(dto1);
        when(mapper.toDto(T2)).thenReturn(dto2);

        // Act
        List<RestaurantTableDto> result = service.getAllTables();

        // Assert
        assertThat(result)
                .hasSize(2)
                .containsExactly(dto1, dto2);

        verify(repository, times(1)).findAll();
        verify(mapper, times(1)).toDto(T1);
        verify(mapper, times(1)).toDto(T2);
        verifyNoMoreInteractions(repository, mapper);
    }

    @Test
    void getAllTables_returnsEmptyListWhenRepositoryEmpty() {
        // Arrange
        when(repository.findAll()).thenReturn(List.of());

        // Act
        List<RestaurantTableDto> result = service.getAllTables();

        // Assert
        assertThat(result).isEmpty();
        verify(repository, times(1)).findAll();
        verifyNoInteractions(mapper);
    }
}