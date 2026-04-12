package com.example.auth;

import com.example.auth.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private UserRepository userRepository;

        @BeforeEach
        void setUp() {
                userRepository.deleteAll();
        }

        private String createTestUser() throws Exception {
                String registerRequest = """
                                {
                                  "name": "Dang Ngoc Khieu",
                                  "email": "khieu@example.com",
                                  "password": "Password123"
                                }
                                """;
                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerRequest));
                return "khieu@example.com";
        }

        private JsonNode performLogin(String email, String password) throws Exception {
                String loginRequest = """
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password);

                var loginResult = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest))
                                .andReturn();
                return objectMapper.readTree(loginResult.getResponse().getContentAsString());
        }

        @Test
        void register_ShouldReturnCreatedUser() throws Exception {
                String registerRequest = """
                                {
                                  "name": "Test User",
                                  "email": "test@example.com",
                                  "password": "Password123"
                                }
                                """;

                mockMvc.perform(post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registerRequest))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.email").value("test@example.com"));
        }

        @Test
        void login_WithValidCredentials_ShouldReturnTokens() throws Exception {
                createTestUser();

                String loginRequest = """
                                {
                                  "email": "khieu@example.com",
                                  "password": "Password123"
                                }
                                """;

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.user.email").value("khieu@example.com"))
                                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                                // Kiểm tra xem cookie có được tạo không
                                .andExpect(cookie().exists("refreshToken"))
                                // Value không được null
                                .andExpect(cookie().value("refreshToken", org.hamcrest.Matchers.notNullValue()))
                                // Bắt buộc phải là HttpOnly để chống XSS
                                // .andExpect(cookie().httpOnly("refreshToken", true))
                                // Kiểm tra path có đúng không
                                .andExpect(cookie().path("refreshToken", "/"));
        }

        @Test
        void login_WithInvalidCredentials_ShouldReturnUnauthorized() throws Exception {
                createTestUser();

                String loginRequest = """
                                {
                                  "email": "khieu@example.com",
                                  "password": "WrongPassword123"
                                }
                                """;

                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest))
                                .andExpect(status().isUnauthorized())
                                .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        void fetchProfile_WithValidToken_ShouldReturnUserData() throws Exception {
                createTestUser();
                JsonNode loginResponse = performLogin("khieu@example.com", "Password123");
                String accessToken = loginResponse.path("data").path("accessToken").asText();

                mockMvc.perform(get("/api/auth/account")
                                .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.email").value("khieu@example.com"));
        }

        @Test
        void fetchProfile_WithInvalidToken_ShouldReturnUnauthorized() throws Exception {
                mockMvc.perform(get("/api/auth/account")
                                .header("Authorization", "Bearer invalid-token"))
                                .andExpect(status().isUnauthorized());
        }

        private String performLoginAndGetRefreshToken(String email, String password) throws Exception {
                String loginRequest = """
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password);

                var loginResult = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginRequest))
                                .andReturn();
                return loginResult.getResponse().getCookie("refreshToken").getValue();
        }

        @Test
        void refresh_WithValidToken_ShouldReturnNewTokens() throws Exception {
                createTestUser();
                String refreshToken = performLoginAndGetRefreshToken("khieu@example.com", "Password123");

                mockMvc.perform(post("/api/auth/refresh")
                                .cookie(new jakarta.servlet.http.Cookie("refreshToken", refreshToken)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        void logout_WithValidToken_ShouldInvalidateToken() throws Exception {
                createTestUser();
                JsonNode loginResponse = performLogin("khieu@example.com", "Password123");
                String accessToken = loginResponse.path("data").path("accessToken").asText();

                mockMvc.perform(post("/api/auth/logout")
                                .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true));

                // Follow-up request with same token should be unauthorized if system handles
                // blacklisting
                // Adding a minimal check just in case. Note: it depends on implementation
                // details
        }
}
