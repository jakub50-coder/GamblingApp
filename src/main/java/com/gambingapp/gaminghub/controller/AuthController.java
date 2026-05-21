/* The controller of the the program, which receives the incoming request from the frontend,
Pull out data from the request, and give it to the service
*/
package com.gambingapp.gaminghub.controller;

import com.gambingapp.gaminghub.dto.LoginRequest;
import com.gambingapp.gaminghub.dto.SignupRequest;
import com.gambingapp.gaminghub.model.User;
import com.gambingapp.gaminghub.security.JwtService;
import com.gambingapp.gaminghub.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    // POST /api/auth/signup
    // Creates a new account for the user and starts them with 100 coins.
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(@RequestBody SignupRequest request) {
        Map<String, String> response = new HashMap<>();

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            response.put("message", "Username is required");
            return ResponseEntity.badRequest().body(response);
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            response.put("message", "Password is required");
            return ResponseEntity.badRequest().body(response);
        }

        boolean success = userService.signup(request.getUsername(), request.getPassword());

        if (success) {
            response.put("message", "Signup successful.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            response.put("message", "Signup failed since username was already taken.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    // POST /api/auth/login
    // Validates the user and password to make sure they are correct
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();

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
}
