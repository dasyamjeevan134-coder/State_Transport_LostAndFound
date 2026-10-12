package com.transport.lostfound.controller;

import com.transport.lostfound.dto.request.LoginRequest;
import com.transport.lostfound.dto.request.RegisterRequest;
import com.transport.lostfound.dto.response.ApiResponse;
import com.transport.lostfound.dto.response.AuthResponse;
import com.transport.lostfound.dto.response.UserProfileResponse;
import com.transport.lostfound.security.UserPrincipal;
import com.transport.lostfound.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and token session verification")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register new account (Passenger, Staff, or Admin)")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and return JWT bearer token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) {
            throw new com.transport.lostfound.exception.UnauthorizedException("Authentication token is missing or invalid");
        }
        UserProfileResponse response = authService.getProfile(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Current user profile retrieved successfully", response));
    }
}
