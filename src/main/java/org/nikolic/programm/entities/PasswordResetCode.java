package org.nikolic.programm.entities;

import jakarta.persistence.*;
import java. time.LocalDateTime;

@Entity
@Table(name = "password_reset_codes")
public class PasswordResetCode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String code;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean used = false;

    // Constructors, getters, setters...
    public PasswordResetCode() {
        this.createdAt = LocalDateTime.now();
        this.used = false;
    }

    // Add all getters and setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getUsed() { return used; }
    public void setUsed(Boolean used) { this.used = used; }
}