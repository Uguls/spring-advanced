package org.example.expert.common.aop;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class LogAop {

	private final ObjectMapper objectMapper;

	@Pointcut("execution(* org.example.expert.domain..*AdminController.*(..)))")
	private void admin() {
	}

	@Around("admin()")
	public Object logging(ProceedingJoinPoint joinPoint) throws Throwable {
		ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();

		MethodSignature signature = (MethodSignature)joinPoint.getSignature();
		Method method = signature.getMethod();
		String[] parameterNames = signature.getParameterNames();
		Object[] args = joinPoint.getArgs();
		String requestURI = request.getRequestURI();
		String userId = String.valueOf(request.getAttribute("userId"));
		LocalDateTime requestTime = LocalDateTime.now();
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();

		Map<String, Object> pathVariables = new LinkedHashMap<>();
		Map<String, Object> requestBodies = new LinkedHashMap<>();

		for (int i = 0; i < args.length; i++) {
			for (Annotation annotation : parameterAnnotations[i]) {
				if (annotation.annotationType() == PathVariable.class) {
					pathVariables.put(parameterNames[i], args[i]);
				} else if (annotation.annotationType() == RequestBody.class) {
					requestBodies.put(parameterNames[i], args[i]);
				}
			}
		}

		String path = objectMapper.writeValueAsString(pathVariables);
		String body = objectMapper.writeValueAsString(requestBodies);

		log.info("[Request] userId = {} | time = {} | URL = {}", userId, requestTime, requestURI);
		log.info("[RequestBody] = {}", body);
		log.info("[PathVariable] = {}", path);

		Object response = joinPoint.proceed();

		log.info("[ResponseBody] = {}", objectMapper.writeValueAsString(response));

		return response;
	}
}
