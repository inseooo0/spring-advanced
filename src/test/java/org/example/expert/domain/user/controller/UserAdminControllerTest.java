package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.GlobalExceptionHandler;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.service.UserAdminService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserAdminControllerTest {

    @Mock
    private UserAdminService userAdminService;

    @InjectMocks
    private UserAdminController userAdminController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userAdminController)
                .setControllerAdvice(new GlobalExceptionHandler())  // @RestControllerAdvice 추가
                .build();
    }

    @Test
    void changeUserRole_shouldReturnOk() throws Exception {
        // Given
        long userId = 1L;
        UserRoleChangeRequest userRoleChangeRequest = new UserRoleChangeRequest("ADMIN");

        // When & Then
        mockMvc.perform(patch("/admin/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRoleChangeRequest)))
                .andExpect(status().isOk())
                .andDo(print());

        verify(userAdminService, times(1)).changeUserRole(eq(userId), any(UserRoleChangeRequest.class));
    }

    @Test
    void changeUserRole_shouldReturnBadRequest_whenInvalidRole() throws Exception {
        // Given
        long userId = 1L;
        UserRoleChangeRequest invalidRequest = new UserRoleChangeRequest("");  // Invalid role (empty string)
        doThrow(new InvalidRequestException("유효하지 않은 UserRole")).when(userAdminService).changeUserRole(eq(1L), any(UserRoleChangeRequest.class));

        // When & Then
        mockMvc.perform(patch("/admin/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("유효하지 않은 UserRole"))
                .andDo(print());
    }

    @Test
    void changeUserRole_shouldReturnNotFound_whenUserNotFound() throws Exception {
        // Given
        long userId = 1L;
        UserRoleChangeRequest userRoleChangeRequest = new UserRoleChangeRequest("ADMIN");

        doThrow(new InvalidRequestException("User not found")).when(userAdminService).changeUserRole(eq(userId), any(UserRoleChangeRequest.class));

        // When & Then
        mockMvc.perform(patch("/admin/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRoleChangeRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"))
                .andDo(print());

        verify(userAdminService, times(1)).changeUserRole(eq(userId), any(UserRoleChangeRequest.class));
    }
}