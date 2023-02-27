package com.example.jshop.carts_and_orders.repository;

import com.example.jshop.carts_and_orders.domain.order.Order;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface OrderRepository extends CrudRepository<Order, Long> {

    @Override
    Order save(Order order);

    Optional<Order> findByOrderIDAndLoggedCustomer_UserName(Long orderId, String userName);

    @Query(value = "SELECT * from orders " +
            "where (:STATUS IS NULL OR order_status LIKE :STATUS) " +
            "ORDER BY order_status", nativeQuery = true)
    List<Order> findOrders(@Param("STATUS") String order_status);

    List<Order> findByLoggedCustomer_UserName(String userName);

    @Query(value = "SELECT * from orders " +
            "where order_status = 'UNPAID' " +
            "AND paid IS NULL " +
            "AND DATEDIFF(CURDATE(), created) = 13",
            nativeQuery = true)
    List<Order> findOrdersCloseTOPayment();

    @Query(value = "SELECT * from orders " +
            "where order_status = 'UNPAID' " +
            "AND paid IS NULL " +
            "AND DATEDIFF(CURDATE(), created) > 14",
            nativeQuery = true)
    List<Order> findUnpaidOrders();
}

