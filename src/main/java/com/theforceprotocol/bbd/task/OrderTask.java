package com.theforceprotocol.bbd.task;

import com.theforceprotocol.bbd.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderTask {
    private final TaskService taskService;

    public OrderTask(TaskService taskService) {
        this.taskService = taskService;
    }
}
