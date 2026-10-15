package com.maheshpasayat.incidentcopilot.web;

import java.util.Map;

import com.maheshpasayat.incidentcopilot.persistence.UserRepository;
import com.maheshpasayat.incidentcopilot.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record LoginRequest(String username, String password) {
    }

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        return users.findByUsername(request.username())
                .filter(u -> encoder.matches(request.password(), u.getPasswordHash()))
                .map(u -> ResponseEntity.ok(Map.of("token", jwt.createToken(u.getUsername()))))
                .orElse(ResponseEntity.status(401).build());
    }
}