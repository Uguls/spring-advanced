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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.*;

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

    @Nested
    @DisplayName("")
    class manager_test {

    }

    @Test
    public void manager_목록_조회_시_Todo가_없다면_Todo_not_found_에러를_던진다() {
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

    @Nested
    @DisplayName("deleteManager 테스트")
    class deleteManger {

        @Test
        @DisplayName("deleteManager 테스트 성공")
        void deleteManager_success () {
            // given
            String email = "email";
            String password = "password";
            UserRole userRole = UserRole.USER;
            long userId = 1L;
            long todoId = 1L;
            long managerId = 1L;

            User user = new User(email, password, userRole);
            ReflectionTestUtils.setField(user, "id", userId);

            Todo todo = new Todo("title", "content", "흐림", user);
            ReflectionTestUtils.setField(todo, "id", todoId);

            Manager manager = new Manager(user, todo);
            ReflectionTestUtils.setField(manager, "id", managerId);

            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
            given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));

            // when
            managerService.deleteManager(userId, todoId, managerId);

            // then
            verify(managerRepository).delete(manager);
        }

        @Test
        @DisplayName("deleteManager 테스트 실패 - 유저가 유효하지 않음")
        void deleteManager_fail_invalid_user () {
            // given
            User owner = new User("email", "password", UserRole.USER);
            ReflectionTestUtils.setField(owner, "id", 1L);

            User otherUser = new User("email2", "password2", UserRole.USER);
            ReflectionTestUtils.setField(otherUser, "id", 2L);

            Todo todo = new Todo("title", "content", "흐림", owner);
            ReflectionTestUtils.setField(todo, "id", 1L);

            Manager manager = new Manager(owner, todo);
            ReflectionTestUtils.setField(manager, "id", 1L);

            given(userRepository.findById(2L)).willReturn(Optional.of(otherUser));
            given(todoRepository.findById(1L)).willReturn(Optional.of(todo));

            // when
            InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> {
                managerService.deleteManager(2L, 1L, 1L);
            });

            // then
            assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", invalidRequestException.getMessage());
        }

        @Test
        @DisplayName("deleteManager 테스트 실패 - 담당자가 아님")
        void deleteManager_fail_not_manager () {
            // given
            User user = new User("email", "password", UserRole.USER);
            ReflectionTestUtils.setField(user, "id", 1L);

            Todo todo = new Todo("title", "content", "흐림", user);
            ReflectionTestUtils.setField(todo, "id", 1L);

            Todo otherTodo = new Todo("title", "content", "흐림", user);
            ReflectionTestUtils.setField(otherTodo, "id", 2L);

            Manager otherManager = new Manager(user, otherTodo);
            ReflectionTestUtils.setField(otherManager, "id", 2L);


            given(userRepository.findById(1L)).willReturn(Optional.of(user));
            given(todoRepository.findById(1L)).willReturn(Optional.of(todo));
            given(managerRepository.findById(2L)).willReturn(Optional.of(otherManager));

            // when
            InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> {
                managerService.deleteManager(1L, 1L, 2L);
            });

            // then
            assertEquals("해당 일정에 등록된 담당자가 아닙니다.", invalidRequestException.getMessage());
        }
    }

    @Test
    @DisplayName("saveManager 테스트 실패 - 작성자는 본인을 담당자로 등록할 수 없음")
    void saveManager_fail_self() {
        // given
        AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
        User user = User.fromAuthUser(authUser);

        Todo todo = new Todo("title", "content", "흐림", user);
        ReflectionTestUtils.setField(todo, "id", 1L);

        ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(1L); // 자기 자신

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(todoRepository.findById(1L)).willReturn(Optional.of(todo));

        // when
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            managerService.saveManager(authUser, 1L, managerSaveRequest);
        });

        // then
        assertEquals("일정 작성자는 본인을 담당자로 등록할 수 없습니다.", exception.getMessage());
    }



}
