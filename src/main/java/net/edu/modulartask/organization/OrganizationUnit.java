package net.edu.modulartask.organization;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "organization_units")
public class OrganizationUnit {

    @Id
    @GeneratedValue
    private UUID unitId;

    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private OrganizationUnit parent;

    @Column(name = "create_at")
    private LocalDateTime createAt;

}
