package com.empOnboarding.api.dto;

import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Generated
@Getter
@Setter
@NoArgsConstructor
public class AppAuthenticationResponse {
    private String accessToken;
    private String userId;
    private String userName;
    private boolean success;
    private String message;
    private boolean isNewUser;
    private int loginAttemptCount;

	public AppAuthenticationResponse(boolean success,String message) {
		this.success = success;
		this.message=message;
	}
}