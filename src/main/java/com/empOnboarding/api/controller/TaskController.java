package com.empOnboarding.api.controller;
import com.empOnboarding.api.dto.EmployeeDTO;
import com.empOnboarding.api.dto.TaskDTO;
import com.empOnboarding.api.service.TaskService;
import org.json.simple.JSONObject;
import org.springframework.web.bind.annotation.*;


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

    @GetMapping("/findById/{id}")
    public TaskDTO findDataById(@PathVariable String id) {
        return taskService.findById(id);
    }
}