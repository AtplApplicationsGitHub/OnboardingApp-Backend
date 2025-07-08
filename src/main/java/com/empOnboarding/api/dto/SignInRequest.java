package com.empOnboarding.api.dto;

import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Generated
@Getter
@Setter
@NoArgsConstructor
public class SignInRequest {

	private String signInId;
	private String password;
}