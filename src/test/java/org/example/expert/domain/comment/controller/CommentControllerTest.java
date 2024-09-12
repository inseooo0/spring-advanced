package org.example.expert.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentResponse;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.service.CommentService;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();
    }

    @Test
    void saveComment_shouldReturnSuccessResponse() throws Exception {
        // Given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        UserResponse userResponse = new UserResponse(1L, "test@example.com");
        CommentSaveRequest commentSaveRequest = new CommentSaveRequest("This is a comment.");
        CommentSaveResponse commentSaveResponse = new CommentSaveResponse(1L, "This is a comment.", userResponse);

        when(commentService.saveComment(any(AuthUser.class), eq(1L), any(CommentSaveRequest.class)))
                .thenReturn(commentSaveResponse);

        // When & Then
        mockMvc.perform(post("/todos/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentSaveRequest))
                        .requestAttr("authUser", authUser))  // Mock the @Auth annotation
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.contents").value("This is a comment."))
                .andDo(print());

        verify(commentService, times(1)).saveComment(any(AuthUser.class), eq(1L), any(CommentSaveRequest.class));
    }

    @Test
    void saveComment_shouldReturnBadRequest_whenInvalidRequest() throws Exception {
        // Given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        CommentSaveRequest invalidRequest = new CommentSaveRequest("");  // Invalid request (empty comment)

        // When & Then
        mockMvc.perform(post("/todos/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .requestAttr("authUser", authUser))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void getComments_shouldReturnListOfComments() throws Exception {
        // Given
        UserResponse userResponse = new UserResponse(1L, "test@example.com");
        List<CommentResponse> commentResponses = Arrays.asList(
                new CommentResponse(1L, "First comment", userResponse),
                new CommentResponse(2L, "Second comment", userResponse)
        );

        when(commentService.getComments(1L)).thenReturn(commentResponses);

        // When & Then
        mockMvc.perform(get("/todos/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].contents").value("First comment"))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].contents").value("Second comment"))
                .andDo(print());

        verify(commentService, times(1)).getComments(1L);
    }
}