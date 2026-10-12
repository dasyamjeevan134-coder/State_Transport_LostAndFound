package com.transport.lostfound.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transport.lostfound.dto.request.LoginRequest;
import com.transport.lostfound.dto.request.RegisterRequest;
import com.transport.lostfound.enums.Role;
import com.transport.lostfound.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.transport.lostfound.repository.ReturnRecordRepository returnRecordRepository;

    @Autowired
    private com.transport.lostfound.repository.ClaimRepository claimRepository;

    @Autowired
    private com.transport.lostfound.repository.ItemMatchRepository itemMatchRepository;

    @Autowired
    private com.transport.lostfound.repository.FoundItemRepository foundItemRepository;

    @Autowired
    private com.transport.lostfound.repository.LostItemRepository lostItemRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp() {
        returnRecordRepository.deleteAll();
        claimRepository.deleteAll();
        itemMatchRepository.deleteAll();
        foundItemRepository.deleteAll();
        lostItemRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void register_ValidPayload_Returns201AndToken() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Jane Doe",
                "jane@example.com",
                "Password@123",
                "+1234567890",
                Role.PASSENGER
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.user.email").value("jane@example.com"))
                .andExpect(jsonPath("$.data.user.fullName").value("Jane Doe"))
                .andExpect(jsonPath("$.data.user.role").value("PASSENGER"));
    }

    @Test
    void register_DuplicateEmail_Returns409() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Jane Doe",
                "jane@example.com",
                "Password@123",
                "+1234567890",
                Role.PASSENGER
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second registration with same email
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("already registered")));
    }

    @Test
    void register_InvalidEmail_Returns400() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Jane Doe",
                "not-an-email",
                "123",
                "+1",
                Role.PASSENGER
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_ValidCredentials_Returns200AndToken() throws Exception {
        RegisterRequest reg = new RegisterRequest(
                "User Login",
                "user@login.com",
                "Secret@123",
                "+1234567890",
                Role.PASSENGER
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest("user@login.com", "Secret@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isString())
                .andExpect(jsonPath("$.data.user.email").value("user@login.com"));
    }

    @Test
    void login_InvalidPassword_Returns401() throws Exception {
        RegisterRequest reg = new RegisterRequest(
                "User Login",
                "user@login.com",
                "Secret@123",
                "+1234567890",
                Role.PASSENGER
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest("user@login.com", "WrongPassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message", containsString("Invalid email or password")));
    }

    @Test
    void getCurrentUser_WithValidToken_Returns200() throws Exception {
        RegisterRequest reg = new RegisterRequest(
                "Me User",
                "me@example.com",
                "Password@123",
                "+1234567890",
                Role.PASSENGER
        );

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn();

        String token = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("token").asText();
        assertNotNull(token);

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("me@example.com"));
    }

    @Test
    void getCurrentUser_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }
}
