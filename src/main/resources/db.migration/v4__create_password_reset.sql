-- Flyway Migration V4: Password Reset Tabelle
-- Speicherort: src/main/resources/db/migration/V4__create_password_reset.sql

-- Erstelle password_resets Tabelle
CREATE TABLE IF NOT EXISTS password_resets (
                                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                               user_id BIGINT NOT NULL,
                                               reset_code VARCHAR(10) NOT NULL,
    reset_token VARCHAR(64),
    expires_at DATETIME NOT NULL,
    token_expires_at DATETIME,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    used_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    attempts INT NOT NULL DEFAULT 0,
    ip_address VARCHAR(45),

    CONSTRAINT fk_password_reset_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE,

    INDEX idx_password_reset_user (user_id),
    INDEX idx_password_reset_token (reset_token),
    INDEX idx_password_reset_expires (expires_at),
    INDEX idx_password_reset_used (used)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Kommentar
ALTER TABLE password_resets
    COMMENT = 'Speichert Password-Reset-Anfragen mit Codes und Tokens';