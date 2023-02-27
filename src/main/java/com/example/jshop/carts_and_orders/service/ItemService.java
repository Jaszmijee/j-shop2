package com.example.jshop.carts_and_orders.service;

import com.example.jshop.carts_and_orders.domain.cart.Item;
import com.example.jshop.carts_and_orders.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public Item save(Item item){
        return itemRepository.save(item);
    }

    public void delete(Item item){
        itemRepository.delete(item);
    }
}
