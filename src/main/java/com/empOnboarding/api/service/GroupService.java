package com.empOnboarding.api.service;

import java.io.IOException;
import java.util.Date;

import org.springframework.stereotype.Service;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.GroupsDTO;
import com.empOnboarding.api.dto.UsersDTO;
import com.empOnboarding.api.entity.Constant;
import com.empOnboarding.api.entity.Groups;
import com.empOnboarding.api.entity.Users;
import com.empOnboarding.api.repository.ConstantRepository;
import com.empOnboarding.api.repository.GroupRepository;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.utils.CommonUtls;
import com.empOnboarding.api.utils.Constants;

@Service
public class GroupService {
	
	private GroupRepository groupRepository;
		
	private AuditTrailService auditTrailService;
	
	private ConstantRepository constantRepository;
	
	public GroupService(GroupRepository groupRepository, AuditTrailService auditTrailService,
			ConstantRepository constantRepository) {
		this.groupRepository = groupRepository;
		this.auditTrailService = auditTrailService;
		this.constantRepository = constantRepository;
	}
	
	
	public Boolean createGroup(GroupsDTO groupDto, CommonDTO dto, UserPrincipal user) throws IOException {
		Groups group = new Groups(null, groupDto.getName(),new Users(Long.valueOf(groupDto.getPgLead())),new Users(Long.valueOf(groupDto.getEgLead())),new Date(),new Date(),new Users(user.getId()),new Users(user.getId()));
		groupRepository.save(group);
		dto.setSystemRemarks(group.toString());
		dto.setModuleId(group.getName());
		auditTrailService.saveAuditTrail(Constants.DATA_INSERT.getValue(), dto);
		return true;
	}
	
	public GroupsDTO populateGroup(Groups group) {
		GroupsDTO gDto = new GroupsDTO();
		gDto.setId(group.getId().toString());
		gDto.setName(group.getName());
		gDto.setPgLead(group.getPgLead().getName());
		gDto.setEgLead(group.getEgLead().getName());
		Constant c = constantRepository.findByConstant("DateFormat");
		gDto.setCreatedTime(CommonUtls.datetoString(group.getCreatedTime(),c.getConstantValue()));
		gDto.setUpdatedTime(CommonUtls.datetoString(group.getUpdatedTime(),c.getConstantValue()));
		return gDto;
	}

}