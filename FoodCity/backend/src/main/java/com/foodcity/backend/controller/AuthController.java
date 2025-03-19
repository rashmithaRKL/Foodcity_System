package com.foodcity.backend.controller;

import com.foodcity.backend.model.User;
import com.foodcity.backend.payload.ApiResponse;
import com.foodcity.backend.payload.JwtAuthenticationResponse;
import com.foodcity.backend.payload.LoginRequest;
import com.foodcity.backend.payload.SignUpRequest;
import com.foodcity.backend.security.JwtTokenProvider;
import com.foodcity.backend.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(),
                loginRequest.getPassword()
            )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userDetailsService.existsByUsername(signUpRequest.getUsername())) {
            return new ResponseEntity<>(
                new ApiResponse(false, "Username is already taken!"),
                HttpStatus.BAD_REQUEST
            );
        }

        if (userDetailsService.existsByEmail(signUpRequest.getEmail())) {
            return new ResponseEntity<>(
                new ApiResponse(false, "Email Address already in use!"),
                HttpStatus.BAD_REQUEST
            );
        }

        // Creating user's account
        User user = new User();
        user.setUsername(signUpRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        user.setEmail(signUpRequest.getEmail());
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        
        // Set default role as ROLE_EMPLOYEE
        user.setRoles(new HashSet<>(Collections.singletonList(User.Role.ROLE_EMPLOYEE)));
        
        User result = userDetailsService.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(result.getUsername()).toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse(true, "User registered successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new ApiResponse(true, "Logged out successfully"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (tokenProvider.validateToken(token)) {
                String userId = tokenProvider.getUserIdFromJWT(token);
                String newToken = tokenProvider.generateTokenFromUserId(userId);
                return ResponseEntity.ok(new JwtAuthenticationResponse(newToken));
            }
        }
        return ResponseEntity.badRequest().body(new ApiResponse(false, "Invalid refresh token"));
    }
}