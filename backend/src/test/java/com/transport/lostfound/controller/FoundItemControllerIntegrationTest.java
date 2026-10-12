package com.transport.lostfound.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transport.lostfound.dto.request.FoundItemCreateRequest;
import com.transport.lostfound.dto.request.FoundItemUpdateRequest;
import com.transport.lostfound.dto.request.RegisterRequest;
import com.transport.lostfound.enums.FoundItemStatus;
import com.transport.lostfound.enums.ItemCategory;
import com.transport.lostfound.enums.Role;
import com.transport.lostfound.enums.TransportType;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FoundItemControllerIntegrationTest {

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

    private String staffToken;
    private String passengerToken;

    @BeforeEach
    void setup() throws Exception {
        returnRecordRepository.deleteAll();
        claimRepository.deleteAll();
        itemMatchRepository.deleteAll();
        foundItemRepository.deleteAll();
        lostItemRepository.deleteAll();
        userRepository.deleteAll();

        // Register staff
        RegisterRequest staffReq = new RegisterRequest("Staff Jane", "jane.staff@transport.gov", "Staff@123", "+12345678", Role.STAFF);
        MvcResult res1 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(staffReq)))
                .andExpect(status().isCreated())
                .andReturn();
        staffToken = objectMapper.readTree(res1.getResponse().getContentAsString()).path("data").path("token").asText();

        // Register passenger
        RegisterRequest passReq = new RegisterRequest("Passenger Paul", "paul@passenger.com", "Pass@123", "+87654321", Role.PASSENGER);
        MvcResult res2 = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passReq)))
                .andExpect(status().isCreated())
                .andReturn();
        passengerToken = objectMapper.readTree(res2.getResponse().getContentAsString()).path("data").path("token").asText();
    }

    @Test
    void createFoundItem_StaffUser_Returns201() throws Exception {
        FoundItemCreateRequest request = new FoundItemCreateRequest(
                "Kindle Paperwhite",
                ItemCategory.ELECTRONICS,
                "Black e-reader in green case",
                LocalDateTime.now().minusHours(1),
                TransportType.BUS,
                "Bus 101",
                "Seat 12B",
                null
        );

        mockMvc.perform(post("/api/found-items")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.itemName").value("Kindle Paperwhite"))
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.staffName").value("Staff Jane"));
    }

    @Test
    void createFoundItem_PassengerUser_Returns403() throws Exception {
        FoundItemCreateRequest request = new FoundItemCreateRequest(
                "Kindle Paperwhite",
                ItemCategory.ELECTRONICS,
                "Black e-reader",
                LocalDateTime.now().minusHours(1),
                TransportType.BUS,
                "Bus 101",
                "Seat 12B",
                null
        );

        mockMvc.perform(post("/api/found-items")
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getFoundItems_PublicAccess_ReturnsPagedItems() throws Exception {
        FoundItemCreateRequest request = new FoundItemCreateRequest(
                "Ray-Ban Sunglasses",
                ItemCategory.CLOTHING_AND_ACCESSORIES,
                "Aviator style in black case",
                LocalDateTime.now().minusHours(2),
                TransportType.METRO,
                "Red Line",
                "Terminal Hall",
                null
        );
        mockMvc.perform(post("/api/found-items")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/found-items")
                        .param("category", "CLOTHING_AND_ACCESSORIES")
                        .param("status", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].itemName").value("Ray-Ban Sunglasses"));
    }

    @Test
    void updateFoundItem_StaffUser_Returns200() throws Exception {
        FoundItemCreateRequest create = new FoundItemCreateRequest(
                "Silver Watch",
                ItemCategory.JEWELRY,
                "Casio digital",
                LocalDateTime.now().minusHours(1),
                TransportType.TRAM,
                "Line 4",
                "Car 2",
                null
        );
        MvcResult res = mockMvc.perform(post("/api/found-items")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();

        FoundItemUpdateRequest update = new FoundItemUpdateRequest();
        update.setItemName("Casio Silver Vintage Watch");

        mockMvc.perform(put("/api/found-items/" + id)
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.itemName").value("Casio Silver Vintage Watch"));
    }

    @Test
    void deleteFoundItem_StaffUser_Returns200() throws Exception {
        FoundItemCreateRequest create = new FoundItemCreateRequest(
                "Water Bottle",
                ItemCategory.OTHER,
                "Hydro Flask blue",
                LocalDateTime.now().minusHours(1),
                TransportType.BUS,
                "Bus 80",
                "Front rack",
                null
        );
        MvcResult res = mockMvc.perform(post("/api/found-items")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(create)))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("id").asLong();

        mockMvc.perform(delete("/api/found-items/" + id)
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/found-items/" + id))
                .andExpect(status().isNotFound());
    }
}
