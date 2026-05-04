package net.edu.modulartask.tasks;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import net.edu.modulartask.user.UserService;
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

    @Autowired
    UserService userService;

    @GetMapping("my")
    public List<TaskResponseDTO> getMyTasks(HttpServletRequest request) {
        List<Task> tasks = taskService.getAllMyTask(request);
        var currentUser = userService.getCurrentlyLoggedUser();
        return taskService.toResponseDTOList(tasks, currentUser);
    }

    @GetMapping("/pool")
    public List<TaskResponseDTO> getAllTasksInPool() {
        List<Task> tasks = taskService.getAllTasksInPool();
        var currentUser = userService.getCurrentlyLoggedUser();
        return taskService.toResponseDTOList(tasks, currentUser);
    }

    @GetMapping("/all")
    public List<TaskResponseDTO> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        var currentUser = userService.getCurrentlyLoggedUser();
        return taskService.toResponseDTOList(tasks, currentUser);
    }

    @GetMapping("/aprroval")
    public List<TaskResponseDTO> getAllTasksApproval() {
        List<Task> tasks = taskService.getAllTaskForApproval();
        var currentUser = userService.getCurrentlyLoggedUser();
        return taskService.toResponseDTOList(tasks, currentUser);
    }

    @GetMapping("/{taskId}")
    public TaskResponseDTO getTaskById(@PathVariable(name = "taskId") UUID taskId) {
        Task task = taskService.findById(taskId);
        var currentUser = userService.getCurrentlyLoggedUser();
        return taskService.toResponseDTO(task, currentUser);
    }

    @GetMapping("/{taskId}/assign/{userId}")
    public void assign(@PathVariable(name = "taskId")UUID taskId,
                       @PathVariable(name = "userId") UUID userId) {
        taskService.addAssignee(taskId, userId);
    }

    @GetMapping("/in-progress")
    public List<TaskResponseDTO> getAllTasksInProgress() {
        List<Task> tasks = taskService.getAllTasksInProgress();
        var currentUser = userService.getCurrentlyLoggedUser();
        return taskService.toResponseDTOList(tasks, currentUser);
    }

    @PostMapping("/create_task")
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody CreateTaskDTO createTaskDTO) {
        Task createdTask = taskService.createTask(createTaskDTO);
        var currentUser = userService.getCurrentlyLoggedUser();
        TaskResponseDTO response = taskService.toResponseDTO(createdTask, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{taskId}/take")
    public ResponseEntity<TaskResponseDTO> takeTask(@PathVariable(name = "taskId") UUID taskId){
        taskService.takeTask(taskId);
        Task task = taskService.findById(taskId);
        var currentUser = userService.getCurrentlyLoggedUser();
        TaskResponseDTO response = taskService.toResponseDTO(task, currentUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{taskId}/start-work")
    public ResponseEntity<Map<String,String>> startWork(@PathVariable(name = "taskId") UUID taskId){
        taskService.startWork(taskId);
        return ResponseEntity.ok(Map.of("message","You have started working on the task"));
    }

}
