package net.edu.modulartask.admin;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "system_config")
public class SystemConfig {

    @Id
    @Column(name = "config_key")
    private String configKey;

    @Column(name = "config_value", nullable = false)
    private String configValue;

    private String description;
}

