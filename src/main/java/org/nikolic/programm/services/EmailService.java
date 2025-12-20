package org.nikolic.programm.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@radbrain.example}")
    private String from;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String to, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("Radbrain - Dein Verifizierungscode");
        msg.setText("Hallo,\n\nDein Verifizierungscode lautet: " + code + "\n\n"
                + "Gib diesen Code auf der Verifikationsseite ein. Der Code ist 15 Minuten gültig.\n\n"
                + "Viele Grüße,\nRadbrain");
        mailSender.send(msg);
    }

    public void sendPasswordResetEmail(String to, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject("Radbrain - Passwort zurücksetzen");
        msg.setText("Hallo,\n\nDein Passwort‑Reset Code lautet: " + code + "\n\n" +
                "Falls du dieses nicht angefordert hast, ignoriere bitte diese E-Mail.\n\n" +
                "Viele Grüße,\nRadbrain Team");
        mailSender.send(msg);
    }
}