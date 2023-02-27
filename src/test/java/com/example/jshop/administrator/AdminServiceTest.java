package com.example.jshop.administrator;

import com.example.jshop.error_handlers.exceptions.*;
import com.example.jshop.warehouse_and_products.domain.category.Category;
import com.example.jshop.warehouse_and_products.domain.category.CategoryDto;
import com.example.jshop.warehouse_and_products.domain.category.CategoryWithProductsDto;
import com.example.jshop.warehouse_and_products.domain.product.Product;
import com.example.jshop.warehouse_and_products.domain.product.ProductDto;
import com.example.jshop.warehouse_and_products.domain.product.ProductDtoAllInfo;
import com.example.jshop.warehouse_and_products.domain.warehouse.WarehouseDto;
import com.example.jshop.warehouse_and_products.repository.CategoryRepository;
import com.example.jshop.warehouse_and_products.repository.ProductRepository;
import com.example.jshop.warehouse_and_products.repository.WarehouseRepository;
import com.example.jshop.warehouse_and_products.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AdminServiceTest {
    @Autowired
    private AdminService adminService;

    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductService productService;

    @Autowired
    WarehouseRepository warehouseRepository;

    @Nested
    @Transactional
    @DisplayName("test addNewCategory")
    class TestAddNewCategory {
        @ParameterizedTest
        @ValueSource(strings = {"", "aa", "", "%t54"})
        void testAddNewCategoryInvalidCategoryNameException() {
            //Given
            CategoryDto categoryDto = new CategoryDto("%%");

            //When & Then
            assertThrows(InvalidCategoryNameException.class, () -> adminService.addNewCategory(categoryDto));
        }

        @Test
        void testAddNewCategoryInvalidCategoryExistsException() {
            //Given
            categoryRepository.save(new Category("Car"));
            CategoryDto categoryDto = new CategoryDto("Car");

            //When & Then
            assertThrows(CategoryExistsException.class, () -> adminService.addNewCategory(categoryDto));
        }

        @Test
        void testAddNewCategoryPositive() throws InvalidCategoryNameException, CategoryExistsException {
            //Given
            CategoryDto categoryDto = new CategoryDto("Books");
            CategoryDto categoryDto1 = new CategoryDto("Pets");

            //When
            adminService.addNewCategory(categoryDto);
            adminService.addNewCategory(categoryDto1);

            //Then
            assertEquals(2, categoryRepository.findAll().size());
            assertEquals(0, categoryRepository.findAll().get(0).getListOfProducts().size());
        }
    }

    @Nested
    @Transactional
    @DisplayName("test removeCategory")
    class TestRemoveCategory {
        @Test
        void testRemoveCategoryCategoryNotFoundException() {
            //Given
            CategoryDto categoryDto = new CategoryDto("Car");

            //When & Then
            assertThrows(CategoryNotFoundException.class, () -> adminService.removeCategory(categoryDto));
        }

        @Test
        void testRemoveCategoryCategoryException() {
            //Given
            Category category = new Category("unknown");
            categoryRepository.save(category);
            CategoryDto categoryDto = new CategoryDto("unknown");

            //When & Then
            assertThrows(CategoryException.class, () -> adminService.removeCategory(categoryDto));
        }

        @Test
        void testRemoveCategoryPositive() throws CategoryException, CategoryNotFoundException, InvalidCategoryNameException, CategoryExistsException {
            //Given
            CategoryDto categoryDto = new CategoryDto("Car");
            adminService.addNewCategory(categoryDto);

            //When
            adminService.removeCategory(categoryDto);

            //Then
            assertEquals(0, categoryRepository.findAll().size());
        }

        @Nested
        @Transactional
        @DisplayName("test showAllCategoriesWithProducts")
        class TestShowAllCategoriesWithProducts {
            @Transactional
            @Test
            void testShowAllCategoriesWithProducts() {
                //Given
                Category category = new Category("Car");
                Category category1 = new Category("Music");
                categoryRepository.save(category);
                categoryRepository.save(category1);
                Product product = new Product("Album1", "CD", category1, new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
                productRepository.save(product);
                category1.getListOfProducts().add(product);
                categoryRepository.save(category1);

                //When & Then
                assertEquals(2, adminService.showAllCategoriesWithProducts().size());
                assertInstanceOf(CategoryWithProductsDto.class, adminService.showAllCategoriesWithProducts().get(0));
                assertEquals(0, adminService.showAllCategoriesWithProducts().get(0).getListOfProducts().size());
                assertEquals(1, adminService.showAllCategoriesWithProducts().get(1).getListOfProducts().size());
                assertEquals("Album1", adminService.showAllCategoriesWithProducts().get(1).getListOfProducts().get(0).getProductName());
            }
        }

        @Nested
        @Transactional
        @DisplayName("test showCategoryByNameWithProducts")
        class TestShowCategoryByNameWithProducts {
            @Test
            void testShowCategoryByNameWithProductsCategoryNotFoundException() {
                //Given
                Category category = new Category("Car");
                Category category1 = new Category("Music");
                categoryRepository.save(category);
                categoryRepository.save(category1);

                //When & Then
                assertThrows(CategoryNotFoundException.class, () -> adminService.showCategoryByNameWithProducts("anyString"));
            }
        }

        @Test
        void testShowCategoryByNameWithProductsPositive() throws CategoryNotFoundException {
            //Given
            Category category = new Category("Car");
            Category category1 = new Category("Music");
            categoryRepository.save(category);
            categoryRepository.save(category1);
            Product product = new Product("Album1", "CD", category1, new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            productRepository.save(product);
            category1.getListOfProducts().add(product);
            categoryRepository.save(category1);

            //When & Then
            assertInstanceOf(CategoryWithProductsDto.class, adminService.showCategoryByNameWithProducts("Music"));
            assertEquals(1, adminService.showCategoryByNameWithProducts("Music").getListOfProducts().size());
            assertEquals("Album1", adminService.showCategoryByNameWithProducts("Music").getListOfProducts().get(0).getProductName());
        }
    }

    @Nested
    @Transactional
    @DisplayName("test addNewProduct")
    class TestAddNewProduct {
        @Test
        void testAddNewProductInvalidPriceException() {
            //Given
            ProductDto productDto = new ProductDto("testProduct", "testDescription", "testCategory", new BigDecimal(-25.12).setScale(2, RoundingMode.HALF_EVEN));

            //When & Then
            assertThrows(InvalidPriceException.class, () -> adminService.addNewProduct(productDto));
        }

        @Test
        void testAddNewProductInvalidCategoryException() {
            //Given
            ProductDto productDto = new ProductDto("testProduct", "testDescription", "%%%", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));

            //When & Then
            try {
                ProductDtoAllInfo productDtoAllInfo = adminService.addNewProduct(productDto);
                Long productId = productRepository.findByProductName("testProduct").getProductID();
                assertEquals(productId, productDtoAllInfo.getProductId());
                assertEquals("Unknown", productDtoAllInfo.getCategory());
                assertEquals("testProduct", productDtoAllInfo.getProductName());
                assertEquals("25.12", productDtoAllInfo.getPrice().toString());
            } catch (InvalidPriceException | InvalidCategoryNameException | CategoryExistsException exception) {
                System.out.println("problem with testAddNewProductInvalidCategory");
            }
        }

        @Test
        void testAddNewProductCategoryExistsException() {
            //Given
            Category category = new Category("Unknown");
            categoryRepository.save(category);
            ProductDto productDto = new ProductDto("testProduct", "testDescription", "%%%", new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));

            //When & Then
            try {
                ProductDtoAllInfo productDtoAllInfo = adminService.addNewProduct(productDto);
                Long productId = productRepository.findByProductName("testProduct").getProductID();
                assertEquals(productId, productDtoAllInfo.getProductId());
                assertEquals("Unknown", productDtoAllInfo.getCategory());
                assertEquals("testProduct", productDtoAllInfo.getProductName());
                assertEquals("25.12", productDtoAllInfo.getPrice().toString());
            } catch (InvalidPriceException | InvalidCategoryNameException | CategoryExistsException exception) {
                System.out.println("problem with testAddNewProductInvalidCategory");
            }
        }

        @Test
        void testAddNewProductPositive() {
            //Given
            Category category = new Category("Pets");
            categoryRepository.save(category);
            ProductDto productDto = new ProductDto("testProduct", "testDescription", "Pets", new BigDecimal(30.455).setScale(2, RoundingMode.HALF_EVEN));

            //When & Then
            try {
                ProductDtoAllInfo productDtoAllInfo = adminService.addNewProduct(productDto);
                Long productId = productRepository.findByProductName("testProduct").getProductID();
                assertEquals(productId, productDtoAllInfo.getProductId());
                assertEquals("Pets", productDtoAllInfo.getCategory());
                assertEquals("testProduct", productDtoAllInfo.getProductName());
                assertEquals("30.45", productDtoAllInfo.getPrice().toString());
            } catch (InvalidPriceException | InvalidCategoryNameException | CategoryExistsException exception) {
                System.out.println("problem with testAddNewProductInvalidCategory");
            }
        }
    }

    @Nested
    @Transactional
    @DisplayName("test updateProduct")
    class TestUpdateProduct {
        @Test
        void updateProductProductNotFoundException() throws InvalidPriceException, InvalidCategoryNameException, CategoryExistsException {
            //Given
            Category category = new Category("Pets");
            categoryRepository.save(category);
            ProductDto productDto = new ProductDto("testProduct", "testDescription", "Pets", new BigDecimal(30.455).setScale(2, RoundingMode.HALF_EVEN));
            adminService.addNewProduct(productDto);
            Long productId = productRepository.findByProductName("testProduct").getProductID();

            //When & Then
            assertThrows(ProductNotFoundException.class, () -> adminService.updateProduct((productId + 1), productDto));
        }

        @Test
        void updateProductInvalidCategoryNameException() throws InvalidPriceException, InvalidCategoryNameException, CategoryExistsException {
            //Given
            Category category = new Category("Pets");
            categoryRepository.save(category);
            ProductDto productDto = new ProductDto("testProduct", "testDescription", "Pets", new BigDecimal(30.455).setScale(2, RoundingMode.HALF_EVEN));
            adminService.addNewProduct(productDto);
            Long productId = productRepository.findByProductName("testProduct").getProductID();
            ProductDto productDtoForUpdate = new ProductDto("testProduct", "testDescription", "Music", new BigDecimal(200).setScale(2, RoundingMode.HALF_EVEN));

            //When & Then
            try {
                ProductDtoAllInfo productDtoAllInfo = adminService.updateProduct(productId, productDtoForUpdate);
                assertEquals(productId, productDtoAllInfo.getProductId());
                assertEquals("Unknown", productDtoAllInfo.getCategory());
                assertEquals("200.00", productDtoAllInfo.getPrice().toString());
            } catch (ProductNotFoundException e) {
                e.printStackTrace();
                       }
        }

        @Transactional
        @Test
        void updateProductPositive() throws InvalidPriceException, InvalidCategoryNameException, CategoryExistsException {
            //Given
            Category category = new Category("Pets");
            Category category1 = new Category("Music");
            categoryRepository.save(category);
            categoryRepository.save(category1);
            ProductDto productDto = new ProductDto("testProduct", "testDescription", "Pets", new BigDecimal(30.455).setScale(2, RoundingMode.HALF_EVEN));
            adminService.addNewProduct(productDto);
            Long productId = productRepository.findByProductName("testProduct").getProductID();
            ProductDto productDtoForUpdate = new ProductDto("testProduct", "testDescription", "Music", new BigDecimal(200).setScale(2, RoundingMode.HALF_EVEN));

            //When & Then
            try {
                ProductDtoAllInfo productDtoAllInfo = adminService.updateProduct(productId, productDtoForUpdate);
                assertEquals(productId, productDtoAllInfo.getProductId());
                assertEquals("Music", productDtoAllInfo.getCategory());
                assertEquals("200.00", productDtoAllInfo.getPrice().toString());
            } catch (ProductNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Nested
    @Transactional
    @DisplayName("test deleteProductById")
    class TestDeleteProductById {
        @Transactional
        @Test
        void deleteProductByIdProductNotFoundException() {
            //Given
            Category category = new Category("Music");
            categoryRepository.save(category);
            Product product = new Product("Album1", "CD", category, new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            productRepository.save(product);
            category.getListOfProducts().add(product);
            categoryRepository.save(category);

            //When & Then
            assertThrows(ProductNotFoundException.class, () -> adminService.deleteProductById((product.getProductID() + 1)));
            assertEquals(1, category.getListOfProducts().size());
        }

        @Transactional
        @Test
        void deleteProductByIdPositive() {
            //Given
            Category category = new Category("Music");
            categoryRepository.save(category);
            Product product = new Product("Album1", "CD", category, new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            productRepository.save(product);
            category.getListOfProducts().add(product);
            categoryRepository.save(category);

            //When
            try {
                adminService.deleteProductById(product.getProductID());
            } catch (ProductNotFoundException e) {
                e.printStackTrace();
            }

            //Then
            assertEquals(0, category.getListOfProducts().size());
            assertTrue(productRepository.findByProductID(product.getProductID()).isEmpty());
        }
    }

    @Nested
    @Transactional
    @DisplayName("test showAllProducts")
    class TestShowAllProducts {

        @Test
        void showAllProducts() {
            //Given
            Category category = new Category("Music");
            categoryRepository.save(category);
            Product product = new Product("Album1", "CD", category, new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            productRepository.save(product);
            Product product1 = new Product("Album2", "MP3", category, new BigDecimal(10.25).setScale(2, RoundingMode.HALF_EVEN));
            productRepository.save(product1);
            category.getListOfProducts().add(product);
            category.getListOfProducts().add(product1);
            categoryRepository.save(category);

            //When
            List<ProductDtoAllInfo> list = adminService.showAllProducts();

            //Then
            assertEquals(2, list.size());
            assertEquals("Music", list.get(0).getCategory());
            assertEquals("10.25", list.get(1).getPrice().toString());
        }
    }

    @Nested
    @Transactional
    @DisplayName("test addOrUpdateProductInWarehouse")
    class TestAddOrUpdateProductInWarehouse {
        @Test
        void addOrUpdateProductInWarehouseInvalidQuantityException() {
            //Given
            Category category = new Category("Music");
            categoryRepository.save(category);
            Product product = new Product("Album1", "CD", category, new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            productRepository.save(product);
            Long productId = product.getProductID();

            //When & Then
            assertThrows(InvalidQuantityException.class, () -> adminService.addOrUpdateProductInWarehouse(productId, -10));
        }

        @Test
        void addOrUpdateProductInWarehouseProductNotFoundException() {
            //Given
            Category category = new Category("Music");
            categoryRepository.save(category);
            Product product = new Product("Album1", "CD", category, new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            productRepository.save(product);
            Long productId = product.getProductID();

            //When & Then
            assertThrows(ProductNotFoundException.class, () -> adminService.addOrUpdateProductInWarehouse((productId + 1), 10));
         }

        @Test
        void addOrUpdateProductInWarehouseCategoryNotFoundException() {
            //Given
            Category category = new Category("Unknown");
            categoryRepository.save(category);
            Product product = new Product("Album1", "CD", category, new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            productRepository.save(product);
            Long productId = product.getProductID();

            //When & Then
            assertThrows(CategoryNotFoundException.class, () -> adminService.addOrUpdateProductInWarehouse(productId, 10));
        }

        @Test
        void addOrUpdateProductInWarehouseCategoryAddNewProduct() {
            //Given
            Category category = new Category("Music");
            categoryRepository.save(category);
            Product product = new Product("Album1", "CD", category, new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            productRepository.save(product);
            Long productId = product.getProductID();

            //When & Then
            try {
                adminService.addOrUpdateProductInWarehouse(productId, 10);
                assertEquals(1, warehouseRepository.findAll().size());
                assertEquals(10, warehouseRepository.findAll().get(0).getProductQuantity());
            } catch (InvalidQuantityException | ProductNotFoundException | CategoryNotFoundException e) {
                e.printStackTrace();
            }
        }

        @Test
        void addOrUpdateProductInWarehouseCategoryUpdateProduct() {
            //Given
            Category category = new Category("Music");
            categoryRepository.save(category);
            Product product = new Product("Album1", "CD", category, new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            productRepository.save(product);
            Long productId = product.getProductID();
            try {
                adminService.addOrUpdateProductInWarehouse(productId, 10);
            } catch (InvalidQuantityException | ProductNotFoundException | CategoryNotFoundException e) {
                e.printStackTrace();
            }

            //When & Then
            try {
                adminService.addOrUpdateProductInWarehouse(productId, 20);
                assertEquals(1, warehouseRepository.findAll().size());
                assertEquals(30, warehouseRepository.findAll().get(0).getProductQuantity());
            } catch (InvalidQuantityException | ProductNotFoundException | CategoryNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Nested
    @Transactional
    @DisplayName("test deleteProductFromWarehouse")
    class TestDeleteProductFromWarehouse {
        @Transactional
        @Test
        void testDeleteProductFromWarehouseProductNotFoundException() {
            //Given
            Category category = new Category("Music");
            categoryRepository.save(category);
            Product product = new Product("Album1", "CD", category, new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            productRepository.save(product);
            Long productId = product.getProductID();
            try {
                adminService.addOrUpdateProductInWarehouse(productId, 10);
            } catch (InvalidQuantityException | ProductNotFoundException | CategoryNotFoundException e) {
                e.printStackTrace();
            }

            //When & Then
            assertThrows(ProductNotFoundException.class, () -> adminService.deleteProductFromWarehouse(productId + 1));
            assertEquals(10, (int) warehouseRepository.findAll().get(0).getProductQuantity());
        }

        @Transactional
        @Test
        void testDeleteProductFromWarehousePositive() {
            //Given
            Category category = new Category("Music");
            categoryRepository.save(category);
            Product product = new Product("Album1", "CD", category, new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            productRepository.save(product);
            Long productId = product.getProductID();
            try {
                adminService.addOrUpdateProductInWarehouse(productId, 10);
            } catch (InvalidQuantityException | ProductNotFoundException | CategoryNotFoundException e) {
                e.printStackTrace();
            }

            //When & Then
            try {
                adminService.deleteProductFromWarehouse(productId);
                assertTrue(warehouseRepository.findAll().isEmpty());
                assertTrue(productRepository.findAll().contains(product));
            } catch (ProductNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Nested
    @Transactional
    @DisplayName("test displayAllProductsInWarehouse")
    class TestDisplayAllProductsInWarehouse {

        @Test
        void testDisplayAllProductsInWarehousePositive() {
            //Given
            Category category = new Category("Music");
            categoryRepository.save(category);
            Product product = new Product("Album1", "CD", category, new BigDecimal(25.12).setScale(2, RoundingMode.HALF_EVEN));
            productRepository.save(product);
            Product product1 = new Product("Album2", "MP3", category, new BigDecimal(10.25).setScale(2, RoundingMode.HALF_EVEN));
            productRepository.save(product1);
            category.getListOfProducts().add(product);
            category.getListOfProducts().add(product1);
            Category category2 = new Category("Pets");
            categoryRepository.save(category2);
            Product product2 = new Product("CatFood", "granules 5kg", category2, new BigDecimal(125.1).setScale(2, RoundingMode.HALF_EVEN));
            productRepository.save(product2);
            category2.getListOfProducts().add(product2);
            categoryRepository.save(category2);
            try {
                adminService.addOrUpdateProductInWarehouse(product.getProductID(), 10);
                adminService.addOrUpdateProductInWarehouse(product1.getProductID(), 12);
                adminService.addOrUpdateProductInWarehouse(product2.getProductID(), 25);
            } catch (InvalidQuantityException | ProductNotFoundException | CategoryNotFoundException e) {
                e.printStackTrace();
            }

            //When & Then
            assertInstanceOf(WarehouseDto.class, adminService.displayAllProductsInWarehouse().get(0));
            assertEquals(3, adminService.displayAllProductsInWarehouse().size());
            assertEquals(25, adminService.displayAllProductsInWarehouse().get(2).getQuantity());
        }
    }
}
