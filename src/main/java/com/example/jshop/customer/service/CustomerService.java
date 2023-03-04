package com.example.jshop.customer.service;

import com.example.jshop.customer.domain.LoggedCustomer;
import com.example.jshop.customer.domain.LoggedCustomerDto;
import com.example.jshop.customer.domain.AuthenticationDataDto;
import com.example.jshop.carts_and_orders.domain.order.OrderDtoToCustomer;
import com.example.jshop.error_handlers.exceptions.AccessDeniedException;
import com.example.jshop.error_handlers.exceptions.InvalidCustomerDataException;
import com.example.jshop.error_handlers.exceptions.UserNotFoundException;
import com.example.jshop.customer.mapper.CustomerMapper;
import com.example.jshop.carts_and_orders.mapper.OrderMapper;
import com.example.jshop.customer.repository.CustomerRepository;
import com.example.jshop.carts_and_orders.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final PasswordEncoder bCryptPasswordEncoder;

    public LoggedCustomer updateCustomer(LoggedCustomer loggedCustomer) {
        return customerRepository.save(loggedCustomer);
    }

    public LoggedCustomer createNewCustomer(LoggedCustomerDto loggedCustomerDto) throws InvalidCustomerDataException {
        checkLoggedCustomerDataValidity(loggedCustomerDto);
        LoggedCustomer loggedCustomer = customerMapper.mapToCustomer(loggedCustomerDto);
        loggedCustomer.setPassword(bCryptPasswordEncoder.encode(loggedCustomerDto.getPassword()).toCharArray());
        return customerRepository.save(loggedCustomer);
    }

    private void checkLoggedCustomerDataValidity(LoggedCustomerDto loggedCustomerDto) throws InvalidCustomerDataException {
        if ((loggedCustomerDto.getUserName() == null || loggedCustomerDto.getUserName().isEmpty())
                || (loggedCustomerDto.getPassword() == null || loggedCustomerDto.getPassword().isEmpty()
                || (loggedCustomerDto.getFirstName() == null || loggedCustomerDto.getFirstName().isEmpty())
                || (loggedCustomerDto.getLastName() == null || loggedCustomerDto.getLastName().isEmpty())
                || (loggedCustomerDto.getStreet() == null || loggedCustomerDto.getStreet().isEmpty())
                || (loggedCustomerDto.getHouseNo() == null || loggedCustomerDto.getHouseNo().isEmpty())
                || (loggedCustomerDto.getCity() == null || loggedCustomerDto.getCity().isEmpty())
                || (loggedCustomerDto.getZipCode() == null || loggedCustomerDto.getZipCode().isEmpty()
                || !loggedCustomerDto.getZipCode().matches("^[0-9]{2}[-]?[0-9]{3}$"))
                || (loggedCustomerDto.getEmail() == null || loggedCustomerDto.getEmail().isEmpty()
                || !loggedCustomerDto.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")))) {
            throw new InvalidCustomerDataException();
        }
    }

    private LoggedCustomer findCustomerByName(String userName) throws UserNotFoundException {
        return customerRepository.findCustomer_LoggedByUserNameEquals(userName).orElseThrow(UserNotFoundException::new);
    }

    public LoggedCustomer verifyLogin(String userName, char[] pwwd) throws UserNotFoundException, AccessDeniedException, InvalidCustomerDataException {
        if (userName == null || userName.isEmpty()
                || pwwd == null || pwwd.length == 0) {
            throw new InvalidCustomerDataException();
        }
        LoggedCustomer _Logged_Customer = findCustomerByName(userName);
        StringBuilder request = new StringBuilder();
        for (char ch : pwwd) {
            request.append(ch);
        }
        StringBuilder response = new StringBuilder();
        for (char ch : _Logged_Customer.getPassword()) {
            response.append(ch);
        }
        if (!bCryptPasswordEncoder.matches(request.toString(), response.toString())) {
            LOGGER.error("Unauthorized access attempt for " + userName);
            throw new AccessDeniedException();
        } else return _Logged_Customer;
    }

    public void removeCustomer(AuthenticationDataDto customerDto) throws UserNotFoundException, AccessDeniedException, InvalidCustomerDataException {
        checkValidityOfAuthenticationData(customerDto);
        verifyLogin(customerDto.getUsername(), customerDto.getPassword());
        customerRepository.deleteById(findCustomerByName(customerDto.getUsername()).getCustomerID());
    }

    private void checkValidityOfAuthenticationData(AuthenticationDataDto customerDto) throws InvalidCustomerDataException {
        if (customerDto.getUsername() == null || customerDto.getUsername().isEmpty()
                || customerDto.getPassword() == null || customerDto.getPassword().length == 0) {
            throw new InvalidCustomerDataException();
        }
    }

    public void deleteUnauthenticatedCustomer(Long customerId) {
        customerRepository.deleteById(customerId);
    }

    public List<OrderDtoToCustomer> showMyOrders(AuthenticationDataDto customerDto) throws UserNotFoundException, AccessDeniedException, InvalidCustomerDataException {
        checkValidityOfAuthenticationData(customerDto);
        verifyLogin(customerDto.getUsername(), customerDto.getPassword());
        return orderMapper.mapToOrderDtoToCustomerList(orderService.findOrdersOfCustomer(customerDto.getUsername()));
    }
}
