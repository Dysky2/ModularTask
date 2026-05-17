package net.edu.modulartask.auth;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import net.edu.modulartask.config.JwtService;
import net.edu.modulartask.user.User;
import net.edu.modulartask.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    TwoFactorService twoFactorService;

    private final Bucket bucket = Bucket.builder()
            .addLimit(Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(1))))
            .build();

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
            userService.validateActiveUser(user.getUsername());

            if(user.isTwoFactorAuthEnabled()) {
                return ResponseEntity.ok(Map.of("2fa_required", true, "username", user.getUsername()));
            }

            String token = jwtService.generateToken(user);
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "role", user.getRole().name(),
                    "isAdmin", user.getRole().name().equals("ADMIN")
            ));
        } else {
            return ResponseEntity.status(401).body("Invalid data");
        }
    }

    public ResponseEntity<?> setupTwoFactorAuth() {
        User user = userService.getCurrentlyLoggedUser();

        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        GoogleAuthenticatorKey key = twoFactorService.generate2FaKey();

        String url = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                "ModularTask",
                user.getEmail(),
                key);

        String qrCodeBase = twoFactorService.generateQrCodeBase64(url);

        return ResponseEntity.ok(Map.of("qrCode", qrCodeBase, "key", key.getKey()));
    }

    public ResponseEntity<?> verify2Fa(TwoFactorRequest twoFactorRequest) {
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(429).body("Too many tries. Try later");
        }

        User user = userService.getCurrentlyLoggedUser();
        String key = twoFactorRequest.key();

        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

        if(googleAuthenticator.authorize(key, twoFactorRequest.code())) {
            return userService.enable2Fa(user, key);
        } else {
            return ResponseEntity.status(401).body("Invalid code");
        }
    }

    public ResponseEntity<?> loginWith2Fa(TwoFactorLoginRequest twoFactorLoginRequest) {
        if (!bucket.tryConsume(1)) {
            return ResponseEntity.status(429).body("Too many tries. Try later");
        }

        User user = userService.getUserByUsername(twoFactorLoginRequest.username());

        if(user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }

        userService.validateActiveUser(user.getUsername());

        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();

        try {
            String key = twoFactorService.decrypt(user.getTwoFactorAuthKey());

            if(googleAuthenticator.authorize(key, twoFactorLoginRequest.code())) {
                String token = jwtService.generateToken(user);
                return ResponseEntity.ok(Map.of(
                        "token", token,
                        "role", user.getRole().name(),
                        "isAdmin", user.getRole().name().equals("ADMIN")
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
            }

        } catch(Exception e) {
            return ResponseEntity.status(400).body("Invalid code");
        }


    }


}
