package com.example.jshop.carts_and_orders.repository;

import com.example.jshop.carts_and_orders.domain.cart.Cart;
import com.example.jshop.carts_and_orders.domain.cart.CartStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public interface CartRepository extends CrudRepository<Cart, Long> {

    @Override
    Cart save(Cart cart);

    void deleteByCartStatus(CartStatus cartStatus);

    @Query(value = "SELECT * from carts " +
            "where status = 'PROCESSING' " +
            "AND DATEDIFF(CURDATE(), created) >= 3",
            nativeQuery = true)
    List<Cart> selectByProcessingTime();

   @Override
   List<Cart> findAll();
}
