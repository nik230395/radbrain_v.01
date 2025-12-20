package org.nikolic.programm.services;

import org.nikolic.programm.entities.PasswordResetCode;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.repositories.PasswordResetCodeRepository;
import org.nikolic.programm.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.security.SecureRandom;

// Service für Passwort-Reset-Code-Logik
@Service
@Transactional
public class PasswordResetCodeService {

    @Autowired
    private PasswordResetCodeRepository resetCodeRepo;

    @Autowired
    private UserRepository userRepo;

    private final SecureRandom secureRandom = new SecureRandom();

    // Neuen Reset-Code für User erzeugen & speichern
    public PasswordResetCode createResetCodeForEmail(String email) {
        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Kein Benutzer mit dieser E-Mail gefunden!");
        }
        User user = userOpt.get();

        // Einfacher 6-stelliger Zahlencode (Demo!)
        String code = String.valueOf(100000 + secureRandom.nextInt(900000));

        PasswordResetCode resetCode = new PasswordResetCode();
        resetCode.setUser(user);
        resetCode.setCode(code);
        resetCode.setCreatedAt(LocalDateTime.now());
        resetCode.setUsed(false);

        return resetCodeRepo.save(resetCode);
    }

    // Prüfe Code, setze Password, invalidiere Code
    public void verifyCodeAndResetPassword(String email, String code, String newPasswordHash) {
        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Benutzer nicht gefunden");
        }
        User user = userOpt.get();

        // Finde letzten unbenutzten Code
        Optional<PasswordResetCode> codeOpt = resetCodeRepo.findFirstByUserAndUsedOrderByCreatedAtDesc(user, false);
        if (codeOpt.isEmpty() || !codeOpt.get().getCode().equals(code)) {
            throw new RuntimeException("Ungültiger oder abgelaufener Code!");
        }

        PasswordResetCode resetCode = codeOpt.get();
        resetCode.setUsed(true);
        resetCodeRepo.save(resetCode);

        // Passwort neu setzen!
        user.setPassword_hash(newPasswordHash);
        userRepo.save(user);
    }
}