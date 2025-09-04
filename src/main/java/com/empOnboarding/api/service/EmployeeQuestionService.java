package com.empOnboarding.api.service;

import com.empOnboarding.api.dto.EmployeeQuestionDTO;
import com.empOnboarding.api.entity.EQuestions;
import com.empOnboarding.api.entity.Employee;
import com.empOnboarding.api.entity.EmployeeQuestions;
import com.empOnboarding.api.repository.EQuestionsRepository;
import com.empOnboarding.api.repository.EmployeeQuestionRepository;
import com.empOnboarding.api.repository.EmployeeRepository;
import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeQuestionService {

    private final EmployeeQuestionRepository employeeQuestionRepository;

    private final EQuestionsRepository eQuestionsRepository;

    private final EmployeeRepository employeeRepository;

    public EmployeeQuestionService(EmployeeQuestionRepository employeeQuestionRepository,EQuestionsRepository eQuestionsRepository,
                                   EmployeeRepository employeeRepository){
        this.employeeQuestionRepository = employeeQuestionRepository;
        this.eQuestionsRepository = eQuestionsRepository;
        this.employeeRepository = employeeRepository;
    }

    public Boolean createAnswer(Long id, String response){
        EmployeeQuestions eQ = employeeQuestionRepository.getReferenceById(id);
        eQ.setResponse(response);
        eQ.setCompletedFlag(true);
        employeeQuestionRepository.save(eQ);
        return true;
    }

    public void createEmployeeQuestion(String level,Long id){
        List<EQuestions> eq = eQuestionsRepository.findAllByLevelKey(level);
        Employee e = employeeRepository.getReferenceById(id);
        Date now = new Date();
        List<EmployeeQuestions> batch = new ArrayList<>(eq.size());
        for (EQuestions q : eq) {
            EmployeeQuestions row = new EmployeeQuestions(
                    null,          // id (generated)
                    e,   // employee
                    q,             // template question
                    null,          // answer (or default)
                    false,         // completed
                    now            // createdTime
            );
            batch.add(row);
        }
        if (!batch.isEmpty()) {
            employeeQuestionRepository.saveAll(batch);
        }
    }

    public EmployeeQuestionDTO populateEmployeeDto(EmployeeQuestions eQ){
        EmployeeQuestionDTO eQDto = new EmployeeQuestionDTO();
        eQDto.setId(eQ.getId().toString());
        eQDto.setQuestion(eQ.getQuestionId().getQuestions());
        eQDto.setResponseType(eQ.getQuestionId().getResponseType());
        eQDto.setResponse(eQ.getResponse());
        eQDto.setCompletedFlag(eQ.getCompletedFlag());
        return eQDto;
    }

    @SuppressWarnings("unchecked")
    public JSONObject filteredEmployeesQuestions(Long id,String pageNo) {
        JSONObject json = new JSONObject();
        Pageable pageable = PageRequest.of(Integer.parseInt(pageNo), 10);
        List<EmployeeQuestionDTO> list;
        Page<EmployeeQuestions> empQuestions = employeeQuestionRepository.findAllByEmployeeIdIdOrderByCreatedTimeDesc(id,pageable);
        list = empQuestions.stream().map(this::populateEmployeeDto).collect(Collectors.toList());
        json.put("commonListDto", list);
        json.put("totalElements", empQuestions.getTotalElements());
        return json;
    }

}