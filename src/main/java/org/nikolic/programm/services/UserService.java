package org.nikolic.programm.services;

import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Standardrolle 'user' und Administrator 'admin'
    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_USER = "user"; // Standardrolle

    // Registrierung eines neuen Nutzers
    public User registerUser(String email, String password, String fullname) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Die angegebene E-Mail ist bereits registriert!");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword_hash(passwordEncoder.encode(password));
        user.setFullname(fullname);
        user.setRole(ROLE_USER); // Standardrolle 'user' setzen
        user.setIs_active(true);
        user.setCreated_at(LocalDateTime.now());

        return userRepository.save(user);
    }

    // Login für Nutzer (E-Mail & Passwort prüfen)
    public User loginUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty() || !userOpt.get().getIs_active()) {
            throw new RuntimeException("Ungültige Zugangsdaten oder Benutzer ist deaktiviert!");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword_hash())) {
            throw new RuntimeException("Das eingegebene Passwort ist ungültig!");
        }

        return user;
    }

    // Benutzer anhand der ID abrufen
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // Alle Benutzer abrufen
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Benutzerprofil bearbeiten: Nur Name ändern
    public User updateUser(Long id, String fullname) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Benutzer mit der angegebenen ID wurde nicht gefunden!"));
        user.setFullname(fullname);
        return userRepository.save(user);
    }

    // Passwort ändern
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Benutzer mit der angegebenen ID wurde nicht gefunden!"));
        if (!passwordEncoder.matches(oldPassword, user.getPassword_hash())) {
            throw new RuntimeException("Das alte Passwort ist falsch!");
        }
        user.setPassword_hash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // Benutzer deaktivieren
    public void deactivateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Benutzer mit der angegebenen ID wurde nicht gefunden!"));
        user.setIs_active(false);
        userRepository.save(user);
    }

    // Benutzer aktivieren
    public void activateUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Benutzer mit der angegebene ID wurde nicht gefunden!"));
        user.setIs_active(true);
        userRepository.save(user);
    }

    // Benutzer löschen
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Benutzer mit der angegebenen ID existiert nicht!");
        }
        userRepository.deleteById(id);
    }

    // Überprüfung: Ist der Benutzer Admin?
    public boolean isUserAdmin(User user) {
        if (user == null) {
            return false;
        }

        // Prüfen, ob der Benutzer die Rolle 'admin' hat
        return ROLE_ADMIN.equals(user.getRole());
    }
}