package com.foodcity.backend.payload;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class SignUpRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters")
    private String password;

    @NotBlank(message = "Email is required")
    @Size(max = 40, message = "Email must not exceed 40 characters")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "First name is required")
    @Size(max = 40, message = "First name must not exceed 40 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 40, message = "Last name must not exceed 40 characters")
    private String lastName;

    // Optional fields
    private String phone;
    private String address;
    private String profileImage;
}