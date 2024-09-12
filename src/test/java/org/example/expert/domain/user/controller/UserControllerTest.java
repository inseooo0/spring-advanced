package org.example.expert.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.AuthUserArgumentResolver;
import org.example.expert.config.GlobalExceptionHandler;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new AuthUserArgumentResolver())// @RestControllerAdvice 추가
                .build();
    }

    @Test
    void getUser_shouldReturnUserResponse() throws Exception {
        // Given
        long userId = 1L;
        UserResponse userResponse = new UserResponse(userId, "test@example.com");

        when(userService.getUser(userId)).thenReturn(userResponse);

        // When & Then
        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andDo(print());

        verify(userService, times(1)).getUser(userId);
    }

    @Test
    void getUser_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        // Given
        long userId = 1L;

        when(userService.getUser(userId)).thenThrow(new InvalidRequestException("User not found"));

        // When & Then
        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"))
                .andDo(print());

        verify(userService, times(1)).getUser(userId);
    }

    @Test
    void changePassword_shouldReturnOk() throws Exception {
        // Given
        // AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        UserChangePasswordRequest changePasswordRequest = new UserChangePasswordRequest("oldPass123", "NewPass123");
        // When & Then
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest))
                        .requestAttr("userId", 1L)
                        .requestAttr("email", "test@example.com")
                        .requestAttr("userRole", "User"))
                .andExpect(status().isOk())
                .andDo(print());

        verify(userService, times(1)).changePassword(any(Long.class), any(UserChangePasswordRequest.class));
    }

    @Test
    void changePassword_shouldReturnBadRequest_whenInvalidPassword() throws Exception {
        // Given
        UserChangePasswordRequest invalidRequest = new UserChangePasswordRequest("oldPass123", "short");

        doThrow(new InvalidRequestException("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.")).when(userService).changePassword(eq(1L), any(UserChangePasswordRequest.class));

        // When & Then
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .requestAttr("userId", 1L)
                        .requestAttr("email", "test@example.com")
                        .requestAttr("userRole", "User"))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void changePassword_shouldReturnForbidden_whenOldPasswordIsIncorrect() throws Exception {
        // Given
        UserChangePasswordRequest changePasswordRequest = new UserChangePasswordRequest("wrongOldPass", "NewPass123");

        doThrow(new InvalidRequestException("잘못된 비밀번호입니다.")).when(userService).changePassword(eq(1L), any(UserChangePasswordRequest.class));

        // When & Then
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest))
                        .requestAttr("userId", 1L)
                        .requestAttr("email", "test@example.com")
                        .requestAttr("userRole", "User"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("잘못된 비밀번호입니다."))
                .andDo(print());
    }
}