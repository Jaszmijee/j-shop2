package com.example.jshop.customer.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticationDataDto {

    private String username;
    private char[] password;
}
