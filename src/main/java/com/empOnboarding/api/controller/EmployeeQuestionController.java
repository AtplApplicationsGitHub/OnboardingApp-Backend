package com.empOnboarding.api.controller;

import com.empOnboarding.api.dto.CommonDTO;
import com.empOnboarding.api.dto.EmployeeDTO;
import com.empOnboarding.api.dto.EmployeeQuestionDTO;
import com.empOnboarding.api.security.CurrentUser;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.service.EmployeeQuestionService;
import com.empOnboarding.api.utils.Constants;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


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

    @PostMapping("/filteredEmployeesQues/{userId}/{pageNo}")
    public JSONObject filteredEmployeesQues(@PathVariable Long userId,@PathVariable String pageNo) throws Exception {
        return employeeQuestionService.filteredEmployeesQuestions(userId,pageNo);
    }

    @GetMapping("/getByTaskId/{taskId}")
    public List<EmployeeQuestionDTO> getEmployeeQuestionsByTask(@PathVariable String taskId) {
        return employeeQuestionService.getEmployeeQuestionsByTask(taskId);
    }

    @GetMapping("/employeesWithQuestions")
    public List<Long> getEmployeesWithQuestions() {
        return employeeQuestionService.getEmployeesWithQuestions();
    }

    @GetMapping("/employeesArchWithQuestions")
    public List<Long> getEmployeesArchWithQuestions() {
        return employeeQuestionService.getEmployeesArchWithQuestions();
    }

}