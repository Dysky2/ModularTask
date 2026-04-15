package net.edu.modulartask.tasks;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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

    @GetMapping("/in-progress")
    public List<Task> getAllTasksInProgress()
    {
        return taskService.getAllTasksInProgress();
    }

    @PostMapping("/create_task")
    public ResponseEntity<Task> createTask(@Valid @RequestBody CreateTaskDTO createTaskDTO) {
        Task createdTask = taskService.createTask(createTaskDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @PostMapping("/{taskId}/take")
    public ResponseEntity<Map<String,String>> takeTask(@PathVariable(name = "taskId") UUID taskId){
        taskService.takeTask(taskId);
        return ResponseEntity.ok(Map.of("message","You have taken the task"));
    }

    @PostMapping("/{taskId}/start-work")
    public ResponseEntity<Map<String,String>> startWork(@PathVariable(name = "taskId") UUID taskId){
        taskService.startWork(taskId);
        return ResponseEntity.ok(Map.of("message","You have started working on the task"));
    }

}
