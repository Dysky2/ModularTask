package net.edu.modulartask.user;

import net.edu.modulartask.admin.SystemConfigRepository;
import net.edu.modulartask.exceptions.AccountDisabledException;
import net.edu.modulartask.exceptions.DuplicateEmailException;
import net.edu.modulartask.exceptions.DuplicateUsernameException;
import net.edu.modulartask.exceptions.UserNotFoundException;
import net.edu.modulartask.organization.OrganizationUnit;
import net.edu.modulartask.tasks.TaskHistoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private SystemConfigRepository systemConfigRepository;

    @Mock
    private UserNotificationSettingsRepository userNotificationSettingsRepository;

    @Mock
    private UserPreferencesRepository userPreferencesRepository;

    @Mock
    private TaskHistoryRepository taskHistoryRepository;

    @InjectMocks
    private UserService userService;

    private User sampleUser;
    private CreateUserDTO sampleCreateUserDTO;
    private UUID sampleId;
    private User adminUser;

    @BeforeEach
    void setUp() {
        sampleId = UUID.randomUUID();

        sampleUser = new User();
        sampleUser.setId(sampleId);
        sampleUser.setUsername("jkowalski");
        sampleUser.setEmail("jan.kowalski@firma.pl");
        sampleUser.setFirstName("Jan");
        sampleUser.setLastName("Kowalski");
        sampleUser.setPassword("$2a$10$hashedPassword");
        sampleUser.setRole(UserRole.USER);
        sampleUser.setActive(true);
        sampleUser.setCreatedAt(LocalDateTime.now());

        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setUsername("admin");
        adminUser.setRole(UserRole.ADMIN);
        adminUser.setActive(true);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null, List.of())
        );
        lenient().when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        sampleCreateUserDTO = new CreateUserDTO(
                "jkowalski",
                "Jan",
                "Kowalski",
                "jan.kowalski@firma.pl",
                "tajneHaslo123",
                UserRole.USER,
                true
        );

        lenient().when(systemConfigRepository.findById(anyString())).thenReturn(Optional.empty());
        lenient().when(taskHistoryRepository.findTop10ByUserOrderByCreatedAtDesc(any())).thenReturn(List.of());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Tworzy użytkownika z poprawnym emailem i username")
    void shouldCreateUserWithValidData() {
        when(userRepository.existsByEmail(sampleCreateUserDTO.email())).thenReturn(false);
        when(userRepository.existsByUsername(sampleCreateUserDTO.username())).thenReturn(false);
        when(passwordEncoder.encode(sampleCreateUserDTO.password())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User saved = inv.getArgument(0);
            saved.setId(sampleId);
            return saved;
        });

        User result = userService.createUser(sampleCreateUserDTO);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("jan.kowalski@firma.pl");
        assertThat(result.getUsername()).isEqualTo("jkowalski");
        assertThat(result.getPassword()).isEqualTo("$2a$10$hashedPassword");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getCreatedAt()).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Nowo tworzony użytkownik dostaje rolę przekazaną w DTO")
    void shouldAssignRoleFromCreationDto() {
        when(userRepository.existsByEmail(sampleCreateUserDTO.email())).thenReturn(false);
        when(userRepository.existsByUsername(sampleCreateUserDTO.username())).thenReturn(false);
        when(passwordEncoder.encode(sampleCreateUserDTO.password())).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createUser(sampleCreateUserDTO);

        assertThat(result.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("Rzuca wyjątek gdy email już istnieje w systemie")
    void shouldThrowWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail(sampleCreateUserDTO.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(sampleCreateUserDTO))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("jan.kowalski@firma.pl");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rzuca wyjątek gdy username już istnieje w systemie")
    void shouldThrowWhenUsernameAlreadyExists() {
        when(userRepository.existsByEmail(sampleCreateUserDTO.email())).thenReturn(false);
        when(userRepository.existsByUsername(sampleCreateUserDTO.username())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(sampleCreateUserDTO))
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessageContaining("jkowalski");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Rzuca wyjątek przy pustym emailu")
    void shouldThrowWhenEmailIsBlank() {
        CreateUserDTO invalid = new CreateUserDTO(
                sampleCreateUserDTO.username(),
                sampleCreateUserDTO.firstName(),
                sampleCreateUserDTO.lastName(),
                "",
                sampleCreateUserDTO.password(),
                sampleCreateUserDTO.role(),
                sampleCreateUserDTO.isActive()
        );

        assertThatThrownBy(() -> userService.createUser(invalid))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Rzuca wyjątek przy pustym username")
    void shouldThrowWhenUsernameIsBlank() {
        CreateUserDTO invalid = new CreateUserDTO(
                "   ",
                sampleCreateUserDTO.firstName(),
                sampleCreateUserDTO.lastName(),
                sampleCreateUserDTO.email(),
                sampleCreateUserDTO.password(),
                sampleCreateUserDTO.role(),
                sampleCreateUserDTO.isActive()
        );

        assertThatThrownBy(() -> userService.createUser(invalid))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Zwraca użytkownika po UUID")
    void shouldReturnUserById() {
        when(userRepository.findById(sampleId)).thenReturn(Optional.of(sampleUser));

        User result = userService.findById(sampleId);

        assertThat(result.getId()).isEqualTo(sampleId);
        assertThat(result.getUsername()).isEqualTo("jkowalski");
    }

    @Test
    @DisplayName("Rzuca wyjątek gdy użytkownik o danym UUID nie istnieje")
    void shouldThrowWhenUserNotFound() {
        UUID unknown = UUID.randomUUID();
        when(userRepository.findById(unknown)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(unknown))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Zwraca użytkownika po username")
    void shouldReturnUserByUsername() {
        when(userRepository.findByUsername("jkowalski")).thenReturn(Optional.of(sampleUser));

        User result = userService.findByUsername("jkowalski");

        assertThat(result.getEmail()).isEqualTo("jan.kowalski@firma.pl");
    }


    @Test
    @DisplayName("Przypisuje użytkownika do jednostki organizacyjnej")
    void shouldAssignUserToOrganizationUnit() {
        OrganizationUnit unit = new OrganizationUnit();
        unit.setUnitId(UUID.randomUUID());
        unit.setName("Dział IT");
        unit.setCreateAt(LocalDateTime.now());

        when(userRepository.findById(sampleId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.assignToUnit(sampleId, unit);

        assertThat(sampleUser.getOrganizationUnit()).isEqualTo(unit);
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Usuwa przypisanie użytkownika do jednostki przy przeniesieniu")
    void shouldUnassignUserFromUnit() {
        OrganizationUnit unit = new OrganizationUnit();
        sampleUser.setOrganizationUnit(unit);

        when(userRepository.findById(sampleId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.removeFromUnit(sampleId);

        assertThat(sampleUser.getOrganizationUnit()).isNull();
    }

    @Test
    @DisplayName("Można nadać użytkownikowi rolę ADMIN")
    void shouldGrantAdminRole() {
        when(userRepository.findById(sampleId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.changeRole(sampleId, UserRole.ADMIN);

        assertThat(sampleUser.getRole()).isEqualTo(UserRole.ADMIN);
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Można cofnąć rolę ADMIN do USER")
    void shouldRevokeAdminRole() {
        sampleUser.setRole(UserRole.ADMIN);
        when(userRepository.findById(sampleId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.changeRole(sampleId, UserRole.USER);

        assertThat(sampleUser.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    @DisplayName("Dezaktywuje użytkownika zamiast go usuwać z bazy")
    void shouldDeactivateUserInsteadOfDeleting() {
        when(userRepository.findById(sampleId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.deactivateUser(sampleId);

        assertThat(sampleUser.isActive()).isFalse();
        verify(userRepository).save(sampleUser);
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Reaktywuje wcześniej dezaktywowanego użytkownika")
    void shouldReactivateUser() {
        sampleUser.setActive(false);
        when(userRepository.findById(sampleId)).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.activateUser(sampleId);

        assertThat(sampleUser.isActive()).isTrue();
    }

    @Test
    @DisplayName("Rzuca wyjątek przy próbie dostępu nieaktywnego użytkownika")
    void shouldThrowWhenInactiveUserTriesToAccess() {
        sampleUser.setActive(false);
        when(userRepository.findByUsername("jkowalski")).thenReturn(Optional.of(sampleUser));

        assertThatThrownBy(() -> userService.validateActiveUser("jkowalski"))
                .isInstanceOf(AccountDisabledException.class);
    }

    @Test
    @DisplayName("Aktualizuje profil użytkownika")
    void shouldUpdateProfile() {
        setAuthenticatedUser(sampleUser);
        when(userRepository.findByUsername(sampleUser.getUsername())).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateProfileDTO dto = new UpdateProfileDTO("Janek", "Kowalski", "Frontend Developer", "https://img", "Opis");

        ProfileDetailsDTO updated = userService.updateProfile(dto);

        assertThat(updated.firstName()).isEqualTo("Janek");
        assertThat(updated.position()).isEqualTo("Frontend Developer");
        assertThat(updated.avatarUrl()).isEqualTo("https://img");
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Zmienia hasło po poprawnej weryfikacji")
    void shouldChangePassword() {
        setAuthenticatedUser(sampleUser);
        when(userRepository.findByUsername(sampleUser.getUsername())).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("old", sampleUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPass123")).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.changePassword(new ChangePasswordDTO("old", "newPass123"));

        assertThat(sampleUser.getPassword()).isEqualTo("encoded");
        verify(userRepository).save(sampleUser);
    }

    @Test
    @DisplayName("Wyłącza 2FA dla użytkownika")
    void shouldDisableTwoFactorAuth() {
        sampleUser.setTwoFactorAuthEnabled(true);
        sampleUser.setTwoFactorAuthKey("enc");
        setAuthenticatedUser(sampleUser);
        when(userRepository.findByUsername(sampleUser.getUsername())).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.disableTwoFactorAuth();

        assertThat(sampleUser.isTwoFactorAuthEnabled()).isFalse();
        assertThat(sampleUser.getTwoFactorAuthKey()).isNull();
    }

    @Test
    @DisplayName("Anonimizuje konto użytkownika")
    void shouldAnonymizeUser() {
        setAuthenticatedUser(sampleUser);
        when(userRepository.findByUsername(sampleUser.getUsername())).thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        userService.anonymizeCurrentUser();

        assertThat(sampleUser.isActive()).isFalse();
        assertThat(sampleUser.getOrganizationUnit()).isNull();
        assertThat(sampleUser.getEmail()).contains("deleted_");
    }

    @Test
    @DisplayName("Tworzy domyślne ustawienia powiadomień")
    void shouldCreateDefaultNotificationSettings() {
        setAuthenticatedUser(sampleUser);
        when(userRepository.findByUsername(sampleUser.getUsername())).thenReturn(Optional.of(sampleUser));
        when(userNotificationSettingsRepository.findByUserId(sampleUser.getId())).thenReturn(Optional.empty());
        when(userNotificationSettingsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificationSettingsDTO settings = userService.getNotificationSettings();

        assertThat(settings.notifyOnAssignment()).isTrue();
        assertThat(settings.dailyDigestEnabled()).isFalse();
        verify(userNotificationSettingsRepository).save(any(UserNotificationSettings.class));
    }

    @Test
    @DisplayName("Aktualizuje ustawienia powiadomień")
    void shouldUpdateNotificationSettings() {
        UserNotificationSettings existing = new UserNotificationSettings();
        existing.setUser(sampleUser);
        existing.setUserId(sampleUser.getId());
        existing.setDailyDigestEnabled(false);

        setAuthenticatedUser(sampleUser);
        when(userRepository.findByUsername(sampleUser.getUsername())).thenReturn(Optional.of(sampleUser));
        when(userNotificationSettingsRepository.findByUserId(sampleUser.getId())).thenReturn(Optional.of(existing));
        when(userNotificationSettingsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        NotificationSettingsDTO updated = userService.updateNotificationSettings(
                new NotificationSettingsDTO(false, true, false, true));

        assertThat(updated.dailyDigestEnabled()).isTrue();
        assertThat(existing.isNotifyOnAssignment()).isFalse();
    }

    @Test
    @DisplayName("Tworzy domyślne preferencje")
    void shouldCreateDefaultPreferences() {
        setAuthenticatedUser(sampleUser);
        when(userRepository.findByUsername(sampleUser.getUsername())).thenReturn(Optional.of(sampleUser));
        when(userPreferencesRepository.findByUserId(sampleUser.getId())).thenReturn(Optional.empty());
        when(userPreferencesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserPreferencesDTO prefs = userService.getPreferences();

        assertThat(prefs.theme()).isEqualTo("light");
        assertThat(prefs.language()).isEqualTo("pl");
    }

    @Test
    @DisplayName("Aktualizuje preferencje")
    void shouldUpdatePreferences() {
        UserPreferences existing = new UserPreferences();
        existing.setUser(sampleUser);
        existing.setUserId(sampleUser.getId());

        setAuthenticatedUser(sampleUser);
        when(userRepository.findByUsername(sampleUser.getUsername())).thenReturn(Optional.of(sampleUser));
        when(userPreferencesRepository.findByUserId(sampleUser.getId())).thenReturn(Optional.of(existing));
        when(userPreferencesRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UserPreferencesDTO updated = userService.updatePreferences(new UserPreferencesDTO("dark", "en"));

        assertThat(updated.theme()).isEqualTo("dark");
        assertThat(updated.language()).isEqualTo("en");
    }

    private void setAuthenticatedUser(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user.getUsername(), "n/a", List.of())
        );
    }

}
