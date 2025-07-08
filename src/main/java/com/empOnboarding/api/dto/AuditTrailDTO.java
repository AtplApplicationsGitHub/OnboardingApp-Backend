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
public class AuditTrailDTO extends CommonDTO {

	private Long loginUserId;

	private String loginUserName;

	private String event;

	private String ipAddress;

	private String createdTime;

	private String browser;

	private String fromDate;

	private String toDate;

	private String systemRemarks;

	private String userRemarks;;

	private String module;

	private String moduleId;

	public AuditTrailDTO(Long loginUserId, String loginUserName, String event, String ipAddress, String createdTime,
			String browser, String systemRemarks, String module, String moduleId, String userRemarks) {
		this.loginUserId = loginUserId;
		this.loginUserName = loginUserName;
		this.event = event;
		this.ipAddress = ipAddress;
		this.createdTime = createdTime;
		this.browser = browser;
		this.systemRemarks = systemRemarks;
		this.module = module;
		this.moduleId = moduleId;
		this.userRemarks = userRemarks;
	}
}