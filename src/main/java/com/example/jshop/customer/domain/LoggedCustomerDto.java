package com.example.jshop.customer.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoggedCustomerDto {

    private String userName;
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String street;
    private String houseNo;
    private String flatNo;
    private String zipCode;
    private String city;
    private String country;
}
