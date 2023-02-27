package com.example.jshop.administrator;

import com.example.jshop.carts_and_orders.domain.order.OrderDtoToCustomer;
import com.example.jshop.error_handlers.exceptions.*;
import com.example.jshop.warehouse_and_products.domain.category.CategoryDto;
import com.example.jshop.warehouse_and_products.domain.category.CategoryWithProductsDto;
import com.example.jshop.warehouse_and_products.domain.product.ProductDto;
import com.example.jshop.warehouse_and_products.domain.product.ProductDtoAllInfo;
import com.example.jshop.warehouse_and_products.domain.warehouse.WarehouseDto;
import com.google.gson.Gson;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
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
import java.math.RoundingMode;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringJUnitWebConfig
@WebMvcTest(controllers = AdminController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminConfig adminConfig;

    @MockBean
    private AdminService adminService;

    @BeforeEach
    public void getAdminData() {
        when(adminConfig.getAdminKey()).thenReturn("1");
        when(adminConfig.getAdminToken()).thenReturn("2");
    }

    @Nested
    @DisplayName("test addNewCategory /v1/j-shop/admin/category")
    class TestAddNewCategory {
        @Test
        void testAddNewCategoryAccessDeniedException() throws Exception {
            //Given
            CategoryDto categoryDto = new CategoryDto("Cars");
            Mockito.doNothing().when(adminService).addNewCategory(any(CategoryDto.class));
            Gson gson = new Gson();
            String jsonContent = gson.toJson(categoryDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/admin/category")
                            .param("key", "1")
                            .param("token", "3")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(result -> assertEquals("Access denied", result.getResponse().getContentAsString()));

            verify(adminService, never()).addNewCategory(any(CategoryDto.class));
        }

        @Test
        void testAddNewCategoryInvalidArgumentException() throws Exception {
            //Given
            CategoryDto empty = new CategoryDto("");
            Mockito.doThrow(InvalidCategoryNameException.class).when(adminService).addNewCategory(any(CategoryDto.class));
            Gson gson = new Gson();
            String jsonContent = gson.toJson(empty);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/admin/category")
                            .param("key", "1")
                            .param("token", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Provide proper name", result.getResponse().getContentAsString()));

            verify(adminService, times(1)).addNewCategory(any(CategoryDto.class));
        }

        @Test
        void testAddNewCategoryCategoryExistsException() throws Exception {
            //Given
            CategoryDto categoryDto = new CategoryDto("Cars");
            Mockito.doThrow(CategoryExistsException.class).when(adminService).addNewCategory(any(CategoryDto.class));
            Gson gson = new Gson();
            String jsonContent = gson.toJson(categoryDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/admin/category")
                            .param("key", "1")
                            .param("token", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Category already exists", result.getResponse().getContentAsString()));

            verify(adminService, times(1)).addNewCategory(any(CategoryDto.class));
        }

        @Test
        void testAddNewCategoryPositive() throws Exception {
            //Given
            CategoryDto categoryDto = new CategoryDto("Cars");
            Mockito.doCallRealMethod().when(adminService).addNewCategory(categoryDto);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(categoryDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/admin/category")
                            .param("key", "1")
                            .param("token", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isOk());

            verify(adminService, times(1)).addNewCategory(any(CategoryDto.class));
        }
    }

    @Nested
    @DisplayName("test removeCategory /v1/j-shop/admin/category")
    class TestRemoveCategory {
        @Test
        void testRemoveCategoryAccessDeniedException() throws Exception {
            //Given
            CategoryDto categoryDto = new CategoryDto("Cars");
            Mockito.doNothing().when(adminService).removeCategory(any(CategoryDto.class));
            Gson gson = new Gson();
            String jsonContent = gson.toJson(categoryDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/v1/j-shop/admin/category")
                            .param("key", "1")
                            .param("token", "3")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(result -> assertEquals("Access denied", result.getResponse().getContentAsString()));

            verify(adminService, never()).removeCategory(any(CategoryDto.class));
        }
    }

    @Test
    void testRemoveCategoryCategoryNotFoundException() throws Exception {
        //Given
        CategoryDto categoryDto = new CategoryDto("Cars");
        Mockito.doThrow(CategoryNotFoundException.class).when(adminService).removeCategory(any(CategoryDto.class));
        Gson gson = new Gson();
        String jsonContent = gson.toJson(categoryDto);

        //When & Then
        mockMvc
                .perform(MockMvcRequestBuilders
                        .delete("/v1/j-shop/admin/category")
                        .param("key", "1")
                        .param("token", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(jsonContent))

                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> assertEquals("Category not found", result.getResponse().getContentAsString()));

        verify(adminService, times(1)).removeCategory(any(CategoryDto.class));
    }

    @Test
    void testRemoveCategoryCategoryException() throws Exception {
        //Given
        CategoryDto categoryDto = new CategoryDto("Unknown");
        Mockito.doThrow(CategoryException.class).when(adminService).removeCategory(any(CategoryDto.class));
        Gson gson = new Gson();
        String jsonContent = gson.toJson(categoryDto);

        //When & Then
        mockMvc
                .perform(MockMvcRequestBuilders
                        .delete("/v1/j-shop/admin/category")
                        .param("key", "1")
                        .param("token", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(jsonContent))

                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> assertEquals("Deleting category \"Unknown\" denied", result.getResponse().getContentAsString()));

        verify(adminService, times(1)).removeCategory(any(CategoryDto.class));
    }

    @Test
    void testRemoveCategoryPositive() throws Exception {
        //Given
        CategoryDto categoryDto = new CategoryDto("Unknown");
        Mockito.doNothing().when(adminService).removeCategory(any(CategoryDto.class));
        Gson gson = new Gson();
        String jsonContent = gson.toJson(categoryDto);

        //When & Then
        mockMvc
                .perform(MockMvcRequestBuilders
                        .delete("/v1/j-shop/admin/category")
                        .param("key", "1")
                        .param("token", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(jsonContent))

                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(adminService, times(1)).removeCategory(any(CategoryDto.class));
    }

    @Nested
    @DisplayName("test removeCategory /v1/j-shop/admin/category")
    class TestShowAllCategoriesAndProducts {
        @Test
        void testShowAllCategoriesAndProductsAccessDeniedException() throws Exception {
            //Given
            List<CategoryWithProductsDto> listEmpty = List.of();
            when(adminService.showAllCategoriesWithProducts()).thenReturn(List.of());
            Gson gson = new Gson();
            String jsonContent = gson.toJson(listEmpty);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/admin/category")
                            .param("key", "1")
                            .param("token", "3")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(result -> assertEquals("Access denied", result.getResponse().getContentAsString()));

            verify(adminService, never()).showAllCategoriesWithProducts();
        }

        @Test
        void testShowAllCategoriesAndProductsPositiveEmptyList() throws Exception {
            //Given
            List<CategoryWithProductsDto> listEmpty = List.of();
            when(adminService.showAllCategoriesWithProducts()).thenReturn(listEmpty);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(listEmpty);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/admin/category")
                            .param("key", "1")
                            .param("token", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(0)));

            verify(adminService, times(1)).showAllCategoriesWithProducts();
        }

        @Test
        void testShowAllCategoriesAndProductsPositiveNonEmptyList() throws Exception {
            //Given
            List<CategoryWithProductsDto> list = List.of(new CategoryWithProductsDto(1L, "Music",
                    List.of(new ProductDtoAllInfo(2L, "Music", "Album1", "CD", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN)))));
            when(adminService.showAllCategoriesWithProducts()).thenReturn(list);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(list);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/admin/category")
                            .param("key", "1")
                            .param("token", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))
                    // CategoryWithProductsDto
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].categoryId", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].categoryName", Matchers.is("Music")))
                    // ProductDtoAllInfo
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].listOfProducts", Matchers.hasSize(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].listOfProducts[0].productId", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].listOfProducts[0].productName", Matchers.is("Album1")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].listOfProducts[0].description", Matchers.is("CD")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].listOfProducts[0].price", Matchers.is(25.12)));

            verify(adminService, times(1)).showAllCategoriesWithProducts();
        }
    }

    @Nested
    @DisplayName("test showCategories /v1/j-shop/admin/category/name")
    class TestShowCategories {
        @Test
        void testShowCategoriesAccessDeniedException() throws Exception {
            //Given
            CategoryWithProductsDto productsInCategory = new CategoryWithProductsDto(1L, "Music",
                    List.of(new ProductDtoAllInfo(2L, "Music", "Album1", "CD", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN))));
            when(adminService.showCategoryByNameWithProducts(any(String.class))).thenReturn(productsInCategory);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/admin/category/name")
                            .param("key", "1")
                            .param("token", "3")
                            .param("categoryName", "Music")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(result -> assertEquals("Access denied", result.getResponse().getContentAsString()));

            verify(adminService, never()).showCategoryByNameWithProducts(anyString());
        }

        @Test
        void testShowCategoriesCategoryNotFoundException() throws Exception {
            //Given
            when(adminService.showCategoryByNameWithProducts("Music")).thenThrow(CategoryNotFoundException.class);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/admin/category/name")
                            .param("key", "1")
                            .param("token", "2")
                            .param("categoryName", "Music")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Category not found", result.getResponse().getContentAsString()));

            verify(adminService, times(1)).showCategoryByNameWithProducts(anyString());
        }

        @Test
        void testShowCategoriesPositiveEmptyCategory() throws Exception {
            //Given
            CategoryWithProductsDto emptyCategory = new CategoryWithProductsDto(1L, "Music",
                    List.of());
            when(adminService.showCategoryByNameWithProducts("Music")).thenReturn(emptyCategory);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/admin/category/name")
                            .param("key", "1")
                            .param("token", "2")
                            .param("categoryName", "Music")
                            .contentType(MediaType.APPLICATION_JSON))
                    // CategoryWithProductsDto
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.categoryId", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.categoryName", Matchers.is("Music")))
                    // ProductDtoAllInfo
                    .andExpect(MockMvcResultMatchers.jsonPath("$.listOfProducts", Matchers.hasSize(0)));

            verify(adminService, times(1)).showCategoryByNameWithProducts(anyString());
        }

        @Test
        void testShowCategoriesPositiveNonEmptyCategory() throws Exception {
            //Given
            CategoryWithProductsDto productsInCategory = new CategoryWithProductsDto(1L, "Music",
                    List.of(new ProductDtoAllInfo(2L, "Music", "Album1", "CD", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN))));
            when(adminService.showCategoryByNameWithProducts("Music")).thenReturn(productsInCategory);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/admin/category/name")
                            .param("key", "1")
                            .param("token", "2")
                            .param("categoryName", "Music")
                            .contentType(MediaType.APPLICATION_JSON))
                    // CategoryWithProductsDto
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.categoryId", Matchers.is(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.categoryName", Matchers.is("Music")))
                    // ProductDtoAllInfo
                    .andExpect(MockMvcResultMatchers.jsonPath("$.listOfProducts", Matchers.hasSize(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.listOfProducts[0].productId", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.listOfProducts[0].productName", Matchers.is("Album1")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.listOfProducts[0].description", Matchers.is("CD")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.listOfProducts[0].price", Matchers.is(25.12)));

            verify(adminService, times(1)).showCategoryByNameWithProducts(anyString());
        }
    }

    @Nested
    @DisplayName("test addProduct /v1/j-shop/admin/product")
    class TestAddProduct {
        @Test
        void testShowCategoriesAccessDeniedException() throws Exception {
            //Given
            ProductDto productDto = new ProductDto("Album1", "CD", "Music", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            ProductDtoAllInfo productDtoAllInfo = new ProductDtoAllInfo(2L, "Music", "Album1", "CD", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            when(adminService.addNewProduct(any(ProductDto.class))).thenReturn(productDtoAllInfo);

            Gson gson = new Gson();
            String jsonContent = gson.toJson(productDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/admin/product")
                            .param("key", "1")
                            .param("token", "3")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(result -> assertEquals("Access denied", result.getResponse().getContentAsString()));

            verify(adminService, never()).addNewProduct(any(ProductDto.class));
        }

        @Test
        void testShowCategoriesInvalidArgumentException() throws Exception {
            //Given
            ProductDto productDto = new ProductDto("Album1", "CD", "Music", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            when(adminService.addNewProduct(any(ProductDto.class))).thenThrow(InvalidCategoryNameException.class);

            Gson gson = new Gson();
            String jsonContent = gson.toJson(productDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/admin/product")
                            .param("key", "1")
                            .param("token", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Provide proper name", result.getResponse().getContentAsString()));

            verify(adminService, times(1)).addNewProduct(any(ProductDto.class));
        }

        @Test
        void testShowCategoriesCategoryExistsException() throws Exception {
            //Given
            ProductDto productDto = new ProductDto("Album1", "CD", "unknown", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            when(adminService.addNewProduct(any(ProductDto.class))).thenThrow(CategoryExistsException.class);

            Gson gson = new Gson();
            String jsonContent = gson.toJson(productDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/admin/product")
                            .param("key", "1")
                            .param("token", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Category already exists", result.getResponse().getContentAsString()));

            verify(adminService, times(1)).addNewProduct(any(ProductDto.class));
        }

        @Test
        void testShowCategoriesInvalidPriceException() throws Exception {
            //Given
            ProductDto productDto = new ProductDto("Album1", "CD", "unknown", new BigDecimal(-25.12).setScale(2, RoundingMode.HALF_EVEN));
            when(adminService.addNewProduct(any(ProductDto.class))).thenThrow(InvalidPriceException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(productDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/admin/product")
                            .param("key", "1")
                            .param("token", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Price is incorrect", result.getResponse().getContentAsString()));

            verify(adminService, times(1)).addNewProduct(any(ProductDto.class));
        }

        @Test
        void testShowCategoriesPositive() throws Exception {
            //Given
            ProductDto productDto = new ProductDto("Album1", "CD", "unknown", new BigDecimal(-25.12).setScale(2, RoundingMode.HALF_EVEN));
            ProductDtoAllInfo productDtoAllInfo = new ProductDtoAllInfo(2L, "Music", "Album1", "CD", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            when(adminService.addNewProduct(any(ProductDto.class))).thenReturn(productDtoAllInfo);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(productDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/admin/product")
                            .param("key", "1")
                            .param("token", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.productId", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.category", Matchers.is("Music")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.productName", Matchers.is("Album1")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.is("CD")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.price", Matchers.is(25.12)));

            verify(adminService, times(1)).addNewProduct(any(ProductDto.class));
        }
    }

    @Nested
    @DisplayName("test updateProduct /v1/j-shop/admin/product")
    class TestUpdateProduct {
        @Test
        void testUpdateProductAccessDeniedException() throws Exception {
            //Given
            ProductDto productDto = new ProductDto("Album1", "CD", "Music", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            ProductDtoAllInfo productDtoAllInfo = new ProductDtoAllInfo(2L, "Music", "Album1", "CD", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            when(adminService.updateProduct(anyLong(), any(ProductDto.class))).thenReturn(productDtoAllInfo);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(productDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/admin/product")
                            .param("key", "1")
                            .param("token", "3")
                            .param("productId", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(result -> assertEquals("Access denied", result.getResponse().getContentAsString()));

            verify(adminService, never()).updateProduct(anyLong(), any(ProductDto.class));
        }

        @Test
        void testUpdateProductCategoryNotFoundException() throws Exception {
            //Given
            ProductDto productDto = new ProductDto("Album1", "CD", "Music", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            when(adminService.updateProduct(anyLong(), any(ProductDto.class))).thenThrow(ProductNotFoundException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(productDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/admin/product")
                            .param("key", "1")
                            .param("token", "2")
                            .param("productId", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Product with given Id not found", result.getResponse().getContentAsString()));

            verify(adminService, times(1)).updateProduct(anyLong(), any(ProductDto.class));
        }

        @Test
        void testUpdateProductInvalidArgumentException() throws Exception {
            //Given
            ProductDto productDto = new ProductDto("Album1", "CD", "Music", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            when(adminService.updateProduct(anyLong(), any(ProductDto.class))).thenThrow(InvalidCategoryNameException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(productDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/admin/product")
                            .param("key", "1")
                            .param("token", "2")
                            .param("productId", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Provide proper name", result.getResponse().getContentAsString()));

            verify(adminService, times(1)).updateProduct(anyLong(), any(ProductDto.class));
        }

        @Test
        void testUpdateProductCategoryExistsException() throws Exception {
            //Given
            ProductDto productDto = new ProductDto("Album1", "CD", "Music", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            when(adminService.updateProduct(anyLong(), any(ProductDto.class))).thenThrow(CategoryExistsException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(productDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/admin/product")
                            .param("key", "1")
                            .param("token", "2")
                            .param("productId", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Category already exists", result.getResponse().getContentAsString()));

            verify(adminService, times(1)).updateProduct(anyLong(), any(ProductDto.class));
        }

        @Test
        void testUpdateProductInvalidPriceException() throws Exception {
            //Given
            ProductDto productDto = new ProductDto("Album1", "CD", "Music", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            when(adminService.updateProduct(anyLong(), any(ProductDto.class))).thenThrow(InvalidPriceException.class);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(productDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/admin/product")
                            .param("key", "1")
                            .param("token", "2")
                            .param("productId", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Price is incorrect", result.getResponse().getContentAsString()));

            verify(adminService, times(1)).updateProduct(anyLong(), any(ProductDto.class));
        }

        @Test
        void testUpdateProductPositive() throws Exception {
            //Given
            ProductDto productDto = new ProductDto("Album1", "CD", "Music", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            ProductDtoAllInfo productDtoAllInfo = new ProductDtoAllInfo(2L, "Movie", "Album2", "CD", new BigDecimal(44.12).setScale(2, RoundingMode.HALF_EVEN));
            when(adminService.updateProduct(anyLong(), any(ProductDto.class))).thenReturn(productDtoAllInfo);
            Gson gson = new Gson();
            String jsonContent = gson.toJson(productDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .put("/v1/j-shop/admin/product")
                            .param("key", "1")
                            .param("token", "2")
                            .param("productId", "2")
                            .contentType(MediaType.APPLICATION_JSON)
                            .characterEncoding("UTF-8")
                            .content(jsonContent))

                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.productId", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.category", Matchers.is("Movie")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.productName", Matchers.is("Album2")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.description", Matchers.is("CD")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.price", Matchers.is(44.12)));

            verify(adminService, times(1)).updateProduct(anyLong(), any(ProductDto.class));
        }
    }

    @Nested
    @DisplayName("test removeProduct /v1/j-shop/admin/product")
    class TestRemoveProduct {
        @Test
        void testRemoveProductAccessDeniedException() throws Exception {
            //Given
            Mockito.doNothing().when(adminService).deleteProductById(any(Long.class));

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/v1/j-shop/admin/product")
                            .param("key", "1")
                            .param("token", "3")
                            .param("productId", "3")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(result -> assertEquals("Access denied", result.getResponse().getContentAsString()));

            verify(adminService, never()).deleteProductById(anyLong());
        }

        @Test
        void testRemoveProductProductNotFoundException() throws Exception {
            //Given
            Mockito.doThrow(ProductNotFoundException.class).when(adminService).deleteProductById(any(Long.class));

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/v1/j-shop/admin/product")
                            .param("key", "1")
                            .param("token", "2")
                            .param("productId", "3")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Product with given Id not found", result.getResponse().getContentAsString()));

            verify(adminService, times(1)).deleteProductById(anyLong());
        }

        @Test
        void testRemoveProductPositive() throws Exception {
            //Given
            Mockito.doNothing().when(adminService).deleteProductById(any(Long.class));

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/v1/j-shop/admin/product")
                            .param("key", "1")
                            .param("token", "2")
                            .param("productId", "3")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            verify(adminService, times(1)).deleteProductById(anyLong());
        }
    }

    @Nested
    @DisplayName("test showAllProducts /v1/j-shop/admin/product")
    class TestShowAllProducts {

        @Test
        void testShowAllProductsAccessDeniedException() throws Exception {
            //Given
            when(adminService.showAllProducts()).thenReturn(List.of());

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/admin/product")
                            .param("key", "1")
                            .param("token", "3")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(result -> assertEquals("Access denied", result.getResponse().getContentAsString()));

            verify(adminService, never()).showAllProducts();
        }

        @Test
        void testShowAllProductsPositive() throws Exception {
            //Given
            List<ProductDtoAllInfo> list = List.of(new ProductDtoAllInfo(2L, "Music", "Album1", "CD", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN)));
            when(adminService.showAllProducts()).thenReturn(list);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/admin/product")
                            .param("key", "1")
                            .param("token", "2")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))

                    .andExpect(MockMvcResultMatchers.jsonPath("$.[0].productId", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.[0].category", Matchers.is("Music")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.[0].productName", Matchers.is("Album1")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.[0].description", Matchers.is("CD")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.[0].price", Matchers.is(25.12)));

            verify(adminService, times(1)).showAllProducts();
        }
    }

    @Nested
    @DisplayName("test addProductToWarehouse /v1/j-shop/admin/warehouse")
    class TestAddProductToWarehouse {
        @Test
        void testAddProductToWarehousesAccessDeniedException() throws Exception {
            //Given
            WarehouseDto warehouseDto = new WarehouseDto(4L, 2L, "Marilyn Manson, \"Antichrist Superstar\"", "Music", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN), 45);
            when(adminService.addOrUpdateProductInWarehouse(anyLong(), anyInt())).thenReturn(warehouseDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/admin/warehouse")
                            .param("key", "1")
                            .param("token", "3")
                            .param("productId", "3")
                            .param("productQuantity", "45")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(result -> assertEquals("Access denied", result.getResponse().getContentAsString()));

            verify(adminService, never()).addOrUpdateProductInWarehouse(anyLong(), anyInt());
        }

        @Test
        void testAddProductToWarehousesProductNotFoundException() throws Exception {
            //Given
            when(adminService.addOrUpdateProductInWarehouse(anyLong(), anyInt())).thenThrow(ProductNotFoundException.class);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/admin/warehouse")
                            .param("key", "1")
                            .param("token", "2")
                            .param("productId", "3")
                            .param("productQuantity", "45")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Product with given Id not found", result.getResponse().getContentAsString()));

            verify(adminService, times(1)).addOrUpdateProductInWarehouse(anyLong(), anyInt());
        }

        @Test
        void testAddProductToWarehousesInvalidQuantityException() throws Exception {
            //Given
            when(adminService.addOrUpdateProductInWarehouse(anyLong(), anyInt())).thenThrow(InvalidQuantityException.class);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/admin/warehouse")
                            .param("key", "1")
                            .param("token", "2")
                            .param("productId", "3")
                            .param("productQuantity", "45")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Provided quantity is out of range 1 - 2 147 483 647", result.getResponse().getContentAsString()));

            verify(adminService, times(1)).addOrUpdateProductInWarehouse(anyLong(), anyInt());
        }

        @Test
        void testAddProductToWarehousesCategoryNotFoundException() throws Exception {
            //Given
            when(adminService.addOrUpdateProductInWarehouse(anyLong(), anyInt())).thenThrow(CategoryNotFoundException.class);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/admin/warehouse")
                            .param("key", "1")
                            .param("token", "2")
                            .param("productId", "3")
                            .param("productQuantity", "45")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Category not found", result.getResponse().getContentAsString()));

            verify(adminService, times(1)).addOrUpdateProductInWarehouse(anyLong(), anyInt());
        }

        @Test
        void testAddProductToWarehousesPositive() throws Exception {
            //Given
            WarehouseDto warehouseDto = new WarehouseDto(4L, 2L, "Marilyn Manson, \"Antichrist Superstar\"", "Music", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN), 45);
            when(adminService.addOrUpdateProductInWarehouse(anyLong(), anyInt())).thenReturn(warehouseDto);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .post("/v1/j-shop/admin/warehouse")
                            .param("key", "1")
                            .param("token", "2")
                            .param("productId", "3")
                            .param("productQuantity", "45")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.warehouseId", Matchers.is(4)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.productId", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.productName", Matchers.is("Marilyn Manson, \"Antichrist Superstar\"")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.category", Matchers.is("Music")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.price", Matchers.is(25.12)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.quantity", Matchers.is(45)));

            verify(adminService, times(1)).addOrUpdateProductInWarehouse(anyLong(), anyInt());
        }
    }

    @Nested
    @DisplayName("test removeProductFromWarehouse /v1/j-shop/admin/warehouse")
    class TestRemoveProductFromWarehouse {

        @Test
        void testRemoveProductFromWarehouseAccessDeniedException() throws Exception {
            //Given
            doNothing().when(adminService).deleteProductFromWarehouse(anyLong());

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/v1/j-shop/admin/warehouse")
                            .param("key", "1")
                            .param("token", "3")
                            .param("productId", "3")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(result -> assertEquals("Access denied", result.getResponse().getContentAsString()));

            verify(adminService, never()).deleteProductFromWarehouse(anyLong());
        }

        @Test
        void testRemoveProductFromWarehouseProductNotFoundException() throws Exception {
            //Given
            doThrow(ProductNotFoundException.class).when(adminService).deleteProductFromWarehouse(anyLong());

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/v1/j-shop/admin/warehouse")
                            .param("key", "1")
                            .param("token", "2")
                            .param("productId", "3")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Product with given Id not found", result.getResponse().getContentAsString()));

            verify(adminService, times(1)).deleteProductFromWarehouse(anyLong());
        }

        @Test
        void testRemoveProductFromWarehousePositive() throws Exception {
            //Given
            doNothing().when(adminService).deleteProductFromWarehouse(anyLong());

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .delete("/v1/j-shop/admin/warehouse")
                            .param("key", "1")
                            .param("token", "2")
                            .param("productId", "3")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isOk());

            verify(adminService, times(1)).deleteProductFromWarehouse(anyLong());
        }
    }

    @Nested
    @DisplayName("test displayAllItemsInWareHouse /v1/j-shop/admin/warehouse")
    class TestDisplayAllItemsInWareHouse {
        @Test
        void testDisplayAllItemsInWareHouseAccessDeniedException() throws Exception {
            //Given
            List<WarehouseDto> warehouseDtoList = List.of(new WarehouseDto(4L, 2L, "Marilyn Manson, \"Antichrist Superstar\"", "Music", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN), 45));
            when(adminService.displayAllProductsInWarehouse()).thenReturn(warehouseDtoList);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/admin/warehouse")
                            .param("key", "1")
                            .param("token", "3")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(result -> assertEquals("Access denied", result.getResponse().getContentAsString()));

            verify(adminService, never()).displayAllProductsInWarehouse();
        }

        @Test
        void testDisplayAllItemsInWareHousePositive() throws Exception {
            //Given
            List<WarehouseDto> warehouseDtoList = List.of(new WarehouseDto(4L, 2L, "Marilyn Manson, \"Antichrist Superstar\"", "Music", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN), 45));
            when(adminService.displayAllProductsInWarehouse()).thenReturn(warehouseDtoList);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/admin/warehouse")
                            .param("key", "1")
                            .param("token", "2")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.[0].warehouseId", Matchers.is(4)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.[0].productId", Matchers.is(2)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.[0].productName", Matchers.is("Marilyn Manson, \"Antichrist Superstar\"")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.[0].category", Matchers.is("Music")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.[0].price", Matchers.is(25.12)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.[0].quantity", Matchers.is(45)));

            verify(adminService, times(1)).displayAllProductsInWarehouse();
        }
    }

    @Nested
    @DisplayName("test displayAllOrders /v1/j-shop/admin/order")
    class TestDisplayAllOrders {
        @Test
        void testDisplayAllOrdersAccessDeniedException() throws Exception {
            //Given
            String listOfProductsDummy = "Dummy list";
            List<OrderDtoToCustomer> orderDtoList = List.of(
                    new OrderDtoToCustomer(8L, LocalDate.of(2023, 2, 20), listOfProductsDummy,
                            new BigDecimal(4025).setScale(2, RoundingMode.HALF_EVEN).toString(), "UNPAID",
                            LocalDate.of(2023, 3, 15)));

            when(adminService.displayOrders(anyString())).thenReturn(orderDtoList);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/admin/order")
                            .param("key", "1")
                            .param("token", "3")
                            .param("order_status", "UNPAID")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                    .andExpect(result -> assertEquals("Access denied", result.getResponse().getContentAsString()));

            verify(adminService, never()).displayOrders(anyString());
        }

        @Test
        void testDisplayAllOrdersInvalidOrderStatusException() throws Exception {
            //Given
            when(adminService.displayOrders(anyString())).thenThrow(InvalidOrderStatusException.class);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/admin/order")
                            .param("key", "1")
                            .param("token", "2")
                            .param("order_status", "UNPAID")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(result -> assertEquals("Provide proper status. Status can be \"paid\" or \"unpaid\"", result.getResponse().getContentAsString()));

            verify(adminService, times(1)).displayOrders(anyString());
        }

        @Test
        void testDisplayAllOrdersPositiveOptionalParamStatusProvided() throws Exception {
            //Given
            String listOfProductsDummy = "Dummy list";
            List<OrderDtoToCustomer> orderDtoList = List.of(
                    new OrderDtoToCustomer(8L, LocalDate.of(2023, 2, 20), listOfProductsDummy,
                            new BigDecimal(4025).setScale(2, RoundingMode.HALF_EVEN).toString(), "UNPAID",
                            LocalDate.of(2023, 3, 15)));

            when(adminService.displayOrders(anyString())).thenReturn(orderDtoList);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/admin/order")
                            .param("key", "1")
                            .param("token", "2")
                            .param("order_status", "UNPAID")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].orderId", Matchers.is(8)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].createdOn", Matchers.is("2023-02-20")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].listOfProducts", Matchers.is("Dummy list")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].totalPrice", Matchers.is("4025.00")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.is("UNPAID")));

            verify(adminService, times(1)).displayOrders(anyString());
        }

        @Test
        void testDisplayAllOrdersPositiveNoOptionalParamStatusProvided() throws Exception {
            //Given
            String listOfProductsDummy = "Dummy list";
            List<OrderDtoToCustomer> orderDtoList = List.of(
                    new OrderDtoToCustomer(8L, LocalDate.of(2023, 2, 20), listOfProductsDummy,
                            new BigDecimal(4025).setScale(2, RoundingMode.HALF_EVEN).toString(), "UNPAID",
                            LocalDate.of(2023, 3, 15)));

            when(adminService.displayOrders(null)).thenReturn(orderDtoList);

            //When & Then
            mockMvc
                    .perform(MockMvcRequestBuilders
                            .get("/v1/j-shop/admin/order")
                            .param("key", "1")
                            .param("token", "2")
                            .contentType(MediaType.APPLICATION_JSON))

                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize(1)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].orderId", Matchers.is(8)))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].createdOn", Matchers.is("2023-02-20")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].listOfProducts", Matchers.is("Dummy list")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].totalPrice", Matchers.is("4025.00")))
                    .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.is("UNPAID")));

            verify(adminService, times(1)).displayOrders(null);
        }
    }
}
