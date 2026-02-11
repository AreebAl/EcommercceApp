package com.ecommeceapp.ecommerceapp.user.controller;


import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/me")
    public String me() {
        return "You are authenticated ✅";
    }
}
