package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    @Autowired
    private UserService userService;

    // 模拟 token 生成
    public String authenticate(String username, String password) {
        if (userService.validateUser(username, password)) {
            return "mock-jwt-token"; // 模拟 JWT Token
        } else {
            return null; // 认证失败
        }
    }
}