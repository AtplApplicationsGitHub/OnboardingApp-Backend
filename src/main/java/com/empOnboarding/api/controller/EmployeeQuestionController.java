package com.empOnboarding.api.controller;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.EmployeeDTO;
import com.empOnboarding.api.security.CurrentUser;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.service.EmployeeQuestionService;
import com.empOnboarding.api.utils.Constants;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/api/eQuestions")
@CrossOrigin
public class EmployeeQuestionController {

    private final EmployeeQuestionService employeeQuestionService;

    public EmployeeQuestionController(EmployeeQuestionService employeeQuestionService) {
        this.employeeQuestionService = employeeQuestionService;
    }

    @PostMapping("/saveEmployeeResponse/{id}/{response}")
    public boolean saveEmployeeResponse(@PathVariable Long id,@PathVariable String response,
                                        @CurrentUser UserPrincipal user,CommonDTO dto,HttpServletRequest request) {
        try {
            dto.setIpAddress(request.getRemoteAddr());
            dto.setAgentRequestForAuditTrail(request.getHeader(Constants.USER_AGENT.getValue()));
            dto.setModule(Constants.EMPLOYEE);
            return employeeQuestionService.createAnswer(id,response);
        } catch (Exception e) {
            return false;
        }
    }





}