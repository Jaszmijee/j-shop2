package com.example.jshop.customer.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@NonNull
@AllArgsConstructor
public class AuthenticationDataDto {

    private String username;
    private char[] password;
}
