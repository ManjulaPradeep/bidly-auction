package com.bidly.auction.config;

import com.bidly.auction.domain.Role;
import com.bidly.auction.domain.User;
import com.bidly.auction.repository.RoleRepository;
import com.bidly.auction.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        final String adminEmail = "admin@bidly.com";
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        Role adminRole = roleRepository.findByNameIgnoreCase("ADMIN")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ADMIN");
                    return roleRepository.save(role);
                });

        User admin = new User();
        admin.setName("System Admin");
        admin.setEmail(adminEmail.toLowerCase(Locale.ROOT));
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(adminRole);

        userRepository.save(admin);
    }
}
