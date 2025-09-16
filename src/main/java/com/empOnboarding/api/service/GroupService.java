package com.empOnboarding.api.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.empOnboarding.api.dto.QuestionsDTO;
import com.empOnboarding.api.entity.*;
import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.DropDownDTO;
import com.empOnboarding.api.dto.GroupsDTO;
import com.empOnboarding.api.repository.ConstantRepository;
import com.empOnboarding.api.repository.GroupRepository;
import com.empOnboarding.api.repository.QuestionRepository;
import com.empOnboarding.api.repository.UsersRepository;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.utils.CommonUtls;
import com.empOnboarding.api.utils.Constants;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupService {
	
	private final GroupRepository groupRepository;
	
	private final QuestionRepository questionRepository;
		
	private final AuditTrailService auditTrailService;
	
	private final ConstantRepository constantRepository;
	
	private final MailerService mailerService;
	
	private final UsersRepository usersRepository;

	private final QuestionService questionService;
	
	public GroupService(GroupRepository groupRepository, AuditTrailService auditTrailService,
			ConstantRepository constantRepository, MailerService mailerService,UsersRepository usersRepository,
			QuestionRepository questionRepository,QuestionService questionService) {
		this.groupRepository = groupRepository;
		this.auditTrailService = auditTrailService;
		this.constantRepository = constantRepository;
		this.mailerService = mailerService;
		this.usersRepository = usersRepository;
		this.questionRepository = questionRepository;
		this.questionService = questionService;
	}
	
	
	public Boolean createGroup(GroupsDTO groupDto, CommonDTO dto, UserPrincipal user) throws IOException {
		Users egLead = !CommonUtls.isCompletlyEmpty(groupDto.getEgLead())? new Users(Long.valueOf(groupDto.getEgLead())) : null;
		Groups group = new Groups(null, groupDto.getName(),new Users(Long.valueOf(groupDto.getPgLead())),
				egLead,groupDto.getAutoAssign(),new Date(),new Date(),new Users(user.getId()),new Users(user.getId()));
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
		gDto.setAutoAssign(group.getAutoAssign());
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
			if(!CommonUtls.isCompletlyEmpty(gDto.getEgLead())) {
				g.setEgLead(new Users(Long.valueOf(gDto.getEgLead())));
			}
			g.setAutoAssign(g.getAutoAssign());
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
		Page<Groups> gList = groupRepository.findAllByOrderByCreatedTimeDesc(pageable);
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

	@Transactional
	public Boolean cloneGroup(GroupsDTO groupDto, UserPrincipal user) {
		Long sourceGroupId = Long.valueOf(groupDto.getId());
		Groups g = groupRepository.getReferenceById(sourceGroupId);
		Date now = new Date();
		Users actor = new Users(user.getId());
		Groups group = new Groups();
		group.setName(g.getName());
		group.setPgLead(g.getPgLead());
		if (groupDto.getEgLead() != null) group.setEgLead(g.getEgLead());
		group.setAutoAssign(g.getAutoAssign());
		group.setCreatedTime(now);
		group.setUpdatedTime(now);
		group.setCreatedBy(actor);
		group.setUpdatedBy(actor);
		group = groupRepository.save(group);
		final String newGroupIdStr = String.valueOf(group.getId());
		List<QuestionsDTO> originals = questionRepository.findAllByGroupIdId(sourceGroupId)
				.stream()
				.map(questionService::populateQuestion)
				.peek(dto -> {
					dto.setId(null);
					dto.setGroupId(newGroupIdStr);
				})
				.toList();
		for (QuestionsDTO qdto : originals) {
			CommonDTO cdto = new CommonDTO();
			questionService.createQuestion(qdto,cdto,user);
		}
		return true;
	}


}