package net.edu.modulartask.admin;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "issue_priorities")
public class IssuePriority {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "color_hex")
    private String colorHex;

    @Column(name = "order_index")
    private int orderIndex;
}

