package com.empOnboarding.api.dto;

import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Generated
@Getter
@Setter
@NoArgsConstructor
public class ApiResponse {
	private boolean success;
	private String message;
	private String status;

	public ApiResponse(boolean success) {
		this.success = success;
	}

	public ApiResponse(boolean b, String message, String status) {
		this.success=b;
		this.message=message;
		this.status=status;
	}

}