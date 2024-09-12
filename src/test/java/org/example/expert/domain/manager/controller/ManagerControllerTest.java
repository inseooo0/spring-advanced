package org.example.expert.domain.manager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.config.JwtUtil;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.service.ManagerService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ManagerControllerTest {

    @Mock
    private ManagerService managerService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private ManagerController managerController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(managerController).build();
    }

    @Test
    void saveManager_shouldReturnSuccessResponse() throws Exception {
        // Given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(2L);
        ManagerSaveResponse managerSaveResponse = new ManagerSaveResponse(1L, new UserResponse(2L, "manager@example.com"));

        when(managerService.saveManager(any(AuthUser.class), eq(1L), any(ManagerSaveRequest.class)))
                .thenReturn(managerSaveResponse);

        // When & Then
        mockMvc.perform(post("/todos/1/managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(managerSaveRequest))
                        .requestAttr("authUser", authUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.user.email").value("manager@example.com"))
                .andDo(print());

        verify(managerService, times(1)).saveManager(any(AuthUser.class), eq(1L), any(ManagerSaveRequest.class));
    }

    @Test
    void saveManager_shouldReturnBadRequest_whenInvalidRequest() throws Exception {
        // Given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        ManagerSaveRequest invalidRequest = new ManagerSaveRequest();  // Invalid request (empty id)

        // When & Then
        mockMvc.perform(post("/todos/1/managers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .requestAttr("authUser", authUser))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void getManagers_shouldReturnListOfManagers() throws Exception {
        // Given
        List<ManagerResponse> managerResponses = Arrays.asList(
                new ManagerResponse(1L, new UserResponse(1L, "manager1@example.com")),
                new ManagerResponse(2L, new UserResponse(2L, "manager2@example.com"))
        );

        when(managerService.getManagers(1L)).thenReturn(managerResponses);

        // When & Then
        mockMvc.perform(get("/todos/1/managers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].user.email").value("manager1@example.com"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].user.email").value("manager2@example.com"))
                .andDo(print());

        verify(managerService, times(1)).getManagers(1L);
    }

    @Test
    void deleteManager_shouldReturnNoContent() throws Exception {
        // Given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);

        // When & Then
        mockMvc.perform(delete("/todos/1/managers/1")
                        .requestAttr("authUser", authUser))
                .andExpect(status().isOk())
                .andDo(print());

        verify(managerService, times(1)).deleteManager(any(AuthUser.class), eq(1L), eq(1L));
    }

    /*@Test
    void deleteManager_shouldReturnNotFound_whenManagerDoesNotExist() throws Exception {
        // Given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        doThrow(new InvalidRequestException("Manager not found")).when(managerService).deleteManager(any(AuthUser.class), eq(1L), eq(1L));

        // When & Then
        mockMvc.perform(delete("/todos/1/managers/1")
                        .requestAttr("authUser", authUser))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Manager not found"))
                .andDo(print());

        verify(managerService, times(1)).deleteManager(any(AuthUser.class), eq(1L), eq(1L));
    }*/
}