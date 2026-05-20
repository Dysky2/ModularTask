package net.edu.modulartask.admin;

import net.edu.modulartask.exceptions.UnauthorizedAdminActionException;
import net.edu.modulartask.user.User;
import net.edu.modulartask.user.UserRepository;
import net.edu.modulartask.user.UserRole;
import net.edu.modulartask.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IssuePriorityRepository issuePriorityRepository;

    @Mock
    private IssueStatusRepository issueStatusRepository;

    @Mock
    private SystemConfigRepository systemConfigRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AdminService adminService;

    private User adminUser;

    @BeforeEach
    void setUp() {
        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setRole(UserRole.ADMIN);
    }

    @Test
    @DisplayName("Blokuje operacje dla nie-admina")
    void shouldBlockNonAdmin() {
        User regular = new User();
        regular.setRole(UserRole.USER);
        when(userService.getCurrentlyLoggedUser()).thenReturn(regular);

        assertThatThrownBy(() -> adminService.listUsers())
                .isInstanceOf(UnauthorizedAdminActionException.class)
                .hasMessageContaining("Only administrator");
    }

    @Test
    @DisplayName("Tworzy priorytet dla admina")
    void shouldCreatePriorityForAdmin() {
        IssuePriorityDTO dto = new IssuePriorityDTO("High", "#FF0000", 1);
        IssuePriority saved = new IssuePriority();
        saved.setId(UUID.randomUUID());
        saved.setName("High");
        saved.setColorHex("#FF0000");
        saved.setOrderIndex(1);

        when(userService.getCurrentlyLoggedUser()).thenReturn(adminUser);
        when(issuePriorityRepository.existsByName(dto.name())).thenReturn(false);
        when(issuePriorityRepository.save(org.mockito.ArgumentMatchers.any(IssuePriority.class)))
                .thenReturn(saved);

        IssuePriority result = adminService.createPriority(dto);

        assertThat(result.getName()).isEqualTo("High");
        assertThat(result.getColorHex()).isEqualTo("#FF0000");
    }

    @Test
    @DisplayName("Aktualizuje role uzytkownika przez admina")
    void shouldUpdateUserRole() {
        UUID userId = UUID.randomUUID();
        User updated = new User();
        updated.setId(userId);
        updated.setRole(UserRole.ADMIN);

        when(userService.getCurrentlyLoggedUser()).thenReturn(adminUser);
        when(userService.findById(userId)).thenReturn(updated);

        User result = adminService.updateUserRole(userId, UserRole.ADMIN);

        assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
    }
}

