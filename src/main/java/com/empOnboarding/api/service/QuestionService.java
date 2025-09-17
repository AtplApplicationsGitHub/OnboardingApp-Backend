package com.empOnboarding.api.service;

import java.util.*;
import java.util.stream.Collectors;

import com.empOnboarding.api.dto.DropDownDTO;
import com.empOnboarding.api.entity.*;
import com.empOnboarding.api.repository.LookupItemsRepository;
import com.empOnboarding.api.repository.TaskRepository;
import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.QuestionsDTO;
import com.empOnboarding.api.repository.ConstantRepository;
import com.empOnboarding.api.repository.QuestionRepository;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.utils.CommonUtls;
import com.empOnboarding.api.utils.Constants;

@Service
public class QuestionService {
	
	private final QuestionRepository questionRepository;
	
	private final AuditTrailService auditTrailService;
	
	private final ConstantRepository constantRepository;
	
	private final MailerService mailerService;

	private final LookupItemsRepository lookupItemsRepository;

	private final TaskRepository taskRepository;
	
	
	public QuestionService(QuestionRepository questionRepository,AuditTrailService auditTrailService,
			ConstantRepository constantRepository, MailerService mailerService,LookupItemsRepository lookupItemsRepository,
						   TaskRepository taskRepository) {
		this.questionRepository = questionRepository;
		this.auditTrailService = auditTrailService;
		this.constantRepository = constantRepository;
		this.mailerService = mailerService;
		this.lookupItemsRepository = lookupItemsRepository;
		this.taskRepository = taskRepository;
	}
	
	public Boolean createQuestion(QuestionsDTO qDto, CommonDTO dto, UserPrincipal user) {
		Set<QuestionLevel> quesLevel = new HashSet<>();
		Set<QuestionsDepartment> quesDep = new HashSet<>();
		Questions ques = new Questions(null, qDto.getText(),qDto.getPeriod(), qDto.getComplainceDay(), qDto.getResponse() , qDto.getDefaultFlag(), new Groups(Long.valueOf(qDto.getGroupId())),
				quesLevel,quesDep,new Date(),new Date(),new Users(user.getId()),new Users(user.getId()));
		if (qDto.getQuestionLevel() != null) {
			for (String level : qDto.getQuestionLevel()) {
				quesLevel.add(new QuestionLevel(null,level,ques,new Date()));
			}
		}
		if (qDto.getQuestionDepartment() != null) {
			for (String dep : qDto.getQuestionDepartment()) {
				Optional<LookupItems> l = lookupItemsRepository.findByKey(dep);
                l.ifPresent(lookupItems -> quesDep.add(new QuestionsDepartment(null, ques, lookupItems)));
			}
		}
		questionRepository.save(ques);
		dto.setSystemRemarks(ques.toString());
		dto.setModuleId(ques.getText());
		auditTrailService.saveAuditTrail(Constants.DATA_INSERT.getValue(), dto);
		return true;
	}
	
	public QuestionsDTO populateQuestion(Questions ques) {
		QuestionsDTO qDto = new QuestionsDTO();
		qDto.setId(ques.getId().toString());
		qDto.setText(ques.getText());
		qDto.setDefaultFlag(ques.getDefaultFlag());
		qDto.setPeriod(ques.getPeriod());
		qDto.setResponse(ques.getResponse());
		qDto.setGroupId(ques.getGroupId().getId().toString());
		
		qDto.setComplainceDay(ques.getComplainceDay());
		List<String> level = new ArrayList<>();
		for (QuestionLevel qLevel : ques.getQuestionLevels()) {
			level.add(qLevel.getLevel());
		}
		qDto.setQuestionLevel(level);
		List<String> dep = new ArrayList<>();
		for (QuestionsDepartment qDep : ques.getQuestionDepartment()) {
			dep.add(qDep.getDepartment().getKey());
		}
		qDto.setQuestionDepartment(dep);
		Constant c = constantRepository.findByConstant("DateFormat");
		qDto.setCreatedTime(CommonUtls.datetoString(ques.getCreatedTime(),c.getConstantValue()));
		qDto.setUpdatedTime(CommonUtls.datetoString(ques.getUpdatedTime(),c.getConstantValue()));
		return qDto;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject findByGroupId(Long groupId,String pageNo) {		
		JSONObject json = new JSONObject();
		Pageable pageable = PageRequest.of(Integer.parseInt(pageNo), 10);
		List<QuestionsDTO> list;
		Page<Questions> qList = questionRepository.findAllByGroupIdIdOrderByCreatedTimeAsc(groupId,pageable);
		list = qList.stream().map(this::populateQuestion).collect(Collectors.toList());
		json.put("commonListDto", list);
		json.put("totalElements", qList.getTotalElements());
		return json;
	}
	
	public Boolean updateQuestion(QuestionsDTO qDto, CommonDTO dto, UserPrincipal user) {
	    Optional<Questions> quesOpt = questionRepository.findById(Long.valueOf(qDto.getId()));
	    boolean result = false;
	    if (quesOpt.isEmpty()) {
	        mailerService.sendEmailOnException(null);
	    } else {
	        Questions q = quesOpt.get();
	        q.setText(qDto.getText());
	        q.setResponse(qDto.getResponse());
			q.setDefaultFlag(qDto.getDefaultFlag());
			q.setPeriod(qDto.getPeriod());
	        q.setComplainceDay(qDto.getComplainceDay());
			q.setUpdatedBy(new Users(user.getId()));
			Set<QuestionLevel> quesLevel = new HashSet<>();
	        if (qDto.getQuestionLevel() != null && !qDto.getQuestionLevel().isEmpty()) {
	            for (String level : qDto.getQuestionLevel()) {
	                quesLevel.add(new QuestionLevel(null, level, q, new Date()));
	            }
	            q.setQuestionLevels(quesLevel);
	        }
	        questionRepository.save(q);
	        dto.setSystemRemarks(q.toString());
	        dto.setModuleId(q.getText());
	        auditTrailService.saveAuditTrail(Constants.DATA_UPDATE.getValue(), dto);
	        result = true;
	    }
	    return result;
	}
	
	public void deleteQuestion(Long id, CommonDTO dto){
		try {
			questionRepository.deleteById(id);
			dto.setModuleId("NA");
			dto.setSystemRemarks(Constants.QUESTION_DELETE.getValue());
			auditTrailService.saveAuditTrail(Constants.DATA_DELETE.getValue(), dto);
		} catch (Exception e) {
			mailerService.sendEmailOnException(e);
		}
	}
	
	public long countQuestions() {
		return questionRepository.count();
	}

	
	public long countQuestionsByGroup(Long id) {
		return questionRepository.countByGroupIdId(id);
	}

	public List<DropDownDTO> getGroups(String level, Long empId){
		List<DropDownDTO> dto;
		List<Task> t = taskRepository.findAllByEmployeeIdId(empId);
		List<Questions> q = questionRepository.findAllByQuestionLevelsLevel(level);
		List<String> assignedGroups = t.stream()
				.flatMap(task -> task.getTaskQuestions().stream())
				.map(tq -> tq.getQuestionId().getGroupId())
				.filter(Objects::nonNull)
				.map(Groups::getName)
				.filter(Objects::nonNull)
				.distinct()
				.toList();
		dto = q.stream().map(Questions::getGroupId)
				.filter(Objects::nonNull)
				.filter(g -> "No".equalsIgnoreCase(g.getAutoAssign()))
				.filter(g -> !assignedGroups.contains(g.getName()))
				.distinct().map(g -> new DropDownDTO(g.getId(),g.getName())).toList();
		return dto;
	}

}