package com.transport.lostfound.service;

import com.transport.lostfound.dto.request.LoginRequest;
import com.transport.lostfound.dto.request.RegisterRequest;
import com.transport.lostfound.dto.request.UserProfileUpdateRequest;
import com.transport.lostfound.dto.response.AuthResponse;
import com.transport.lostfound.dto.response.UserProfileResponse;
import com.transport.lostfound.entity.User;
import com.transport.lostfound.enums.Role;
import com.transport.lostfound.exception.BadRequestException;
import com.transport.lostfound.exception.DuplicateResourceException;
import com.transport.lostfound.exception.ResourceNotFoundException;
import com.transport.lostfound.repository.UserRepository;
import com.transport.lostfound.security.JwtTokenProvider;
import com.transport.lostfound.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("John Doe", "john@example.com", "encodedPassword", "+1234567890", Role.PASSENGER);
        sampleUser.setId(1L);
    }

    @Test
    void register_Success() {
        RegisterRequest request = new RegisterRequest("John Doe", "john@example.com", "password123", "+1234567890", Role.PASSENGER);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(tokenProvider.generateToken(any(UserPrincipal.class))).thenReturn("dummy.jwt.token");
        when(tokenProvider.getExpirationMs()).thenReturn(86400000L);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("dummy.jwt.token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("John Doe", response.getUser().getFullName());
        assertEquals("john@example.com", response.getUser().getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_DuplicateEmail_ThrowsException() {
        RegisterRequest request = new RegisterRequest("Jane Doe", "john@example.com", "password123", "+1234567890", null);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("john@example.com", "password123");
        UserPrincipal principal = UserPrincipal.create(sampleUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.generateToken(principal)).thenReturn("valid.jwt.token");
        when(tokenProvider.getExpirationMs()).thenReturn(86400000L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("valid.jwt.token", response.getToken());
        assertEquals("john@example.com", response.getUser().getEmail());
    }

    @Test
    void login_InvalidCredentials_ThrowsException() {
        LoginRequest request = new LoginRequest("john@example.com", "wrongpassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void updateProfile_Success() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest("John Updated", "+9876543210");
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        UserProfileResponse response = authService.updateProfile(1L, request);

        assertNotNull(response);
        assertEquals("John Updated", sampleUser.getFullName());
        assertEquals("+9876543210", sampleUser.getPhone());
    }

    @Test
    void updateProfile_InvalidCurrentPassword_ThrowsException() {
        UserProfileUpdateRequest request = new UserProfileUpdateRequest();
        request.setCurrentPassword("wrongCurrent");
        request.setNewPassword("newPassword123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongCurrent", sampleUser.getPasswordHash())).thenReturn(false);

        assertThrows(BadRequestException.class, () -> authService.updateProfile(1L, request));
    }
}
