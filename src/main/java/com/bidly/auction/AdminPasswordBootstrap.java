package com.bidly.auction;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminPasswordBootstrap implements CommandLineRunner {
    private final UserAccountRepository userAccountRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin-username:admin}")
    private String adminUsername;

    @Value("${app.bootstrap.admin-password:Admin@123}")
    private String adminPassword;

    @Value("${app.bootstrap.admin-email:admin@auction.local}")
    private String adminEmail;

    public AdminPasswordBootstrap(
            UserAccountRepository userAccountRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        userAccountRepository.findByUsername(adminUsername).ifPresentOrElse(admin -> {
            if (admin.getPassword() == null || !admin.getPassword().startsWith("$2")) {
                admin.setPassword(passwordEncoder.encode(adminPassword));
                userAccountRepository.save(admin);
            }
        }, this::createAdminIfMissing);

        userAccountRepository.findAll().forEach(user -> {
            if (user.getPassword() != null && !user.getPassword().startsWith("$2")) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                userAccountRepository.save(user);
            }
        });
    }

    private void createAdminIfMissing() {
        Role adminRole = roleRepository.findByName("ADMIN").orElse(null);
        if (adminRole == null) {
            return;
        }
        UserAccount admin = new UserAccount();
        admin.setUsername(adminUsername);
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(adminRole);
        admin.setEnabled(true);
        userAccountRepository.save(admin);
    }
}
