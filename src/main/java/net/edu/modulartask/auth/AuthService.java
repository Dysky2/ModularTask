package net.edu.modulartask.auth;

import net.edu.modulartask.config.JwtService;
import net.edu.modulartask.user.User;
import net.edu.modulartask.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    public ResponseEntity<?> login(LoginRequest loginRequest) {
        User user = userService.getUserByUsername(loginRequest.username());

        if(user == null) {
            return ResponseEntity.status(401).body("Invalid data_");
        }

        boolean isPasswordCorrect = passwordEncoder.matches(
                loginRequest.password(),
                user.getPassword()
        );

        if(isPasswordCorrect) {
            String token = jwtService.generateToken(user);
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity.status(401).body("Invalid data");
        }
    }

}
