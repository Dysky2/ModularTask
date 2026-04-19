package net.edu.modulartask.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("http://localhost:5173")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/api/auth/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @GetMapping("api/auth/2fa/setup")
    public ResponseEntity<?> setupTwoFactorAuth() {
        return authService.setupTwoFactorAuth();
    }

    @PostMapping("api/auth/2fa/verify")
    public ResponseEntity<?> verify2Fa(@RequestBody TwoFactorRequest twoFactorRequest) {
        return authService.verify2Fa(twoFactorRequest);
    }

    @PostMapping("/api/auth/login/2fa")
    public ResponseEntity<?> loginWith2Fa(@RequestBody TwoFactorLoginRequest twoFactorLoginRequest) {
        return authService.loginWith2Fa(twoFactorLoginRequest);
    }

}
