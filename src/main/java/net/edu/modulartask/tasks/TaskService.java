package net.edu.modulartask.tasks;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import net.edu.modulartask.config.JwtService;
import net.edu.modulartask.exceptions.*;
import net.edu.modulartask.notification.NotificationProducer;
import net.edu.modulartask.notification.NotificationService;
import net.edu.modulartask.subtask.SubTaskDTO;
import net.edu.modulartask.user.User;
import net.edu.modulartask.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TaskService {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    UserService userService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    private NotificationProducer notificationProducer;

    @Autowired
    JwtService jwtService;

    @Autowired
    private TaskHistoryRepository taskHistoryRepository;

    @Autowired
    TemplateEngine templateEngine;

    private void logTaskHistory(Task task, User user, String action, String details){
        TaskHistory log = new TaskHistory(task, user, action, details);
        taskHistoryRepository.save(log);
    }

    private boolean isUserAssignedToTask(Task task, User user) {
        return task.getAssignees().contains(user);
    }

    private TaskStatus getUserStatus(Task task, User user) {
        if (task.getStatus() != TaskStatus.IN_POOL) {
            return task.getStatus();
        }
        return isUserAssignedToTask(task, user) ? TaskStatus.NEW : TaskStatus.IN_POOL;
    }

    private boolean isTaskLimitFull(Task task) {
        return task.getAssignees().size() >= task.getLimit();
    }

    private void updatePoolStatusByCapacity(Task task) {
        task.setStatus(isTaskLimitFull(task) ? TaskStatus.NEW : TaskStatus.IN_POOL);
    }

    public TaskResponseDTO toResponseDTO(Task task, User currentUser) {
        return new TaskResponseDTO(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            getUserStatus(task, currentUser),
            task.getAssignees(),
            task.getLimit(),
            task.getDeadline(),
            task.getCreatedAt()
        );
    }

    public List<TaskResponseDTO> toResponseDTOList(List<Task> tasks, User currentUser) {
        return tasks.stream()
                .map(task -> toResponseDTO(task, currentUser))
                .toList();
    }

    public TaskDetailsResponseDTO toDetailsResponseDTO(Task task, User currentUser) {
        List<TaskResponseDTO> subtaskDtos = toResponseDTOList(task.getSubtasks(), currentUser);

        return new TaskDetailsResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                getUserStatus(task, currentUser),
                task.getAssignees(),
                task.getLimit(),
                task.getDeadline(),
                task.getCreatedAt(),
                task.getCreator(),
                task.getParentTask() == null ? null : task.getParentTask().getId(),
                subtaskDtos
        );
    }

    public Task findById(UUID taskId) {
        return taskRepository.findById(taskId).orElseThrow(
                () -> new TaskNotFoundException("Task is not exist"));
    }

    public List<Task> getAllMyTask(HttpServletRequest request) {

        String token = request.getHeader("Authorization").substring(7);
        String username = jwtService.extractUsername(token);

        User user = userService.findByUsername(username);

        return taskRepository.findByAssigneesContaining(user);
    }

    public List<Task> getAllTasksInPool() {
        List<Task> tasks = taskRepository.findAll();
        User currentUser = userService.getCurrentlyLoggedUser();

        tasks.removeIf(task -> task.getStatus() != TaskStatus.IN_POOL);

        tasks.removeIf(this::isTaskLimitFull);

        if (currentUser != null) {
            tasks.removeIf(task -> task.getAssignees().contains(currentUser));
        }

        return tasks;
    }

    public List<Task> getAllTasksInProgress() {
        List<Task> tasks = taskRepository.findAll();

        tasks.removeIf(task -> task.getStatus() != TaskStatus.IN_PROGRESS);

        return tasks;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public List<Task> getAllTaskForApproval() {
        User user = userService.getCurrentlyLoggedUser();

        return taskRepository.findALlByCreatorIdAndStatus(user.getId() ,TaskStatus.PENDING_ACCEPTANCE);
    }

    public ResponseEntity<Map<String,String>> changeStatus(UUID taskID, TaskStatus status) {
        Task task = findById(taskID);

        User user = userService.getCurrentlyLoggedUser();

        if(status.equals(TaskStatus.PENDING_ACCEPTANCE) && !task.getAssignees().contains(user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "You are not part of the team"));
        }

        task.setStatus(status);

        taskRepository.save(task);

        return ResponseEntity.ok(Map.of("message", "Task change status to approved by creator"));
    }

    @Transactional
    public Task createTask(CreateTaskDTO createTaskDTO) {

        Task parentTask = new Task();

        if(createTaskDTO.title().isBlank()) {
            throw new IllegalArgumentException("Title is empty");
        }

        if(LocalDateTime.now().isAfter(createTaskDTO.deadline())) {
            throw new InvalidDeadlineException("Deadline is in past");
        }

        parentTask.setTitle(createTaskDTO.title());
        parentTask.setDescription(createTaskDTO.description());
        parentTask.setDeadline(createTaskDTO.deadline());
        parentTask.setCreatedAt(LocalDateTime.now());
        parentTask.setStatus(TaskStatus.IN_POOL);

        int taskLimit = createTaskDTO.limit() == null ? 10 : createTaskDTO.limit();
        if(taskLimit < 1) {
            throw new IllegalArgumentException("Limit must be at least 1");
        }
        parentTask.setLimit(taskLimit);

        User loggedUser = userService.getCurrentlyLoggedUser();

        if(loggedUser != null) {
            parentTask.setCreator(loggedUser);
        }

        List<UUID> assigneeIds = createTaskDTO.assigneeIds() == null ? Collections.emptyList() : createTaskDTO.assigneeIds();
        List<SubTaskDTO> subtasks = createTaskDTO.subtasks() == null ? Collections.emptyList() : createTaskDTO.subtasks();

        if(assigneeIds.size() > taskLimit) {
            throw new IllegalArgumentException("Cannot assign more users than task limit");
        }


        //Wariant zadanie posiada subtaski -> wykonawca glownego zadania musi byc przypisany
        if(!subtasks.isEmpty()) {

            if(!assigneeIds.isEmpty()) {
                Set<User> set = parentTask.getAssignees();
                for(var assigneeId : assigneeIds) {
                    User user = userService.findById(assigneeId);
                    set.add(user);
                }
                parentTask.setAssignees(set);
            }else {
                throw new IllegalArgumentException("Task with subtasks must have at least one assignee");
            }

            for(var subtask : subtasks) {

                Task task = new Task();
                task.setTitle(subtask.title());
                task.setStatus(TaskStatus.IN_POOL);
                task.setDeadline(createTaskDTO.deadline().plusDays(subtask.offsetDays()));
                task.setCreatedAt(LocalDateTime.now());
                
                int subtaskLimit = subtask.limit() == null ? 1 : subtask.limit();
                if(subtaskLimit < 1) {
                    throw new IllegalArgumentException("Subtask limit must be at least 1");
                }
                task.setLimit(subtaskLimit);

                if(subtask.assigneeId() != null) {
                    User user = userService.findById(subtask.assigneeId());
                    task.getAssignees().add(user);
                    notificationProducer.sendNotification("You are assignee to new task", subtask.title(), subtask.assigneeId());
                }

                updatePoolStatusByCapacity(task);

                task.setParentTask(parentTask);
                parentTask.getSubtasks().add(task);
            }
        }else{
            //Wariant zadanie nie posiada subtaskow -> nie musi miec wykonawcy
            Set<User> set = parentTask.getAssignees();
            for(var assigneeId : assigneeIds) {
                User user = userService.findById(assigneeId);
                set.add(user);
            }
            parentTask.setAssignees(set);

        }

        updatePoolStatusByCapacity(parentTask);

        for(var assigneeId : assigneeIds) {
            if(loggedUser == null || !assigneeId.equals(loggedUser.getId())) {
                notificationProducer.sendNotification("You are manager of task " + parentTask.getTitle(), parentTask.getDescription(), assigneeId);
            }
        }

        Task savedTask = taskRepository.save(parentTask);

        logTaskHistory(savedTask, loggedUser, "CREATED", "Task created");

        if (!savedTask.getAssignees().isEmpty()) {
            StringBuilder assigneesLog = new StringBuilder("Assigned users: ");
            savedTask.getAssignees().forEach(u -> assigneesLog.append(u.getUsername()).append(", "));
            String formattedLog = assigneesLog.substring(0, assigneesLog.length() - 2);
            logTaskHistory(savedTask, loggedUser, "ASSIGNEES_ADDED", formattedLog);
        }

        for (Task savedSubtask : savedTask.getSubtasks()) {
            logTaskHistory(savedSubtask, loggedUser, "CREATED", "Subtask created");
            if (!savedSubtask.getAssignees().isEmpty()) {
                StringBuilder stAssigneesLog = new StringBuilder("Assigned users: ");
                savedSubtask.getAssignees().forEach(u -> stAssigneesLog.append(u.getUsername()).append(", "));
                String stFormattedLog = stAssigneesLog.substring(0, stAssigneesLog.length() - 2);
                logTaskHistory(savedSubtask, loggedUser, "ASSIGNEES_ADDED", stFormattedLog);
            }
        }

        return savedTask;
    }

    @Transactional
    public void createSubtask(String title,UUID userId, Task parentTask,LocalDateTime deadline, int offset) {
        Task task = new Task();

        User user = userService.findById(userId);

        task.setTitle(title);
        task.setDeadline(deadline.plusDays(offset));
        task.setCreator(user);
        task.setParentTask(parentTask);

        taskRepository.save(task);
        logTaskHistory(task, user, "CREATED", "Subtask created manually: " + title);
    }

    public void addAssignee(UUID taskId, UUID userId) {
        Task task = findById(taskId);

        User user = userService.findById(userId);

        if(user == null) {
            throw new UserNotFoundException("User not found");
        }

        if(!user.isActive()) {
            throw new AccountDisabledException("User " + user.getUsername() + " is not active");
        }

        Set<User> set = task.getAssignees();

        if (set.contains(user)) {
            throw new UserAlreadyAssignedException(user.getUsername() + " is already assign");
        }

        if(set.size() >= task.getLimit()) {
            throw new IllegalArgumentException(
                "Cannot add more assignees. Task limit (" + task.getLimit() + ") has been reached"
            );
        }

        set.add(user);

        task.setAssignees(set);
        updatePoolStatusByCapacity(task);

        notificationService.notifyAssignment(task, user);

        taskRepository.save(task);

        User currentUser = userService.getCurrentlyLoggedUser();
        logTaskHistory(task, currentUser, "ASSIGNEE_ADDED", "User " + user.getUsername() + " added to task");
    }

    public void removeAssignee(UUID taskId, UUID userId) {
        Task task = findById(taskId);

        User user = userService.findById(userId);

        Set<User> set = task.getAssignees();

        if(!set.contains(user)) {
            throw new UserNotFoundException("User does no exist");
        }

        set.remove(user);

        task.setAssignees(set);
        updatePoolStatusByCapacity(task);

        notificationService.notifyRemovedFromTask(task, user);

        taskRepository.save(task);

        User currentUser = userService.getCurrentlyLoggedUser();
        logTaskHistory(task, currentUser, "ASSIGNEE_REMOVED", "User " + user.getUsername() + " removed from task");
    }

    public void submitReport(UUID taskId, UUID userId, String title)  {
        Task task = findById(taskId);

        User user = userService.findById(userId);

        Set<User> set = task.getAssignees();

        if(!set.contains(user)) {
            throw new UnauthorizedTaskActionException(user.getUsername() + " is not qualified to submit raport");
        }

        if(title.isBlank()) {
            throw new EmptyReportException("Empty title");
        }

        task.setStatus(TaskStatus.PENDING_ACCEPTANCE);

        // TODO
        // wysylka do kogos do acceptu

        taskRepository.save(task);
        logTaskHistory(task, user, "STATUS_CHANGED", "Task submitted for report: " + title);
    }

    public User getCreator(Task task) {

        if (task.getCreator() != null) {
            return task.getCreator();
        }

        if (task.getParentTask() != null) {
            return getCreator(task.getParentTask());
        }

        return null;
    }

    private void validateAccess(User user, Task task) {
        User creatorUser = getCreator(task);

        if (creatorUser == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Creator not found");
        }

        if (!user.getId().equals(creatorUser.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not qualified to perform this action");
        }
    }

    public ResponseEntity<Map<String, String>> acceptTask(UUID taskId) {
        Task task = findById(taskId);

        List<Task> subTasks = taskRepository.findByParentTask(task);

        User user = userService.getCurrentlyLoggedUser();

        validateAccess(user, task);

        for(var tempTask : subTasks) {
            if(!tempTask.getStatus().equals(TaskStatus.COMPLETED)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "All subtasks, are not finished"));
            }
        }

        task.setStatus(TaskStatus.COMPLETED);

        taskRepository.save(task);

        logTaskHistory(task, user, "STATUS_CHANGED", "Task ACCEPTED and COMPLETED");

        return ResponseEntity.ok(Map.of("message", "Task is completed"));
    }

    public ResponseEntity<Map<String, String>> rejectTask(UUID taskId, String reason) {
        Task task = findById(taskId);

        User user = userService.getCurrentlyLoggedUser();

        validateAccess(user, task);

        // TODO
        // dodanie komentarza do zadanie ze nie zostal zaakceptowany

        task.setStatus(TaskStatus.IN_PROGRESS);

        taskRepository.save(task);

        logTaskHistory(task, user, "STATUS_CHANGED", "Task REJECTED. Moved to IN_PROGRESS. Comment: " + reason);

        return ResponseEntity.ok(Map.of("message", "Task is rejected"));
    }

    @Transactional
    public void takeTask(UUID taskId) {
        User user = userService.getCurrentlyLoggedUser();

        if(user == null){
            throw new UserNotFoundException("Logged user not found");
        }

        if(!user.isActive()) {
            throw new AccountDisabledException("Account is not active");
        }

        Task task = findById(taskId);

        if(task.getAssignees().contains(user)) {
            throw new UserAlreadyAssignedException("You are already assigned to this task");
        }

        if(task.getAssignees().size() >= task.getLimit()) {
            throw new IllegalArgumentException(
                "Cannot take this task. Task limit (" + task.getLimit() + ") has been reached"
            );
        }

        task.getAssignees().add(user);
        updatePoolStatusByCapacity(task);
        taskRepository.save(task);

        logTaskHistory(task, user, "TASK_TAKEN", "User took the task");

        if(task.getCreator() != null && task.getCreator() != user){
            notificationProducer.sendNotification(
                    "Task taken",
                    "User " + user.getUsername() + " has taken task created by you: " + task.getTitle(),
                    task.getCreator().getId()
            );
        }
    }

    public void startWork(UUID taskId) {
        User user = userService.getCurrentlyLoggedUser();

        if(user == null){
            throw new UserNotFoundException("Logged user not found");
        }

        if(!user.isActive()) {
            throw new AccountDisabledException("Account is not active");
        }

        Task task = findById(taskId);

        if(!task.getAssignees().contains(user)) {
            throw new UnauthorizedTaskActionException("You are not assigned to this task");
        }

        task.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task);

        logTaskHistory(task, user, "STATUS_CHANGED", "Status changed to IN_PROGRESS. Work started.");

        if(task.getCreator() != null && task.getCreator() != user){
            notificationProducer.sendNotification(
                    "Work started on task",
                    "User " + user.getUsername() + " has started work on your task: " + task.getTitle(),
                    task.getCreator().getId()
            );
        }

    }

    public byte[] generatePdfFromHtml(String html) {
        try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.useFont(() -> getClass().getResourceAsStream("/fonts/Roboto-Regular.ttf"), "RobotoCustom");
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch( Exception e) {
            throw new RuntimeException("Error, while generator pdf from html");
        }
    }

    public byte[] generateReport(UUID taskId) {
        Task task = findById(taskId);

        User user = userService.getCurrentlyLoggedUser();

        Context context = new Context();
        context.setVariable("task", task);
        context.setVariable("subtasks", task.getSubtasks());
        context.setVariable("generatedBy", user.getFirstName() + " " + user.getLastName());
        context.setVariable("reportDate", LocalDateTime.now());
        context.setVariable("generatedBy", user.getFirstName() + " " + user.getLastName());

        String renderedHtmlContent = templateEngine.process("reportTemplate", context);

        return generatePdfFromHtml(renderedHtmlContent);
    }


//    public Task createFromTemplate(UUID templateId, LocalDateTime deadline) {
//        TaskTemplate taskTemplate = taskTemplateService.findById(templateId);
//
//        List<SubtaskTemplate> subtaskTemplates = subtaskTemplateService.findByTemplate(taskTemplate);
//
//        Task parentTask = createTask(taskTemplate.getTitle(), deadline);
//        for(var subtaskTemplate : subtaskTemplates) {
//            Task subTask = createTask(subtaskTemplate.getTitle(), deadline.plusDays(subtaskTemplate.getOffsetDays()));
//
//            subTask.setParentTask(parentTask);
//        }
//
//        return parentTask;
//    }

}
