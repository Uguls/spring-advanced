package org.example.expert.common.interceptor;

import java.time.LocalDateTime;

import org.example.expert.domain.user.enums.UserRole;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws
		Exception {

		LocalDateTime requestTime = LocalDateTime.now();

		Object userRole = request.getAttribute("userRole");

		if (!UserRole.ADMIN.name().equals(userRole)) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "관리자 권한이 없습니다.");
			return false;
		}

		log.info("[request time] = {}, request url = {}", requestTime, request.getRequestURI());

		return true;
	}
}
