package com.empOnboarding.api.controller;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.EmployeeDTO;
import com.empOnboarding.api.security.CurrentUser;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.service.EmployeeService;
import com.empOnboarding.api.utils.Constants;

@RestController
@RequestMapping("/api/employee")
@CrossOrigin
public class EmployeeController {
	
	final EmployeeService employeeService;

	public EmployeeController(EmployeeService employeeService) {
		this.employeeService = employeeService;
	}
	
	@PostMapping("/saveEmployee")
	public boolean saveGroup(@RequestBody EmployeeDTO eDto, CommonDTO dto,
			HttpServletRequest request, @CurrentUser UserPrincipal user) {
		try {
			dto.setIpAddress(request.getRemoteAddr());
			dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
			dto.setModule(Constants.EMPLOYEE);
			return employeeService.createEmployee(eDto, dto,user);
		} catch (Exception e) {
			return false;
		}
	}
	
//	@PostMapping("/updateGroup")
//	public boolean updateGroup(@RequestBody GroupsDTO groupsDTO, CommonDTO dto,
//			HttpServletRequest request, @CurrentUser UserPrincipal user) {
//		try {
//			dto.setIpAddress(request.getRemoteAddr());
//			dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
//			dto.setModule(Constants.GROUPS);
//			return groupService.updateGroup(groupsDTO, dto, user);
//		} catch (Exception e) {
//			return false;
//		}
//	}
	
	@PostMapping("/findFilteredEmployee/{search}/{pageNo}")
	public JSONObject findFilteredGroups(@PathVariable String search,@PathVariable String pageNo) throws Exception {
        return employeeService.filteredEmployees(pageNo,search);
    }
	
	@GetMapping("/findById/{id}")
	public EmployeeDTO findDataById(@PathVariable Long id) {
		return employeeService.findById(id);
	}

	
	@DeleteMapping("/deleteGroup/{id}")
	public void deleteGroup(@PathVariable Long id, CommonDTO dto) throws Exception {
		employeeService.deleteEmployee(id, dto);
	}
}