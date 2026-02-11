package com.ecommeceapp.ecommerceapp.auth.dto;


import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String name;

    @Email @NotBlank
    private String email;

    @Size(min = 6) @NotBlank
    private String password;
}
