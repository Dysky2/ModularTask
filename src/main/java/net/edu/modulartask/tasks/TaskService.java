package net.edu.modulartask.tasks;

import jakarta.servlet.http.HttpServletRequest;
import net.edu.modulartask.config.JwtService;
import net.edu.modulartask.exceptions.*;
import net.edu.modulartask.notification.NotificationService;
import net.edu.modulartask.subtask.SubtaskTemplate;
import net.edu.modulartask.subtask.SubtaskTemplateService;
import net.edu.modulartask.tasktemplate.TaskTemplate;
import net.edu.modulartask.tasktemplate.TaskTemplateService;
import net.edu.modulartask.user.User;
import net.edu.modulartask.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TaskService {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    UserService userService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    TaskTemplateService taskTemplateService;

    @Autowired
    SubtaskTemplateService subtaskTemplateService;

    @Autowired
    JwtService jwtService;

    public Task findById(UUID taskId) {
        return taskRepository.findById(taskId).orElseThrow(
                () -> new IllegalArgumentException("Task is not exist"));
    }

    public List<Task> getAllMyTask(HttpServletRequest request) {

        String token = request.getHeader("Authorization").substring(7);
        String username = jwtService.extractUsername(token);

        User user = userService.findByUsername(username);

        return taskRepository.findByAssigneesContaining(user);
    }

    public List<Task> getAllTasksInPool() {
        List<Task> tasks = taskRepository.findAll();

        tasks.removeIf(task -> task.getStatus() != TaskStatus.IN_POOL);

        return tasks;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task createTask(String title, LocalDateTime deadline) {
        Task task = new Task();

        if(title.isBlank()) {
            throw new IllegalArgumentException("Title is empty");
        }

        task.setTitle(title);

        if(LocalDateTime.now().isAfter(deadline)) {
            throw new InvalidDeadlineException("Deadline is in past");
        }

        task.setDeadline(deadline);
        task.setStatus(TaskStatus.NEW);

        return taskRepository.save(task);
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

        set.add(user);

        task.setAssignees(set);

        notificationService.notifyAssignment(task, user);

        taskRepository.save(task);
    }

    public void removeAssignee(UUID taskId, UUID userId) {
        Task task = findById(taskId);

        User user = userService.findById(userId);

        Set<User> set = task.getAssignees();

        if(!set.contains(user)) {
            throw new IllegalArgumentException("User does no exist");
        }

        set.remove(user);

        task.setAssignees(set);

        notificationService.notifyRemovedFromTask(task, user);

        taskRepository.save(task);
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
    }

    public void acceptTask(UUID taskId, UUID creatorId) {
        Task task = findById(taskId);

        List<Task> subTasks = taskRepository.findByParentTask(task);

        User user = userService.findById(creatorId);

        if(!task.getCreator().getEmail().equals(user.getEmail())) {
            throw new UserNotFoundException("User not exist");
        }

        // TODO
        // sprawdzic czy napewno kazdy z subTaskow jest wykonany

        for(var tempTask : subTasks) {
            tempTask.setStatus(TaskStatus.COMPLETED);
        }

        task.setStatus(TaskStatus.COMPLETED);

        taskRepository.save(task);
    }

    public void rejectTask(UUID taskId, UUID creatorId, String comment) {
        Task task = findById(taskId);

        User user = userService.findById(creatorId);

        if(!task.getCreator().getEmail().equals(user.getEmail())) {
            throw new UserNotFoundException("User not exist");
        }

        // TODO
        // dodanie komentarza do zadanie ze nie zostal zaakceptowany

        task.setStatus(TaskStatus.IN_PROGRESS);

        taskRepository.save(task);
    }

    public Task createFromTemplate(UUID templateId, LocalDateTime deadline) {
        TaskTemplate taskTemplate = taskTemplateService.findById(templateId);

        List<SubtaskTemplate> subtaskTemplates = subtaskTemplateService.findByTemplate(taskTemplate);

        Task parentTask = createTask(taskTemplate.getTitle(), deadline);
        for(var subtaskTemplate : subtaskTemplates) {
            Task subTask = createTask(subtaskTemplate.getTitle(), deadline.plusDays(subtaskTemplate.getOffsetDays()));

            subTask.setParentTask(parentTask);
        }

        return parentTask;
    }

}

