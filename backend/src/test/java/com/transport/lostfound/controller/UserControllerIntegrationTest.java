package com.transport.lostfound.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transport.lostfound.dto.request.RegisterRequest;
import com.transport.lostfound.dto.request.UserProfileUpdateRequest;
import com.transport.lostfound.enums.Role;
import com.transport.lostfound.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReturnRecordRepository returnRecordRepository;

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private ItemMatchRepository itemMatchRepository;

    @Autowired
    private FoundItemRepository foundItemRepository;

    @Autowired
    private LostItemRepository lostItemRepository;

    @Autowired
    private UserRepository userRepository;

    private String passengerToken;

    @BeforeEach
    void setup() throws Exception {
        returnRecordRepository.deleteAll();
        claimRepository.deleteAll();
        itemMatchRepository.deleteAll();
        foundItemRepository.deleteAll();
        lostItemRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequest passReq = new RegisterRequest("Profile Tester", "tester@test.com", "Password@123", "+12345678", Role.PASSENGER);
        MvcResult res = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passReq)))
                .andExpect(status().isCreated())
                .andReturn();
        passengerToken = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("token").asText();
    }

    @Test
    void getProfile_Authenticated_ReturnsProfile() throws Exception {
        mockMvc.perform(get("/api/users/profile")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("tester@test.com"))
                .andExpect(jsonPath("$.data.fullName").value("Profile Tester"));
    }

    @Test
    void updateProfile_Authenticated_UpdatesAndReturnsProfile() throws Exception {
        UserProfileUpdateRequest update = new UserProfileUpdateRequest("Tester Updated", "+99998888");

        mockMvc.perform(put("/api/users/profile")
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Tester Updated"))
                .andExpect(jsonPath("$.data.phone").value("+99998888"));
    }

    @Test
    void getMyLostItems_Authenticated_ReturnsPagedList() throws Exception {
        mockMvc.perform(get("/api/users/my-lost-items")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @Test
    void getMyClaims_Authenticated_ReturnsPagedList() throws Exception {
        mockMvc.perform(get("/api/users/my-claims")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
