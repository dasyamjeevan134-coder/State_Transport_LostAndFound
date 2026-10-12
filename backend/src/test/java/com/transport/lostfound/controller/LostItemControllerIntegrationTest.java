package com.transport.lostfound.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transport.lostfound.dto.request.LostItemCreateRequest;
import com.transport.lostfound.dto.request.LostItemUpdateRequest;
import com.transport.lostfound.dto.request.RegisterRequest;
import com.transport.lostfound.enums.ItemCategory;
import com.transport.lostfound.enums.Role;
import com.transport.lostfound.enums.TransportType;
import com.transport.lostfound.repository.ItemMatchRepository;
import com.transport.lostfound.repository.LostItemRepository;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LostItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private com.transport.lostfound.repository.ReturnRecordRepository returnRecordRepository;

    @Autowired
    private com.transport.lostfound.repository.ClaimRepository claimRepository;

    @Autowired
    private com.transport.lostfound.repository.FoundItemRepository foundItemRepository;

    @Autowired
    private LostItemRepository lostItemRepository;

    @Autowired
    private ItemMatchRepository itemMatchRepository;

    @Autowired
    private UserRepository userRepository;

    private String passengerToken;
    private String otherPassengerToken;

    @BeforeEach
    void setup() throws Exception {
        returnRecordRepository.deleteAll();
        claimRepository.deleteAll();
        itemMatchRepository.deleteAll();
        foundItemRepository.deleteAll();
        lostItemRepository.deleteAll();
        userRepository.deleteAll();

        // Register user 1
        RegisterRequest user1 = new RegisterRequest("User One", "user1@test.com", "Password@123", "+12345678", Role.PASSENGER);
        MvcResult res1 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andExpect(status().isCreated())
                .andReturn();
        passengerToken = objectMapper.readTree(res1.getResponse().getContentAsString())
                .path("data").path("token").asText();

        // Register user 2
        RegisterRequest user2 = new RegisterRequest("User Two", "user2@test.com", "Password@123", "+87654321", Role.PASSENGER);
        MvcResult res2 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andExpect(status().isCreated())
                .andReturn();
        otherPassengerToken = objectMapper.readTree(res2.getResponse().getContentAsString())
                .path("data").path("token").asText();
    }

    @Test
    void createLostItem_Authenticated_Returns201() throws Exception {
        LostItemCreateRequest request = new LostItemCreateRequest(
                "MacBook Air M2",
                ItemCategory.ELECTRONICS,
                "Silver MacBook with stickers",
                LocalDateTime.now().minusHours(3),
                TransportType.METRO,
                "Line 1",
                "Central Station",
                "http://example.com/photo.jpg"
        );

        mockMvc.perform(post("/api/lost-items")
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.itemName").value("MacBook Air M2"))
                .andExpect(jsonPath("$.data.category").value("ELECTRONICS"))
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.data.passengerName").value("User One"));
    }

    @Test
    void createLostItem_Unauthenticated_Returns401() throws Exception {
        LostItemCreateRequest request = new LostItemCreateRequest(
                "MacBook Air M2",
                ItemCategory.ELECTRONICS,
                "Silver",
                LocalDateTime.now().minusHours(3),
                TransportType.METRO,
                "Line 1",
                "Central Station",
                null
        );

        mockMvc.perform(post("/api/lost-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getLostItems_PublicAccess_ReturnsPagedItems() throws Exception {
        // Create an item first
        LostItemCreateRequest request = new LostItemCreateRequest(
                "Student ID Card",
                ItemCategory.DOCUMENTS_AND_ID,
                "University ID",
                LocalDateTime.now().minusDays(1),
                TransportType.BUS,
                "Route 22",
                "Campus Gate",
                null
        );
        mockMvc.perform(post("/api/lost-items")
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Call without authentication header
        mockMvc.perform(get("/api/lost-items")
                        .param("keyword", "Student")
                        .param("category", "DOCUMENTS_AND_ID"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].itemName").value("Student ID Card"))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.last").value(true));
    }

    @Test
    void getLostItemById_NotFound_Returns404() throws Exception {
        mockMvc.perform(get("/api/lost-items/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateLostItem_UnauthorizedUser_Returns403() throws Exception {
        // Create item with user 1
        LostItemCreateRequest request = new LostItemCreateRequest(
                "Leather Jacket",
                ItemCategory.CLOTHING_AND_ACCESSORIES,
                "Brown size L",
                LocalDateTime.now().minusHours(4),
                TransportType.TRAIN,
                "Express 10",
                "Platform 2",
                null
        );
        MvcResult res = mockMvc.perform(post("/api/lost-items")
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        long itemId = objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // Attempt update with user 2 token
        LostItemUpdateRequest update = new LostItemUpdateRequest();
        update.setItemName("Hacked Jacket");

        mockMvc.perform(put("/api/lost-items/" + itemId)
                        .header("Authorization", "Bearer " + otherPassengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void deleteLostItem_Owner_Returns200() throws Exception {
        LostItemCreateRequest request = new LostItemCreateRequest(
                "Blue Umbrella",
                ItemCategory.OTHER,
                "Foldable",
                LocalDateTime.now().minusHours(2),
                TransportType.TRAM,
                "Route 5",
                "South Station",
                null
        );
        MvcResult res = mockMvc.perform(post("/api/lost-items")
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        long itemId = objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(delete("/api/lost-items/" + itemId)
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify it is gone
        mockMvc.perform(get("/api/lost-items/" + itemId))
                .andExpect(status().isNotFound());
    }
}
