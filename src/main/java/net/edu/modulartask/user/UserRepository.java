package net.edu.modulartask.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User getUserById(UUID uuid);

    List<User> findAll();
}
