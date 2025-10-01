package com.empOnboarding.api.service;

import com.empOnboarding.api.entity.Task;
import com.empOnboarding.api.repository.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class cronJob {

    private final TaskService taskService;

    public cronJob(TaskService taskService){
        this.taskService = taskService;
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Kolkata")
    public void updatePastAppointments() {
        taskService.taskReminder();
    }

}