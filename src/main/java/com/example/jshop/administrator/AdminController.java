package com.example.jshop.administrator;

import com.example.jshop.error_handlers.exceptions.*;
import com.example.jshop.warehouse_and_products.domain.category.CategoryWithProductsDto;
import com.example.jshop.carts_and_orders.domain.order.OrderDtoToCustomer;
import com.example.jshop.warehouse_and_products.domain.product.ProductDto;
import com.example.jshop.warehouse_and_products.domain.product.ProductDtoAllInfo;
import com.example.jshop.warehouse_and_products.domain.warehouse.WarehouseDto;
import com.example.jshop.warehouse_and_products.domain.category.CategoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/j-shop/admin")
public class AdminController {

    private final AdminConfig adminConfig;
    private final AdminService adminService;

    private void verifyAdmin(@RequestParam String key, @RequestParam String token) throws AccessDeniedException {
        if (!(adminConfig.getAdminKey().equals(key) && adminConfig.getAdminToken().equals(token))) {
            throw new AccessDeniedException();
        }
    }

    @PostMapping("category")
    ResponseEntity<Void> addNewCategory(@RequestParam String key, @RequestParam String token, @RequestBody CategoryDto categoryDto) throws AccessDeniedException, InvalidCategoryNameException, CategoryExistsException {
        verifyAdmin(key, token);
        adminService.addNewCategory(categoryDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("category")
    ResponseEntity<Void> removeCategory(@RequestParam String key, @RequestParam String token, @RequestBody CategoryDto categoryDto) throws AccessDeniedException, CategoryNotFoundException, CategoryException {
        verifyAdmin(key, token);
        adminService.removeCategory(categoryDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("category")
    ResponseEntity<List<CategoryWithProductsDto>> showAllCategoriesAndProducts(@RequestParam String key, @RequestParam String token) throws AccessDeniedException {
        verifyAdmin(key, token);
        return ResponseEntity.ok(adminService.showAllCategoriesWithProducts());
    }

    @GetMapping("category/name")
    ResponseEntity<CategoryWithProductsDto> showCategoriesByName(@RequestParam String key, @RequestParam String token, @RequestParam String categoryName) throws CategoryNotFoundException, AccessDeniedException {
        verifyAdmin(key, token);
        return ResponseEntity.ok(adminService.showCategoryByNameWithProducts(categoryName));
    }

    @PostMapping("product")
    ResponseEntity<ProductDtoAllInfo> addProduct(@RequestParam String key, @RequestParam String token, @RequestBody ProductDto productDto) throws AccessDeniedException, InvalidCategoryNameException, CategoryExistsException, InvalidPriceException {
        verifyAdmin(key, token);
        return ResponseEntity.ok(adminService.addNewProduct(productDto));
    }

    @PutMapping("product")
    ResponseEntity<ProductDtoAllInfo> updateProduct(@RequestParam String key, @RequestParam String token, @RequestParam Long productId, @RequestBody ProductDto productDto) throws AccessDeniedException, ProductNotFoundException, InvalidCategoryNameException, CategoryExistsException, InvalidPriceException {
        verifyAdmin(key, token);
        return ResponseEntity.ok(adminService.updateProduct(productId, productDto));
    }

    @DeleteMapping("product")
    ResponseEntity<Void> removeProduct(@RequestParam String key, @RequestParam String token, @RequestParam Long productId) throws AccessDeniedException,ProductNotFoundException {
        verifyAdmin(key, token);
        adminService.deleteProductById(productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("product")
    ResponseEntity<List<ProductDtoAllInfo>> showAllProducts(@RequestParam String key, @RequestParam String token) throws AccessDeniedException {
        verifyAdmin(key, token);
        return ResponseEntity.ok(adminService.showAllProducts());
    }

    @PostMapping("warehouse")
    ResponseEntity<WarehouseDto> addProductToWarehouse(@RequestParam String key, @RequestParam String token, @RequestParam Long productId, @RequestParam Integer productQuantity) throws AccessDeniedException, ProductNotFoundException, InvalidQuantityException, CategoryNotFoundException {
        verifyAdmin(key, token);
        return ResponseEntity.ok(adminService.addOrUpdateProductInWarehouse(productId, productQuantity));
    }

    @DeleteMapping("warehouse")
    ResponseEntity<Void> removeProductFromWarehouse(@RequestParam String key, @RequestParam String token, @RequestParam Long productId) throws AccessDeniedException, ProductNotFoundException {
        verifyAdmin(key, token);
        adminService.deleteProductFromWarehouse(productId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("warehouse")
    ResponseEntity<List<WarehouseDto>> displayAllProductsInWarehouse(@RequestParam String key, @RequestParam String token) throws AccessDeniedException {
        verifyAdmin(key, token);
        return ResponseEntity.ok(adminService.displayAllProductsInWarehouse());
    }

    @GetMapping("order")
    ResponseEntity<List<OrderDtoToCustomer>> displayAllOrders(@RequestParam String key, @RequestParam String token, @RequestParam(required = false) String order_status) throws AccessDeniedException, InvalidOrderStatusException, OrderNotFoundException {
        verifyAdmin(key, token);
        return ResponseEntity.ok(adminService.displayOrders(order_status));
    }
}
