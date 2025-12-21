package org.nikolic.programm.security;

import org.nikolic.programm.entities.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class DataInitializer implements CommandLineRunner {

    // RoleRepository optional - falls vorhanden wird es verwendet
    @Autowired(required = false)
    private RoleRepository roleRepository;

    private final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Override
    public void run(String... args) {
        logger.info("=== DataInitializer wird ausgeführt ===");

        if (roleRepository == null) {
            logger.info("Kein RoleRepository vorhanden (role-Table nicht verwendet). DataInitializer übersprungen.");
            return;
        }

        try {
            // Erstelle ROLE_USER wenn sie nicht existiert
            try {
                if (!roleRepository.existsByName("ROLE_USER")) {
                    Role userRole = new Role();
                    userRole.setName("ROLE_USER");
                    userRole.setDescription("Standard User Rolle");
                    roleRepository.save(userRole);
                    logger.info("ROLE_USER wurde erstellt");
                } else {
                    logger.info("ROLE_USER existiert bereits");
                }
            } catch (DataAccessException dae) {
                logger.warn("Datenbankabfrage für ROLE_USER fehlgeschlagen: {}", dae.getMessage());
            }

            // Optional: Erstelle ROLE_ADMIN
            try {
                if (!roleRepository.existsByName("ROLE_ADMIN")) {
                    Role adminRole = new Role();
                    adminRole.setName("ROLE_ADMIN");
                    adminRole.setDescription("Administrator Rolle");
                    roleRepository.save(adminRole);
                    logger.info("ROLE_ADMIN wurde erstellt");
                } else {
                    logger.info("ROLE_ADMIN existiert bereits");
                }
            } catch (DataAccessException dae) {
                logger.warn("Datenbankabfrage für ROLE_ADMIN fehlgeschlagen: {}", dae.getMessage());
            }

            logger.info("=== DataInitializer abgeschlossen ===");
        } catch (Exception ex) {
            logger.error("Unerwarteter Fehler im DataInitializer (wurde abgefangen):", ex);
        }
    }
}