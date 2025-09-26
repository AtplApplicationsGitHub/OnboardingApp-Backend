package com.empOnboarding.api.controller;
import com.empOnboarding.api.dto.EmployeeDTO;
import com.empOnboarding.api.dto.TaskDTO;
import com.empOnboarding.api.security.CurrentUser;
import com.empOnboarding.api.security.UserPrincipal;
import com.empOnboarding.api.service.TaskService;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/task")
@CrossOrigin
public class TaskController {

    final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/findFilteredTask/{search}/{pageNo}")
    public JSONObject findFilteredGroups(@PathVariable String search, @PathVariable String pageNo,
                                         @CurrentUser UserPrincipal user) {
        return taskService.filteredTask(search,user.getId(),pageNo);
    }

    @PostMapping("/filteredTaskForAdmin/{search}/{pageNo}")
    public JSONObject filteredTaskForAdmin(@PathVariable String search,@PathVariable String pageNo) {
        return taskService.filteredTaskForAdmin(search,pageNo);
    }

    @PostMapping("/filteredArchiveTaskForAdmin/{search}/{pageNo}")
    public JSONObject filteredArchiveTaskForAdmin(@PathVariable String search,@PathVariable String pageNo) {
        return taskService.filteredArchiveTaskForAdmin(search,pageNo);
    }

    @PostMapping("/filteredTaskForEmployee/{pageNo}")
    public JSONObject filteredTaskForEmployee(@CurrentUser UserPrincipal user,@PathVariable String pageNo) {
        return taskService.filteredTaskForEmployee(user.getId(),pageNo);
    }


    @GetMapping("/findById/{id}")
    public List<TaskDTO> findDataById(@PathVariable String id) {
        return taskService.findById(id);
    }

    @GetMapping("/findByArchTaskId/{id}")
    public List<TaskDTO> findArchDataById(@PathVariable String id) {
        return taskService.findArchiveTaskById(id);
    }

    @GetMapping("/findByEmpId/{id}")
    public List<TaskDTO> findDataById(@PathVariable Long id) {
        return taskService.findByEmpId(id);
    }

    @GetMapping("/reassignTask/{taskId}/{id}")
    public boolean reassignTask(@PathVariable String taskId,@PathVariable Long id) {
        return taskService.reassignTask(taskId,id);
    }

    @GetMapping("/freezeTask/{taskId}")
    public boolean freezeTask(@PathVariable String taskId) {
        return taskService.freezeTask(taskId);
    }

    @PostMapping("/taskQuestionAnswer/{qId}/{response}")
    public boolean taskQuestionAnswer(@PathVariable Long qId,@PathVariable String response) {
        return taskService.taskQuestionAnswer(qId,response);
    }

    @GetMapping("/taskCountForAdmin")
    public long taskCountForAdmin() {
        return taskService.taskCountForAdmin();
    }

    @GetMapping("/taskCountForGL")
    public JSONObject taskCountForGL(@CurrentUser UserPrincipal user) {
        return taskService.taskCountForGL(user);
    }

}