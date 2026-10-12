package com.transport.lostfound.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transport.lostfound.dto.request.RegisterRequest;
import com.transport.lostfound.enums.Role;
import com.transport.lostfound.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileUploadControllerIntegrationTest {

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

    private String userToken;

    @BeforeEach
    void setup() throws Exception {
        returnRecordRepository.deleteAll();
        claimRepository.deleteAll();
        itemMatchRepository.deleteAll();
        foundItemRepository.deleteAll();
        lostItemRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequest userReq = new RegisterRequest("Uploader User", "uploader@test.com", "Password@123", "+12345678", Role.PASSENGER);
        MvcResult res = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userReq)))
                .andExpect(status().isCreated())
                .andReturn();
        userToken = objectMapper.readTree(res.getResponse().getContentAsString()).path("data").path("token").asText();
    }

    @Test
    void uploadAndDownloadImage_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-photo.jpg",
                "image/jpeg",
                "fake image content".getBytes()
        );

        MvcResult uploadResult = mockMvc.perform(multipart("/api/uploads")
                        .file(file)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fileName").isString())
                .andExpect(jsonPath("$.data.fileUrl").isString())
                .andReturn();

        String fileName = objectMapper.readTree(uploadResult.getResponse().getContentAsString())
                .path("data").path("fileName").asText();

        // Download/view the file without auth (public photo view)
        mockMvc.perform(get("/api/uploads/" + fileName))
                .andExpect(status().isOk())
                .andExpect(content().bytes("fake image content".getBytes()));
    }

    @Test
    void uploadInvalidFileType_Returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "not an image".getBytes()
        );

        mockMvc.perform(multipart("/api/uploads")
                        .file(file)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
