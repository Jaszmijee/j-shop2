package com.example.jshop.administrator;

import com.example.jshop.error_handlers.exceptions.*;
import com.example.jshop.warehouse_and_products.domain.category.Category;
import com.example.jshop.warehouse_and_products.domain.category.CategoryDto;
import com.example.jshop.warehouse_and_products.domain.category.CategoryWithProductsDto;
import com.example.jshop.carts_and_orders.domain.order.Order;
import com.example.jshop.carts_and_orders.domain.order.OrderDtoToCustomer;
import com.example.jshop.warehouse_and_products.domain.product.Product;
import com.example.jshop.warehouse_and_products.domain.product.ProductDto;
import com.example.jshop.warehouse_and_products.domain.product.ProductDtoAllInfo;
import com.example.jshop.warehouse_and_products.domain.warehouse.Warehouse;
import com.example.jshop.warehouse_and_products.domain.warehouse.WarehouseDto;
import com.example.jshop.warehouse_and_products.mapper.CategoryMapper;
import com.example.jshop.carts_and_orders.mapper.OrderMapper;
import com.example.jshop.warehouse_and_products.mapper.ProductMapper;
import com.example.jshop.warehouse_and_products.mapper.WarehouseMapper;
import com.example.jshop.warehouse_and_products.service.CategoryService;
import com.example.jshop.carts_and_orders.service.OrderService;
import com.example.jshop.warehouse_and_products.service.ProductService;
import com.example.jshop.warehouse_and_products.service.WarehouseService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;
    private final ProductService productService;
    private final ProductMapper productMapper;
    private final WarehouseService warehouseService;
    private final WarehouseMapper warehouseMapper;
    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public void addNewCategory(CategoryDto categoryDto) throws InvalidCategoryNameException, CategoryExistsException {
        Category category = categoryMapper.mapToCategory(categoryDto);
        categoryService.addCategory(category);
    }

    public void removeCategory(CategoryDto categoryDto) throws CategoryNotFoundException, CategoryException {
        categoryService.deleteCategory(categoryDto.getName());
    }

    public List<CategoryWithProductsDto> showAllCategoriesWithProducts() {
        List<Category> listCategories = categoryService.showAllCategories();
        return categoryMapper.mapToCategoryDtoListAllInfo(listCategories);
    }

    public CategoryWithProductsDto showCategoryByNameWithProducts(String categoryName) throws CategoryNotFoundException {
        Category category = categoryService.searchForProductsInCategory(categoryName);
        return categoryMapper.mapToCategoryDtoAllInfo(category);
    }

    private void validatePrice(BigDecimal price) throws InvalidPriceException {
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidPriceException();
        }
    }

    private Category setUpCategoryNameForNewProduct(Product product) throws InvalidCategoryNameException, CategoryExistsException {
        Category category = categoryService.findByName(product.getCategory().getName());
        if (category != null) {
            category.getListOfProducts().add(product);
            product.setCategory(category);
        } else {
            category = categoryService.findByName("unknown");
            if (category == null) {
                category = categoryService.addCategory(new Category("Unknown"));
            }
        }
        return category;
    }

    public ProductDtoAllInfo addNewProduct(ProductDto productDto) throws InvalidPriceException, InvalidCategoryNameException, CategoryExistsException {
        validatePrice(productDto.getPrice());
        Product product = productMapper.mapToProduct(productDto);
        Category category = setUpCategoryNameForNewProduct(product);
        product.setCategory(category);
        category.getListOfProducts().add(product);
        categoryService.save(category);
        Product savedProduct = productService.saveProduct(product);
        return productMapper.mapToProductDtoAllInfo(savedProduct);
    }

    public ProductDtoAllInfo updateProduct(Long productId, ProductDto productDto) throws ProductNotFoundException, InvalidCategoryNameException, CategoryExistsException, InvalidPriceException {
        validatePrice(productDto.getPrice());
        Product productToUpdate = productService.findProductById(productId);
        Product product = productMapper.mapToProduct(productDto);
        Category categoryToUpdate = setUpCategoryNameForNewProduct(product);
        productToUpdate.setCategory(categoryToUpdate);
        productToUpdate.setPrice(productDto.getPrice());
        categoryToUpdate.getListOfProducts().add(productToUpdate);
        categoryService.save(categoryToUpdate);
        productService.saveProduct(productToUpdate);
        return productMapper.mapToProductDtoAllInfo(productToUpdate);
    }

    public void deleteProductById(Long productId) throws ProductNotFoundException {
        if (warehouseService.findWarehouseByProductId(productId) != null) {
            deleteProductFromWarehouse(productId);
        }
        Product productToRemove = productService.findProductById(productId);
        Category category = productToRemove.getCategory();
        category.getListOfProducts().remove(productToRemove);
        categoryService.save(category);
        productService.deleteProductById(productId);
    }

    public List<ProductDtoAllInfo> showAllProducts() {
        List<Product> productList = productService.findAllProducts();
        return productMapper.mapToProductDtoList(productList);
    }

    private void validateQuantity(Integer quantity) throws InvalidQuantityException {
        if (quantity < 0) {
            throw new InvalidQuantityException();
        }
    }

    public WarehouseDto addOrUpdateProductInWarehouse(Long productId, Integer productQuantity) throws InvalidQuantityException, ProductNotFoundException, CategoryNotFoundException {
       validateQuantity(productQuantity);
        Product product = productService.findProductById(productId);
        if (product.getCategory().getName().equalsIgnoreCase("Unknown")) {
            throw new CategoryNotFoundException();
        }
        Warehouse warehouse = warehouseService.findWarehouseByProductId(product.getProductID());
        if (warehouse == null) {
            warehouse = new Warehouse(product, productQuantity);
        } else {
            warehouse.setProductQuantity(warehouse.getProductQuantity() + productQuantity);
        }
        warehouseService.save(warehouse);
        return warehouseMapper.mapToWarehouseDto(warehouse);
    }

    public void deleteProductFromWarehouse(Long productId) throws ProductNotFoundException {
        if (warehouseService.findWarehouseByProductId(productId) == null) {
            throw new ProductNotFoundException();
        }
          warehouseService.deleteProductFromWarehouseByProductId(productId);
    }

    public List<WarehouseDto> displayAllProductsInWarehouse() {
        List<Warehouse> listOfAllItems = warehouseService.findAllProductsInWarehouse();
        return warehouseMapper.mapToWarehouseDtoList(listOfAllItems);
    }

    public List<OrderDtoToCustomer> displayOrders(String order_status) throws InvalidOrderStatusException, OrderNotFoundException {
        List<Order> listOfOrders = orderService.findOrders(order_status);
        if (listOfOrders.isEmpty()) {
            throw new OrderNotFoundException();
        }
        return orderMapper.mapToOrderDtoToCustomerList(listOfOrders);
    }
}

