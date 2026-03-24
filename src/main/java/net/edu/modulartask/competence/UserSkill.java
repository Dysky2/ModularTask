package net.edu.modulartask.competence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import net.edu.modulartask.user.User;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_skill")
public class UserSkill {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userId;

    @ManyToOne
    @JoinColumn(name = "skill_id")
    private Skill skillId;

    @Column(name = "level")
    private Integer level;
}
