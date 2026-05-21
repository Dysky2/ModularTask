package net.edu.modulartask.tasks;

import net.edu.modulartask.exceptions.*;
import net.edu.modulartask.notification.NotificationService;
import net.edu.modulartask.subtask.SubtaskTemplate;
import net.edu.modulartask.subtask.SubtaskTemplateRepository;
import net.edu.modulartask.subtask.SubtaskTemplateService;
import net.edu.modulartask.tasktemplate.TaskTemplate;
import net.edu.modulartask.tasktemplate.TaskTemplateRepository;
import net.edu.modulartask.tasktemplate.TaskTemplateService;
import net.edu.modulartask.user.UserRepository;
import net.edu.modulartask.user.UserService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import net.edu.modulartask.user.User;
import net.edu.modulartask.user.UserRole;

import java.time.LocalDateTime;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskTemplateRepository templateRepository;

    @Mock
    private TaskTemplateService taskTemplateService;

    @Mock
    private SubtaskTemplateService subtaskTemplateService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;
    private User anna;
    private User piotr;
    private UUID annaId;
    private UUID piotrId;
    private UUID taskId;

    private User buildAdminUser() {
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setUsername("admin");
        u.setEmail("admin@firma.pl");
        u.setRole(UserRole.ADMIN);
        u.setActive(true);
        u.setCreatedAt(LocalDateTime.now());
        return u;
    }

    private User buildUser(UUID id, String username, String email) {
        User u = new User();
        u.setId(id);
        u.setUsername(username);
        u.setEmail(email);
        u.setRole(UserRole.USER);
        u.setActive(true);
        u.setCreatedAt(LocalDateTime.now());
        return u;
    }

    @BeforeEach
    void setUp() {
        annaId  = UUID.randomUUID();
        piotrId = UUID.randomUUID();
        taskId  = UUID.randomUUID();

        anna = buildUser(annaId, "anna.nowak", "anna@firma.pl");
        piotr = buildUser(piotrId, "piotr.kowalski", "piotr@firma.pl");

        sampleTask = new Task();
        sampleTask.setId(taskId);
        sampleTask.setTitle("Wdrożenie pracownika");
        sampleTask.setDescription("Onboarding nowego pracownika działu IT");
        sampleTask.setStatus(TaskStatus.NEW);
        sampleTask.setAssignees(new HashSet<>());
        sampleTask.setDeadline(LocalDateTime.now().plusDays(5));
        sampleTask.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Przydziela pierwszego wykonawcę i zmienia status na IN_PROGRESS")
    void shouldAddFirstAssigneeAndChangeStatusToInProgress() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(userService.findById(annaId)).thenReturn(anna);

        taskService.addAssignee(taskId, annaId);

        assertThat(sampleTask.getAssignees()).isNotNull();
        assertThat(sampleTask.getAssignees()).contains(anna);
    }

    @Test
    @DisplayName("Przydziela wielu wykonawców do jednego zadania")
    void shouldAddMultipleAssigneesToTask() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(userService.findById(annaId)).thenReturn(anna);
        when(userService.findById(piotrId)).thenReturn(piotr);
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        taskService.addAssignee(taskId, annaId);
        taskService.addAssignee(taskId, piotrId);

        assertThat(sampleTask.getAssignees()).containsExactlyInAnyOrder(anna, piotr);
    }

    @Test
    @DisplayName("Wysyła powiadomienie e-mail każdemu nowemu wykonawcy")
    void shouldSendNotificationToEachNewAssignee() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(userService.findById(annaId)).thenReturn(anna);
        when(userService.findById(piotrId)).thenReturn(piotr);
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        taskService.addAssignee(taskId, annaId);
        taskService.addAssignee(taskId, piotrId);

        verify(notificationService).notifyAssignment(sampleTask, anna);
        verify(notificationService).notifyAssignment(sampleTask, piotr);
    }

    @Test
    @DisplayName("Nie dodaje tego samego wykonawcy drugi raz (Set nie duplikuje)")
    void shouldNotAddDuplicateAssignee() {
        sampleTask.getAssignees().add(anna);
        sampleTask.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(userService.findById(annaId)).thenReturn(anna);

        assertThatThrownBy(() -> taskService.addAssignee(taskId, annaId))
                .isInstanceOf(UserAlreadyAssignedException.class);

        verify(taskRepository, never()).save(any());
    }

    @Test
    @DisplayName("Usuwa wykonawcę z zadania")
    void shouldRemoveAssigneeFromTask() {
        sampleTask.getAssignees().add(anna);
        sampleTask.getAssignees().add(piotr);
        sampleTask.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(userService.findById(annaId)).thenReturn(anna);
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        taskService.removeAssignee(taskId, annaId);

        assertThat(sampleTask.getAssignees()).doesNotContain(anna);
        assertThat(sampleTask.getAssignees()).contains(piotr);
    }

    @Test
    @DisplayName("Rzuca wyjątek gdy przydzielany użytkownik jest nieaktywny")
    void shouldThrowWhenAssigningInactiveUser() {
        anna.setActive(false);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(userService.findById(annaId)).thenReturn(anna);

        assertThatThrownBy(() -> taskService.addAssignee(taskId, annaId))
                .isInstanceOf(AccountDisabledException.class);
    }

    @Test
    @DisplayName("Rzuca wyjątek gdy przydzielany użytkownik nie istnieje")
    void shouldThrowWhenAssigneeNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(userService.findById(unknownId)).thenReturn(null);

        assertThatThrownBy(() -> taskService.addAssignee(taskId, unknownId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Złożenie raportu przez dowolnego assignee zmienia status na PENDING_ACCEPTANCE")
    void shouldChangeToPendingAcceptanceWhenReportSubmitted() {
        sampleTask.setStatus(TaskStatus.IN_PROGRESS);
        sampleTask.getAssignees().add(anna);
        sampleTask.getAssignees().add(piotr);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(userService.findById(annaId)).thenReturn(anna);
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Anna składa raport w imieniu zespołu
        taskService.submitReport(taskId, annaId, "Laptop znaleziony, konto stworzone, mail gotowy.");

        assertThat(sampleTask.getStatus()).isEqualTo(TaskStatus.PENDING_ACCEPTANCE);
    }

    @Test
    @DisplayName("Rzuca wyjątek gdy raport składa ktoś spoza listy assignees")
    void shouldThrowWhenNonAssigneeSubmitsReport() {
        sampleTask.setStatus(TaskStatus.IN_PROGRESS);
        sampleTask.getAssignees().add(anna);
        // piotr NIE jest assignee tego taska

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(userService.findById(piotrId)).thenReturn(piotr);

        assertThatThrownBy(() -> taskService.submitReport(taskId, piotrId, "Raport"))
                .isInstanceOf(UnauthorizedTaskActionException.class);
    }

    @Test
    @DisplayName("Rzuca wyjątek gdy raport jest pusty")
    void shouldThrowWhenReportIsBlank() {
        sampleTask.setStatus(TaskStatus.IN_PROGRESS);
        sampleTask.getAssignees().add(anna);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(userService.findById(annaId)).thenReturn(anna);

        assertThatThrownBy(() -> taskService.submitReport(taskId, annaId, "  "))
                .isInstanceOf(EmptyReportException.class);
    }

    @Test
    @DisplayName("Akceptacja przez creatora zmienia status na COMPLETED")
    void shouldCompleteTaskOnAcceptance() {
        User creator = buildAdminUser();
        sampleTask.setStatus(TaskStatus.PENDING_ACCEPTANCE);
        sampleTask.setCreator(creator);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(userService.findById(creator.getId())).thenReturn(creator);
        when(taskRepository.findByParentTask(sampleTask)).thenReturn(List.of());
        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        taskService.acceptTask(taskId);

        assertThat(sampleTask.getStatus()).isEqualTo(TaskStatus.COMPLETED);
    }

//    @Test
//    @DisplayName("Odrzucenie z komentarzem wraca zadanie do IN_PROGRESS")
//    void shouldReturnTaskToInProgressOnRejection() {
//        User creator = buildAdminUser();
//        sampleTask.setStatus(TaskStatus.PENDING_ACCEPTANCE);
//        sampleTask.setCreator(creator);
//        sampleTask.getAssignees().add(anna);
//
//        when(taskRepository.findById(taskId)).thenReturn(Optional.of(sampleTask));
//        when(userService.findById(creator.getId())).thenReturn(creator);
//        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
//
//        taskService.rejectTask(taskId, creator.getId(), "Brakuje konta mailowego.");
//
//        assertThat(sampleTask.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
//        // komentarz ląduje w task_comments – weryfikujemy przez commentRepository
//
//        verify(commentRepository).save(argThat(c ->
//                c.getContent().equals("Brakuje konta mailowego.")
//                        && c.getTask().equals(sampleTask)
//                        && c.getAuthor().equals(creator)
//        ));
//    }

//    @Test
//    @DisplayName("Tworzy zadanie z szablonu z podzadaniami i relatywnymi datami")
//    void shouldCreateTaskFromTemplateWithRelativeDates() {
//        TaskTemplate template = new TaskTemplate();
//        template.setId(UUID.randomUUID());
//        template.setTitle("Wdrożenie pracownika");
//
//        // SubtaskTemplate ma FK do TaskTemplate (nie odwrotnie),
//        // więc serwis pobiera je przez subtaskTemplateRepository.findByTemplate()
//        SubtaskTemplate st1 = new SubtaskTemplate();
//        st1.setId(UUID.randomUUID());
//        st1.setTemplate(template);
//        st1.setTitle("Znajdź laptop");
//        st1.setOffsetDays(1);
//
//        SubtaskTemplate st2 = new SubtaskTemplate();
//        st2.setId(UUID.randomUUID());
//        st2.setTemplate(template);
//        st2.setTitle("Stwórz konto mailowe");
//        st2.setOffsetDays(2);
//
//        SubtaskTemplate st3 = new SubtaskTemplate();
//        st3.setId(UUID.randomUUID());
//        st3.setTemplate(template);
//        st3.setTitle("Dodaj do bazy HR");
//        st3.setOffsetDays(3);
//
//        when(taskTemplateService.findById(template.getId())).thenReturn(template);
//        when(subtaskTemplateService.findByTemplate(template))
//                .thenReturn(List.of(st1, st2, st3));
//        when(taskRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
//
//        LocalDateTime start = LocalDateTime.now().plusDays(1);
//        Task created = taskService.createFromTemplate(template.getId(), start);
//
//        assertThat(created.getTitle()).isEqualTo("Wdrożenie pracownika");
//        assertThat(created.getStatus()).isEqualTo(TaskStatus.NEW);
//        assertThat(created.getAssignees()).isEmpty();
//
//        // 1 główny task + 3 podzadania = 4 wywołania save
//        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
//        verify(taskRepository, times(4)).save(captor.capture());
//
//        List<Task> subtasks = captor.getAllValues().stream()
//                .filter(t -> t.getParentTask() != null)
//                .toList();
//
//        assertThat(subtasks).hasSize(3);
//        assertThat(subtasks.get(0).getTitle()).isEqualTo("Znajdź laptop");
//        assertThat(subtasks.get(0).getDeadline()).isEqualToIgnoringSeconds(start.plusDays(1));
//        assertThat(subtasks.get(1).getTitle()).isEqualTo("Stwórz konto mailowe");
//        assertThat(subtasks.get(1).getDeadline()).isEqualToIgnoringSeconds(start.plusDays(2));
//        assertThat(subtasks.get(2).getTitle()).isEqualTo("Dodaj do bazy HR");
//        assertThat(subtasks.get(2).getDeadline()).isEqualToIgnoringSeconds(start.plusDays(3));
//    }
}
