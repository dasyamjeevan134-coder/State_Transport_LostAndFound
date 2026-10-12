package com.transport.lostfound.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transport.lostfound.dto.request.*;
import com.transport.lostfound.enums.ClaimStatus;
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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClaimAndReturnWorkflowIntegrationTest {

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

        // 1. Register Staff
        RegisterRequest staffReq = new RegisterRequest("Transit Officer Jack", "jack@transport.gov", "Staff@12345", "+15551234", Role.STAFF);
        MvcResult staffRes = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(staffReq)))
                .andExpect(status().isCreated())
                .andReturn();
        staffToken = objectMapper.readTree(staffRes.getResponse().getContentAsString()).path("data").path("token").asText();

        // 2. Register Passenger
        RegisterRequest passReq = new RegisterRequest("Passenger Penny", "penny@passenger.com", "Pass@12345", "+15555678", Role.PASSENGER);
        MvcResult passRes = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passReq)))
                .andExpect(status().isCreated())
                .andReturn();
        passengerToken = objectMapper.readTree(passRes.getResponse().getContentAsString()).path("data").path("token").asText();
    }

    @Test
    void completeClaimAndReturnWorkflow_EndToEnd() throws Exception {
        // Step 1: Staff registers found item
        FoundItemCreateRequest foundReq = new FoundItemCreateRequest(
                "Canon DSLR Camera",
                ItemCategory.ELECTRONICS,
                "Black Canon EOS with 50mm lens",
                LocalDateTime.now().minusHours(2),
                TransportType.TRAIN,
                "Suburban Line 3",
                "Coach B Seat 14",
                null
        );

        MvcResult foundRes = mockMvc.perform(post("/api/found-items")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(foundReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("AVAILABLE"))
                .andReturn();

        long foundItemId = objectMapper.readTree(foundRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Step 2: Passenger submits claim for this found item
        ClaimCreateRequest claimReq = new ClaimCreateRequest(
                foundItemId,
                "I have the warranty card and serial number CN-98745 for this Canon camera."
        );

        MvcResult claimRes = mockMvc.perform(post("/api/claims")
                        .header("Authorization", "Bearer " + passengerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(claimReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.claimStatus").value("PENDING"))
                .andExpect(jsonPath("$.data.foundItemId").value(foundItemId))
                .andReturn();

        long claimId = objectMapper.readTree(claimRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Step 3: Passenger checks my claims
        mockMvc.perform(get("/api/users/my-claims")
                        .header("Authorization", "Bearer " + passengerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id").value(claimId))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        // Step 4: Staff reviews and approves the claim
        ClaimStatusUpdateRequest reviewReq = new ClaimStatusUpdateRequest(
                ClaimStatus.APPROVED,
                "Serial number and serial card successfully matched."
        );

        mockMvc.perform(put("/api/claims/" + claimId + "/status")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reviewReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.claimStatus").value("APPROVED"));

        // Step 5: Staff creates formal return handover record
        ReturnRecordCreateRequest returnReq = new ReturnRecordCreateRequest(
                foundItemId,
                claimId,
                LocalDateTime.now(),
                "Owner presented national ID; item successfully released."
        );

        MvcResult returnRes = mockMvc.perform(post("/api/returns")
                        .header("Authorization", "Bearer " + staffToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(returnReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.foundItemId").value(foundItemId))
                .andExpect(jsonPath("$.data.claimId").value(claimId))
                .andReturn();

        long returnRecordId = objectMapper.readTree(returnRes.getResponse().getContentAsString()).path("data").path("id").asLong();

        // Step 6: Verify found item is now RETURNED
        mockMvc.perform(get("/api/found-items/" + foundItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RETURNED"));

        // Step 7: Verify staff can view return record
        mockMvc.perform(get("/api/returns/" + returnRecordId)
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(returnRecordId));

        // Step 8: Check dashboard metrics
        mockMvc.perform(get("/api/dashboard/stats")
                        .header("Authorization", "Bearer " + staffToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalReturns").value(1))
                .andExpect(jsonPath("$.data.approvedClaims").value(1));
    }
}
