package net.edu.modulartask.tasks;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin("http://localhost:5173")
public class TaskController {

    @Autowired
    TaskService taskService;

    @GetMapping("my")
    public List<Task> getMyTasks(HttpServletRequest request) {
        return taskService.getAllMyTask(request);
    }

    @GetMapping("/pool")
    public List<Task> getAllTasksInPool() {
        return taskService.getAllTasksInPool();
    }

    @GetMapping("/all")
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{taskId}")
    public Task getTaskById(@PathVariable(name = "taskId") UUID taskId) {
        return taskService.findById(taskId);
    }

    @GetMapping("/{taskId}/assign/{userId}")
    public void assign(@PathVariable(name = "taskId")UUID taskId,
                       @PathVariable(name = "userId") UUID userId) {
        taskService.addAssignee(taskId, userId);
    }

    @PostMapping("/create_task")
    public ResponseEntity<Task> createTask(@Valid @RequestBody CreateTaskDTO createTaskDTO) {
        Task createdTask = taskService.createTask(createTaskDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }
}
