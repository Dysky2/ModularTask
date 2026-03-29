package net.edu.modulartask.organization;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "organization_units")
public class OrganizationUnit {

    @Id
    @GeneratedValue
    private UUID unitId;

    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private OrganizationUnit parent;

    @Column(name = "create_at")
    private LocalDateTime createAt;

}
