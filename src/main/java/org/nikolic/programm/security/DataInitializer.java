package org.nikolic.programm.security;

import org.nikolic.programm.entities.User;
import org.nikolic.programm.entities.UserRole;
import org.nikolic. programm.repositories.UserRepository;
import org.springframework.beans. factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app. admin.email:admin@radbrain.de}")
    private String adminEmail;

    @Value("${app.admin.password:Admin123!}")
    private String adminPassword;

    @Value("${app.admin.fullname:System Administrator}")
    private String adminFullname;

    @Value("${app.init.create-admin:true}")
    private boolean createAdmin;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("🚀 Starting data initialization...");

        try {
            initializeAdminUser();
            initializeTestUsers();
            logSystemStatus();
        } catch (Exception e) {
            logger.error("❌ Data initialization failed", e);
        }

        logger.info("✅ Data initialization completed");
    }

    private void initializeAdminUser() {
        if (! createAdmin) {
            logger.info("🔧 Admin creation disabled by configuration");
            return;
        }

        // ✅ Korrigierte Existence-Überprüfung
        if (userRepository.existsByEmail(adminEmail)) {
            logger.info("👤 Admin user already exists:  {}", adminEmail);
            return;
        }

        // Check if any admin exists
        if (userRepository.hasActiveAdmin()) {
            logger.info("👤 Active admin already exists, skipping creation");
            return;
        }

        try {
            User admin = new User();
            admin.setEmail(adminEmail. toLowerCase().trim());
            admin. setFullname(adminFullname);
            admin.setPassword_hash(passwordEncoder.encode(adminPassword));
            admin.setRole(UserRole.ADMIN);
            admin.setIs_active(true);
            admin.setEmailVerified(true); // @Transient field
            admin.setCreated_at(LocalDateTime.now());

            User savedAdmin = userRepository.save(admin);

            logger.info("✅ Admin user created successfully:");
            logger.info("   📧 Email: {}", savedAdmin.getEmail());
            logger. info("   👤 Name: {}", savedAdmin.getFullname());
            logger.info("   🔑 Role: {}", savedAdmin.getRole());
            logger.info("   🔐 Password: {} (change this in production!)", adminPassword);

        } catch (Exception e) {
            logger.error("❌ Failed to create admin user", e);
            throw new RuntimeException("Admin user creation failed", e);
        }
    }

    private void initializeTestUsers() {
        // Only create test users in development
        String profile = System.getProperty("spring.profiles. active");
        if (!"dev".equals(profile) && !"development".equals(profile)) {
            logger.info("🔧 Test user creation skipped (not in dev profile)");
            return;
        }

        createTestUserIfNotExists("test@radbrain.de", "Test User", "TestPass123!");
        createTestUserIfNotExists("demo@radbrain.de", "Demo User", "DemoPass123!");
        createTestUserIfNotExists("student@radbrain.de", "Medical Student", "StudentPass123!");
    }

    private void createTestUserIfNotExists(String email, String fullname, String password) {
        // ✅ Korrigierte Existence-Überprüfung
        if (userRepository. existsByEmail(email)) {
            logger.debug("Test user already exists: {}", email);
            return;
        }

        try {
            User testUser = new User();
            testUser.setEmail(email.toLowerCase().trim());
            testUser. setFullname(fullname);
            testUser.setPassword_hash(passwordEncoder.encode(password));
            testUser.setRole(UserRole.USER);
            testUser.setIs_active(true);
            testUser.setEmailVerified(true); // @Transient field
            testUser.setCreated_at(LocalDateTime.now());

            User savedUser = userRepository.save(testUser);
            logger.info("✅ Test user created:  {} ({})", savedUser.getEmail(), savedUser.getFullname());

        } catch (Exception e) {
            logger.error("❌ Failed to create test user:  {}", email, e);
        }
    }

    private void logSystemStatus() {
        try {
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.countActiveUsers(); // ✅ Korrigierte Methode
            long adminUsers = userRepository.countByRole(UserRole.ADMIN);

            logger.info("📊 System Status:");
            logger.info("   👥 Total Users: {}", totalUsers);
            logger.info("   ✅ Active Users: {}", activeUsers);
            logger.info("   👑 Admin Users: {}", adminUsers);
            logger.info("   ⚠️ Inactive Users: {}", totalUsers - activeUsers);

            if (adminUsers == 0) {
                logger. warn("⚠️ WARNING: No admin users found!  System may not be manageable.");
            }

            // ✅ Admin Login Info
            if (adminUsers > 0) {
                logger.info("🔑 Admin Login:");
                logger.info("   📧 Email: {}", adminEmail);
                logger.info("   🔐 Password: {}", adminPassword);
                logger.info("   🌐 URL: http://localhost:8080/login. html");
            }

        } catch (Exception e) {
            logger.error("❌ Failed to log system status", e);
        }
    }
}