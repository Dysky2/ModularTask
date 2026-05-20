package net.edu.modulartask.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface IssueStatusRepository extends JpaRepository<IssueStatus, UUID> {
    boolean existsByName(String name);
}

