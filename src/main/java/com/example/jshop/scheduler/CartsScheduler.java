package com.example.jshop.scheduler;

import com.example.jshop.carts_and_orders.domain.cart.CartStatus;
import com.example.jshop.carts_and_orders.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Slf4j
public class CartsScheduler {

    @Autowired
    private CartService cartService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void removeEmptyCarts() {
        log.info("deleting empty carts" + LocalDate.now());
        try {
            cartService.deleteByCartStatus(CartStatus.EMPTY);
            log.info("empty carts deleted" + LocalDate.now());
        } catch (Exception e) {
            log.error("empty cars were not removed", e);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void removeUnfinalizedCarts() {
        log.info("deleting carts with status\"PROCESSING\" longer than 3 days" + LocalDate.now());
        try {
            cartService.deleteByProcessingTime();
            log.info("carts with status\"PROCESSING\" longer than 3 days deleted" + LocalDate.now());
        } catch (Exception e) {
            log.error("\"PROCESSING\" carts were not removed", e);
        }
    }
}
