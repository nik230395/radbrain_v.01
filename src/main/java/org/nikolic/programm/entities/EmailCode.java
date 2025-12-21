package org.nikolic.programm.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "email_codes")
public class EmailCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Verknüpfung zum User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Zweck (VERIFY / RESET)
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private CodePurpose purpose;

    // SHA256-Hash des Codes (hex)
    @Column(name = "code_hash", length = 64, nullable = false)
    private String codeHash;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "consumed_at")
    private LocalDateTime consumedAt;

    @Column(name = "attempts")
    private Integer attempts = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public EmailCode() {}
}