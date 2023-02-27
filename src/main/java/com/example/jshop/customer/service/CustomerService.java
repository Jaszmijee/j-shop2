package com.example.jshop.customer.service;

import com.example.jshop.customer.domain.LoggedCustomer;
import com.example.jshop.customer.domain.LoggedCustomerDto;
import com.example.jshop.customer.domain.AuthenticationDataDto;
import com.example.jshop.carts_and_orders.domain.order.OrderDtoToCustomer;
import com.example.jshop.error_handlers.exceptions.AccessDeniedException;
import com.example.jshop.error_handlers.exceptions.UserNotFoundException;
import com.example.jshop.customer.mapper.CustomerMapper;
import com.example.jshop.carts_and_orders.mapper.OrderMapper;
import com.example.jshop.customer.repository.CustomerRepository;
import com.example.jshop.carts_and_orders.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerMapper customerMapper;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderMapper orderMapper;

    @Autowired
    PasswordEncoder bCryptPasswordEncoder;

    public LoggedCustomer updateCustomer(LoggedCustomer loggedCustomer) {
        return customerRepository.save(loggedCustomer);
    }

    public LoggedCustomer createNewCustomer(LoggedCustomerDto loggedCustomerDto) {
        LoggedCustomer loggedCustomer = customerMapper.mapToCustomer(loggedCustomerDto);
        loggedCustomer.setPassword(bCryptPasswordEncoder.encode(loggedCustomerDto.getPassword()).toCharArray());
        return customerRepository.save(loggedCustomer);
    }

    private LoggedCustomer findCustomerByName(String userName) throws UserNotFoundException {
        return customerRepository.findCustomer_LoggedByUserNameEquals(userName).orElseThrow(UserNotFoundException::new);
    }

    public LoggedCustomer verifyLogin(String userName, char[] pwwd) throws UserNotFoundException, AccessDeniedException {
        LoggedCustomer _Logged_Customer = findCustomerByName(userName);
        StringBuilder request = new StringBuilder();
        for (char ch : pwwd) {
            request.append(ch);
        }
        StringBuilder response = new StringBuilder();
        for (char ch : _Logged_Customer.getPassword()) {
            response.append(ch);
        }
        if (!bCryptPasswordEncoder.matches(request.toString(), response.toString())){
            LOGGER.error("Unauthorized access attempt for " + userName);
            throw new AccessDeniedException();
        }
        else return _Logged_Customer;
    }

    public void removeCustomer(AuthenticationDataDto customerDto) throws UserNotFoundException, AccessDeniedException {
        verifyLogin(customerDto.getUsername(), customerDto.getPassword());
        customerRepository.deleteById(findCustomerByName(customerDto.getUsername()).getCustomerID());
    }

    public void deleteUnauthenticatedCustomer(Long customerId) {
        customerRepository.deleteById(customerId);
    }

    public List<OrderDtoToCustomer> showMyOrders(AuthenticationDataDto customerDto) throws UserNotFoundException, AccessDeniedException {
        verifyLogin(customerDto.getUsername(), customerDto.getPassword());
        return orderMapper.mapToOrderDtoToCustomerList(orderService.findOrdersOfCustomer(customerDto.getUsername()));
    }


}
