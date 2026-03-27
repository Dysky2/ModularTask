package net.edu.modulartask.organization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrganizationUnitRepository extends JpaRepository<OrganizationUnit, UUID> {
    List<OrganizationUnit> findAll();

    OrganizationUnit getOrganizationUnitByUnitId(UUID unitId);

    boolean existsByName(String name);
}
