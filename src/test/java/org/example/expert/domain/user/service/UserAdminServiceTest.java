package org.example.expert.domain.user.service;

import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAdminService userAdminService;

    @Test
    void changeUserRole_shouldChangeUserRoleSuccessfully() {
        // Given
        User user = new User("test@example.com", "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // When
        userAdminService.changeUserRole(1L, request);

        // Then
        verify(userRepository, times(1)).findById(1L);
        assertEquals(UserRole.ADMIN, user.getUserRole());
    }

    @Test
    void changeUserRole_shouldThrowException_whenUserNotFound() {
        // Given
        UserRoleChangeRequest request = new UserRoleChangeRequest("ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userAdminService.changeUserRole(1L, request));
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(1L);
    }
}