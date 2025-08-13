package com.empOnboarding.api.controller;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.QuestionsDTO;
import com.empOnboarding.api.security.CurrentUser;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.service.QuestionService;
import com.empOnboarding.api.utils.Constants;

@RestController
@RequestMapping("/api/question")
@CrossOrigin
public class QuestionController {
	
	final QuestionService questionService;
	
	public QuestionController(QuestionService questionService) {
		this.questionService = questionService;
	}
	
	@PostMapping("/saveQuestion")
	public boolean saveQuestion(@RequestBody QuestionsDTO qDto, CommonDTO dto,
			HttpServletRequest request, @CurrentUser UserPrincipal user) {
		try {
			dto.setIpAddress(request.getRemoteAddr());
			dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
			dto.setModule(Constants.QUESTIONS);
			return questionService.createQuestion(qDto, dto,user);
		} catch (Exception e) {
			return false;
		}
	}
	
	@PostMapping("/findFilteredQuestionByGroup/{pageNo}/{groupId}")
	public JSONObject findFilteredGroups(@PathVariable String pageNo, @PathVariable Long groupId) throws Exception {
        return questionService.findByGroupId(groupId,pageNo);
    }
	
	@PostMapping("/updateQuestion")
	public boolean updateQuestion(@RequestBody QuestionsDTO qDto, CommonDTO dto,
			HttpServletRequest request, @CurrentUser UserPrincipal user) {
		try {
			dto.setIpAddress(request.getRemoteAddr());
			dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
			dto.setModule(Constants.GROUPS);
			return questionService.updateQuestion(qDto, dto, user);
		} catch (Exception e) {
			return false;
		}
	}
	
	@DeleteMapping("/deleteQuestion/{id}")
	public void deleteQuestion(@PathVariable Long id, CommonDTO dto) throws Exception {
		questionService.deleteQuestion(id, dto);
	}

}