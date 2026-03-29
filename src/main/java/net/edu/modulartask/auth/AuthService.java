package net.edu.modulartask.auth;

import net.edu.modulartask.user.User;
import net.edu.modulartask.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

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
            return ResponseEntity.ok().body("{\"token\": \"TESTY-BRAK-KODU\"}");
        } else {
            return ResponseEntity.status(401).body("Invalid data");
        }
    }

}
