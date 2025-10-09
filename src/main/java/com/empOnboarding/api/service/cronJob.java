package com.empOnboarding.api.service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class cronJob {

    private final TaskService taskService;

    public cronJob(TaskService taskService){
        this.taskService = taskService;
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Kolkata")
    public void taskIncomplete() {
        taskService.taskReminder();
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "Asia/Kolkata")
    public void taskEscalation() {
        taskService.escalationMail();
    }

}