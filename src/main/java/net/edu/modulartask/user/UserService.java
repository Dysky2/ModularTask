package net.edu.modulartask.user;

import jakarta.transaction.Transactional;
import net.edu.modulartask.exceptions.AccountDisabledException;
import net.edu.modulartask.exceptions.DuplicateEmailException;
import net.edu.modulartask.exceptions.DuplicateUsernameException;
import net.edu.modulartask.exceptions.UserNotFoundException;
import net.edu.modulartask.organization.OrganizationUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID id) {
        return userRepository.getUserById(id);
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
            throw new RuntimeException("No logged user");
        }

        String username = authentication.getName();

        return findByUsername(username);
    }

    @Transactional
    public User createUser(User user) throws DuplicateEmailException, DuplicateUsernameException {

        if(user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is empty");
        }

        if(userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateEmailException("Email " + user.getEmail() + " exists");
        }

        if(user.getUsername() == null || user.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is empty");
        }

        if(userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateUsernameException("Username " + user.getUsername() + " exists");
        }

        userRepository.save(user);
        return user;
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
}
