package com.empOnboarding.api.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import com.empOnboarding.api.dto.*;
import com.empOnboarding.api.entity.*;
import com.empOnboarding.api.repository.*;
import org.json.simple.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.utils.CommonUtls;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private final TaskQuestionRepository taskQuestionRepository;

    private final UsersRepository usersRepository;

    private final QuestionRepository questionRepository;

    private final ConstantRepository constantRepository;

    private final GroupRepository groupRepository;

    public TaskService(TaskRepository taskRepository, TaskQuestionRepository taskQuestionRepository,UsersRepository usersRepository, QuestionRepository questionRepository,
                       ConstantRepository constantRepository,GroupRepository groupRepository) {
        this.taskRepository = taskRepository;
        this.usersRepository = usersRepository;
        this.questionRepository = questionRepository;
        this.constantRepository = constantRepository;
        this.groupRepository = groupRepository;
        this.taskQuestionRepository = taskQuestionRepository;
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
                Groups g = groupRepository.getReferenceById(groupId);
                if (questionsList == null || questionsList.isEmpty()) return;
                Task task = new Task();
                task.setId(nextId());
                task.setEmployeeId(emp);
                task.setGroupId(g);
                task.setCreatedBy(actor);
                task.setUpdatedBy(actor);
                task.setCreatedTime(now);
                task.setUpdatedTime(now);
                task.setFreezeTask("N");
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

    @SuppressWarnings("unchecked")
    public JSONObject filteredTask(String pageNo) {
        JSONObject json = new JSONObject();
        Pageable pageable = PageRequest.of(Integer.parseInt(pageNo), 10);
        List<TaskDTO> dtoList;
        Page<Task> taskList;
        taskList = taskRepository.findAllByOrderByCreatedTimeDesc(pageable);
        dtoList = taskList.stream().map(this::populateTask).collect(Collectors.toList());
        json.put("commonListDto", dtoList);
        json.put("totalElements", taskList.getTotalElements());
        return json;
    }



    @SuppressWarnings("unchecked")
    public JSONObject filteredTaskForAdmin(String search,String pageNo) {
        JSONObject json = new JSONObject();
        Pageable pageable = PageRequest.of(Integer.parseInt(pageNo), 10);
        Page<TaskProjection> taskList;
        if(!CommonUtls.isCompletlyEmpty(search)){
            taskList = taskRepository.findEmployeeTaskSummariesWithSearch(search,pageable);
        }else{
            taskList = taskRepository.findEmployeeTaskSummaries(pageable);
        }
        json.put("commonListDto", taskList);
        json.put("totalElements", taskList.getTotalElements());
        return json;
    }


    public TaskDTO populateTask(Task task) {
        TaskDTO tDto = new TaskDTO();
        Constant c = constantRepository.findByConstant("DateFormat");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(c.getConstantValue());
        Employee e = task.getEmployeeId();
        tDto.setId(task.getId());
        tDto.setGroupName(task.getGroupId().getName());
        tDto.setEmployeeId(e.getId());
        tDto.setEmployeeName(e.getName());
        tDto.setLevel(e.getLevel());
        tDto.setDepartment(e.getDepartment());
        tDto.setRole(e.getRole());
        tDto.setLab(e.getLabAllocation());
        tDto.setPastExperience(e.getTotalExperience());
        tDto.setPrevCompany(e.getPastOrganization());
        tDto.setComplianceDay(e.getComplainceDay());
        tDto.setAssignedTo(task.getAssignedTo().getName());
        tDto.setFreezeTask(task.getFreezeTask());
        Set<TaskQuestions> tq = task.getTaskQuestions();
        long completed = tq.stream()
                .filter(q -> "completed".equalsIgnoreCase(q.getStatus()))
                .count();
        if (completed == tq.size()){
            tDto.setStatus("Completed");
        } else{
            tDto.setStatus("In Progress");
        }
        tDto.setDoj(e.getDate().format(formatter));
        tDto.setTotalQuestions(tq.size());
        List<TaskQuestionsDTO> questionDto = tq.stream()
                .map(q -> populateTaskQuestion(q, e.getDate()))
                .collect(Collectors.toList());
        tDto.setQuestionList(questionDto);
        tDto.setCompletedQuestions(completed);
        tDto.setCreatedTime(CommonUtls.datetoString(task.getCreatedTime(), c.getConstantValue()));
        tDto.setUpdatedTime(CommonUtls.datetoString(task.getUpdatedTime(), c.getConstantValue()));
        return tDto;
    }

    public TaskQuestionsDTO populateTaskQuestion(TaskQuestions taskQuestions, LocalDate baseDate) {
        TaskQuestionsDTO dto = new TaskQuestionsDTO();
        dto.setId(taskQuestions.getId().toString());
        dto.setQuestionId(taskQuestions.getQuestionId().getText());

        // Determine sign
        int offsetDays = taskQuestions.getQuestionId().getPeriod().equalsIgnoreCase("after")
                ?  Integer.parseInt(taskQuestions.getQuestionId().getComplainceDay())
                : -Integer.parseInt(taskQuestions.getQuestionId().getComplainceDay());
        LocalDate complianceDate = baseDate.plusDays(offsetDays);

        dto.setOverDueFlag(!"completed".equalsIgnoreCase(taskQuestions.getStatus())
                && complianceDate.isBefore(LocalDate.now()));
        Date utilDate = java.sql.Date.valueOf(complianceDate);
        Constant c = constantRepository.findByConstant("DateFormat");
        String formattedDate = CommonUtls.datetoString(utilDate, c.getConstantValue());
        dto.setComplianceDay(formattedDate);

        dto.setResponse(taskQuestions.getResponse());
        dto.setStatus(taskQuestions.getStatus());

        return dto;
    }



    public List<TaskDTO> findById(String id) {
        List<TaskDTO> tDTOs = new ArrayList<>();
        List<String> tId = Arrays.stream(id.split(",")).toList();
        List<Task> t =  taskRepository.findAllById(tId);
        tDTOs = t.stream().map(this::populateTask).collect(Collectors.toList());
        return tDTOs;
    }

    public Boolean freezeTask(String id){
        List<String> taskIdList = Arrays.stream(id.split(",")).toList();
        List<Task> t =  taskRepository.findAllById(taskIdList);
        t.forEach(task -> task.setFreezeTask("Y"));
        taskRepository.saveAll(t);
        return true;
    }

    public boolean reassignTask(String taskId, Long id){
        Optional<Task> t = taskRepository.findById(taskId);
        boolean result = false;
        if (t.isPresent()){
            Task task = t.get();
            task.setAssignedTo(new Users(id));
            taskRepository.save(task);
            result = true;
        }
        return result;
    }

    public boolean taskQuestionAnswer(Long qId, String response){
        TaskQuestions tq = taskQuestionRepository.getReferenceById(qId);
        tq.setResponse(response);
        taskQuestionRepository.save(tq);
        return true;
    }
}