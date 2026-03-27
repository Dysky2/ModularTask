package net.edu.modulartask.organization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizationUnitRepository extends JpaRepository<OrganizationUnit, UUID> {
    OrganizationUnit getOrganizationUnitByUnitId(UUID unitId);

    boolean existsByName(String name);

    Optional<OrganizationUnit> findByUnitId(UUID unitId);

    Optional<OrganizationUnit> findByName(String name);

    List<OrganizationUnit> findByParent(OrganizationUnit parent);
}
