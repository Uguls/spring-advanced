package org.example.expert.domain.todo.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.example.expert.client.WeatherClient;

import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoResponse;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

	@Mock
	private TodoRepository todoRepository;
	@Mock
	private WeatherClient weatherClient;
	@InjectMocks
	private TodoService todoService;


	@Test
	@DisplayName("todo 저장 - 성공")
	void saveTodo() {
		// given
		TodoSaveRequest todoSaveRequest = new TodoSaveRequest("title", "contents");
		AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
		User user = User.fromAuthUser(authUser);
		Todo todo = new Todo("title", "contents", weatherClient.getTodayWeather(), user);

		given(todoRepository.save(any())).willReturn(todo);

		// when
		TodoSaveResponse result = todoService.saveTodo(authUser, todoSaveRequest);

		// then
		assertNotNull(result);
		assertEquals("title", result.getTitle());
		assertEquals("contents", result.getContents());
	}

	@Test
	@DisplayName("todo 단건 조회 - 성공")
	void getTodo() {
		// given
		long todoId = 1L;
		AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
		User user = User.fromAuthUser(authUser);
		Todo todo = new Todo("title", "contents", weatherClient.getTodayWeather(), user);
		ReflectionTestUtils.setField(todo, "id", 1L);

		given(todoRepository.findByIdWithUser(todoId)).willReturn(Optional.of(todo));

		// when
		TodoResponse result = todoService.getTodo(todoId);

		// then
		assertNotNull(result);
		assertEquals("title", result.getTitle());
		assertEquals("contents", result.getContents());
	}

	@Test
	@DisplayName("Todo 목록 페이징 조회 - 성공")
	void getTodos_success() {
		// given
		int page = 1;
		int size = 2;

		AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
		User user = User.fromAuthUser(authUser);
		Todo todo1 = new Todo("title1", "content1", "맑음", user);
		Todo todo2 = new Todo("title2", "content2", "비", user);

		List<Todo> todoList = List.of(todo1, todo2);
		Page<Todo> todoPage = new PageImpl<>(todoList);

		given(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class))).willReturn(todoPage);

		// when
		Page<TodoResponse> result = todoService.getTodos(page, size);

		// then
		assertThat(result.getContent()).hasSize(2);
		assertEquals("title1", result.getContent().get(0).getTitle());
		assertEquals("title2", result.getContent().get(1).getTitle());
	}

}