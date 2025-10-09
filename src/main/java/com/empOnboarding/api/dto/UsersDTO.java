package com.empOnboarding.api.dto;

import lombok.AllArgsConstructor;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Generated
public class UsersDTO {
	
	private String id;
	private String name;
	private String email;
	private String role;
	private String loginType;
	private String password;
	private String activeFlag;
	private String createdTime;
	private String updatedTime;
	
}