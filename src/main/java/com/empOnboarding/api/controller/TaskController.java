package com.empOnboarding.api.controller;
import com.empOnboarding.api.dto.EmployeeDTO;
import com.empOnboarding.api.dto.TaskDTO;
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
    public JSONObject findFilteredGroups(@PathVariable String search,@PathVariable String pageNo) {
        return taskService.filteredTask(pageNo);
    }

    @PostMapping("/filteredTaskForAdmin/{search}/{pageNo}")
    public JSONObject filteredTaskForAdmin(@PathVariable String search,@PathVariable String pageNo) {
        return taskService.filteredTaskForAdmin(pageNo);
    }

    @GetMapping("/findById/{id}")
    public List<TaskDTO> findDataById(@PathVariable String id) {
        return taskService.findById(id);
    }

    @GetMapping("/reassignTask/{id}")
    public boolean reassignTask(@PathVariable String reassignTask,@PathVariable Long id) {
        return taskService.reassignTask(reassignTask,id);
    }
}