package net.edu.modulartask.organization;

import net.edu.modulartask.exceptions.CyclicHierarchyException;
import net.edu.modulartask.exceptions.OrganizationUnitNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrganizationUnitService {

    @Autowired
    OrganizationUnitRepository organizationUnitRepository;

    public OrganizationUnit findByUnitId(UUID parentId) {
        return organizationUnitRepository.findByUnitId(parentId).orElseThrow(
                () -> new OrganizationUnitNotFoundException("Parent not found"));
    }

    public OrganizationUnit createUnit(String newUnitName, UUID parentId) {
        OrganizationUnit organizationUnit = new OrganizationUnit();

        if(newUnitName.isBlank()) {
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

    public OrganizationUnit moveUnit(UUID unitId, UUID parentId) {
        if(unitId.equals(parentId)) {
            throw new CyclicHierarchyException("Parent unit is the same as current");
        }

        OrganizationUnit organizationUnit = findByUnitId(unitId);

        OrganizationUnit parent = findByUnitId(parentId);

        organizationUnit.setParent(parent);

        return organizationUnitRepository.save(organizationUnit);
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
}
