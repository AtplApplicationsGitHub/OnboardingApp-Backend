package com.empOnboarding.api.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.DropDownDTO;
import com.empOnboarding.api.dto.GroupsDTO;
import com.empOnboarding.api.entity.Constant;
import com.empOnboarding.api.entity.Groups;
import com.empOnboarding.api.entity.Users;
import com.empOnboarding.api.repository.ConstantRepository;
import com.empOnboarding.api.repository.GroupRepository;
import com.empOnboarding.api.repository.QuestionRepository;
import com.empOnboarding.api.repository.UsersRepository;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.utils.CommonUtls;
import com.empOnboarding.api.utils.Constants;

@Service
public class GroupService {
	
	private GroupRepository groupRepository;
	
	private QuestionRepository questionRepository;
		
	private AuditTrailService auditTrailService;
	
	private ConstantRepository constantRepository;
	
	private MailerService mailerService;
	
	private UsersRepository usersRepository;
	
	public GroupService(GroupRepository groupRepository, AuditTrailService auditTrailService,
			ConstantRepository constantRepository, MailerService mailerService,UsersRepository usersRepository,
			QuestionRepository questionRepository) {
		this.groupRepository = groupRepository;
		this.auditTrailService = auditTrailService;
		this.constantRepository = constantRepository;
		this.mailerService = mailerService;
		this.usersRepository = usersRepository;
		this.questionRepository = questionRepository;
	}
	
	
	public Boolean createGroup(GroupsDTO groupDto, CommonDTO dto, UserPrincipal user) throws IOException {
		Users egLead = !CommonUtls.isCompletlyEmpty(groupDto.getEgLead())? new Users(Long.valueOf(groupDto.getEgLead())) : null;
		Groups group = new Groups(null, groupDto.getName(),new Users(Long.valueOf(groupDto.getPgLead())),egLead,new Date(),new Date(),new Users(user.getId()),new Users(user.getId()));
		groupRepository.save(group);
		dto.setSystemRemarks(group.toString());
		dto.setModuleId(group.getName());
		auditTrailService.saveAuditTrail(Constants.DATA_INSERT.getValue(), dto);
		return true;
	}
	
	public GroupsDTO populateGroup(Groups group) {
		GroupsDTO gDto = new GroupsDTO();
		Constant c = constantRepository.findByConstant("DateFormat");
		Long quesCount = questionRepository.countByGroupIdId(group.getId());
		gDto.setId(group.getId().toString());
		gDto.setName(group.getName());
		gDto.setPgLead(group.getPgLead().getName());
		gDto.setEgLead(group.getEgLead()!=null?group.getEgLead().getName():"-");
		gDto.setQuesCount(quesCount);
		if(quesCount==0L) {
			gDto.setDeleteFlag(true);
		}
		gDto.setCreatedTime(CommonUtls.datetoString(group.getCreatedTime(),c.getConstantValue()));
		gDto.setUpdatedTime(CommonUtls.datetoString(group.getUpdatedTime(),c.getConstantValue()));
		return gDto;
	}
	
	public GroupsDTO findById(Long id) {
		GroupsDTO gDTO = null;
		Optional<Groups> isPatient = groupRepository.findById(id);
		if (isPatient.isPresent()) {
			gDTO = populateGroup(isPatient.get());
		}
		return gDTO;
	}
	
	public Boolean updateGroup(GroupsDTO gDto, CommonDTO dto, UserPrincipal userp) throws IOException {
		Optional<Groups> gOpt = groupRepository.findById(Long.valueOf(gDto.getId()));
		Boolean result = false;
		if (!gOpt.isPresent()) {
			mailerService.sendEmailOnException(null);
		} else {
			Groups g = gOpt.get();
			g.setName(gDto.getName());
			g.setPgLead(new Users(Long.valueOf(gDto.getPgLead())));
			g.setEgLead(new Users(Long.valueOf(gDto.getEgLead())));
			g.setUpdatedTime(new Date());
			g.setUpdatedBy(new Users(userp.getId()));
			groupRepository.save(g);
			dto.setSystemRemarks(g.toString());
			dto.setModuleId(g.getName());
			auditTrailService.saveAuditTrail(Constants.DATA_UPDATE.getValue(), dto);
			result = true;
		}
		return result;
	}
	
	public void deleteGroup(Long id, CommonDTO dto) throws Exception{
		try {
			groupRepository.deleteById(id);
			dto.setModuleId("NA");
			dto.setSystemRemarks(Constants.GROUP_DELETE.getValue());
			auditTrailService.saveAuditTrail(Constants.DATA_DELETE.getValue(), dto);
		} catch (Exception e) {
			mailerService.sendEmailOnException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject filteredGroups(String pageNo) {
		JSONObject json = new JSONObject();
		Pageable pageable = PageRequest.of(Integer.parseInt(pageNo), 10);
		List<GroupsDTO> list;
		Page<Groups> gList = groupRepository.findAllByOrderByCreatedTimeAsc(pageable);
		list = gList.stream().map(this::populateGroup).collect(Collectors.toList());
		json.put("commonListDto", list);
		json.put("totalElements", gList.getTotalElements());
		return json;
	}
	
	public List<DropDownDTO> groupLeadDropDown() {
		List<DropDownDTO> dto = new ArrayList<>();
		List<Users> user = usersRepository.findAllByRole("group_lead");
		dto = user.stream().map(u -> new DropDownDTO(u.getId(),u.getName(),u.getEmail())).collect(Collectors.toList());
		return dto;
	}
	
	public long totalGroupCount() {
		return groupRepository.count();
	}

}