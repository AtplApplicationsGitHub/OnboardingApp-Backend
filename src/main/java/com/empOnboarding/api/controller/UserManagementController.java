package com.empOnboarding.api.controller;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.UserPrincipalDTO;
import com.empOnboarding.api.service.UserManagementService;
import com.empOnboarding.api.utils.Constants;


@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserManagementController {
	
	final UserManagementService userManagementService;
	
	public UserManagementController(UserManagementService userManagementService) {
		this.userManagementService = userManagementService;
	}
	
	@PostMapping("/saveUser")
	public boolean saveUser(@RequestBody UserPrincipalDTO userDto, CommonDTO dto,
			HttpServletRequest request) {
		try {
			dto.setIpAddress(request.getRemoteAddr());
			dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
			dto.setModule(Constants.USER_MANAGEMENT);
			return userManagementService.createUser(userDto, dto);
		} catch (Exception e) {
			return false;
		}
	}
	
	@PostMapping("/findFilteredPatient/{role}/{pageNo}")
	public JSONObject patientService(@PathVariable String role,@PathVariable String pageNo) throws Exception {
        return userManagementService.filteredUsers(pageNo,role);
    }
	
}