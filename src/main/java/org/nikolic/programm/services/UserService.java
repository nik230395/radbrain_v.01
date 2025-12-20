package org.nikolic.programm.services;

import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Service für User-Funktionen (Registrierung, Login, Verwaltung)
@Service
@Transactional
public class UserService {
    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Registrierung eines neuen Nutzers
    public User registerUser(String email, String password, String fullname) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("E-Mail ist bereits registriert!");
        }
        User user = new User();
        user.setEmail(email);
        user.setPassword_hash(passwordEncoder.encode(password));
        user.setFullname(fullname);
        user.setIs_active(true);
        user.setCreated_at(LocalDateTime.now());
        return userRepository.save(user);
    }

    // Login für Nutzer: E-Mail & Passwort prüfen
    public User loginUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty() || !userOpt.get().getIs_active()) {
            throw new RuntimeException("Ungültige Zugangsdaten!");
        }
        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword_hash())) {
            throw new RuntimeException("Ungültige Zugangsdaten!");
        }
        return user;
    }

    // Userdaten anzeigen nach ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Alle User anzeigen
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Profil bearbeiten (Name ändern)
    public User updateUser(Long id, String fullname) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User nicht gefunden!"));
        user.setFullname(fullname);
        return userRepository.save(user);
    }

    // Passwort ändern
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User nicht gefunden!"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword_hash())) {
            throw new RuntimeException("Das alte Passwort stimmt nicht!");
        }
        user.setPassword_hash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // User deaktivieren
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User nicht gefunden!"));
        user.setIs_active(false);
        userRepository.save(user);
    }

    // User aktivieren
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User nicht gefunden!"));
        user.setIs_active(true);
        userRepository.save(user);
    }

    // User löschen
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}