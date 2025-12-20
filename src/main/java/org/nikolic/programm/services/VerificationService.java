package org.nikolic.programm.services;

import org.nikolic.programm.entities.EmailCode;
import org.nikolic.programm.entities.User;
import org.nikolic.programm.entities.CodePurpose;
import org.nikolic.programm.repositories.EmailCodeRepository;
import org.nikolic.programm.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Formatter;
import java.util.List;
import java.util.Optional;

@Service
public class VerificationService {

    private static final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    private final EmailCodeRepository emailCodeRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_MINUTES = 15;
    private static final int MAX_ATTEMPTS = 5;

    public VerificationService(EmailCodeRepository emailCodeRepository,
                               UserRepository userRepository,
                               EmailService emailService) {
        this.emailCodeRepository = emailCodeRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    private String generateNumericCode(int digits) {
        int min = (int) Math.pow(10, digits - 1);
        int max = (int) Math.pow(10, digits) - 1;
        int code = secureRandom.nextInt(max - min + 1) + min;
        return Integer.toString(code);
    }

    private String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            try (Formatter fmt = new Formatter()) {
                for (byte b : digest) {
                    fmt.format("%02x", b);
                }
                return fmt.toString();
            }
        } catch (Exception ex) {
            throw new RuntimeException("Failed to hash code", ex);
        }
    }

    @Transactional
    public void createAndSendVerificationCode(User user) {
        String code = generateNumericCode(CODE_LENGTH);
        String hash = sha256Hex(code);

        EmailCode ec = new EmailCode();
        ec.setUser(user);
        ec.setPurpose(CodePurpose.VERIFY);
        ec.setCodeHash(hash);
        ec.setCreatedAt(LocalDateTime.now());
        ec.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        ec.setAttempts(0);
        emailCodeRepository.save(ec);

        // Try sending the email, but don't let a mail failure break the flow (dev/prod difference).
        try {
            emailService.sendVerificationEmail(user.getEmail(), code);
        } catch (Exception ex) {
            // Log error, keep code persisted so you can verify via logs / DB in dev if needed.
            logger.error("Failed to send verification email to {}: {}", user.getEmail(), ex.getMessage());
            // Optionally you could mark ec as not_sent or similar, or notify admin.
        }

        // For local debug you can also log the code here (only enable in dev!)
        // logger.info("Verification code for {} is {}", user.getEmail(), code);
    }

    @Transactional
    public boolean verifyCode(String email, String plainCode) {
        Optional<User> u = userRepository.findByEmail(email);
        if (u.isEmpty()) return false;
        User user = u.get();

        List<EmailCode> codes = emailCodeRepository.findByUserAndPurposeOrderByCreatedAtDesc(user, CodePurpose.VERIFY);
        if (codes.isEmpty()) return false;

        EmailCode candidate = null;
        for (EmailCode c : codes) {
            if (c.getConsumedAt() == null) {
                candidate = c;
                break;
            }
        }
        if (candidate == null) return false;

        if (candidate.getExpiresAt() != null && candidate.getExpiresAt().isBefore(LocalDateTime.now())) {
            candidate.setConsumedAt(LocalDateTime.now());
            emailCodeRepository.save(candidate);
            return false;
        }

        if (candidate.getAttempts() != null && candidate.getAttempts() >= MAX_ATTEMPTS) {
            candidate.setConsumedAt(LocalDateTime.now());
            emailCodeRepository.save(candidate);
            return false;
        }

        String providedHash = sha256Hex(plainCode);
        if (!providedHash.equals(candidate.getCodeHash())) {
            candidate.setAttempts((candidate.getAttempts() == null ? 0 : candidate.getAttempts()) + 1);
            emailCodeRepository.save(candidate);
            return false;
        }

        candidate.setConsumedAt(LocalDateTime.now());
        emailCodeRepository.save(candidate);

        user.setIs_active(true); // mark user active after verification
        userRepository.save(user);

        return true;
    }
}