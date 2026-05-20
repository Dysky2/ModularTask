package net.edu.modulartask.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IssuePriorityRepository extends JpaRepository<IssuePriority, UUID> {
    boolean existsByName(String name);
}

