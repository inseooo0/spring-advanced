package org.example.expert.domain.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.service.TodoService;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class TodoControllerTest {

    @Mock
    private TodoService todoService;

    @InjectMocks
    private TodoController todoController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(todoController).build();
    }

    @Test
    void saveTodo_shouldReturnSuccessResponse() throws Exception {
        // Given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest("Test Todo", "This is a test");
        TodoSaveResponse todoSaveResponse = new TodoSaveResponse(1L, "Test Todo", "This is a test", "sunny", new UserResponse(1L, "test@example.com"));

        when(todoService.saveTodo(any(AuthUser.class), any(TodoSaveRequest.class))).thenReturn(todoSaveResponse);

        // When & Then
        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(todoSaveRequest))
                        .requestAttr("authUser", authUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Todo"))
                .andExpect(jsonPath("$.contents").value("This is a test"))
                .andDo(print());

        verify(todoService, times(1)).saveTodo(any(AuthUser.class), any(TodoSaveRequest.class));
    }

    @Test
    void saveTodo_shouldReturnBadRequest_whenInvalidRequest() throws Exception {
        // Given
        AuthUser authUser = new AuthUser(1L, "test@example.com", UserRole.USER);
        TodoSaveRequest invalidRequest = new TodoSaveRequest("", "");  // Invalid request (empty title and description)

        // When & Then
        mockMvc.perform(post("/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest))
                        .requestAttr("authUser", authUser))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    void getTodos_shouldReturnPagedListOfTodos() throws Exception {
        // Given
        List<TodoResponse> todoResponses = Arrays.asList(
                new TodoResponse(1L, "Test Todo 1", "Description 1",
                        "sunny", new UserResponse(1L, "test@example.com"), LocalDateTime.now(), LocalDateTime.now()),
                new TodoResponse(2L, "Test Todo 2", "Description 2",
                        "sunny", new UserResponse(1L, "test@example.com"), LocalDateTime.now(), LocalDateTime.now())
        );
        Page<TodoResponse> todoPage = new PageImpl<>(todoResponses, PageRequest.of(0, 10), 2);

        when(todoService.getTodos(1, 10)).thenReturn(todoPage);

        // When & Then
        mockMvc.perform(get("/todos")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].title").value("Test Todo 1"))
                .andExpect(jsonPath("$.content[1].id").value(2L))
                .andExpect(jsonPath("$.content[1].title").value("Test Todo 2"))
                .andDo(print());

        verify(todoService, times(1)).getTodos(1, 10);
    }

    @Test
    void getTodo_shouldReturnTodo() throws Exception {
        // Given
        TodoResponse todoResponse = new TodoResponse(1L, "Test Todo", "This is a test",
                "sunny", new UserResponse(1L, "test@example.com"), LocalDateTime.now(), LocalDateTime.now());

        when(todoService.getTodo(1L)).thenReturn(todoResponse);

        // When & Then
        mockMvc.perform(get("/todos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Todo"))
                .andExpect(jsonPath("$.contents").value("This is a test"))
                .andDo(print());

        verify(todoService, times(1)).getTodo(1L);
    }

/*    @Test
    void getTodo_shouldReturnNotFound_whenTodoDoesNotExist() throws Exception {
        // Given
        when(todoService.getTodo(1L)).thenThrow(new IllegalArgumentException("Todo not found"));

        // When & Then
        mockMvc.perform(get("/todos/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Todo not found"))
                .andDo(print());

        verify(todoService, times(1)).getTodo(1L);
    }*/
}