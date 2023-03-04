package com.example.jshop.warehouse_and_products.controller;

import com.example.jshop.error_handlers.exceptions.LimitException;
import com.example.jshop.error_handlers.exceptions.ProductNotFoundException;
import com.example.jshop.warehouse_and_products.domain.category.Category;
import com.example.jshop.warehouse_and_products.domain.product.Product;
import com.example.jshop.warehouse_and_products.domain.warehouse.Warehouse;
import com.example.jshop.warehouse_and_products.domain.warehouse.WarehouseDto;
import com.example.jshop.warehouse_and_products.mapper.WarehouseMapper;
import com.example.jshop.warehouse_and_products.service.WarehouseService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringJUnitWebConfig
@WebMvcTest(controllers = ShopSearchController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ShopSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WarehouseService warehouseService;

    @MockBean
    private WarehouseMapper warehouseMapper;

    @Nested
    @DisplayName("test showSelectedProducts v1/j-shop/search/select")
    class TestShowSelectedProducts {
        @Test
        void showSelectedProductsLimitException() throws Exception {
            //Given
            List<WarehouseDto> listOfAvailableProducts = List.of(
                    WarehouseDto.builder()
                            .productId(1L)
                            .productName("testName")
                            .category("testCategory")
                            .price(new BigDecimal("48.00"))
                            .quantity(256)
                            .build()
            );
            when(warehouseService.findProductsInWarehouseWithSelection(anyString(), anyString(), any(BigDecimal.class), anyInt())).thenThrow(LimitException.class);
            when(warehouseMapper.mapToWarehouseDtoList(anyList())).thenReturn(listOfAvailableProducts);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/search/select")
                            .param("categoryName", "testCategory")
                            .param("productName", "testProduct")
                            .param("productPrice", "48.00")
                            .param("limit", "200")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Limit range should be between 1 and 100", result.getResponse().getContentAsString()));

            verify(warehouseService, times(1)).findProductsInWarehouseWithSelection(anyString(), anyString(), any(BigDecimal.class), anyInt());
            verify(warehouseMapper, never()).mapToWarehouseDtoList(anyList());
        }

        @Test
        void showSelectedProductsProductNotFoundException() throws Exception {
            //Given
            List<WarehouseDto> listOfAvailableProducts = List.of(
                    WarehouseDto.builder()
                            .productId(1L)
                            .productName("testName")
                            .category("testCategory")
                            .price(new BigDecimal("48.00"))
                            .quantity(256)
                            .build()
            );
            when(warehouseService.findProductsInWarehouseWithSelection(anyString(), anyString(), any(BigDecimal.class), anyInt())).thenThrow(ProductNotFoundException.class);
            when(warehouseMapper.mapToWarehouseDtoList(anyList())).thenReturn(listOfAvailableProducts);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/search/select")
                            .param("categoryName", "testCategory")
                            .param("productName", "testProduct")
                            .param("productPrice", "48.00")
                            .param("limit", "200")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Product with given Id not found", result.getResponse().getContentAsString()));

            verify(warehouseService, times(1)).findProductsInWarehouseWithSelection(anyString(), anyString(), any(BigDecimal.class), anyInt());
            verify(warehouseMapper, never()).mapToWarehouseDtoList(anyList());
        }

        @Test
        void showSelectedProductsPositiveNoOptionalSelection() throws Exception {
            //Given
            List<WarehouseDto> listOfAvailableProducts = new ArrayList<>();
            listOfAvailableProducts.add(WarehouseDto.builder()
                    .productId(1L)
                    .productName("testName")
                    .category("testCategory")
                    .price(new BigDecimal("48.00"))
                    .quantity(256)
                    .build()

            );
            listOfAvailableProducts.add(WarehouseDto.builder()
                    .productId(2L)
                    .productName("testName2")
                    .category("testCategory")
                    .price(new BigDecimal("66.00"))
                    .quantity(380)
                    .build());

            listOfAvailableProducts.add(WarehouseDto.builder()
                    .productId(3L)
                    .productName("testName3")
                    .category("testCategory1")
                    .price(new BigDecimal("2.00"))
                    .quantity(15)
                    .build());

            List<Warehouse> warehousesList = new ArrayList<>();
            warehousesList.add(new Warehouse(new Product("testName", "testDescription", new Category("testCategory"), new BigDecimal("48.0")), 256));
            warehousesList.add(new Warehouse(new Product("testName2", "testDescription", new Category("testCategory"), new BigDecimal("66.0")), 380));
            warehousesList.add(new Warehouse(new Product("testName3", "testDescription", new Category("testCategory1"), new BigDecimal("2.0")), 15));

            when(warehouseService.findProductsInWarehouseWithSelection(null, null, null, 200)).thenReturn(warehousesList);
            when(warehouseMapper.mapToWarehouseDtoList(anyList())).thenReturn(listOfAvailableProducts);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/search/select")
                            .param("limit", "200")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(3)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].productName", Matchers.is("testName")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].quantity", Matchers.is(256)));


            verify(warehouseService, times(1)).findProductsInWarehouseWithSelection(null, null, null, 200);
            verify(warehouseMapper, times(1)).mapToWarehouseDtoList(anyList());
        }

        @Test
        void showSelectedProductsPositiveOptionalSelection() throws Exception {
            //Given
            List<WarehouseDto> listOfAvailableProducts = new ArrayList<>();
            listOfAvailableProducts.add(WarehouseDto.builder()
                    .productId(1L)
                    .productName("testName")
                    .category("testCategory")
                    .price(new BigDecimal("48.00"))
                    .quantity(256)
                    .build()

            );
            listOfAvailableProducts.add(WarehouseDto.builder()
                    .productId(2L)
                    .productName("testName2")
                    .category("testCategory")
                    .price(new BigDecimal("66.00"))
                    .quantity(380)
                    .build());

            listOfAvailableProducts.add(WarehouseDto.builder()
                    .productId(3L)
                    .productName("testName3")
                    .category("testCategory1")
                    .price(new BigDecimal("2.00"))
                    .quantity(15)
                    .build());

            List<Warehouse> warehousesList = new ArrayList<>();
            warehousesList.add(new Warehouse(new Product("testName", "testDescription", new Category("testCategory"), new BigDecimal("48.0")), 256));
            warehousesList.add(new Warehouse(new Product("testName2", "testDescription", new Category("testCategory"), new BigDecimal("66.0")), 380));
            warehousesList.add(new Warehouse(new Product("testName3", "testDescription", new Category("testCategory1"), new BigDecimal("2.0")), 15));

            when(warehouseService.findProductsInWarehouseWithSelection("testCategory", null, null, 200)).thenReturn(warehousesList.subList(0, 2));
            when(warehouseMapper.mapToWarehouseDtoList(anyList())).thenReturn(listOfAvailableProducts.subList(0, 2));

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/search/select")
                            .param("categoryName", "testCategory")
                            .param("limit", "200")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[1].productName", Matchers.is("testName2")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[1].quantity", Matchers.is(380)));


            verify(warehouseService, times(1)).findProductsInWarehouseWithSelection("testCategory", null, null, 200);
            verify(warehouseMapper, times(1)).mapToWarehouseDtoList(anyList());

        }
    }


    @Nested
    @DisplayName("test findProductByID /v1/j-shop/search/productId")
    class TestFindProductByID {
        @Test
        void findProductByIDProductNotFoundException() throws Exception {
            //Given
            Warehouse warehouse = new Warehouse(new Product(1L, "testName", "testDescription", new Category("testCategory"), new BigDecimal("48.0")), 256);
            WarehouseDto warehouseDto = WarehouseDto.builder()
                    .productId(1L)
                    .productName("testName")
                    .category("testCategory")
                    .price(new BigDecimal("48.00"))
                    .quantity(256)
                    .build();

            when(warehouseService.findWarehouseByProductId(anyLong())).thenReturn(warehouse);
            when(warehouseMapper.mapToWarehouseDto(warehouse)).thenReturn(warehouseDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/search/1")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.productName", Matchers.is("testName")));

            verify(warehouseService, times(1)).findWarehouseByProductId(anyLong());
            verify(warehouseMapper, times(1)).mapToWarehouseDto(any(Warehouse.class));
        }

        @Test
        void findProductByIDPositive() throws Exception {
            //Given
            Warehouse warehouse = null;
            when(warehouseService.findWarehouseByProductId(anyLong())).thenReturn(warehouse);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/search/4")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Product with given Id not found", result.getResponse().getContentAsString()));

            verify(warehouseService, times(1)).findWarehouseByProductId(4L);
            verify(warehouseMapper, times(0)).mapToWarehouseDto(any(Warehouse.class));
        }
    }
}