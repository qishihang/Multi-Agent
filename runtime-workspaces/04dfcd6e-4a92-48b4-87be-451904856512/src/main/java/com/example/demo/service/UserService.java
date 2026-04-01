package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class UserService {

    // 模拟用户校验逻辑
    public boolean validateUser(String username, String password) {
        // 简单模拟：仅当用户名为 "admin" 且密码为 "123456" 时通过
        return "admin".equals(username) && "123456".equals(password);
    }
}