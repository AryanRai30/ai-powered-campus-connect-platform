package com.campusconnect.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CorsAndAuthSecurityIntegrationTest {

    private static final String PROD_VERCEL_ORIGIN = "https://ai-powered-campus-connect-platform.vercel.app";
    private static final String UNAUTHORIZED_ORIGIN = "https://malicious-unauthorized-website.com";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("CORS Preflight: Should permit OPTIONS /api/auth/login from production Vercel origin with required headers")
    void testCorsPreflightForProductionOriginOnLogin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                .header("Origin", PROD_VERCEL_ORIGIN)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Authorization, Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", PROD_VERCEL_ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("POST")))
                .andExpect(header().string("Access-Control-Allow-Methods", containsString("OPTIONS")))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    @Test
    @DisplayName("CORS Preflight: Should permit OPTIONS /api/auth/register from production Vercel origin")
    void testCorsPreflightForProductionOriginOnRegister() throws Exception {
        mockMvc.perform(options("/api/auth/register")
                .header("Origin", PROD_VERCEL_ORIGIN)
                .header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", PROD_VERCEL_ORIGIN))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    @DisplayName("CORS Preflight: Should reject or omit Access-Control-Allow-Origin for unauthorized origin")
    void testCorsPreflightRejectsUnauthorizedOrigin() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                .header("Origin", UNAUTHORIZED_ORIGIN)
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Endpoint Path: /api/auth/login is the mapped authentication route, not 404")
    void testAuthLoginPathIsMapped() throws Exception {
        // Sending invalid/empty payload to /api/auth/login should be handled by AuthController (returns 400 Bad Request), NOT 404
        mockMvc.perform(post("/api/auth/login")
                .header("Origin", PROD_VERCEL_ORIGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Access-Control-Allow-Origin", PROD_VERCEL_ORIGIN));
    }

    @Test
    @DisplayName("Endpoint Path: Duplicate /api/api/auth/login is not permitted as public auth and requires full authentication (401)")
    void testDuplicateApiPathReturnsUnauthorized() throws Exception {
        // The duplicated path /api/api/auth/login does NOT match the public /api/auth/** permitAll rule,
        // so Spring Security immediately rejects unauthenticated login requests with 401 Unauthorized.
        mockMvc.perform(post("/api/api/auth/login")
                .header("Origin", PROD_VERCEL_ORIGIN)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
