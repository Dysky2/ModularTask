package net.edu.modulartask.organization;

import net.edu.modulartask.exceptions.CyclicHierarchyException;
import net.edu.modulartask.exceptions.OrganizationUnitHasChildrenException;
import net.edu.modulartask.exceptions.OrganizationUnitNotFoundException;
import net.edu.modulartask.exceptions.UnauthorizedOrganizationActionException;
import net.edu.modulartask.user.User;
import net.edu.modulartask.user.UserRole;
import net.edu.modulartask.user.UserRepository;
import net.edu.modulartask.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrganizationUnitService {

    @Autowired
    OrganizationUnitRepository organizationUnitRepository;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    public OrganizationUnit findByUnitId(UUID unitId) {
        return organizationUnitRepository.findByUnitId(unitId).orElseThrow(
                () -> new OrganizationUnitNotFoundException("Organization unit not found: " + unitId));
    }

    public OrganizationUnit createUnit(String newUnitName, UUID parentId) {
        ensureAdminPrivileges();

        OrganizationUnit organizationUnit = new OrganizationUnit();

        if(newUnitName == null || newUnitName.isBlank()) {
            throw new IllegalArgumentException("Unit name is empty");
        }

        organizationUnit.setName(newUnitName);

        organizationUnit.setCreateAt(LocalDateTime.now());

        if(parentId != null) {
            OrganizationUnit parent = findByUnitId(parentId);

            organizationUnit.setParent(parent);
        }

        return organizationUnitRepository.save(organizationUnit);
    }

    public OrganizationUnit createUnit(CreateOrganizationUnitDTO createOrganizationUnitDTO) {
        return createUnit(createOrganizationUnitDTO.name(), createOrganizationUnitDTO.parentId());
    }

    public OrganizationUnit moveUnit(UUID unitId, UUID parentId) {
        ensureAdminPrivileges();

        OrganizationUnit organizationUnit = findByUnitId(unitId);

        if(parentId == null) {
            organizationUnit.setParent(null);
            return organizationUnitRepository.save(organizationUnit);
        }

        if(unitId.equals(parentId)) {
            throw new CyclicHierarchyException("Parent unit is the same as current");
        }

        OrganizationUnit parent = findByUnitId(parentId);

        if (wouldCreateCycle(organizationUnit, parent)) {
            throw new CyclicHierarchyException("Moving unit would create cyclic hierarchy");
        }

        organizationUnit.setParent(parent);

        return organizationUnitRepository.save(organizationUnit);
    }

    public OrganizationUnit moveUnit(UUID unitId, MoveOrganizationUnitDTO moveOrganizationUnitDTO) {
        return moveUnit(unitId, moveOrganizationUnitDTO.parentId());
    }

    public OrganizationUnit renameUnit(UUID unitId, String newName) {
        ensureAdminPrivileges();

        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("Unit name is empty");
        }

        OrganizationUnit unit = findByUnitId(unitId);
        unit.setName(newName);

        return organizationUnitRepository.save(unit);
    }

    public OrganizationUnit renameUnit(UUID unitId, RenameOrganizationUnitDTO renameOrganizationUnitDTO) {
        return renameUnit(unitId, renameOrganizationUnitDTO.name());
    }

    public void deleteUnit(UUID unitId) {
        ensureAdminPrivileges();

        OrganizationUnit unit = findByUnitId(unitId);
        List<OrganizationUnit> children = organizationUnitRepository.findByParent(unit);

        if (!children.isEmpty()) {
            throw new OrganizationUnitHasChildrenException("Cannot delete organization unit with children");
        }

        List<User> usersInUnit = userRepository.findByOrganizationUnit_UnitId(unitId);
        for (User user : usersInUnit) {
            user.setOrganizationUnit(null);
        }
        userRepository.saveAll(usersInUnit);

        organizationUnitRepository.delete(unit);
    }

    public List<OrganizationUnit> getDirectChildren(UUID unitId) {
           OrganizationUnit parent = findByUnitId(unitId);

           return organizationUnitRepository.findByParent(parent);
    }

    public List<OrganizationUnit> getPathFromRoot(UUID unitId) {
        OrganizationUnit organizationUnit = findByUnitId(unitId);

        List<OrganizationUnit> list = new ArrayList<>();

        list.add(organizationUnit);

        while (organizationUnit.getParent() != null) {
            organizationUnit = organizationUnit.getParent();

            list.add(organizationUnit);
        }

        Collections.reverse(list);

        return list;
    }

    public OrganizationGraphDTO getOrganizationGraph() {
        List<OrganizationUnit> units = organizationUnitRepository.findAll();

        List<GraphNodeDTO> nodes = units.stream()
                .map(unit -> new GraphNodeDTO(
                        unit.getUnitId().toString(),
                        unit.getName(),
                        "unit"
                ))
                .collect(Collectors.toList());

        List<GraphEdgeDTO> edges = units.stream()
                .filter(unit -> unit.getParent() != null)
                .map(unit -> new GraphEdgeDTO(
                        "edge-" + unit.getParent().getUnitId() + "-" + unit.getUnitId(),
                        unit.getParent().getUnitId().toString(),
                        unit.getUnitId().toString(),
                        "reports_to"
                ))
                .collect(Collectors.toList());

        return new OrganizationGraphDTO(nodes, edges);
    }

    private boolean wouldCreateCycle(OrganizationUnit unit, OrganizationUnit proposedParent) {
        OrganizationUnit current = proposedParent;

        while (current != null) {
            if (current.getUnitId().equals(unit.getUnitId())) {
                return true;
            }
            current = current.getParent();
        }

        return false;
    }

    private void ensureAdminPrivileges() {
        User currentUser = userService.getCurrentlyLoggedUser();

        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedOrganizationActionException("Only administrator can modify organization structure");
        }
    }
}
