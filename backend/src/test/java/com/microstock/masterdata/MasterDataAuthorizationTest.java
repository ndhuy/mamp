package com.microstock.masterdata;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microstock.support.AbstractIntegrationTest;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Master data is global and admin-managed (BR-013): only ADMIN may write, any
 * authenticated user may read, and unauthenticated callers are rejected.
 */
@AutoConfigureMockMvc
class MasterDataAuthorizationTest extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String userToken;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        userToken = register("user_" + UUID.randomUUID().toString().substring(0, 8));
        adminToken = login("admin", "Admin123!");
    }

    @Test
    void regularUserCannotCreateCaptureDevice() throws Exception {
        mockMvc.perform(post("/api/capture-devices")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(devicePayload("Nikon", "Z9 " + suffix())))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateCaptureDevice() throws Exception {
        mockMvc.perform(post("/api/capture-devices")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(devicePayload("Sony", "A7 " + suffix())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void duplicateBrandModelIsRejected() throws Exception {
        String model = "R5 " + suffix();
        mockMvc.perform(post("/api/capture-devices")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(devicePayload("Canon", model)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/capture-devices")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(devicePayload("canon", "  " + model.toLowerCase() + " "))) // normalized dup
                .andExpect(status().isConflict());
    }

    @Test
    void authenticatedUserCanListDevices() throws Exception {
        mockMvc.perform(get("/api/capture-devices").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());
    }

    @Test
    void unauthenticatedCannotListDevices() throws Exception {
        mockMvc.perform(get("/api/capture-devices"))
                .andExpect(status().isUnauthorized());
    }

    // ---- helpers ----

    private String suffix() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String devicePayload(String brand, String model) throws Exception {
        return objectMapper.writeValueAsString(Map.of(
                "brand", brand, "model", model, "deviceType", "INTERCHANGEABLE_LENS"));
    }

    private String register(String username) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", username + "@example.com", "username", username,
                "password", "Password123", "confirmPassword", "Password123"));
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String login(String identifier, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("identifier", identifier, "password", password));
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }
}
