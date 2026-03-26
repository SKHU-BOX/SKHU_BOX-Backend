package com.example.skhubox.dto.auth;

import lombok.Getter;

@Getter
public class LoginRequest {
    private String studentNumber;
    private String password;
}