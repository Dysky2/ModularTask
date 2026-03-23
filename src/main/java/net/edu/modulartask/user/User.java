package net.edu.modulartask.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    private String username;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(name = "unit_id")
    private UUID unitId;

    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
