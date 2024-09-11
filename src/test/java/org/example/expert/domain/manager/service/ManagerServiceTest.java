package org.example.expert.domain.manager.service;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ManagerServiceTest {

    @Mock
    private ManagerRepository managerRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TodoRepository todoRepository;
    @InjectMocks
    private ManagerService managerService;

    @Test
    public void manager_목록_조회_시_Todo가_없다면_InvalidRequestException_에러를_던진다() {
        // given
        long todoId = 1L;
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> managerService.getManagers(todoId));
        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    void todo의_user가_null인_경우_예외가_발생한다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        long todoId = 1L;
        long managerUserId = 2L;

        Todo todo = new Todo();
        ReflectionTestUtils.setField(todo, "user", null);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
            managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test // 테스트코드 샘플
    public void manager_목록_조회에_성공한다() {
        // given
        long todoId = 1L;
        User user = new User("user1@example.com", "password", UserRole.USER);
        Todo todo = new Todo("Title", "Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        Manager mockManager = new Manager(todo.getUser(), todo);
        List<Manager> managerList = List.of(mockManager);

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

        // when
        List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

        // then
        assertEquals(1, managerResponses.size());
        assertEquals(mockManager.getId(), managerResponses.get(0).getId());
        assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
    }

    @Test // 테스트코드 샘플
    void todo가_정상적으로_등록된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
        given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));

        // when
        ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

        // then
        assertNotNull(response);
        assertEquals(managerUser.getId(), response.getUser().getId());
        assertEquals(managerUser.getEmail(), response.getUser().getEmail());
    }

    @Test
    void 본인_담당자_등록시_예외발생() {
        // given
        long userId = 1;
        AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

        User managerUser = new User("a@a.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", userId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(userId); // request dto 생성

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(userRepository.findById(userId)).willReturn(Optional.of(managerUser));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("일정 작성자는 본인을 담당자로 등록할 수 없습니다.", exception.getMessage());
    }

    @Test
    void 잘못된_담당자_작성_시도_유저() {
        // given
        long userId = 1;
        AuthUser authUser = new AuthUser(userId, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 담당자를 등록 하려는 유저

        long todoMakeUserId = 2;
        User todoMakeUser = new User("b@b.com", "password", UserRole.USER); // 일정을 만든 유저
        ReflectionTestUtils.setField(todoMakeUser, "id", todoMakeUserId);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", todoMakeUser);

        User managerUser = new User("a@a.com", "password", UserRole.USER);  // 매니저로 등록할 유저
        ReflectionTestUtils.setField(managerUser, "id", userId);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(userId); // request dto 생성

        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.saveManager(authUser, todoId, managerSaveRequest)
        );

        assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test
    void delete_manager_정상삭제() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 삭제할 매니저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        long managerId = 1;
        Manager manager = new Manager(user, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        todo.getManagers().add(manager);

        given(userRepository.findById(authUser.getId())).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));

        // when
        managerService.deleteManager(authUser, todoId, managerId);
        // then
        verify(managerRepository).delete(manager); // 행위 검증

    }

    @Test
    void delete_manager_존재하지_않는_유저() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", null);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 삭제할 매니저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        long managerId = 1;
        Manager manager = new Manager(managerUser, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        todo.getManagers().add(manager);

        given(userRepository.findById(authUser.getId())).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(authUser, todoId, managerId)
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void delete_manager_존재하지_않는_todo() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
        User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

        long todoId = 1L;

        long managerUserId = 2L;
        User managerUser = new User("b@b.com", "password", UserRole.USER);  // 삭제할 매니저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        long managerId = 1;
        Manager manager = new Manager(managerUser, null);
        ReflectionTestUtils.setField(manager, "id", managerId);

        given(userRepository.findById(authUser.getId())).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(authUser, todoId, managerId)
        );

        assertEquals("Todo not found", exception.getMessage());
    }

    @Test
    void delete_manager_todo_주인_invalid() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER); // 삭제를 시도하는 유저
        User requestUser = User.fromAuthUser(authUser);

        User user = new User("b@b.com", "password", UserRole.USER);  // 일정을 만든 유저
        ReflectionTestUtils.setField(user, "id", 2L);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long managerUserId = 3L;
        User managerUser = new User("c@c.com", "password", UserRole.USER);  // 삭제할 매니저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        long managerId = 1;
        Manager manager = new Manager(managerUser, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        todo.getManagers().add(manager);

        given(userRepository.findById(authUser.getId())).willReturn(Optional.of(requestUser));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(authUser, todoId, managerId)
        );

        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test
    void delete_manager_todo_주인_invalid2() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER); // 삭제를 시도하는 유저
        User requestUser = User.fromAuthUser(authUser);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", null);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long managerUserId = 3L;
        User managerUser = new User("c@c.com", "password", UserRole.USER);  // 삭제할 매니저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        long managerId = 1;
        Manager manager = new Manager(managerUser, todo);
        ReflectionTestUtils.setField(manager, "id", managerId);

        todo.getManagers().add(manager);

        given(userRepository.findById(authUser.getId())).willReturn(Optional.of(requestUser));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(authUser, todoId, managerId)
        );

        assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
    }

    @Test
    void delete_manager_삭제할매니저_없음() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER); // 삭제를 시도하는 유저
        User user = User.fromAuthUser(authUser);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long managerUserId = 3L;
        User managerUser = new User("c@c.com", "password", UserRole.USER);  // 삭제할 매니저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        long managerId = 1;

        given(userRepository.findById(authUser.getId())).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findById(managerId)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(authUser, todoId, managerId)
        );

        assertEquals("Manager not found", exception.getMessage());
    }

    @Test
    void delete_manager_다른일정의_매니저() {
        // given
        AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER); // 삭제를 시도하는 유저
        User user = User.fromAuthUser(authUser);

        long todoId = 1L;
        Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo, "id", todoId);

        long todoId2 = 2L;
        Todo todo2 = new Todo("Test Title", "Test Contents", "Sunny", user);
        ReflectionTestUtils.setField(todo2, "id", todoId2);

        long managerUserId = 3L;
        User managerUser = new User("c@c.com", "password", UserRole.USER);  // 삭제할 매니저
        ReflectionTestUtils.setField(managerUser, "id", managerUserId);

        long managerId = 1;
        Manager manager = new Manager(managerUser, todo2);
        ReflectionTestUtils.setField(manager, "id", managerId);

        todo2.getManagers().add(manager);

        given(userRepository.findById(authUser.getId())).willReturn(Optional.of(user));
        given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
        given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                managerService.deleteManager(authUser, todoId, managerId)
        );

        assertEquals("해당 일정에 등록된 담당자가 아닙니다.", exception.getMessage());
    }
}
