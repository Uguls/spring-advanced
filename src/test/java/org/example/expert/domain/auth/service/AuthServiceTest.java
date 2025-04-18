package org.example.expert.domain.auth.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtUtil jwtUtil;
	@InjectMocks
	private AuthService authService;

	@Nested
	@DisplayName("signup 테스트")
	class signupTest {

		@Test
		@DisplayName("signup 테스트 성공")
		void signup_success() {
			// given
			String email = "test@email.com";
			String password = "password";
			UserRole userRole = UserRole.USER;

			SignupRequest signupRequest = new SignupRequest(email, password, userRole.name());

			given(userRepository.existsByEmail(email)).willReturn(false);
			given(passwordEncoder.encode(password)).willReturn("encodedPassword");

			User user = new User(email, "encodedPassword", userRole);
			ReflectionTestUtils.setField(user, "id", 1L);

			given(userRepository.save(any())).willReturn(user);
			given(jwtUtil.createToken(user.getId(), user.getEmail(), userRole)).willReturn("bearerToken");

			// when
			SignupResponse signup = authService.signup(signupRequest);

			// then
			assertEquals("bearerToken", signup.getBearerToken());
		}

		@Test
		@DisplayName("signup 테스트 실패 - 이미 존재하는 이메일")
		void signup_fail_existsUser() {
			// given
			String email = "test@email.com";
			String password = "password";
			UserRole userRole = UserRole.USER;

			SignupRequest signupRequest = new SignupRequest(email, password, userRole.name());

			given(userRepository.existsByEmail(email)).willReturn(true);

			User user = new User(email, "encodedPassword", userRole);
			ReflectionTestUtils.setField(user, "id", 1L);

			// when
			InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> {
				authService.signup(signupRequest);
			});

			// then
			assertEquals("이미 존재하는 이메일입니다.", invalidRequestException.getMessage());
		}

	}

	@Nested
	@DisplayName("signin 테스트")
	class signinTest {
		@Test
		@DisplayName("signin 테스트 성공")
		void signin_success() {
			// given
			String email = "test@email.com";
			String password = "password";
			UserRole userRole = UserRole.USER;
			SigninRequest signinRequest = new SigninRequest(email, password);
			User user = new User(email, "encodedPassword", userRole);

			given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
			given(passwordEncoder.matches(password, user.getPassword())).willReturn(true);
			given(jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole())).willReturn("bearerToken");

			// when
			SigninResponse signin = authService.signin(signinRequest);

			// then
			assertEquals("bearerToken", signin.getBearerToken());
		}

		@Test
		@DisplayName("signin 테스트 실패 - 가입되지 않은 유저")
		void signin_fail_InvalidUser() {
			// given
			String email = "test@email.com";
			String password = "password";
			SigninRequest signinRequest = new SigninRequest(email, password);

			given(userRepository.findByEmail(email)).willReturn(Optional.empty());

			// when
			InvalidRequestException invalidRequestException = assertThrows(InvalidRequestException.class, () -> {
				authService.signin(signinRequest);
			});

			// then
			assertEquals("가입되지 않은 유저입니다.", invalidRequestException.getMessage());
		}

		@Test
		@DisplayName("signin 테스트 실패 - 잘못된 비밀번호")
		void signin_fail_InvalidPassword() {
			// given
			String email = "test@email.com";
			String password = "password";
			UserRole userRole = UserRole.USER;
			SigninRequest signinRequest = new SigninRequest(email, password);
			User user = new User(email, "encodedPassword", userRole);

			given(userRepository.findByEmail(email)).willReturn(Optional.of(user));
			given(passwordEncoder.matches(password, user.getPassword())).willReturn(false);

			// when
			AuthException authException = assertThrows(AuthException.class, () -> {
				authService.signin(signinRequest);
			});

			// then
			assertEquals("잘못된 비밀번호입니다.", authException.getMessage());
		}
	}
}