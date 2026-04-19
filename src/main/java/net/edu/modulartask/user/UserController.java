package net.edu.modulartask.user;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@CrossOrigin("http://localhost:5173")
public class UserController {
    @Autowired
    UserService userService;

    @GetMapping("/all")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }

    @PostMapping("/create")
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        User createdUser = userService.createUser(createUserDTO);
        return ResponseEntity.ok(createdUser);
    }
}
