package net.edu.modulartask.organization;

import net.edu.modulartask.exceptions.CyclicHierarchyException;
import net.edu.modulartask.exceptions.OrganizationUnitHasChildrenException;
import net.edu.modulartask.exceptions.UnauthorizedOrganizationActionException;
import net.edu.modulartask.user.User;
import net.edu.modulartask.user.UserRole;
import net.edu.modulartask.user.UserRepository;
import net.edu.modulartask.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationUnitServiceTest {

    @Mock
    private OrganizationUnitRepository organizationUnitRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrganizationUnitService organizationUnitService;

    private OrganizationUnit root;
    private OrganizationUnit child;
    private OrganizationUnit grandChild;

    @BeforeEach
    void setUp() {
        root = new OrganizationUnit();
        root.setUnitId(UUID.randomUUID());
        root.setName("Zarzad");

        child = new OrganizationUnit();
        child.setUnitId(UUID.randomUUID());
        child.setName("IT");
        child.setParent(root);

        grandChild = new OrganizationUnit();
        grandChild.setUnitId(UUID.randomUUID());
        grandChild.setName("Backend");
        grandChild.setParent(child);
    }

    @Test
    @DisplayName("Buduje graf organizacji w formacie nodes i edges")
    void shouldBuildOrganizationGraph() {
        when(organizationUnitRepository.findAll()).thenReturn(List.of(root, child, grandChild));

        OrganizationGraphDTO graph = organizationUnitService.getOrganizationGraph();

        assertThat(graph.nodes()).hasSize(3);
        assertThat(graph.edges()).hasSize(2);
        assertThat(graph.edges())
                .anyMatch(edge -> edge.source().equals(root.getUnitId().toString())
                        && edge.target().equals(child.getUnitId().toString()))
                .anyMatch(edge -> edge.source().equals(child.getUnitId().toString())
                        && edge.target().equals(grandChild.getUnitId().toString()));
    }

    @Test
    @DisplayName("Rzuca wyjątek gdy jednostka jest przenoszona pod siebie")
    void shouldThrowWhenUnitMovedToItself() {
        mockAdminUser();
        when(organizationUnitRepository.findByUnitId(root.getUnitId())).thenReturn(Optional.of(root));

        assertThatThrownBy(() -> organizationUnitService.moveUnit(root.getUnitId(), root.getUnitId()))
                .isInstanceOf(CyclicHierarchyException.class)
                .hasMessageContaining("same as current");
    }

    @Test
    @DisplayName("Rzuca wyjątek gdy przeniesienie tworzy cykl")
    void shouldThrowWhenMoveCreatesCycle() {
        mockAdminUser();
        when(organizationUnitRepository.findByUnitId(root.getUnitId())).thenReturn(Optional.of(root));
        when(organizationUnitRepository.findByUnitId(grandChild.getUnitId())).thenReturn(Optional.of(grandChild));

        assertThatThrownBy(() -> organizationUnitService.moveUnit(root.getUnitId(), grandChild.getUnitId()))
                .isInstanceOf(CyclicHierarchyException.class)
                .hasMessageContaining("cyclic hierarchy");
    }

    @Test
    @DisplayName("Pozwala przenieść jednostkę na poziom root")
    void shouldMoveUnitToRootWhenParentIsNull() {
        mockAdminUser();
        when(organizationUnitRepository.findByUnitId(child.getUnitId())).thenReturn(Optional.of(child));
        when(organizationUnitRepository.save(child)).thenReturn(child);

        OrganizationUnit moved = organizationUnitService.moveUnit(child.getUnitId(), (UUID) null);

        assertThat(moved.getParent()).isNull();
        verify(organizationUnitRepository).save(child);
    }

    @Test
    @DisplayName("Blokuje tworzenie jednostki gdy użytkownik nie jest administratorem")
    void shouldBlockCreateForNonAdmin() {
        User regularUser = new User();
        regularUser.setRole(UserRole.USER);
        when(userService.getCurrentlyLoggedUser()).thenReturn(regularUser);

        assertThatThrownBy(() -> organizationUnitService.createUnit("HR", null))
                .isInstanceOf(UnauthorizedOrganizationActionException.class)
                .hasMessageContaining("Only administrator");
    }

    @Test
    @DisplayName("Rzuca wyjątek dla pustej lub null nazwy jednostki")
    void shouldThrowWhenUnitNameIsNullOrBlank() {
        mockAdminUser();

        assertThatThrownBy(() -> organizationUnitService.createUnit("   ", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unit name is empty");

        assertThatThrownBy(() -> organizationUnitService.createUnit(null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unit name is empty");
    }

    @Test
    @DisplayName("ADMIN może zmienić nazwę jednostki")
    void shouldRenameUnitForAdmin() {
        mockAdminUser();
        when(organizationUnitRepository.findByUnitId(child.getUnitId())).thenReturn(Optional.of(child));
        when(organizationUnitRepository.save(child)).thenReturn(child);

        OrganizationUnit renamed = organizationUnitService.renameUnit(child.getUnitId(), "IT Core");

        assertThat(renamed.getName()).isEqualTo("IT Core");
        verify(organizationUnitRepository).save(child);
    }

    @Test
    @DisplayName("Blokuje usuwanie jednostki gdy ma dzieci")
    void shouldBlockDeleteWhenUnitHasChildren() {
        mockAdminUser();
        when(organizationUnitRepository.findByUnitId(root.getUnitId())).thenReturn(Optional.of(root));
        when(organizationUnitRepository.findByParent(root)).thenReturn(List.of(child));

        assertThatThrownBy(() -> organizationUnitService.deleteUnit(root.getUnitId()))
                .isInstanceOf(OrganizationUnitHasChildrenException.class)
                .hasMessageContaining("with children");

        verify(organizationUnitRepository, never()).delete(root);
    }

    @Test
    @DisplayName("ADMIN może usunąć jednostkę bez dzieci")
    void shouldDeleteLeafUnitForAdmin() {
        mockAdminUser();
        User userInUnit = new User();
        userInUnit.setOrganizationUnit(child);

        when(organizationUnitRepository.findByUnitId(child.getUnitId())).thenReturn(Optional.of(child));
        when(organizationUnitRepository.findByParent(child)).thenReturn(List.of());
        when(userRepository.findByOrganizationUnit_UnitId(child.getUnitId())).thenReturn(List.of(userInUnit));

        organizationUnitService.deleteUnit(child.getUnitId());

        assertThat(userInUnit.getOrganizationUnit()).isNull();
        verify(userRepository).saveAll(List.of(userInUnit));
        verify(organizationUnitRepository).delete(child);
    }

    private void mockAdminUser() {
        User adminUser = new User();
        adminUser.setRole(UserRole.ADMIN);
        when(userService.getCurrentlyLoggedUser()).thenReturn(adminUser);
    }
}

