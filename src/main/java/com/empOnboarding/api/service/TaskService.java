package com.empOnboarding.api.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.empOnboarding.api.dto.*;
import com.empOnboarding.api.entity.*;
import com.empOnboarding.api.repository.TaskRepository;
import com.empOnboarding.api.repository.UsersRepository;
import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.empOnboarding.api.repository.ConstantRepository;
import com.empOnboarding.api.repository.QuestionRepository;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.utils.CommonUtls;
import com.empOnboarding.api.utils.Constants;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private final UsersRepository usersRepository;

    private final QuestionRepository questionRepository;

    private final ConstantRepository constantRepository;

    public TaskService(TaskRepository taskRepository, UsersRepository usersRepository, QuestionRepository questionRepository,
                       ConstantRepository constantRepository) {
        this.taskRepository = taskRepository;
        this.usersRepository = usersRepository;
        this.questionRepository = questionRepository;
        this.constantRepository = constantRepository;
    }

    @Transactional
    public void createTask(List<Employee> employees, UserPrincipal user) {
        Users actor = usersRepository.getReferenceById(user.getId());
        for (Employee emp : employees) {
            List<Questions> questions = questionRepository.findDistinctByLevel(emp.getLevel());
            Map<Long, List<Questions>> byGroup = questions.stream()
                    .filter(q -> q.getGroupId() != null && q.getGroupId().getId() != null)
                    .collect(Collectors.groupingBy(q -> q.getGroupId().getId()));
            Date now = new Date();
            byGroup.forEach((groupId, questionsList) -> {
                if (questionsList == null || questionsList.isEmpty()) return;
                Task task = new Task();
                task.setId(nextId());
                task.setEmployeeId(emp);
                task.setCreatedBy(actor);
                task.setUpdatedBy(actor);
                task.setCreatedTime(now);
                task.setUpdatedTime(now);
                Users lead = questionsList.get(0).getGroupId().getPgLead();
                task.setAssignedTo(lead != null ? lead : actor);
                for (Questions qn : questionsList) {
                    TaskQuestions tq = new TaskQuestions();
                    tq.setTaskId(task);
                    tq.setQuestionId(qn);
                    task.getTaskQuestions().add(tq);
                }
                taskRepository.save(task);
            });
        }
    }

    @Transactional(readOnly = true)
    public String nextId() {
        LocalDate now = LocalDate.now();
        String mm = String.format("%02d", now.getMonthValue());
        String yy = String.format("%02d", now.getYear() % 100);
        String prefix = "T" + mm + yy;
        Task last = taskRepository.findTopByIdStartingWithOrderByIdDesc(prefix);
        int nextSeq = 1;
        if (last != null) {
            String lastId = last.getId();
            String seqStr = lastId.substring(prefix.length());
            nextSeq = Integer.parseInt(seqStr) + 1;
        }
        return String.format("%s%05d", prefix, nextSeq);
    }

    public JSONObject filteredTask(String pageNo) {
        JSONObject json = new JSONObject();
        Pageable pageable = PageRequest.of(Integer.parseInt(pageNo), 10);
        List<TaskDTO> list;
        Page<Task> taskList;
        taskList = taskRepository.findAllByOrderByCreatedTimeDesc(pageable);
//        if (!CommonUtls.isCompletlyEmpty(search)) {
//            empList = taskRepository.findAllBySearch(search, pageable);
//        } else {
//        }
        list = taskList.stream().map(this::populateTask).collect(Collectors.toList());
        json.put("commonListDto", list);
        json.put("totalElements", taskList.getTotalElements());
        return json;
    }

    public TaskDTO populateTask(Task task) {
        TaskDTO tDto = new TaskDTO();
        Constant c = constantRepository.findByConstant("DateFormat");
        Employee e = task.getEmployeeId();
        tDto.setId(task.getId());
        tDto.setEmployeeName(e.getName());
        tDto.setLevel(e.getLevel());
        tDto.setRole(e.getRole());
        tDto.setLab(e.getLabAllocation());
        tDto.setPastExperience(e.getTotalExperience());
        tDto.setPrevCompany(e.getPastOrganization());
        tDto.setComplianceDay(e.getComplainceDay());
        tDto.setAssignedTo(task.getAssignedTo().getName());
        Set<TaskQuestions> tq = task.getTaskQuestions();
        long completed = tq.stream()
                .filter(q -> "completed".equalsIgnoreCase(q.getStatus()))
                .count();
        if (completed == tq.size()){
            tDto.setStatus("Completed");
        } else{
            tDto.setStatus("In Progress");
        }
        tDto.setTotalQuestions(tq.size());
        List<TaskQuestionsDTO> questionDtos = tq.stream()
                .map(q -> populateTaskQuestion(q, e.getDate())) // pass date via lambda
                .collect(Collectors.toList());
        tDto.setQuestionList(questionDtos);        tDto.setCompletedQuestions(completed);
        tDto.setCreatedTime(CommonUtls.datetoString(task.getCreatedTime(), c.getConstantValue()));
        tDto.setUpdatedTime(CommonUtls.datetoString(task.getUpdatedTime(), c.getConstantValue()));
        return tDto;
    }

    public TaskQuestionsDTO populateTaskQuestion(TaskQuestions taskQuestions,LocalDate date){
        TaskQuestionsDTO dto = new TaskQuestionsDTO();
        dto.setId(taskQuestions.getId().toString());
        dto.setQuestionId(taskQuestions.getQuestionId().getText());
        if(taskQuestions.getQuestionId().getPeriod().equalsIgnoreCase("after")){
            dto.setComplianceDay("+"+taskQuestions.getQuestionId().getComplainceDay());
        }else{
            dto.setComplianceDay("-"+taskQuestions.getQuestionId().getComplainceDay());
        }
        dto.setResponse(taskQuestions.getResponse());
        dto.setStatus(taskQuestions.getStatus());
        return dto;
    }

    public TaskDTO findById(String id) {
        TaskDTO tDTO = null;
        Optional<Task> isTask = taskRepository.findById(id);
        if (isTask.isPresent()) {
            tDTO = populateTask(isTask.get());
        }
        return tDTO;
    }
}