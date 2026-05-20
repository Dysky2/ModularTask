package net.edu.modulartask.admin;

import net.edu.modulartask.exceptions.UnauthorizedAdminActionException;
import net.edu.modulartask.exceptions.UserNotFoundException;
import net.edu.modulartask.user.User;
import net.edu.modulartask.user.UserRepository;
import net.edu.modulartask.user.UserRole;
import net.edu.modulartask.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AdminService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IssuePriorityRepository issuePriorityRepository;

    @Autowired
    private IssueStatusRepository issueStatusRepository;

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void ensureAdmin() {
        User user = userService.getCurrentlyLoggedUser();
        if (user.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedAdminActionException("Only administrator can perform this action");
        }
    }

    public List<User> listUsers() {
        ensureAdmin();
        return userRepository.findAll();
    }

    public User updateUserRole(UUID userId, UserRole role) {
        ensureAdmin();
        userService.changeRole(userId, role);
        User updated = userService.findById(userId);
        logAction("UPDATE_USER_ROLE", "users", userId, "{\"role\":\"" + role + "\"}");
        return updated;
    }

    public User setUserActive(UUID userId, boolean active) {
        ensureAdmin();
        if (active) {
            userService.activateUser(userId);
        } else {
            userService.deactivateUser(userId);
        }
        User updated = userService.findById(userId);
        logAction(active ? "ACTIVATE_USER" : "DEACTIVATE_USER", "users", userId, null);
        return updated;
    }

    public IssuePriority createPriority(IssuePriorityDTO dto) {
        ensureAdmin();
        if (dto.name() == null || dto.name().isBlank()) {
            throw new IllegalArgumentException("Priority name is empty");
        }
        if (issuePriorityRepository.existsByName(dto.name())) {
            throw new IllegalArgumentException("Priority already exists");
        }
        IssuePriority priority = new IssuePriority();
        priority.setName(dto.name());
        priority.setColorHex(dto.colorHex());
        priority.setOrderIndex(dto.orderIndex());
        IssuePriority saved = issuePriorityRepository.save(priority);
        logAction("CREATE_PRIORITY", "issue_priorities", saved.getId(), null);
        return saved;
    }

    public List<IssuePriority> listPriorities() {
        ensureAdmin();
        return issuePriorityRepository.findAll();
    }

    public IssuePriority updatePriority(UUID id, IssuePriorityDTO dto) {
        ensureAdmin();
        IssuePriority priority = getPriorityOrThrow(id);
        applyPriorityUpdates(priority, dto);
        IssuePriority saved = issuePriorityRepository.save(priority);
        logAction("UPDATE_PRIORITY", "issue_priorities", saved.getId(), null);
        return saved;
    }

    private IssuePriority getPriorityOrThrow(UUID id) {
        return issuePriorityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Priority not found"));
    }

    private void applyPriorityUpdates(IssuePriority priority, IssuePriorityDTO dto) {
        if (dto.name() != null && !dto.name().isBlank()) {
            priority.setName(dto.name());
        }
        if (dto.colorHex() != null) {
            priority.setColorHex(dto.colorHex());
        }
        priority.setOrderIndex(dto.orderIndex());
    }

    public void deletePriority(UUID id) {
        ensureAdmin();
        issuePriorityRepository.deleteById(id);
        logAction("DELETE_PRIORITY", "issue_priorities", id, null);
    }

    public IssueStatus createStatus(IssueStatusDTO dto) {
        ensureAdmin();
        if (dto.name() == null || dto.name().isBlank()) {
            throw new IllegalArgumentException("Status name is empty");
        }
        if (dto.category() == null || dto.category().isBlank()) {
            throw new IllegalArgumentException("Status category is empty");
        }
        if (issueStatusRepository.existsByName(dto.name())) {
            throw new IllegalArgumentException("Status already exists");
        }
        IssueStatus status = new IssueStatus();
        status.setName(dto.name());
        status.setCategory(dto.category());
        status.setOrderIndex(dto.orderIndex());
        IssueStatus saved = issueStatusRepository.save(status);
        logAction("CREATE_STATUS", "issue_statuses", saved.getId(), null);
        return saved;
    }

    public List<IssueStatus> listStatuses() {
        ensureAdmin();
        return issueStatusRepository.findAll();
    }

    public IssueStatus updateStatus(UUID id, IssueStatusDTO dto) {
        ensureAdmin();
        IssueStatus status = getStatusOrThrow(id);
        applyStatusUpdates(status, dto);
        IssueStatus saved = issueStatusRepository.save(status);
        logAction("UPDATE_STATUS", "issue_statuses", saved.getId(), null);
        return saved;
    }

    private IssueStatus getStatusOrThrow(UUID id) {
        return issueStatusRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Status not found"));
    }

    private void applyStatusUpdates(IssueStatus status, IssueStatusDTO dto) {
        if (dto.name() != null && !dto.name().isBlank()) {
            status.setName(dto.name());
        }
        if (dto.category() != null && !dto.category().isBlank()) {
            status.setCategory(dto.category());
        }
        status.setOrderIndex(dto.orderIndex());
    }

    public void deleteStatus(UUID id) {
        ensureAdmin();
        issueStatusRepository.deleteById(id);
        logAction("DELETE_STATUS", "issue_statuses", id, null);
    }

    public List<SystemConfig> listConfigs() {
        ensureAdmin();
        return systemConfigRepository.findAll();
    }

    public SystemConfig upsertConfig(SystemConfigDTO dto) {
        ensureAdmin();
        if (dto.configKey() == null || dto.configKey().isBlank()) {
            throw new IllegalArgumentException("Config key is empty");
        }
        SystemConfig config = systemConfigRepository.findById(dto.configKey())
                .orElseGet(SystemConfig::new);
        config.setConfigKey(dto.configKey());
        config.setConfigValue(dto.configValue());
        config.setDescription(dto.description());
        SystemConfig saved = systemConfigRepository.save(config);
        logAction("UPDATE_CONFIG", "system_config", null, "{\"key\":\"" + saved.getConfigKey() + "\"}");
        return saved;
    }

    public List<AuditLog> listAuditLogs() {
        ensureAdmin();
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }

    private void logAction(String action, String targetType, UUID targetId, String details) {
        User user = userService.getCurrentlyLoggedUser();
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}
