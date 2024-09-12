package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    public void beforeEach() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void getUser_shouldReturnUserResponse_whenUserExists() {
        // Given
        long userId = 1;
        User user = new User("test@example.com", "password", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // When
        UserResponse response = userService.getUser(1L);

        // Then
        assertNotNull(response);
        assertEquals(userId, response.getId());
        assertEquals("test@example.com", response.getEmail());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void getUser_shouldThrowException_whenUserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.getUser(1L));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void changePassword_shouldChangePasswordSuccessfully() {
        // Given
        User user = new User("test@example.com", passwordEncoder.encode("oldPassword"), UserRole.USER);
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "newPassword1");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        userService.changePassword(1L, request);

        // Then
        verify(userRepository, times(1)).findById(1L);
        assertTrue(passwordEncoder.matches("newPassword1", user.getPassword()));
    }

    @Test
    void changePassword_shouldThrowException_whenNewPasswordIsSameAsOldPassword() {
        // Given
        User user = new User("test@example.com", passwordEncoder.encode("OldPassword1"), UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        UserChangePasswordRequest request = new UserChangePasswordRequest("OldPassword1", "OldPassword1");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When & Then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.changePassword(1L, request));
        assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", exception.getMessage());

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void changePassword_shouldThrowException_whenOldPasswordDoesNotMatch() {
        // Given
        User user = new User("test@example.com", passwordEncoder.encode("OldPassword"), UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        UserChangePasswordRequest request = new UserChangePasswordRequest("WrongOldPassword", "NewPassword1");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When & Then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.changePassword(1L, request));
        assertEquals("잘못된 비밀번호입니다.", exception.getMessage());

        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void changePassword_shouldThrowException_whenNewPasswordInvalid() {
        // Given
        UserChangePasswordRequest request = new UserChangePasswordRequest("OldPassword1", "short");

        // When & Then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.changePassword(1L, request));
        assertEquals("새 비밀번호는 8자 이상이어야 하고, 숫자와 대문자를 포함해야 합니다.", exception.getMessage());
    }
}
