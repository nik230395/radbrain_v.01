-- Flyway Migration V3: Email Verification Tabelle
-- Speicherort: src/main/resources/db/migration/V3__create_email_verification.sql

-- Erstelle email_verifications Tabelle
CREATE TABLE IF NOT EXISTS email_verifications (
                                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                   user_id BIGINT NOT NULL UNIQUE,
                                                   verification_code VARCHAR(10) NOT NULL,
    expires_at DATETIME NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    attempts INT NOT NULL DEFAULT 0,

    CONSTRAINT fk_email_verification_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE,

    INDEX idx_verification_user (user_id),
    INDEX idx_verification_expires (expires_at),
    INDEX idx_verification_verified (verified)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Kommentar
ALTER TABLE email_verifications
    COMMENT = 'Speichert Email-Verifizierungsdaten für User-Registrierung';