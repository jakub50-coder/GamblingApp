/* The controller of the the program, which receives the incoming request from the frontend,
Pull out data from the request, and give it to the service
*/
package com.gambingapp.gaminghub.controller;

import com.gambingapp.gaminghub.dto.LoginRequest;
import com.gambingapp.gaminghub.dto.SignupRequest;
import com.gambingapp.gaminghub.model.User;
import com.gambingapp.gaminghub.model.PasswordResetToken;
import com.gambingapp.gaminghub.repository.PasswordResetTokenRepository;
import com.gambingapp.gaminghub.security.JwtService;
import com.gambingapp.gaminghub.service.PasswordResetEmailService;
import com.gambingapp.gaminghub.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordResetEmailService passwordResetEmailService;
    private final String frontendUrl;

    public AuthController(UserService userService, JwtService jwtService, PasswordResetTokenRepository passwordResetTokenRepository, PasswordResetEmailService passwordResetEmailService, @Value("${app.password-reset.frontend-url}") String frontendUrl) {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordResetEmailService = passwordResetEmailService;
        this.frontendUrl = frontendUrl;
    }

    // POST /api/auth/signup
    // Creates a new account for the user and starts them with 100 coins.
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody SignupRequest request) {
        Map<String, String> response = new HashMap<>();
        
        if(request.getEmail() == null || request.getEmail().isBlank()) {
            response.put("message", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }
        if(!request.getEmail().contains("@")){
            response.put("message", "Please enter a valid email address");
            return ResponseEntity.badRequest().body(response);
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            response.put("message", "Username is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (request.getUsername().length() > 50) {
            response.put("message", "Username must be at most 50 characters long");
            return ResponseEntity.badRequest().body(response);
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            response.put("message", "Password is required");
            return ResponseEntity.badRequest().body(response);
        }
        if (request.getPassword().length() < 14) {
            response.put("message", "Password must be at least 14 characters long");
            return ResponseEntity.badRequest().body(response);
        }

        boolean success = userService.signup(request.getUsername(), request.getPassword(), request.getEmail());

        if (success) {
            response.put("message", "Signup successful.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } 
        else {
            response.put("message", "Signup failed since username was already taken.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    // POST /api/auth/login
    // Validates the user and password to make sure they are correct
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();

        if (request.getUsername() == null || request.getPassword() == null) {
            response.put("message", "Username and password are required");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<User> userOpt = userService.login(request.getUsername(), request.getPassword());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = jwtService.generateToken(user.getUsername());

            response.put("message", "Login successful.");
            response.put("token", token);
            response.put("username", user.getUsername());
            response.put("coins", user.getCoins());
            response.put("hasSeenBlackjackTutorial", user.isHasSeenBlackjackTutorial());
            return ResponseEntity.ok(response);
        } else {
            response.put("message", "Login failed since the username or password was invalid");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    //POST /api/auth/forgot-password
    //Accepts an email address and generates a reset token for the user
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody Map<String, String> request){
        Map<String, String> response = new HashMap<>();
        String email = request.get("email");
        if(email == null || email.isBlank()){
            response.put("message", "Email is required");
            return ResponseEntity.badRequest().body(response);
        }
        response.put("message", "If that email is registered, a password reset link has been sent.");
        Optional<User> userOpt = userService.getUserByEmail(email);
        if(userOpt.isEmpty()){
            return ResponseEntity.ok(response);
        }
        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        LocalDateTime expirationTime = LocalDateTime.now().plusHours(1);
        PasswordResetToken resetToken = new PasswordResetToken(user, token, expirationTime);
        passwordResetTokenRepository.save(resetToken);
        String resetLink = frontendUrl + "/reset-password.html?token=" + token;
        boolean emailSent = passwordResetEmailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        if (!emailSent) {
            log.warn("Password reset token created for {} but email delivery failed", user.getEmail());
            response.put("message", "If that email is registered, a password reset link has been sent.");
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.ok(response);
    }
    //POST /api/auth/reset-password
    //Accepts a reset token and sets a new password
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> request){
        Map<String, String> response = new HashMap<>();
        String token = request.get("token");
        String newPassword = request.get("newPassword");
        if(token == null || token.isBlank()){
            response.put("message", "Reset token is required");
            return ResponseEntity.badRequest().body(response);
        }
        if(newPassword == null || newPassword.length() < 14){
            response.put("message", "Password must be at least 14 characters long");
            return ResponseEntity.badRequest().body(response);
        }
        Optional<PasswordResetToken> tokenOpt = passwordResetTokenRepository.findByToken(token);
        if(tokenOpt.isEmpty()  || !tokenOpt.get().isValid()){
            response.put("message", "Reset link is invalid or has expired");
            return ResponseEntity.badRequest().body(response);
        }
        PasswordResetToken resetToken = tokenOpt.get();
        userService.resetPassword(resetToken.getUser().getUsername(), newPassword);
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        response.put("message", "Password has been reset successfully. You can now log in with your new password.");
        return ResponseEntity.ok(response);
    }
}

