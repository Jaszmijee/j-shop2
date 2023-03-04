package com.example.jshop.warehouse_and_products.service;

import com.example.jshop.error_handlers.exceptions.CategoryException;
import com.example.jshop.error_handlers.exceptions.CategoryExistsException;
import com.example.jshop.error_handlers.exceptions.CategoryNotFoundException;
import com.example.jshop.error_handlers.exceptions.InvalidCategoryNameException;
import com.example.jshop.warehouse_and_products.domain.category.Category;
import com.example.jshop.warehouse_and_products.domain.product.Product;
import com.example.jshop.warehouse_and_products.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CategoryServiceTest {

    @Autowired
    CategoryService categoryService;
    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ProductService productService;

    @Nested
    @Transactional
    @DisplayName("test findByName")
    class TestFindByName {
        @Test
        void findByNamePositiveNull() {
            // Given
            Category category = new Category("testCategory");
            categoryRepository.save(category);

            //When & Then
            assertNull(categoryService.findByName("otherCategory"));
        }


        @Test
        void findByNamePositiveNotNull() {
            // Given
            Category category = new Category("testName");
            categoryRepository.save(category);

            //When & Then
            assertEquals("testName", categoryService.findByName("TESTNAME").getName());
        }
    }


    @Nested
    @Transactional
    @DisplayName("test save")
    class TestSave {
        @Test
        void savePositive() {
            //Given
            Category category = new Category("testCategory");

            //When & Then
            assertEquals("testCategory", categoryService.save(category).getName());

        }
    }

    @Nested
    @Transactional
    @DisplayName("test addCategory")
    class TestAddCategory {
        @Test
        void addCategoryInvalidCategoryNameException() {
            // Given
            Category category1 = new Category(null);
            Category category2 = new Category("");
            Category category3 = new Category("  ");
            Category category4 = new Category("ab");
            Category category5 = new Category("$%t");

            //When & Then
            assertThrows(InvalidCategoryNameException.class, () -> categoryService.addCategory(category1));
            assertThrows(InvalidCategoryNameException.class, () -> categoryService.addCategory(category2));
            assertThrows(InvalidCategoryNameException.class, () -> categoryService.addCategory(category3));
            assertThrows(InvalidCategoryNameException.class, () -> categoryService.addCategory(category4));
            assertThrows(InvalidCategoryNameException.class, () -> categoryService.addCategory(category5));
        }

        @Test
        void addCategoryCategoryExistsException() {
            //Given
            Category category = new Category("testCategory");
            categoryRepository.save(category);

            //When & Then
            assertThrows(CategoryExistsException.class, () -> categoryService.addCategory(new Category("testCategory")));
        }

        @Test
        void addCategoryCategoryPositive() throws Exception {
            //Given
            Category category = new Category("testCategory");

            //When & Then
            categoryService.addCategory(category);
            assertDoesNotThrow(() -> categoryService.addCategory(new Category("testCategory1")));
            assertEquals(2, categoryRepository.findAll().size());
        }
    }

    @Nested
    @Transactional
    @DisplayName("test deleteCategory")
    class TestDeleteCategory {
        @Test
        void deleteCategoryCategoryNotFoundException() {
            //Given
            Category category = new Category("testCategory");
            categoryRepository.save(category);

            //When & Then
            assertThrows(CategoryNotFoundException.class, () -> categoryService.deleteCategory("anotherName"));
        }

        @Test
        void deleteCategoryCategoryException() {
            //Given
            Category category = new Category("unknown");
            categoryRepository.save(category);

            //When & Then
            assertThrows(CategoryException.class, () -> categoryService.deleteCategory("unknown"));
        }

        @Test
        void deleteCategoryPositive() throws Exception {
            //Given
            Category category = new Category("testCategory");
            categoryRepository.save(category);
            Product product = new Product("productName", "productDescription", category, new BigDecimal("25.12"));
            productService.saveProduct(product);
            category.getListOfProducts().add(product);
            categoryRepository.save(category);

            //When & Then
            categoryService.deleteCategory("testCategory");
            assertFalse(categoryRepository.existsById(category.getCategoryID()));
            assertEquals("Unknown", product.getCategory().getName());
        }
    }

    @Nested
    @Transactional
    @DisplayName("test showAllCategories")
    class TestShowAllCategories {
        @Test
        void showAllCategories() {
            //Given
            Category category = new Category("testCategory");
            Category category1 = new Category("testCategory1");
            categoryRepository.save(category);
            categoryRepository.save(category1);

            //When & Then
            assertEquals(2, categoryService.showAllCategories().size());
        }
    }
}
