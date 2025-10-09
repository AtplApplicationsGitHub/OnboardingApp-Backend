package com.empOnboarding.api.controller;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.UsersDTO;
import com.empOnboarding.api.service.UserManagementService;
import com.empOnboarding.api.utils.Constants;

import java.util.List;


@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserManagementController {
	
	final UserManagementService userManagementService;
	
	public UserManagementController(UserManagementService userManagementService) {
		this.userManagementService = userManagementService;
	}
	
	@PostMapping("/saveUser")
	public boolean saveUser(@RequestBody UsersDTO userDto, CommonDTO dto,
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

	@PostMapping("/saveUserList")
	public boolean saveUserList(@RequestBody List<UsersDTO> userDto, CommonDTO dto,
							HttpServletRequest request) {
		try {
			dto.setIpAddress(request.getRemoteAddr());
			dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
			dto.setModule(Constants.USER_MANAGEMENT);
			return userManagementService.createUserList(userDto, dto);
		} catch (Exception e) {
			return false;
		}
	}
	
	@PostMapping("/updateUser")
	public boolean updateUser(@RequestBody UsersDTO userDto, CommonDTO dto,
			HttpServletRequest request) {
		try {
			dto.setIpAddress(request.getRemoteAddr());
			dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
			dto.setModule(Constants.USER_MANAGEMENT);
			return userManagementService.updateUser(userDto, dto);
		} catch (Exception e) {
			return false;
		}
	}
	
	@PostMapping("/findFilteredPatient/{search}/{pageNo}")
	public JSONObject patientService(@PathVariable String search,@PathVariable String pageNo) throws Exception {
        return userManagementService.filteredUsers(pageNo,search);
    }
	
	@GetMapping("/findById/{id}")
	public UsersDTO findDataById(@PathVariable Long id) {
		return userManagementService.findById(id);
	}
	
	@GetMapping("/countUser")
	public long countUser() {
		return userManagementService.totalUsers();
	}
	
	@GetMapping("/emailExists/{email}")
	public Boolean emailExists(@PathVariable String email) {
		return userManagementService.emailExists(email);
	}
}