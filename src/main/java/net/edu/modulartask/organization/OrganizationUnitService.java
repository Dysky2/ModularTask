package net.edu.modulartask.organization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.core.support.RepositoryMethodInvocationListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrganizationUnitService {
    @Autowired
    OrganizationUnitRepository organizationUnitRepository;

    public OrganizationUnit createUnit(String newUnitName, UUID parentId) throws IllegalArgumentException {
        OrganizationUnit organizationUnit = new OrganizationUnit();

        organizationUnit.setName(newUnitName);

        if(organizationUnitRepository.existsByName(newUnitName)) {
            throw new IllegalArgumentException("This unit exists");
        }

        OrganizationUnit parent = organizationUnitRepository.getOrganizationUnitByUnitId(parentId);

        if(organizationUnit.equals(parent)) {
            throw new IllegalArgumentException("Unit cannot be parent for yourself");
        }

        organizationUnit.setParent(parent);

        organizationUnitRepository.save(organizationUnit);

        return organizationUnit;
    }

    public void updateParent(UUID parentId, UUID parentId2) {

        OrganizationUnit parent_old = organizationUnitRepository.getOrganizationUnitByUnitId(parentId);

        if(parent_old == null) {
            throw new IllegalArgumentException("This unit not exists");
        }

        OrganizationUnit parent_new = organizationUnitRepository.getOrganizationUnitByUnitId(parentId2);

        if(parent_new == null) {
            throw new IllegalArgumentException("This unit not exists");
        }

        if(parent_old.equals(parent_new)) {
            throw new IllegalArgumentException("Unit cannot be parent for yourself");
        }

        parent_old.setParent(parent_new);

        organizationUnitRepository.save(parent_old);
    }
}
