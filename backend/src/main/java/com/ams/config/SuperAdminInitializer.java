package com.ams.config;

import com.ams.entity.User;
import com.ams.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Locale;

/**
 * Creates the single, initial SUPER_ADMIN account on application startup from
 * environment-configured credentials (SUPER_ADMIN_USERNAME / SUPER_ADMIN_PASSWORD).
 *
 * This is idempotent and safe to run on every startup:
 * - If a SUPER_ADMIN already exists in the database, nothing happens (the "only
 *   one SUPER_ADMIN" invariant is enforced here as well as in UserServiceImpl).
 * - If no SUPER_ADMIN exists yet, exactly one is created from the configured
 *   env vars. If those env vars are not set, a warning is logged and no
 *   SUPER_ADMIN is created (the app still starts normally).
 */
@Component
public class SuperAdminInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String superAdminUsername;
    private final String superAdminPassword;
    private final String securitySport;
    private final String securityHobby;

    public SuperAdminInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${ams.super-admin.username:}") String superAdminUsername,
            @Value("${ams.super-admin.password:}") String superAdminPassword,
            @Value("${ams.super-admin.security-sport:}") String securitySport,
            @Value("${ams.super-admin.security-hobby:}") String securityHobby) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.superAdminUsername = superAdminUsername;
        this.superAdminPassword = superAdminPassword;
        this.securitySport = securitySport;
        this.securityHobby = securityHobby;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByRole("SUPER_ADMIN")) {
            log.info("SUPER_ADMIN already exists — skipping initial Super Admin creation.");
            return;
        }

        if (superAdminUsername == null || superAdminUsername.isBlank()
                || superAdminPassword == null || superAdminPassword.isBlank()
                || securitySport == null || securitySport.isBlank() || securityHobby == null || securityHobby.isBlank()) {
            log.warn("No SUPER_ADMIN exists yet, and required SUPER_ADMIN_USERNAME / SUPER_ADMIN_PASSWORD / security-answer "
                    + "environment variables are not set. Set them and restart to bootstrap the initial Super Admin.");
            return;
        }

        if (userRepository.existsByUserName(superAdminUsername)) {
            log.error("Cannot create SUPER_ADMIN: username '{}' is already taken by another user.", superAdminUsername);
            return;
        }

        User superAdmin = new User();
        superAdmin.setUserName(superAdminUsername);
        superAdmin.setPassword(passwordEncoder.encode(superAdminPassword));
        superAdmin.setRole("SUPER_ADMIN");
        superAdmin.setAdminStatus("APPROVED");
        superAdmin.setFavouriteSport(passwordEncoder.encode(normalize(securitySport)));
        superAdmin.setFavouriteHobby(passwordEncoder.encode(normalize(securityHobby)));
        userRepository.save(superAdmin);

        log.info("Initial SUPER_ADMIN account '{}' created.", superAdminUsername);
    }

    private String normalize(String value) { return value.trim().toLowerCase(Locale.ROOT); }
}
