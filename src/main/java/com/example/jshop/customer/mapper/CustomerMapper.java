package com.example.jshop.customer.mapper;

import com.example.jshop.customer.domain.Address;
import com.example.jshop.customer.domain.LoggedCustomer;
import com.example.jshop.customer.domain.LoggedCustomerDto;
import org.springframework.stereotype.Service;

@Service
public class CustomerMapper {

    public LoggedCustomer mapToCustomer(LoggedCustomerDto loggedCustomerDto){
       return new LoggedCustomer(
               loggedCustomerDto.getUserName(),
               loggedCustomerDto.getPassword().toCharArray(),
               loggedCustomerDto.getFirstName(),
               loggedCustomerDto.getLastName(),
               loggedCustomerDto.getEmail(),
               new Address(
                       loggedCustomerDto.getStreet(),
                       loggedCustomerDto.getHouseNo(),
                       loggedCustomerDto.getFlatNo(),
                       loggedCustomerDto.getZipCode(),
                       loggedCustomerDto.getCity(),
                       loggedCustomerDto.getCountry()
               )
       );
    }
}
