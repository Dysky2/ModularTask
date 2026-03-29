package net.edu.modulartask.tasks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin("http://localhost:5173")
public class TaskController {

    @Autowired
    TaskService taskService;

    @GetMapping("/api/tasks/my")
    public List<Task> getMyTasks() {
        return taskService.getAllMyTask();
    }

    @GetMapping("/api/tasks/pool")
    public List<Task> getAllTasksInPool() {
        return taskService.getAllTasksInPool();
    }

    @GetMapping("/api/tasks/all")
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/api/tasks/{taskId}")
    public Task getTaskById(@PathVariable(name = "taskId") UUID taskId) {
        return taskService.findById(taskId);
    }

    @GetMapping("/tasks/{taskId}/assign/{userId}")
    public void assign(@PathVariable(name = "taskId")UUID taskId,
                       @PathVariable(name = "userId") UUID userId) {
        taskService.addAssignee(taskId, userId);
    }
}
