package net.edu.modulartask.tasks;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import net.edu.modulartask.config.JwtService;
import net.edu.modulartask.exceptions.*;
import net.edu.modulartask.notification.NotificationProducer;
import net.edu.modulartask.notification.NotificationService;
import net.edu.modulartask.subtask.SubtaskTemplateService;
import net.edu.modulartask.tasktemplate.TaskTemplateService;
import net.edu.modulartask.user.User;
import net.edu.modulartask.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

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
    private NotificationProducer notificationProducer;

    @Autowired
    JwtService jwtService;

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

        tasks.removeIf(task -> task.getStatus() != TaskStatus.IN_POOL);

        return tasks;
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
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
        parentTask.setStatus(TaskStatus.NEW);
        parentTask.setDeadline(createTaskDTO.deadline());
        parentTask.setCreatedAt(LocalDateTime.now());

        User loggedUser = userService.getCurrentlyLoggedUser();

        if(loggedUser != null) {
            parentTask.setCreator(loggedUser);
        }

        if(!createTaskDTO.assigneeIds().isEmpty()) {
            Set<User> set = parentTask.getAssignees();
            for(var assigneeId : createTaskDTO.assigneeIds()) {
                User user = userService.findById(assigneeId);
                set.add(user);
            }
            parentTask.setAssignees(set);
        }

        if(!createTaskDTO.subtasks().isEmpty()) {
            for(var subtask : createTaskDTO.subtasks()) {

                Task task = new Task();
                task.setTitle(subtask.title());
                task.setStatus(TaskStatus.NEW);
                task.setDeadline(createTaskDTO.deadline().plusDays(subtask.offsetDays()));
                task.setCreatedAt(LocalDateTime.now());

                if(subtask.assigneeId() != null) {
                    User user = userService.findById(subtask.assigneeId());
                    task.getAssignees().add(user);
                    notificationProducer.sendNotification("You are assignee to new task", subtask.title(), subtask.assigneeId());
                }

                task.setParentTask(parentTask);
                parentTask.getSubtasks().add(task);
            }
        }

        for(var assigneeId : createTaskDTO.assigneeIds()) {
            if(!assigneeId.equals(loggedUser.getId())) {
                notificationProducer.sendNotification("You are manager of task " + parentTask.getTitle(), parentTask.getDescription(), assigneeId);
            }
        }

        return taskRepository.save(parentTask);
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
            throw new UserNotFoundException("User does no exist");
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

    @Transactional
    public void takeTask(UUID taskId) {
        User user = userService.getCurrentlyLoggedUser();

        if(user == null){
            throw new UserNotFoundException("Logged user not found");
        }

        if(!user.isActive()) {
            throw new AccountDisabledException("Account is not active");
        }

        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new TaskNotFoundException("Task not found"));

        if(task.getAssignees().contains(user)) {
            throw new UserAlreadyAssignedException("You are already assigned to this task");
        }

        task.setStatus(TaskStatus.NEW);
        task.getAssignees().add(user);
        taskRepository.save(task);

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

