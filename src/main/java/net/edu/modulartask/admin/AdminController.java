package net.edu.modulartask.admin;

import net.edu.modulartask.user.User;
import net.edu.modulartask.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")

public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/users")
    public List<User> listUsers() {
        return adminService.listUsers();
    }

    @PatchMapping("/users/{userId}/role")
    public User updateUserRole(@PathVariable UUID userId, @RequestBody UserRoleUpdateDTO dto) {
        return adminService.updateUserRole(userId, dto.role());
    }

    @PatchMapping("/users/{userId}/activate")
    public User activateUser(@PathVariable UUID userId) {
        return adminService.setUserActive(userId, true);
    }

    @PatchMapping("/users/{userId}/deactivate")
    public User deactivateUser(@PathVariable UUID userId) {
        return adminService.setUserActive(userId, false);
    }

    @GetMapping("/priorities")
    public List<IssuePriority> listPriorities() {
        return adminService.listPriorities();
    }

    @PostMapping("/priorities")
    public IssuePriority createPriority(@RequestBody IssuePriorityDTO dto) {
        return adminService.createPriority(dto);
    }

    @PutMapping("/priorities/{id}")
    public IssuePriority updatePriority(@PathVariable UUID id, @RequestBody IssuePriorityDTO dto) {
        return adminService.updatePriority(id, dto);
    }

    @DeleteMapping("/priorities/{id}")
    public ResponseEntity<Void> deletePriority(@PathVariable UUID id) {
        adminService.deletePriority(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statuses")
    public List<IssueStatus> listStatuses() {
        return adminService.listStatuses();
    }

    @PostMapping("/statuses")
    public IssueStatus createStatus(@RequestBody IssueStatusDTO dto) {
        return adminService.createStatus(dto);
    }

    @PutMapping("/statuses/{id}")
    public IssueStatus updateStatus(@PathVariable UUID id, @RequestBody IssueStatusDTO dto) {
        return adminService.updateStatus(id, dto);
    }

    @DeleteMapping("/statuses/{id}")
    public ResponseEntity<Void> deleteStatus(@PathVariable UUID id) {
        adminService.deleteStatus(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/config")
    public List<SystemConfig> listConfigs() {
        return adminService.listConfigs();
    }

    @PutMapping("/config")
    public SystemConfig upsertConfig(@RequestBody SystemConfigDTO dto) {
        return adminService.upsertConfig(dto);
    }

    @GetMapping("/audit-logs")
    public List<AuditLog> listAuditLogs() {
        return adminService.listAuditLogs();
    }

    @GetMapping("/roles")
    public List<UserRole> listRoles() {
        return List.of(UserRole.values());
    }
}
