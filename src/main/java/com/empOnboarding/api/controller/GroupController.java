package com.empOnboarding.api.controller;

import java.util.List;

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
import com.empOnboarding.api.dto.DropDownDTO;
import com.empOnboarding.api.dto.GroupsDTO;
import com.empOnboarding.api.security.CurrentUser;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.service.GroupService;
import com.empOnboarding.api.utils.Constants;

@RestController
@RequestMapping("/api/group")
@CrossOrigin
public class GroupController {

	final GroupService groupService;

	public GroupController(GroupService groupService) {
		this.groupService = groupService;
	}
	
	@PostMapping("/saveGroup")
	public boolean saveGroup(@RequestBody GroupsDTO gDto, CommonDTO dto,
			HttpServletRequest request, @CurrentUser UserPrincipal user) {
		try {
			dto.setIpAddress(request.getRemoteAddr());
			dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
			dto.setModule(Constants.GROUPS);
			return groupService.createGroup(gDto, dto,user);
		} catch (Exception e) {
			return false;
		}
	}
	
	@PostMapping("/updateGroup")
	public boolean updateGroup(@RequestBody GroupsDTO groupsDTO, CommonDTO dto,
			HttpServletRequest request, @CurrentUser UserPrincipal user) {
		try {
			dto.setIpAddress(request.getRemoteAddr());
			dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
			dto.setModule(Constants.GROUPS);
			return groupService.updateGroup(groupsDTO, dto, user);
		} catch (Exception e) {
			return false;
		}
	}
	
	@PostMapping("/findFilteredGroups/{pageNo}")
	public JSONObject findFilteredGroups(@PathVariable String pageNo) throws Exception {
        return groupService.filteredGroups(pageNo);
    }
	
	@GetMapping("/findById/{id}")
	public GroupsDTO findDataById(@PathVariable Long id) {
		return groupService.findById(id);
	}
	
	@GetMapping("/loadGL")
	public List<DropDownDTO> loadGL() {
		return groupService.groupLeadDropDown();
	}
	
	@DeleteMapping("/deleteGroup/{id}")
	public void deleteGroup(@PathVariable Long id, CommonDTO dto) throws Exception {
		groupService.deleteGroup(id, dto);
	}

	@GetMapping("/countGroup")
	public long countGroup() {
		return groupService.totalGroupCount();
	}

	@PostMapping("/cloneGroup")
	public Boolean cloneGroup(@RequestBody GroupsDTO groupsDTO,@CurrentUser UserPrincipal user){
		return groupService.cloneGroup(groupsDTO,user);
	}
}