package net.edu.modulartask.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class TaskController {

    @Autowired
    TaskService taskService;

    @GetMapping("/tasks/{taskId}/assign/{userId}")
    public void assign(@PathVariable(name = "taskId")UUID taskId, @PathVariable(name = "userId") UUID userId) {
        taskService.addAssignee(taskId, userId);
    }
}
