package org.nikolic.programm.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * DataInitializer is no longer needed as we use UserRole enum instead of Role entity.
 * Keeping this class as a placeholder for future initialization logic if needed.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Override
    public void run(String... args) {
        logger.info("=== DataInitializer executed (using UserRole enum, no Role entity initialization needed) ===");
    }
}