package com.empOnboarding.api.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import com.empOnboarding.api.dto.*;
import com.empOnboarding.api.entity.*;
import com.empOnboarding.api.repository.*;
import com.empOnboarding.api.utils.Constants;
import org.json.simple.JSONObject;
import org.springframework.core.io.ClassPathResource;
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

    private final EmployeeFeedbackRepository employeeFeedbackRepository;

    private final EmployeeFeedbackArchRepository employeeFeedbackArchRepository;

    private final EmployeeRepository employeeRepository;

    private final TaskArchRepository taskArchRepository;

    private final AuditTrailService auditTrailService;

    private final MailerService mailerService;

    public TaskService(TaskRepository taskRepository, TaskQuestionRepository taskQuestionRepository,UsersRepository usersRepository, QuestionRepository questionRepository,
                       ConstantRepository constantRepository,GroupRepository groupRepository,
                       EmployeeFeedbackRepository employeeFeedbackRepository,EmployeeRepository employeeRepository,
                       TaskArchRepository taskArchRepository, AuditTrailService auditTrailService,EmployeeFeedbackArchRepository employeeFeedbackArchRepository,
                       MailerService mailerService) {
        this.taskRepository = taskRepository;
        this.usersRepository = usersRepository;
        this.questionRepository = questionRepository;
        this.constantRepository = constantRepository;
        this.groupRepository = groupRepository;
        this.taskQuestionRepository = taskQuestionRepository;
        this.employeeFeedbackRepository = employeeFeedbackRepository;
        this.employeeRepository = employeeRepository;
        this.auditTrailService = auditTrailService;
        this.taskArchRepository = taskArchRepository;
        this.employeeFeedbackArchRepository = employeeFeedbackArchRepository;
        this.mailerService = mailerService;
    }

    @Transactional
    public void createTask(List<Employee> employees, UserPrincipal user) {
        final Users actor = usersRepository.getReferenceById(user.getId());
        final Date now = new Date();

        for (Employee emp : employees) {
            final List<Questions> questions =
                    questionRepository.findDistinctByLevelAndDepartment(emp.getLevel(), emp.getDepartment());

            final Map<Long, List<Questions>> byGroup = questions.stream()
                    .filter(q -> q != null && q.getGroupId() != null && q.getGroupId().getId() != null)
                    .collect(Collectors.groupingBy(q -> q.getGroupId().getId()));

            for (Map.Entry<Long, List<Questions>> entry : byGroup.entrySet()) {
                final Long groupId = entry.getKey();
                final List<Questions> questionsList = entry.getValue();

                if (groupId == null || questionsList == null || questionsList.isEmpty()) {
                    continue;
                }

                final Groups g = groupRepository.findById(groupId).orElse(null);
                if (g == null) {
                    continue;
                }

                final String autoAssign = g.getAutoAssign();
                if (autoAssign == null || autoAssign.equalsIgnoreCase("No")) {
                    continue;
                }

                final Task task = new Task();
                task.setId(nextId());
                task.setEmployeeId(emp);
                task.setGroupId(g);
                task.setCreatedBy(actor);
                task.setUpdatedBy(actor);
                task.setCreatedTime(now);
                task.setUpdatedTime(now);
                task.setFreezeTask("N");
                if (task.getTaskQuestions() == null) {
                    task.setTaskQuestions(new HashSet<>());
                }

                final Users lead = (g.getPgLead() != null) ? g.getPgLead() : actor;
                task.setAssignedTo(lead);

                for (Questions qn : questionsList) {
                    if (qn == null) continue;

                    final TaskQuestions tq = new TaskQuestions();
                    tq.setTaskId(task);
                    tq.setQuestionId(qn);

                    final String def = qn.getDefaultFlag();
                    if ("yes".equalsIgnoreCase(def)) {
                        tq.setResponse("YES");
                        tq.setStatus("completed");
                    } else {
                        tq.setStatus("pending");
                    }

                    task.getTaskQuestions().add(tq);
                }
                try {
                    sendTaskAssign(task);
                } catch (Exception ignored) {
                }
                taskRepository.save(task);
            }
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
    public JSONObject filteredTask(String search,Long glId,String pageNo) {
        JSONObject json = new JSONObject();
        Pageable pageable = PageRequest.of(Integer.parseInt(pageNo), 10);
        List<TaskDTO> dtoList;
        Page<Task> taskList;
        if(!CommonUtls.isCompletlyEmpty(search)){
            taskList = taskRepository.findAllBySearch(search,glId,pageable);
        }else{
            taskList = taskRepository.findAllByAssignedToIdOrderByCreatedTimeDesc(glId,pageable);
        }
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
//            taskList.stream().map(m-> m.setDoj(m.getDoj().format(formatter))).collect(Collectors.toList());
        }else{
            taskList = taskRepository.findEmployeeTaskSummaries(pageable);
        }
        json.put("commonListDto", taskList);
        json.put("totalElements", taskList.getTotalElements());
        return json;
    }

    public JSONObject filteredArchiveTaskForAdmin(String search,String pageNo) {
        JSONObject json = new JSONObject();
        Pageable pageable = PageRequest.of(Integer.parseInt(pageNo), 10);
        Page<TaskProjection> taskList;
        if(!CommonUtls.isCompletlyEmpty(search)){
            taskList = taskRepository.findArchievedEmployeeTaskSummariesWithSearch(search,pageable);
//            taskList.stream().map(m-> m.setDoj(m.getDoj().format(formatter))).collect(Collectors.toList());
        }else{
            taskList = taskRepository.findArchievedEmployeeTaskSummaries(pageable);
        }
        json.put("commonListDto", taskList);
        json.put("totalElements", taskList.getTotalElements());
        return json;
    }

    @SuppressWarnings("unchecked")
    public JSONObject filteredTaskForEmployee(Long eId,String pageNo) {
        JSONObject json = new JSONObject();
        Pageable pageable = PageRequest.of(Integer.parseInt(pageNo), 10);
        List<TaskDTO> dtoList;
        Page<Task> taskList = taskRepository.findAllByEmployeeIdIdOrderByCreatedTimeDesc(eId,pageable);
        dtoList = taskList.stream().map(this::populateTask).collect(Collectors.toList());
        json.put("commonListDto", dtoList);
        json.put("totalElements", taskList.getTotalElements());
        return json;
    }


    public TaskDTO populateTask(Task task) {
        TaskDTO tDto = new TaskDTO();
        Constant c = constantRepository.findByConstant("DateFormat");
        Optional<EmployeeFeedback> ef = employeeFeedbackRepository.findByTaskIdId(task.getId());
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
        if(ef.isPresent()){
            tDto.setEFId(ef.get().getId().toString());
            tDto.setEFStar(ef.get().getStar());
            tDto.setFeedback(ef.get().getFeedback());
        }
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
                .sorted(Comparator.comparing(TaskQuestionsDTO::getCreatedTime)) // ascending
                .collect(Collectors.toList());
        tDto.setQuestionList(questionDto);
        tDto.setCompletedQuestions(completed);
        tDto.setCreatedTime(CommonUtls.datetoString(task.getCreatedTime(), c.getConstantValue()));
        tDto.setUpdatedTime(CommonUtls.datetoString(task.getUpdatedTime(), c.getConstantValue()));
        return tDto;
    }

    public TaskDTO populateTaskArch(TaskArch task) {
        TaskDTO tDto = new TaskDTO();
        Constant c = constantRepository.findByConstant("DateFormat");
        Optional<EmployeeFeedbackArch> ef = employeeFeedbackArchRepository.findByTaskIdId(task.getId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(c.getConstantValue());
        EmployeeArch e = task.getEmployeeId();
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
        Set<TaskQuestionsArch> tq = task.getTaskQuestions();
        if(ef.isPresent()){
            tDto.setEFId(ef.get().getId().toString());
            tDto.setEFStar(ef.get().getStar());
            tDto.setFeedback(ef.get().getFeedback());
        }
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
                .map(q -> populateTaskQuestionArch(q, e.getDate()))
                .sorted(Comparator.comparing(TaskQuestionsDTO::getCreatedTime)) // ascending
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
        
        try {
            Questions question = taskQuestions.getQuestionId();
            if (question != null) {
                dto.setQuestionId(question.getText());
                
                int offsetDays = question.getPeriod().equalsIgnoreCase("after")
                        ?  Integer.parseInt(question.getComplainceDay())
                        : -Integer.parseInt(question.getComplainceDay());
                LocalDate complianceDate = baseDate.plusDays(offsetDays);

                dto.setOverDueFlag(!"completed".equalsIgnoreCase(taskQuestions.getStatus())
                        && complianceDate.isBefore(LocalDate.now()));
                Date utilDate = java.sql.Date.valueOf(complianceDate);
                Constant c = constantRepository.findByConstant("DateFormat");
                String formattedDate = CommonUtls.datetoString(utilDate, c.getConstantValue());
                dto.setComplianceDay(formattedDate);
                dto.setResponseType(question.getResponse());
                dto.setCreatedTime(question.getCreatedTime().toString());
            } else {
                // Handle missing question gracefully
                dto.setQuestionId("Question not found (ID: " + taskQuestions.getId() + ")");
                dto.setComplianceDay(baseDate.toString());
                dto.setResponseType("TEXT");
                dto.setOverDueFlag(false);
                dto.setCreatedTime(new Date().toString());
            }
        } catch (Exception e) {
            // Handle any Hibernate proxy exceptions or missing entities
            System.err.println("Error loading question for TaskQuestion ID " + taskQuestions.getId() + ": " + e.getMessage());
            dto.setQuestionId("Question not available (Error loading question)");
            dto.setComplianceDay(baseDate.toString());
            dto.setResponseType("TEXT");
            dto.setOverDueFlag(false);
            dto.setCreatedTime(new Date().toString());
        }
        
        dto.setResponse(taskQuestions.getResponse());
        dto.setStatus(taskQuestions.getStatus());
        return dto;
    }

    public TaskQuestionsDTO populateTaskQuestionArch(TaskQuestionsArch taskQuestions, LocalDate baseDate) {
        TaskQuestionsDTO dto = new TaskQuestionsDTO();
        dto.setId(taskQuestions.getId().toString());

        try {
            // Check if the question exists and is accessible
            Questions question = taskQuestions.getQuestionId();
            if (question != null) {
                dto.setQuestionId(question.getText());

                int offsetDays = question.getPeriod().equalsIgnoreCase("after")
                        ?  Integer.parseInt(question.getComplainceDay())
                        : -Integer.parseInt(question.getComplainceDay());
                LocalDate complianceDate = baseDate.plusDays(offsetDays);

                dto.setOverDueFlag(!"completed".equalsIgnoreCase(taskQuestions.getStatus())
                        && complianceDate.isBefore(LocalDate.now()));
                Date utilDate = java.sql.Date.valueOf(complianceDate);
                Constant c = constantRepository.findByConstant("DateFormat");
                String formattedDate = CommonUtls.datetoString(utilDate, c.getConstantValue());
                dto.setComplianceDay(formattedDate);
                dto.setResponseType(question.getResponse());
                dto.setCreatedTime(question.getCreatedTime().toString());
            } else {
                // Handle missing question gracefully
                dto.setQuestionId("Question not found (ID: " + taskQuestions.getId() + ")");
                dto.setComplianceDay(baseDate.toString());
                dto.setResponseType("TEXT");
                dto.setOverDueFlag(false);
                dto.setCreatedTime(new Date().toString());
            }
        } catch (Exception e) {
            // Handle any Hibernate proxy exceptions or missing entities
            System.err.println("Error loading question for TaskQuestion ID " + taskQuestions.getId() + ": " + e.getMessage());
            dto.setQuestionId("Question not available (Error loading question)");
            dto.setComplianceDay(baseDate.toString());
            dto.setResponseType("TEXT");
            dto.setOverDueFlag(false);
            dto.setCreatedTime(new Date().toString());
        }

        dto.setResponse(taskQuestions.getResponse());
        dto.setStatus(taskQuestions.getStatus());
        return dto;
    }


    public List<TaskDTO> findById(String id) {
        List<TaskDTO> tDTOs;
        List<String> tId = Arrays.stream(id.split(",")).toList();
        List<Task> t =  taskRepository.findAllById(tId);
        tDTOs = t.stream().map(this::populateTask).collect(Collectors.toList());
        return tDTOs;
    }

    public List<TaskDTO> findArchiveTaskById(String id) {
        List<TaskDTO> tDTOs;
        List<String> tId = Arrays.stream(id.split(",")).toList();
        List<TaskArch> t =  taskArchRepository.findAllById(tId);
        tDTOs = t.stream().map(this::populateTaskArch).collect(Collectors.toList());
        return tDTOs;
    }

    public List<TaskDTO> findByEmpId(Long id) {
        List<TaskDTO> tDTOs;
        List<Task> t =  taskRepository.findAllByEmployeeIdIdAndFreezeTask(id,"N");
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
            String oldAssigned = task.getAssignedTo().getName();
            task.setAssignedTo(new Users(id));
            taskRepository.save(task);
            try{
                sendTaskReAssign(task,oldAssigned);
            } catch (Exception ignored) {
            }
            result = true;
        }
        return result;
    }

    LocalDate dueDate(LocalDate doj, int complianceDay, String period) {
        return "after".equalsIgnoreCase(period) ? doj.plusDays(complianceDay) : doj.minusDays(complianceDay);
    }

    boolean isCompleted(TaskQuestions tq) {
        return "completed".equalsIgnoreCase(String.valueOf(tq.getStatus()).trim());
    }

    public boolean taskQuestionAnswer(Long qId, String response){
        TaskQuestions tq = taskQuestionRepository.getReferenceById(qId);
        tq.setResponse(response);
        tq.setStatus("completed");
        taskQuestionRepository.save(tq);
        return true;
    }

    public long taskCountForAdmin(){
        return taskRepository.count();
    }

    public JSONObject taskCountForGL(UserPrincipal user) {
        JSONObject json = new JSONObject();
        LocalDate today = LocalDate.now();

        List<Task> tasks = taskRepository.findAllByAssignedToId(user.getId());

        long distinctEmployeeCount = tasks.stream()
                .map(Task::getEmployeeId)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        long fullyCompletedTaskCount = tasks.stream()
                .filter(t -> t.getTaskQuestions() != null && !t.getTaskQuestions().isEmpty())
                .filter(t -> t.getTaskQuestions().stream()
                        .allMatch(tq -> "completed".equalsIgnoreCase(String.valueOf(tq.getStatus()).trim()))
                )
                .count();

        long overdueTaskCount = tasks.stream()
                .filter(t -> t.getTaskQuestions() != null && !t.getTaskQuestions().isEmpty())
                .filter(t -> {
                    LocalDate doj = extractDoj(t);
                    if (doj == null) return false;

                    List<TaskQuestions> incomplete = t.getTaskQuestions().stream()
                            .filter(q -> !isCompleted(q))
                            .toList();

                    if (incomplete.isEmpty()) return false;

                    return incomplete.stream()
                            .map(q -> computeDue(doj, q))
                            .allMatch(due -> due != null && due.isBefore(today));
                })
                .count();

        long pendingTaskCount = tasks.stream()
                .filter(t -> t.getTaskQuestions() != null && !t.getTaskQuestions().isEmpty())
                .filter(t -> {
                    LocalDate doj = extractDoj(t);
                    if (doj == null) return false;

                    List<TaskQuestions> incomplete = t.getTaskQuestions().stream()
                            .filter(q -> !isCompleted(q))
                            .toList();

                    if (incomplete.isEmpty()) return false;
                    return incomplete.stream()
                            .map(q -> computeDue(doj, q))
                            .anyMatch(due -> due == null || !due.isBefore(today)); // null or due >= today
                })
                .count();

        json.put("totalEmployees", distinctEmployeeCount);
        json.put("totalCompletedTasks", fullyCompletedTaskCount);
        json.put("totalPendingTasks", pendingTaskCount);
        json.put("overdueTasks", overdueTaskCount);
        return json;
    }

    private static LocalDate computeDue(LocalDate doj, TaskQuestions tq) {
        if (tq == null || tq.getQuestionId() == null || doj == null) return null;
        Questions q = tq.getQuestionId();

        Integer days = parsePositiveIntOrNull(q.getComplainceDay());
        if (days == null) return null;

        String period = String.valueOf(q.getPeriod()).trim();
        if ("after".equalsIgnoreCase(period)) {
            return doj.plusDays(days);
        } else {
            return doj.minusDays(days);
        }
    }

    private static Integer parsePositiveIntOrNull(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        if (!trimmed.matches("\\d+")) return null;
        int v = Integer.parseInt(trimmed);
        return v >= 0 ? v : null;
    }

    private static LocalDate extractDoj(Task t) {
        if (t == null || t.getEmployeeId() == null) return null;
        try {
            return t.getEmployeeId().getDate();
        } catch (Exception ignored) {}
        try {
            LocalDate d = t.getEmployeeId().getDate();
            if (d != null) return d;
        } catch (Exception ignored) {}

        return null;
    }

    @Transactional
    public void createTaskManual(long employeeId, List<Long> groupIds, UserPrincipal user, CommonDTO dto) {
        // Load required refs
        final Employee e = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));
        final Users actor = usersRepository.getReferenceById(user.getId()); // should exist
        final Date now = new Date();
        if (groupIds == null || groupIds.isEmpty()) {
            return;
        }
        for (Long groupId : groupIds) {
            if (groupId == null) {
                continue;
            }

            final Groups g = groupRepository.findById(groupId).orElse(null);
            if (g == null) {
                continue;
            }
            final List<Questions> questions = questionRepository.findByGroupIdIdAndQuestionDepartmentDepartmentValue(groupId,e.getDepartment());
            if (questions == null || questions.isEmpty()) {
                // no questions for this group; skip
                continue;
            }

            // Build Task
            final Task task = new Task();
            task.setId(nextId()); // ensure non-null
            task.setEmployeeId(e);
            task.setGroupId(g);
            task.setCreatedBy(actor);
            task.setUpdatedBy(actor);
            task.setCreatedTime(now);
            task.setUpdatedTime(now);
            task.setFreezeTask("N");

            // Initialize Set if your entity doesn't by default
            if (task.getTaskQuestions() == null) {
                task.setTaskQuestions(new java.util.LinkedHashSet<>()); // deterministic order
            }

            // Assign to group's PG Lead if present; else actor
            final Users lead = (g.getPgLead() != null) ? g.getPgLead() : actor;
            task.setAssignedTo(lead);

            // Create TaskQuestions (use a guard against duplicates by question id)
            final java.util.Set<Long> seenQ = new java.util.HashSet<>();
            for (Questions qn : questions) {
                if (qn == null || qn.getId() == null) continue;
                if (!seenQ.add(qn.getId())) continue; // avoid duplicates

                final TaskQuestions tq = new TaskQuestions();
                tq.setTaskId(task);
                tq.setQuestionId(qn);

                final String def = qn.getDefaultFlag();
                if ("yes".equalsIgnoreCase(def)) {
                    tq.setResponse("YES");
                    tq.setStatus("completed");
                } else {
                    tq.setStatus("pending");
                }

                task.getTaskQuestions().add(tq);
            }
            taskRepository.save(task);
            try{
                sendTaskAssign(task);
            }catch(Exception ignored){
            }
        }
        if (dto != null) {
            dto.setModuleId("NA");
            dto.setSystemRemarks("Manual task creation");
            auditTrailService.saveAuditTrail("DATA_CREATE", dto);
        }
    }

    private void sendTaskAssign(final Task t) {
        CompletableFuture.runAsync(() -> {
            try {
                Constant c = constantRepository.findByConstant("TaskPageGL");
                InputStream inputStream2 = new ClassPathResource("EmailTemplates/TaskAssign.html")
                        .getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream2));
                String emailBody;
                StringBuilder stringBuilder = new StringBuilder();
                while ((emailBody = br.readLine()) != null) {
                    stringBuilder.append(emailBody);
                }
                emailBody = stringBuilder.toString();
                emailBody = emailBody.replaceFirst("@src", Constants.TASK_ASSIGN);
                emailBody = emailBody.replaceFirst("@name", t.getGroupId().getPgLead().getName());
                emailBody = emailBody.replaceFirst("@taskId", t.getId());
                emailBody = emailBody.replaceFirst("@employeeName", t.getEmployeeId().getName());
                emailBody = emailBody.replaceFirst("@groupId", t.getGroupId().getName());
                emailBody = emailBody.replaceFirst("@taskUrl", c.getConstantValue());
                long totalQuestion = t.getTaskQuestions().size();
                long pendingCount = t.getTaskQuestions().stream()
                        .filter(q -> "pending".equalsIgnoreCase(q.getStatus()))
                        .count();
                long completedCount = totalQuestion-pendingCount;
                emailBody = emailBody.replaceFirst("@totalQuestion", String.valueOf(totalQuestion));
                emailBody = emailBody.replaceFirst("@pendingQuestion",  String.valueOf(pendingCount));
                emailBody = emailBody.replaceFirst("@completedQuestion", String.valueOf(completedCount));

                EmailDetailsDTO emailDetailsDTO = new EmailDetailsDTO(Constants.TASK_ASSIGN,
                        t.getGroupId().getPgLead().getEmail().split(","), null, null, emailBody);
                mailerService.sendHTMLMail(emailDetailsDTO);
            } catch (Exception ignored) {
            }
        });
    }

    private void sendTaskReAssign(final Task t,final String oldAssigned) {
        CompletableFuture.runAsync(() -> {
            try {
                Constant c = constantRepository.findByConstant("TaskPageGL");
                InputStream inputStream2 = new ClassPathResource("EmailTemplates/ReassignTask.html")
                        .getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream2));
                String emailBody;
                StringBuilder stringBuilder = new StringBuilder();
                while ((emailBody = br.readLine()) != null) {
                    stringBuilder.append(emailBody);
                }
                emailBody = stringBuilder.toString();
                emailBody = emailBody.replaceFirst("@src", Constants.TASK_REASSIGN);
                emailBody = emailBody.replaceFirst("@name", t.getGroupId().getPgLead().getName());
                emailBody = emailBody.replaceFirst("@fromUser", oldAssigned);
                emailBody = emailBody.replaceFirst("@taskId", t.getId());
                emailBody = emailBody.replaceFirst("@employeeName", t.getEmployeeId().getName());
                emailBody = emailBody.replaceFirst("@groupId", t.getGroupId().getName());
                emailBody = emailBody.replaceFirst("@taskUrl", c.getConstantValue());
                long totalQuestion = t.getTaskQuestions().size();
                long pendingCount = t.getTaskQuestions().stream()
                        .filter(q -> "pending".equalsIgnoreCase(q.getStatus()))
                        .count();
                long completedCount = totalQuestion-pendingCount;
                emailBody = emailBody.replaceFirst("@totalQuestion", String.valueOf(totalQuestion));
                emailBody = emailBody.replaceFirst("@pendingQuestion",  String.valueOf(pendingCount));
                emailBody = emailBody.replaceFirst("@completedQuestion", String.valueOf(completedCount));

                EmailDetailsDTO emailDetailsDTO = new EmailDetailsDTO(Constants.TASK_REASSIGN,
                        t.getGroupId().getPgLead().getEmail().split(","), null, null, emailBody);
                mailerService.sendHTMLMail(emailDetailsDTO);
            } catch (Exception ignored) {
            }
        });
    }

    public void taskReminder() {
        final LocalDate todayPlusOne = LocalDate.now().plusDays(1);
        final List<Task> tasks = taskRepository.findAllByFreezeTask("N");
        final Map<Users, List<Task>> tasksByPgLead = tasks.stream()
                .filter(t -> t != null
                        && t.getGroupId() != null
                        && t.getGroupId().getPgLead() != null)
                .collect(Collectors.groupingBy(t -> t.getGroupId().getPgLead()));
        for (Map.Entry<Users, List<Task>> entry : tasksByPgLead.entrySet()) {
            final Users pgLead = entry.getKey();
            final List<Task> taskList = entry.getValue();
            final List<Task> pendingForLead = new ArrayList<>();
            for (Task task : taskList) {
                if (task == null || task.getEmployeeId() == null) continue;
                final LocalDate doj = task.getEmployeeId().getDate();
                if (doj == null) continue;
                final Collection<TaskQuestions> tqs = task.getTaskQuestions();
                if (tqs == null || tqs.isEmpty()) continue;
                boolean added = false;
                for (TaskQuestions tq : tqs) {
                    if (tq == null) continue;
                    if ("Completed".equals(tq.getStatus())) continue;
                    final LocalDate due = computeDue(doj, tq);
                    if (due != null && due.isEqual(todayPlusOne)) {
                        pendingForLead.add(task);
                        added = true;
                        break;
                    }
                }
                if (added) {
                   break;
                }
            }
            if (!pendingForLead.isEmpty()) {
                sendTaskIncompleteReminder(pgLead, pendingForLead);
            }
        }
    }

    private void sendTaskIncompleteReminder(final Users u,final List<Task> t) {
        CompletableFuture.runAsync(() -> {
            try {
                InputStream inputStream2 = new ClassPathResource("EmailTemplates/TaskInCompleteReminder.html")
                        .getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream2));
                String emailBody;
                StringBuilder stringBuilder = new StringBuilder();
                while ((emailBody = br.readLine()) != null) {
                    stringBuilder.append(emailBody);
                }
                emailBody = stringBuilder.toString();
                emailBody = emailBody.replaceFirst("@src", Constants.WELCOME_MAIL_NOTE_FOR_NEW_EMPLOYEE);
//                emailBody = emailBody.replaceFirst("@email", dto.getEmail());
                EmailDetailsDTO emailDetailsDTO = new EmailDetailsDTO(Constants.WELCOME_MAIL_NOTE_FOR_NEW_EMPLOYEE,
                       u.getEmail().split(","), null, null, emailBody);
                mailerService.sendHTMLMail(emailDetailsDTO);
            } catch (Exception ignored) {
            }
        });
    }



}