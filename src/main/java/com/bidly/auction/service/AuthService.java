package com.bidly.auction.service;

import com.bidly.auction.domain.Role;
import com.bidly.auction.domain.User;
import com.bidly.auction.dto.AuthResponse;
import com.bidly.auction.dto.LoginRequest;
import com.bidly.auction.dto.SignupRequest;
import com.bidly.auction.repository.RoleRepository;
import com.bidly.auction.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@Service
public class AuthService {
    private static final List<String> ALLOWED_ROLES = List.of("ADMIN", "BIDDER");

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        String requestedRole = request.role() == null || request.role().isBlank()
                ? "BIDDER"
                : request.role().trim().toUpperCase(Locale.ROOT);

        String finalRoleName = ALLOWED_ROLES.contains(requestedRole) ? requestedRole : "BIDDER";

        Role role = roleRepository.findByNameIgnoreCase(finalRoleName)
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName(finalRoleName);
                    return roleRepository.save(newRole);
                });

        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(role);

        User saved = userRepository.save(user);
        return new AuthResponse(
                "Signup successful",
                saved.getEmail(),
                saved.getName(),
                saved.getRole().getName(),
                generateToken(saved.getEmail())
        );
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email().trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return new AuthResponse(
                "Login successful",
                user.getEmail(),
                user.getName(),
                user.getRole().getName(),
                generateToken(user.getEmail())
        );
    }

    private String generateToken(String email) {
        String raw = email + ":" + Instant.now().toEpochMilli();
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
