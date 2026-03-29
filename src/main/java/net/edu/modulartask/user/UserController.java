package net.edu.modulartask.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin("http://localhost:5173")
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping("/api/users/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/api/users/{id}")
    public User getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }
}
