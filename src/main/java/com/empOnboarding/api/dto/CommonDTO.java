package com.empOnboarding.api.dto;

import java.util.List;

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
public class CommonDTO {

	private Long loginUserId;

	private String loginFullName;

	private Long roleId;

	private String roleName;

	private String userRemarks = "";

	private String systemRemarks = "";

	private String ipAddress;

	private String agentRequestForAuditTrail;
	
	private String moduleType;

	private String uniqueDocCodeForAudit;

	private Long documentPrimaryKey;
	
	private List<String> updatedValues;
	
	private String module;

	private String moduleId;

}
