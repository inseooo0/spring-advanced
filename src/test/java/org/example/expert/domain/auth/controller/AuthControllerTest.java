package org.example.expert.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void signup_shouldReturnSuccessResponse() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest("test@example.com", "Password123", "User");
        SignupResponse signupResponse = new SignupResponse("User registered successfully");
        when(authService.signup(any(SignupRequest.class))).thenReturn(signupResponse);

        // When & Then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value("User registered successfully"))
                .andDo(print());

        verify(authService, times(1)).signup(any(SignupRequest.class));
    }

    @Test
    void signup_shouldReturnBadRequest_whenInvalidRequest() throws Exception {
        // Given
        SignupRequest signupRequest = new SignupRequest("", "short", "User"); // Invalid request (empty email, short password)

        // When & Then
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }


    @Test
    void signin_shouldReturnSuccessResponse() throws Exception {
        // Given
        SigninRequest signinRequest = new SigninRequest("test@example.com", "Password123");
        SigninResponse signinResponse = new SigninResponse("Bearer token");
        when(authService.signin(any(SigninRequest.class))).thenReturn(signinResponse);

        // When & Then
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signinRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bearerToken").value("Bearer token"))
                .andDo(print());

        verify(authService, times(1)).signin(any(SigninRequest.class));
    }

    @Test
    void signin_shouldReturnBadRequest_whenInvalidRequest() throws Exception {
        // Given
        SigninRequest signinRequest = new SigninRequest("", ""); // Invalid request (empty email and password)

        // When & Then
        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signinRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }
}