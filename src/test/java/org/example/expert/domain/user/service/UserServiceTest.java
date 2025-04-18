package org.example.expert.domain.user.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@InjectMocks
	private UserService userService;

	@Nested
	@DisplayName("getUser 테스트")
	class getUser_Test {

		@Test
		@DisplayName("getUser 테스트 성공")
		void getUser_success () {
		    // given
			long userId = 1L;
			User user = new User("test@email.com", "password", UserRole.USER);

			given(userRepository.findById(userId)).willReturn(Optional.of(user));

			// when
			UserResponse result = userService.getUser(userId);

			// then
			assertNotNull(result);
			assertEquals(user.getId(), result.getId());
			assertEquals(user.getEmail(), result.getEmail());
		}

		@Test
		@DisplayName("getUser 테스트 실패 - User not found")
		void getUser_fail_user_not_found () {
			// given
			long userId = 1L;
			User user = new User("test@email.com", "password", UserRole.USER);

			given(userRepository.findById(userId)).willReturn(Optional.empty());

			// when
			InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> {
				userService.getUser(userId);
			});

			// then
			assertEquals("User not found", invalidRequestException.getMessage());
		}

	}

	@Nested
	@DisplayName("changePassword 테스트")
	class changePassword_Test {
		
		@Test
		@DisplayName("changePassword 테스트 성공")
		void changePassword_success () {
		    // given
			long userId = 1L;
			User user = new User("test@email.com", "password", UserRole.USER);
			UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest("old", "new");

			given(userRepository.findById(userId)).willReturn(Optional.of(user));
			given(passwordEncoder.matches("new", user.getPassword())).willReturn(false);
			given(passwordEncoder.matches("old", user.getPassword())).willReturn(true);
			given(passwordEncoder.encode("new")).willReturn("encode");

			// when
			userService.changePassword(userId, userChangePasswordRequest);
		
		    // then
			assertEquals("encode", user.getPassword());

		}

		@Test
		@DisplayName("changePassword 테스트 실패 - user not found")
		void changePassword_fail_user_not_found () {
			// given
			long userId = 1L;
			UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest("old", "new");

			given(userRepository.findById(userId)).willReturn(Optional.empty());

			// when
			InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> {
				userService.changePassword(userId, userChangePasswordRequest);
			});

			// then
			assertEquals("User not found", invalidRequestException.getMessage());
		}

		@Test
		@DisplayName("changePassword 테스트 실패 - 새 비밀번호 = 기존 비밀번호")
		void changePassword_fail_old_new_equal () {
			// given
			long userId = 1L;
			User user = new User("test@email.com", "password", UserRole.USER);
			UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest("old", "new");

			given(userRepository.findById(userId)).willReturn(Optional.of(user));
			given(passwordEncoder.matches("new", user.getPassword())).willReturn(true);

			// when
			InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> {
				userService.changePassword(userId, userChangePasswordRequest);
			});

			// then
			assertEquals("새 비밀번호는 기존 비밀번호와 같을 수 없습니다.", invalidRequestException.getMessage());
		}

		@Test
		@DisplayName("changePassword 테스트 실패 - 잘못된 비밀번호")
		void changePassword_fail_invalid_password () {
			// given
			long userId = 1L;
			User user = new User("test@email.com", "password", UserRole.USER);
			UserChangePasswordRequest userChangePasswordRequest = new UserChangePasswordRequest("old", "new");

			given(userRepository.findById(userId)).willReturn(Optional.of(user));
			given(passwordEncoder.matches("new", user.getPassword())).willReturn(false);
			given(passwordEncoder.matches("old", user.getPassword())).willReturn(false);

			// when
			InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> {
				userService.changePassword(userId, userChangePasswordRequest);
			});

			// then
			assertEquals("잘못된 비밀번호입니다.", invalidRequestException.getMessage());
		}

	}

}