package net.edu.modulartask.user;

import jakarta.transaction.Transactional;
import net.edu.modulartask.auth.TwoFactorService;
import net.edu.modulartask.exceptions.AccountDisabledException;
import net.edu.modulartask.exceptions.DuplicateEmailException;
import net.edu.modulartask.exceptions.DuplicateUsernameException;
import net.edu.modulartask.exceptions.UserNotFoundException;
import net.edu.modulartask.exceptions.UnauthorizedAdminActionException;
import net.edu.modulartask.exceptions.UnauthorizedException;
import net.edu.modulartask.organization.OrganizationUnit;
import net.edu.modulartask.tasks.TaskHistory;
import net.edu.modulartask.tasks.TaskHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import net.edu.modulartask.admin.SystemConfig;
import net.edu.modulartask.admin.SystemConfigRepository;
import net.edu.modulartask.notification.NotificationProducer;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private NotificationProducer notificationProducer;

    @Autowired
    private UserNotificationSettingsRepository userNotificationSettingsRepository;

    @Autowired
    private UserPreferencesRepository userPreferencesRepository;

    @Autowired
    private SystemConfigRepository systemConfigRepository;

    @Autowired
    TwoFactorService twoFactorService;

    @Autowired
    TaskHistoryRepository taskHistoryRepository;

    public List<User> getAllUsers() {
        ensureAdminPrivileges();
        return userRepository.findAll();
    }

    public User getUserById(UUID id) {
        return findById(id);
    }

    public User getUserByUsername(String username) {
        return userRepository.getUserByUsername(username);
    }

    public User findById(UUID id) {
        return userRepository.findById(id).orElseThrow(
                () -> new UserNotFoundException("User does not exists"));
    }

    public User findByUsername(String userName) {
        return userRepository.findByUsername(userName).orElseThrow(
                () -> new UserNotFoundException("User with username: " + userName +  " does not exits"));
    }

    public void registerUser(User user) {
        String planPassword = user.getPassword();

        String hashedPassword = passwordEncoder.encode(planPassword);

        user.setPassword(hashedPassword);

        userRepository.save(user);
    }

    public User getCurrentlyLoggedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("No logged user");
        }

        String username = authentication.getName();

        return findByUsername(username);
    }

    @Transactional
    public User createUser(CreateUserDTO createUserDTO) {
        ensureAdminPrivileges();

        if(createUserDTO.email() == null || createUserDTO.email().isBlank()) {
            throw new IllegalArgumentException("Email is empty");
        }

        if(userRepository.existsByEmail(createUserDTO.email())) {
            throw new DuplicateEmailException("Email " + createUserDTO.email() + " exists");
        }

        if(createUserDTO.username() == null || createUserDTO.username().isBlank()) {
            throw new IllegalArgumentException("Username is empty");
        }

        if(userRepository.existsByUsername(createUserDTO.username())) {
            throw new DuplicateUsernameException("Username " + createUserDTO.username() + " exists");
        }

        User user = new User();

        user.setUsername(createUserDTO.username());
        user.setFirstName(createUserDTO.firstName());
        user.setLastName(createUserDTO.lastName());
        user.setEmail(createUserDTO.email());

        String planPassword = createUserDTO.password();
        validatePasswordPolicy(planPassword);

        String hashedPassword = passwordEncoder.encode(planPassword);

        user.setPassword(hashedPassword);
        user.setRole(createUserDTO.role());
        boolean isActive = createUserDTO.isActive() == null || createUserDTO.isActive();
        user.setActive(isActive);
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public ResponseEntity<String> enable2Fa(User user, String verifiedKey) {

        if(user.isTwoFactorAuthEnabled()) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try  {
            String encryptedKey = twoFactorService.encrypt(verifiedKey);
            user.setTwoFactorAuthKey(encryptedKey);
            user.setTwoFactorAuthEnabled(true);
            userRepository.save(user);

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            return ResponseEntity.status(403).body("2Fa enabled failed, " + e.getMessage());
        }
    }


    public void assignToUnit(UUID userId, OrganizationUnit unit) {
        User user = findById(userId);

        user.setOrganizationUnit(unit);

        userRepository.save(user);
    }

    public void removeFromUnit(UUID userId) {
        User user = findById(userId);

        user.setOrganizationUnit(null);

        userRepository.save(user);
    }

    public void changeRole(UUID userId, UserRole userRole) {
        User user = findById(userId);

        user.setRole(userRole);

        userRepository.save(user);
    }

    public void deactivateUser(UUID userId) {
        User user = findById(userId);

        user.setActive(false);

        userRepository.save(user);
    }

    public void activateUser(UUID userId) {
        User user = findById(userId);

        user.setActive(true);

        userRepository.save(user);
    }

    public void validateActiveUser(String username) {
        User user = findByUsername(username);

        if(!user.isActive()) {
            throw new AccountDisabledException("This account is turn off");
        }

    }

    public ProfileDetailsDTO getProfileDetails() {
        User user = getCurrentlyLoggedUser();

        if(user == null){
            throw new UserNotFoundException("User not found");
        }

        List<TaskHistory> history = taskHistoryRepository.findTop10ByUserOrderByCreatedAtDesc(user);

        List<UserActivityDTO> recentActivity = history.stream()
                .map(event -> new UserActivityDTO(
                        event.getTask().getId(),
                        event.getTask().getTitle(),
                        event.getAction(),
                        event.getDetails(),
                        event.getCreatedAt()
                )).toList();

        ProfileDetailsDTO details = new ProfileDetailsDTO(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getDescription(),
                user.getPosition(),
                user.getAvatarUrl(),
                user.getOrganizationUnit(),
                user.isActive(),
                recentActivity
        );

        return details;
    }

    public ProfileDetailsDTO updateUserDetails(String description){
        if(description == null || description.isBlank()){
            throw new IllegalArgumentException("Description is empty");
        }

        User user = getCurrentlyLoggedUser();
        if(user == null){
            throw new UserNotFoundException("User not found");
        }

        user.setDescription(description);
        userRepository.save(user);

        return getProfileDetails();
    }

    public ProfileDetailsDTO updateProfile(UpdateProfileDTO updateProfileDTO) {
        User user = getCurrentlyLoggedUser();
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        if (updateProfileDTO.firstName() != null && !updateProfileDTO.firstName().isBlank()) {
            user.setFirstName(updateProfileDTO.firstName());
        }
        if (updateProfileDTO.lastName() != null && !updateProfileDTO.lastName().isBlank()) {
            user.setLastName(updateProfileDTO.lastName());
        }
        if (updateProfileDTO.position() != null && !updateProfileDTO.position().isBlank()) {
            user.setPosition(updateProfileDTO.position());
        }
        if (updateProfileDTO.avatarUrl() != null && !updateProfileDTO.avatarUrl().isBlank()) {
            user.setAvatarUrl(updateProfileDTO.avatarUrl());
        }
        if (updateProfileDTO.description() != null && !updateProfileDTO.description().isBlank()) {
            user.setDescription(updateProfileDTO.description());
        }

        userRepository.save(user);
        return getProfileDetails();
    }

    public void changePassword(ChangePasswordDTO dto) {
        User user = getCurrentlyLoggedUser();
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        if (dto.currentPassword() == null || dto.currentPassword().isBlank()) {
            throw new IllegalArgumentException("Current password is empty");
        }
        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is invalid");
        }
        if (dto.newPassword() == null || dto.newPassword().isBlank()) {
            throw new IllegalArgumentException("New password is empty");
        }
        validatePasswordPolicy(dto.newPassword());

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
        notificationProducer.sendNotification("Password changed", "Your account password has been updated.", user.getId());
    }

    public void disableTwoFactorAuth() {
        User user = getCurrentlyLoggedUser();
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        user.setTwoFactorAuthKey(null);
        user.setTwoFactorAuthEnabled(false);
        userRepository.save(user);
        notificationProducer.sendNotification("Two-factor auth disabled", "Two-factor authentication has been disabled.", user.getId());
    }

    public void anonymizeCurrentUser() {
        User user = getCurrentlyLoggedUser();
        if (user == null) {
            throw new UserNotFoundException("User not found");
        }
        String anonymizedEmail = "deleted_" + user.getId() + "@deleted.local";
        user.setUsername("deleted_" + user.getId());
        user.setFirstName(null);
        user.setLastName(null);
        user.setEmail(anonymizedEmail);
        user.setDescription(null);
        user.setPosition(null);
        user.setAvatarUrl(null);
        user.setOrganizationUnit(null);
        user.setActive(false);
        userRepository.save(user);
        notificationProducer.sendNotification("Account deleted", "Your account has been deleted and anonymized.", user.getId());
    }

    public NotificationSettingsDTO getNotificationSettings() {
        User user = getCurrentlyLoggedUser();
        UserNotificationSettings settings = userNotificationSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultNotificationSettings(user));
        return new NotificationSettingsDTO(
                settings.isNotifyOnAssignment(),
                settings.isNotifyOnMention(),
                settings.isNotifyOnStatusChange(),
                settings.isDailyDigestEnabled()
        );
    }

    public NotificationSettingsDTO updateNotificationSettings(NotificationSettingsDTO dto) {
        User user = getCurrentlyLoggedUser();
        UserNotificationSettings settings = userNotificationSettingsRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultNotificationSettings(user));

        settings.setNotifyOnAssignment(dto.notifyOnAssignment());
        settings.setNotifyOnMention(dto.notifyOnMention());
        settings.setNotifyOnStatusChange(dto.notifyOnStatusChange());
        settings.setDailyDigestEnabled(dto.dailyDigestEnabled());
        userNotificationSettingsRepository.save(settings);

        return new NotificationSettingsDTO(
                settings.isNotifyOnAssignment(),
                settings.isNotifyOnMention(),
                settings.isNotifyOnStatusChange(),
                settings.isDailyDigestEnabled()
        );
    }

    public UserPreferencesDTO getPreferences() {
        User user = getCurrentlyLoggedUser();
        UserPreferences preferences = userPreferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreferences(user));

        return new UserPreferencesDTO(preferences.getTheme(), preferences.getLanguage());
    }

    public UserPreferencesDTO updatePreferences(UserPreferencesDTO dto) {
        User user = getCurrentlyLoggedUser();
        UserPreferences preferences = userPreferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultPreferences(user));

        if (dto.theme() != null && !dto.theme().isBlank()) {
            preferences.setTheme(dto.theme());
        }
        if (dto.language() != null && !dto.language().isBlank()) {
            preferences.setLanguage(dto.language());
        }
        userPreferencesRepository.save(preferences);

        return new UserPreferencesDTO(preferences.getTheme(), preferences.getLanguage());
    }

    private UserNotificationSettings createDefaultNotificationSettings(User user) {
        if (user.getId() == null) {
            throw new IllegalStateException("User id is null");
        }
        UserNotificationSettings settings = new UserNotificationSettings();
        settings.setUser(user);
        settings.setUserId(user.getId());
        return userNotificationSettingsRepository.save(settings);
    }

    private UserPreferences createDefaultPreferences(User user) {
        if (user.getId() == null) {
            throw new IllegalStateException("User id is null");
        }
        UserPreferences preferences = new UserPreferences();
        preferences.setUser(user);
        preferences.setUserId(user.getId());
        return userPreferencesRepository.save(preferences);
    }

    private void validatePasswordPolicy(String password) {
        int minLength = getIntConfig("PASSWORD_MIN_LENGTH", 8);
        boolean requireSpecial = getBooleanConfig("PASSWORD_REQUIRE_SPECIAL", false);

        if (password == null || password.length() < minLength) {
            throw new IllegalArgumentException("Password must be at least " + minLength + " characters");
        }
        if (requireSpecial && password.chars().noneMatch(ch -> !Character.isLetterOrDigit(ch))) {
            throw new IllegalArgumentException("Password must include a special character");
        }
    }

    private void ensureAdminPrivileges() {
        User currentUser = getCurrentlyLoggedUser();
        if (currentUser.getRole() != UserRole.ADMIN) {
            throw new UnauthorizedAdminActionException("Only administrator can perform this action");
        }
    }

    private int getIntConfig(String key, int defaultValue) {
        return systemConfigRepository.findById(key)
                .map(SystemConfig::getConfigValue)
                .map(value -> {
                    try {
                        return Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

    private boolean getBooleanConfig(String key, boolean defaultValue) {
        return systemConfigRepository.findById(key)
                .map(SystemConfig::getConfigValue)
                .map(value -> value.equalsIgnoreCase("true"))
                .orElse(defaultValue);
    }

}
