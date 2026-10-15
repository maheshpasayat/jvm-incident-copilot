package com.maheshpasayat.incidentcopilot.web;

import com.maheshpasayat.incidentcopilot.persistence.UserEntity;
import com.maheshpasayat.incidentcopilot.persistence.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the demo admin user at startup if it does not exist yet.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String username;
    private final String password;

    public AdminSeeder(UserRepository users, PasswordEncoder encoder,
                       @Value("${app.admin.username}") String username,
                       @Value("${app.admin.password}") String password) {
        this.users = users;
        this.encoder = encoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (users.findByUsername(username).isEmpty()) {
            users.save(new UserEntity(username, encoder.encode(password)));
        }
    }
}