package org.example.expert.domain.todo.service;

import org.assertj.core.api.Assertions;
import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {
    @Mock
    private TodoRepository todoRepository;
    @Mock
    private WeatherClient weatherClient;
    @InjectMocks
    private TodoService todoService;

    @Test
    void save_todo() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        String todoTitle = "todoTitle";
        String todoContents = "content";
        TodoSaveRequest todoSaveRequest = new TodoSaveRequest(todoTitle, todoContents);

        String weather = "sunny";
        given(weatherClient.getTodayWeather()).willReturn(weather);

        Todo todo = new Todo(todoTitle, todoContents, weather, user);
        Todo savedTodo = new Todo(todoTitle, todoContents, weather, user);
        ReflectionTestUtils.setField(savedTodo, "id", 1L);

        when(todoRepository.save(any(Todo.class))).thenReturn(savedTodo);

        // when & then
        TodoSaveResponse todoSaveResponse = todoService.saveTodo(authUser, todoSaveRequest);

        Assertions.assertThat(todoSaveResponse.getId()).isEqualTo(1L);
        Assertions.assertThat(todoSaveResponse.getTitle()).isEqualTo(todoTitle);
        Assertions.assertThat(todoSaveResponse.getContents()).isEqualTo(todoContents);
        Assertions.assertThat(todoSaveResponse.getWeather()).isEqualTo(weather);
        Assertions.assertThat(todoSaveResponse.getUser().getId()).isEqualTo(1L);
        Assertions.assertThat(todoSaveResponse.getUser().getEmail()).isEqualTo("a@a.com");
    }

    @Test
    void get_todos() {
        // given
        int page = 1;
        int size = 10;

        Pageable pageable = PageRequest.of(page - 1, size);

        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        String todoTitle = "todoTitle";
        String todoContents = "content";
        String weather = "sunny";

        Todo todo = new Todo(todoTitle, todoContents, weather, user);
        ReflectionTestUtils.setField(todo, "id", 1L);

        List<Todo> todoList = new ArrayList<>();
        todoList.add(todo);

        Page<Todo> todos = new PageImpl<>(todoList, pageable, 1L);
        given(todoRepository.findAllByOrderByModifiedAtDesc(pageable)).willReturn(todos);

        // when & then
        Page<TodoResponse> responses = todoService.getTodos(page, size);
        Assertions.assertThat(responses).isInstanceOf(Page.class);
        Assertions.assertThat(responses.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(responses.getTotalPages()).isEqualTo(1);
        TodoResponse todoResponse = responses.getContent().get(0);
        Assertions.assertThat(todoResponse.getUser().getId()).isEqualTo(1L);
        Assertions.assertThat(todoResponse.getUser().getEmail()).isEqualTo("a@a.com");
        Assertions.assertThat(todoResponse.getTitle()).isEqualTo(todoTitle);
        Assertions.assertThat(todoResponse.getContents()).isEqualTo(todoContents);
    }

    @Test
    void get_todo() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);
        String todoTitle = "todoTitle";
        String todoContents = "content";
        String weather = "sunny";

        long todoId = 1;
        Todo todo = new Todo(todoTitle, todoContents, weather, user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.of(todo));

        // when & then
        TodoResponse todoResponse = todoService.getTodo(todoId);
        Assertions.assertThat(todoResponse.getId()).isEqualTo(todoId);
        Assertions.assertThat(todoResponse.getTitle()).isEqualTo(todoTitle);
        Assertions.assertThat(todoResponse.getContents()).isEqualTo(todoContents);
    }

    @Test
    void get_todo_todo_notfound() {
        // given
        long todoId = 1;
        given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> todoService.getTodo(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }

}