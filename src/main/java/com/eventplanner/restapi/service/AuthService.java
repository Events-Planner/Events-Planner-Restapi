package com.eventplanner.restapi.service;

import com.eventplanner.restapi.dto.ApiResponse;
import com.eventplanner.restapi.dto.AuthResponse;
import com.eventplanner.restapi.dto.LoginRequest;
import com.eventplanner.restapi.dto.RegisterRequest;
import com.eventplanner.restapi.entity.User;
import com.eventplanner.restapi.repository.UserRepository;
import com.eventplanner.restapi.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // --- REGISTRATION LOGIC ---
    public ApiResponse registerUser(RegisterRequest request) {
        // 1. Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return new ApiResponse(false, "Email is already registered!");
        }

        // 2. Create and populate new User object
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Encrypt password

        // 3. Save user to database
        userRepository.save(user);

        return new ApiResponse(true, "User registered successfully!");
    }

    // --- LOGIN LOGIC ---
    public AuthResponse loginUser(LoginRequest request) {
        // 1. Fetch user from database
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = userOptional.get();

        // 2. Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        // 3. Generate JWT Token
        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(
                token,
                "Login successful",
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        );
    }
}