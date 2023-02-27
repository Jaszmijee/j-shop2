package com.example.jshop.customer.repository;

import com.example.jshop.customer.domain.LoggedCustomer;
import jakarta.transaction.Transactional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Transactional
public interface CustomerRepository extends CrudRepository<LoggedCustomer, Long> {

    @Override
    LoggedCustomer save(LoggedCustomer loggedCustomer);

    Optional<LoggedCustomer> findCustomer_LoggedByUserNameEquals(String userName);
}
