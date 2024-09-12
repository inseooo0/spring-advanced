package org.example.expert.domain.comment.controller;

import org.example.expert.domain.comment.service.CommentAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class CommentAdminControllerTest {

    @Mock
    private CommentAdminService commentAdminService;

    @InjectMocks
    private CommentAdminController commentAdminController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentAdminController).build();
    }

    @Test
    void deleteComment_shouldReturnNoContent() throws Exception {
        // Given
        long commentId = 1L;

        // When & Then
        mockMvc.perform(delete("/admin/comments/{commentId}", commentId))
                .andExpect(status().isOk())
                .andDo(print());

        verify(commentAdminService, times(1)).deleteComment(commentId);
    }
}