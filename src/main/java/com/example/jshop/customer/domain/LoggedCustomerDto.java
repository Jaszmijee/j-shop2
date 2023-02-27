package com.example.jshop.customer.domain;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class LoggedCustomerDto {

    private String userName;
    private String password;
    @NonNull
    private String firstName;
    @NonNull
    private String lastName;
    @NonNull
    private String email;
    @NonNull
    private String street;
    @NonNull
    private String houseNo;
    @NonNull
    private String flatNo;
    @NonNull
    private String zipCode;
    @NonNull
    private String city;
    @NonNull
    private String country;

}
