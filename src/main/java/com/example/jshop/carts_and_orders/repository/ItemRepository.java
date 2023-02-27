package com.example.jshop.carts_and_orders.repository;

import com.example.jshop.carts_and_orders.domain.cart.Item;
import jakarta.transaction.Transactional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@Transactional
public interface ItemRepository extends CrudRepository<Item, Long> {
    @Override
    Item save(Item item);
}
