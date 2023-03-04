package com.example.jshop.warehouse_and_products.repository;

import com.example.jshop.warehouse_and_products.domain.warehouse.Warehouse;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface WarehouseRepository extends CrudRepository<Warehouse, Long> {

    @Override
    List<Warehouse> findAll();

    @Override
    Warehouse save(Warehouse warehouse);

    void deleteByProduct_ProductID(Long productId);

     @Query(value = "SELECT * from Warehouse " +
            "where products_product_id = :productId", nativeQuery = true)
    Optional<Warehouse> findWarehouseByProductId(@Param("productId") Long productId);

    @Query(value = "SELECT * from Warehouse w " +
            "JOIN products p on w.products_product_id = p.product_id " +
            "JOIN categories c ON p.categories_categoryID = c.categoryID " +
            "where (:CATEGORY_NAME IS NULL OR c.category LIKE %:CATEGORY_NAME%) " +
            "AND (:PRODUCT_NAME IS NULL OR p.product_name LIKE %:PRODUCT_NAME%) " +
            "AND (:PRICE IS NULL OR p.price <=:PRICE) " +
            "AND w.quantity > 0 " +
            "ORDER BY p.price ASC " +
            "LIMIT :limit", nativeQuery = true)
    List<Warehouse> findWarehouseByProduct_CategoryOrProduct_ProductNameOAndProduct_Price(@Param("CATEGORY_NAME") String categoryName, @Param("PRODUCT_NAME") String productName,
                                                                                          @Param("PRICE") BigDecimal price, Integer limit);
}



