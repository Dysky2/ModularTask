package net.edu.modulartask.user;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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

    @GetMapping("/profile/me")
    public ResponseEntity<ProfileDetailsDTO> getProfileDetails(){
        ProfileDetailsDTO details = userService.getProfileDetails();
        return ResponseEntity.ok(details);
    }

    @PostMapping("/profile/me/update")
    public ResponseEntity<ProfileDetailsDTO> updateUserDetails(@RequestBody String description){
        ProfileDetailsDTO details = userService.updateUserDetails(description);
        return ResponseEntity.ok(details);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<Map<String, String>> updateUser(@PathVariable(name = "userId") UUID userId,
                                                          @Valid @RequestBody UpdateUserDTO updateUserDTO) {
        return userService.updateUser(userId, updateUserDTO);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable(name = "userId") UUID userId) {
        return userService.deleteUser(userId);
    }
}
