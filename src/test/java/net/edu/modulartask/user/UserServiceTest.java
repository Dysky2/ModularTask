package net.edu.modulartask.user;

import net.edu.modulartask.exceptions.AccountDisabledException;
import net.edu.modulartask.exceptions.DuplicateEmailException;
import net.edu.modulartask.exceptions.DuplicateUsernameException;
import net.edu.modulartask.exceptions.UserNotFoundException;
import net.edu.modulartask.organization.OrganizationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User sampleUser;
    private CreateUserDTO sampleCreateUserDTO;
    private UUID sampleId;

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

        sampleCreateUserDTO = new CreateUserDTO(
                "jkowalski",
                "Jan",
                "Kowalski",
                "jan.kowalski@firma.pl",
                "tajneHaslo123",
                UserRole.USER,
                true
        );
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

}
