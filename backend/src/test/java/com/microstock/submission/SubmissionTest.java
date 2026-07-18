package com.microstock.submission;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microstock.support.AbstractIntegrationTest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/** Submission tracking rules: target sites, ownership, rejection/category/date validation. */
@AutoConfigureMockMvc
class SubmissionTest extends AbstractIntegrationTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String aliceToken;
    private String bobToken;
    private String adminToken;
    private UUID aliceMediaId;
    private UUID iStockId;      // categoriesRequired = 0
    private UUID shutterId;     // categoriesRequired = 1

    @BeforeEach
    void setUp() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        aliceToken = register("alice_" + suffix);
        bobToken = register("bob_" + suffix);
        adminToken = login("admin", "Admin123!");
        UUID deviceId = firstDeviceId(aliceToken);
        iStockId = siteIdByName(aliceToken, "iStock");
        shutterId = siteIdByName(aliceToken, "Shutterstock");
        aliceMediaId = createMedia(aliceToken, deviceId);
    }

    @Test
    void addingTargetSiteCreatesNotSubmittedRecord() throws Exception {
        mockMvc.perform(post("/api/media/" + aliceMediaId + "/submissions")
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("stockSiteId", iStockId.toString()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("NOT_SUBMITTED"));
    }

    @Test
    void sameSiteCannotBeAddedTwice() throws Exception {
        addTarget(aliceToken, aliceMediaId, iStockId);
        mockMvc.perform(post("/api/media/" + aliceMediaId + "/submissions")
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("stockSiteId", iStockId.toString()))))
                .andExpect(status().isConflict());
    }

    @Test
    void otherUserCannotListSubmissionsForForeignMedia() throws Exception {
        mockMvc.perform(get("/api/media/" + aliceMediaId + "/submissions").header("Authorization", bearer(bobToken)))
                .andExpect(status().isNotFound());
    }

    @Test
    void otherUserCannotAddTargetSiteToForeignMedia() throws Exception {
        mockMvc.perform(post("/api/media/" + aliceMediaId + "/submissions")
                        .header("Authorization", bearer(bobToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("stockSiteId", iStockId.toString()))))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectedStatusRequiresCategoryAndDetail() throws Exception {
        UUID submissionId = addTarget(aliceToken, aliceMediaId, iStockId);
        Map<String, Object> body = new HashMap<>();
        body.put("status", "REJECTED"); // no rejection category/detail
        mockMvc.perform(put("/api/submissions/" + submissionId)
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reviewedDateCannotPrecedeSubmittedDate() throws Exception {
        UUID submissionId = addTarget(aliceToken, aliceMediaId, iStockId);
        Map<String, Object> body = new HashMap<>();
        body.put("status", "ACCEPTED");
        body.put("submittedDate", "2026-05-10");
        body.put("reviewedDate", "2026-05-01"); // before submitted
        mockMvc.perform(put("/api/submissions/" + submissionId)
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void siteRequiringCategoryRejectsUpdateWithoutPrimary_thenAcceptsWithIt() throws Exception {
        UUID submissionId = addTarget(aliceToken, aliceMediaId, shutterId);
        // Missing required primary category → 400
        Map<String, Object> noCat = new HashMap<>();
        noCat.put("status", "SUBMITTED");
        mockMvc.perform(put("/api/submissions/" + submissionId)
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(noCat)))
                .andExpect(status().isBadRequest());
        // Admin creates a category on Shutterstock
        UUID categoryId = createCategory(adminToken, shutterId, "People " + UUID.randomUUID());
        Map<String, Object> withCat = new HashMap<>();
        withCat.put("status", "SUBMITTED");
        withCat.put("primaryCategoryId", categoryId.toString());
        withCat.put("submittedDate", "2026-05-10");
        mockMvc.perform(put("/api/submissions/" + submissionId)
                        .header("Authorization", bearer(aliceToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(withCat)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.primaryCategory.id").value(categoryId.toString()));
    }

    // ---- helpers ----

    private UUID addTarget(String token, UUID mediaId, UUID siteId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/media/" + mediaId + "/submissions")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("stockSiteId", siteId.toString()))))
                .andExpect(status().isCreated()).andReturn();
        return UUID.fromString(json(result).get("id").asText());
    }

    private UUID createCategory(String token, UUID siteId, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/stock-sites/" + siteId + "/categories")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("name", name))))
                .andExpect(status().isCreated()).andReturn();
        return UUID.fromString(json(result).get("id").asText());
    }

    private UUID firstDeviceId(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/capture-devices").header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andReturn();
        return UUID.fromString(json(result).get(0).get("id").asText());
    }

    private UUID siteIdByName(String token, String name) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/stock-sites").header("Authorization", bearer(token)))
                .andExpect(status().isOk()).andReturn();
        for (JsonNode node : json(result)) {
            if (name.equals(node.get("name").asText())) {
                return UUID.fromString(node.get("id").asText());
            }
        }
        throw new AssertionError("Stock site not found: " + name);
    }

    private UUID createMedia(String token, UUID deviceId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/media")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "title", "Media", "mediaType", "PHOTO", "captureDeviceId", deviceId.toString()))))
                .andExpect(status().isCreated()).andReturn();
        return UUID.fromString(json(result).get("id").asText());
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private String register(String username) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", username + "@example.com", "username", username,
                                "password", "Password123", "confirmPassword", "Password123"))))
                .andExpect(status().isCreated()).andReturn();
        return json(result).get("accessToken").asText();
    }

    private String login(String identifier, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("identifier", identifier, "password", password))))
                .andExpect(status().isOk()).andReturn();
        return json(result).get("accessToken").asText();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }
}
