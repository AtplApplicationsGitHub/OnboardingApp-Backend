package com.empOnboarding.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LDAPSettingsDTO extends CommonDTO{

	private long id;
	private String hostName;
	private String clientUserName;
	private String clientPassword;

}