package net.edu.modulartask.admin;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "issue_statuses")
public class IssueStatus {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(name = "order_index")
    private int orderIndex;
}

