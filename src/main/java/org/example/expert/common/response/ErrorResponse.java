package org.example.expert.common.response;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
	private int code;
	private String status;
	private String message;

	public static ErrorResponse of(HttpStatus status, String message) {
		return new ErrorResponse(status.value(), status.name(), message);
	}
}
