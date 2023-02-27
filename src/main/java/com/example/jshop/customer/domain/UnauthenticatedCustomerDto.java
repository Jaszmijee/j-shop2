package com.example.jshop.customer.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@NonNull
@AllArgsConstructor
public class UnauthenticatedCustomerDto {

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
