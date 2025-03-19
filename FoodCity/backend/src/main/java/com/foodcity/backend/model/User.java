package com.foodcity.backend.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Set;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    private String firstName;
    private String lastName;
    private Set<Role> roles;
    private boolean enabled = true;
    private String profileImage;
    private String lastLoginDate;

    public enum Role {
        ROLE_ADMIN,
        ROLE_CASHIER,
        ROLE_INVENTORY_MANAGER,
        ROLE_EMPLOYEE
    }
}