package net.edu.modulartask.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    User getUserById(UUID uuid);

    User getUserByUsername(String username);

    List<User> findAll();

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    User findByEmail(String email);

    List<User> findByOrganizationUnit_UnitId(UUID organizationUnitUnitId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
