package com.microstock.media;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microstock.common.domain.DeviceType;
import com.microstock.masterdata.domain.CaptureDevice;
import com.microstock.masterdata.repository.CaptureDeviceRepository;
import com.microstock.support.AbstractIntegrationTest;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

/**
 * The most important tests in the platform: a User may never access, modify,
 * delete, or restore another User's media (BR-002, VAL-015, AC-SEC-001), while
 * an Administrator may access any (AC-SEC-002). Denials return 404, not 403.
 */
@AutoConfigureMockMvc
class MediaAuthorizationTest extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired CaptureDeviceRepository captureDeviceRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String aliceToken;
    private String bobToken;
    private String adminToken;
    private UUID aliceMediaId;
    private UUID bobMediaId;

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        UUID deviceId = ensureCaptureDevice(suffix);

        aliceToken = register("alice_" + suffix);
        bobToken = register("bob_" + suffix);
        adminToken = login("admin", "Admin123!");

        aliceMediaId = createMedia(aliceToken, "Alice photo", deviceId);
        bobMediaId = createMedia(bobToken, "Bob photo", deviceId);
    }

    @Test
    void userCannotReadAnotherUsersMedia() throws Exception {
        mockMvc.perform(get("/api/media/" + aliceMediaId).header("Authorization", bearer(bobToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCannotUpdateAnotherUsersMedia() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "title", "Hijacked", "mediaType", "PHOTO"));
        mockMvc.perform(put("/api/media/" + aliceMediaId)
                        .header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCannotDeleteAnotherUsersMedia() throws Exception {
        mockMvc.perform(delete("/api/media/" + aliceMediaId).header("Authorization", bearer(bobToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void userCannotRestoreAnotherUsersMedia() throws Exception {
        mockMvc.perform(post("/api/media/" + aliceMediaId + "/restore").header("Authorization", bearer(bobToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void ownerCanReadOwnMedia() throws Exception {
        mockMvc.perform(get("/api/media/" + aliceMediaId).header("Authorization", bearer(aliceToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Alice photo"));
    }

    @Test
    void adminCanReadAnyUsersMedia() throws Exception {
        mockMvc.perform(get("/api/media/" + aliceMediaId).header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk());
    }

    @Test
    void listReturnsOnlyOwnedMedia() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/media?size=100").header("Authorization", bearer(bobToken)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode content = objectMapper.readTree(result.getResponse().getContentAsString()).get("content");
        boolean hasBob = false;
        for (JsonNode node : content) {
            UUID id = UUID.fromString(node.get("id").asText());
            assertThat(id).isNotEqualTo(aliceMediaId); // never leaks Alice's media
            if (id.equals(bobMediaId)) hasBob = true;
        }
        assertThat(hasBob).isTrue();
    }

    @Test
    void softDeleteHidesFromListAndRestoreBringsItBack() throws Exception {
        // delete
        mockMvc.perform(delete("/api/media/" + bobMediaId).header("Authorization", bearer(bobToken)))
                .andExpect(status().isNoContent());
        // active list excludes it
        assertThat(listIds(bobToken, "/api/media?size=100")).doesNotContain(bobMediaId);
        // deleted list includes it
        assertThat(listIds(bobToken, "/api/media/deleted?size=100")).contains(bobMediaId);
        // restore
        mockMvc.perform(post("/api/media/" + bobMediaId + "/restore").header("Authorization", bearer(bobToken)))
                .andExpect(status().isOk());
        assertThat(listIds(bobToken, "/api/media?size=100")).contains(bobMediaId);
    }

    // ---- helpers ----

    private UUID ensureCaptureDevice(String suffix) {
        CaptureDevice device = new CaptureDevice(
                "Canon", "EOS " + suffix, "canon|eos " + suffix, DeviceType.INTERCHANGEABLE_LENS);
        return captureDeviceRepository.save(device).getId();
    }

    private String register(String username) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", username + "@example.com",
                "username", username,
                "password", "Password123",
                "confirmPassword", "Password123"));
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private String login(String identifier, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("identifier", identifier, "password", password));
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("accessToken").asText();
    }

    private UUID createMedia(String token, String title, UUID deviceId) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "title", title, "mediaType", "PHOTO", "captureDeviceId", deviceId.toString()));
        MvcResult result = mockMvc.perform(post("/api/media")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());
    }

    private java.util.List<UUID> listIds(String token, String url) throws Exception {
        MvcResult result = mockMvc.perform(get(url).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode content = objectMapper.readTree(result.getResponse().getContentAsString()).get("content");
        java.util.List<UUID> ids = new java.util.ArrayList<>();
        content.forEach(n -> ids.add(UUID.fromString(n.get("id").asText())));
        return ids;
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
