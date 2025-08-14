package com.empOnboarding.api.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.empOnboarding.api.dto.PdfDTO;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.QuestionsDTO;
import com.empOnboarding.api.entity.Constant;
import com.empOnboarding.api.entity.Groups;
import com.empOnboarding.api.entity.QuestionLevel;
import com.empOnboarding.api.entity.Questions;
import com.empOnboarding.api.entity.Users;
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
	
	
	public QuestionService(QuestionRepository questionRepository,AuditTrailService auditTrailService,
			ConstantRepository constantRepository, MailerService mailerService) {
		this.questionRepository = questionRepository;
		this.auditTrailService = auditTrailService;
		this.constantRepository = constantRepository;
		this.mailerService = mailerService;
	}
	
	public Boolean createQuestion(QuestionsDTO qDto, CommonDTO dto, UserPrincipal user) throws IOException {
		Set<QuestionLevel> quesLevel = new HashSet<>();
		Questions ques = new Questions(null, qDto.getText(), qDto.getComplainceDay(), qDto.getResponse() ,new Groups(Long.valueOf(qDto.getGroupId())),
				quesLevel,new Date(),new Date(),new Users(user.getId()),new Users(user.getId()));
		if (qDto.getQuestionLevel() != null) {
			for (String level : qDto.getQuestionLevel()) {
				quesLevel.add(new QuestionLevel(null,level,ques,new Date()));
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
		qDto.setResponse(ques.getResponse());
		qDto.setGroupId(ques.getGroupId().getId().toString());
		
		qDto.setComplainceDay(ques.getComplainceDay());
		List<String> level = new ArrayList<String>();
		for (QuestionLevel qLevel : ques.getQuestionLevels()) {
			level.add(qLevel.getLevel());
		}
		qDto.setQuestionLevel(level);
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
	    Boolean result = false;
	    if (!quesOpt.isPresent()) {
	        mailerService.sendEmailOnException(null);
	    } else {
	        Questions q = quesOpt.get();
	        q.setText(qDto.getText());
	        q.setResponse(qDto.getResponse());
	        q.setComplainceDay(qDto.getComplainceDay());
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
	
	public void deleteQuestion(Long id, CommonDTO dto) throws Exception{
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

}