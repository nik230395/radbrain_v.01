package org.nikolic.programm.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${app.name:RadBrain}")
    private String appName;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * ✅ Sends verification email with 6-digit code
     */
    public void sendVerificationEmail(String toEmail, String fullname, String verificationCode) {
        if (!mailEnabled) {
            logToConsole(toEmail, fullname, verificationCode, "VERIFICATION");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, appName);
            helper.setTo(toEmail);
            helper.setSubject(appName + " - E-Mail-Verifikation");

            String htmlContent = createVerificationEmailHtml(fullname, verificationCode);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("📧 Verification email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            logger.error("❌ Failed to send verification email to: {}", toEmail, e);
            logToConsole(toEmail, fullname, verificationCode, "VERIFICATION (FALLBACK)");
            throw new RuntimeException("E-Mail-Versand fehlgeschlagen: " + e.getMessage());
        }
    }

    /**
     * ✅ Sends welcome email after successful registration
     */
    public void sendWelcomeEmail(String toEmail, String fullname) {
        if (!mailEnabled) {
            logger.info("📧 Welcome email would be sent to: {} (mail disabled)", toEmail);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, appName);
            helper.setTo(toEmail);
            helper.setSubject("Willkommen bei " + appName + "!");

            String htmlContent = createWelcomeEmailHtml(fullname);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("📧 Welcome email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            logger.warn("📧 Failed to send welcome email to: {} - {}", toEmail, e.getMessage());
            // Welcome email is not critical, so we don't throw
        }
    }

    /**
     * ✅ Sends password reset email with reset token
     */
    public void sendPasswordResetEmail(String toEmail, String fullname, String resetToken) {
        if (!mailEnabled) {
            logToConsole(toEmail, fullname, resetToken, "PASSWORD RESET");
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, appName);
            helper.setTo(toEmail);
            helper.setSubject(appName + " - Passwort zurücksetzen");

            String resetUrl = baseUrl + "/auth/reset-password.html?token=" + resetToken;
            String htmlContent = createPasswordResetEmailHtml(fullname, resetUrl);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("📧 Password reset email sent successfully to: {}", toEmail);

        } catch (Exception e) {
            logger.error("❌ Failed to send password reset email to: {}", toEmail, e);
            logToConsole(toEmail, fullname, resetToken, "PASSWORD RESET (FALLBACK)");
            throw new RuntimeException("E-Mail-Versand fehlgeschlagen: " + e.getMessage());
        }
    }

    /**
     * Console fallback for development/testing
     */
    private void logToConsole(String email, String fullname, String code, String type) {
        logger.info("=".repeat(60));
        logger.info("📧 {} - CODE", type);
        logger.info("📬 Email: {}", email);
        logger.info("👤 Name: {}", fullname);
        logger.info("🔑 Code/Token: {}", code);
        logger.info("⏰ Timestamp: {}", java.time.LocalDateTime.now());
        logger.info("=".repeat(60));
    }

    /**
     * ✅ Email-client-compatible HTML template for verification email
     * Uses tables and inline styles for maximum compatibility
     */
    private String createVerificationEmailHtml(String fullname, String verificationCode) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style type="text/css">
                        @media only screen and (max-width: 600px) {
                            .email-container { width: 100%% !important; }
                            .header-title { font-size: 24px !important; }
                            .code-text { font-size: 28px !important; letter-spacing: 4px !important; }
                            .content-padding { padding: 25px 15px !important; }
                        }
                    </style>
                </head>
                <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f4; padding: 20px 0;">
                        <tr>
                            <td align="center">
                                <table class="email-container" width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); max-width: 600px;">
                                    
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #645CBB 0%%, #A084DC 100%%); padding: 35px 20px; text-align: center;">
                                            <h1 class="header-title" style="margin: 0; color: #ffffff; font-size: 28px; font-weight: bold;">%s</h1>
                                            <p style="margin: 8px 0 0 0; color: #ffffff; font-size: 15px; opacity: 0.95;">E-Mail-Verifikation</p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Content -->
                                    <tr>
                                        <td class="content-padding" style="padding: 35px 25px;">
                                            <p style="margin: 0 0 18px 0; font-size: 17px; color: #333333;">
                                                Hallo <strong style="color: #645CBB;">%s</strong>!
                                            </p>
                                            
                                            <p style="margin: 0 0 25px 0; font-size: 15px; color: #666666; line-height: 1.6;">
                                                Willkommen bei %s! Um Ihre Registrierung abzuschließen, 
                                                geben Sie bitte den folgenden Verifikationscode ein:
                                            </p>
                                            
                                            <!-- Code Box -->
                                            <table width="100%%" cellpadding="0" cellspacing="0" style="margin: 25px 0;">
                                                <tr>
                                                    <td align="center" style="background-color: #f8f9ff; border: 3px dashed #645CBB; border-radius: 8px; padding: 25px 15px;">
                                                        <p style="margin: 0 0 8px 0; font-size: 11px; color: #999999; text-transform: uppercase; letter-spacing: 1px;">
                                                            Ihr Verifikationscode
                                                        </p>
                                                        <p class="code-text" style="margin: 0; font-size: 36px; font-weight: bold; color: #645CBB; letter-spacing: 8px; font-family: 'Courier New', monospace;">
                                                            %s
                                                        </p>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Warning Box -->
                                            <table width="100%%" cellpadding="0" cellspacing="0" style="margin: 25px 0;">
                                                <tr>
                                                    <td style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; border-radius: 4px;">
                                                        <p style="margin: 0; font-size: 14px; color: #856404; font-weight: bold;">
                                                            ⏱️ Dieser Code ist nur 10 Minuten gültig
                                                        </p>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Security Tips -->
                                            <table width="100%%" cellpadding="0" cellspacing="0" style="margin: 25px 0; background-color: #f8f9fa; border-radius: 8px; padding: 20px;">
                                                <tr>
                                                    <td>
                                                        <p style="margin: 0 0 12px 0; font-size: 16px; color: #333333; font-weight: bold;">
                                                            🔒 Sicherheitshinweise
                                                        </p>
                                                        <p style="margin: 8px 0; font-size: 14px; color: #666666;">
                                                            ✓ Verwenden Sie diesen Code nur auf unserer offiziellen Website
                                                        </p>
                                                        <p style="margin: 8px 0; font-size: 14px; color: #666666;">
                                                            ✓ Teilen Sie diesen Code niemals mit anderen Personen
                                                        </p>
                                                        <p style="margin: 8px 0; font-size: 14px; color: #666666;">
                                                            ✓ Unser Team wird Sie niemals nach diesem Code fragen
                                                        </p>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <p style="margin: 25px 0 0 0; font-size: 14px; color: #999999; line-height: 1.6;">
                                                Falls Sie sich nicht registriert haben, können Sie diese E-Mail 
                                                ignorieren. Ihr Konto wird nicht aktiviert.
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e0e0e0;">
                                            <p style="margin: 0; font-size: 14px; color: #333333; font-weight: bold;">
                                                © 2025 %s
                                            </p>
                                            <p style="margin: 5px 0 0 0; font-size: 13px; color: #999999;">
                                                Medizinisches Lernportal für Radiologie
                                            </p>
                                        </td>
                                    </tr>
                                    
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """, appName, fullname, appName, verificationCode, appName);
    }

    /**
     * ✅ Email-client-compatible HTML template for welcome email
     */
    private String createWelcomeEmailHtml(String fullname) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0;">
                    <style type="text/css">
                        @media only screen and (max-width: 600px) {
                            .email-container { width: 100%% !important; }
                            .header-title { font-size: 26px !important; }
                            .content-padding { padding: 30px 15px !important; }
                            .cta-button { padding: 14px 30px !important; font-size: 15px !important; }
                        }
                    </style>
                </head>
                <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f4; padding: 20px 0;">
                        <tr>
                            <td align="center">
                                <table class="email-container" width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); max-width: 600px;">
                                    
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #645CBB 0%%, #A084DC 100%%); padding: 40px 20px; text-align: center;">
                                            <h1 class="header-title" style="margin: 0; color: #ffffff; font-size: 30px; font-weight: bold;">
                                                Willkommen bei %s!
                                            </h1>
                                        </td>
                                    </tr>
                                    
                                    <!-- Content -->
                                    <tr>
                                        <td class="content-padding" style="padding: 35px 25px;">
                                            <p style="margin: 0 0 18px 0; font-size: 17px; color: #333333;">
                                                Hallo <strong style="color: #645CBB;">%s</strong>!
                                            </p>
                                            
                                            <p style="margin: 0 0 25px 0; font-size: 15px; color: #666666; line-height: 1.6;">
                                                Ihre Registrierung war erfolgreich! Wir freuen uns, Sie in unserer 
                                                Lern-Community begrüßen zu dürfen.
                                            </p>
                                            
                                            <!-- Features Box -->
                                            <table width="100%%" cellpadding="0" cellspacing="0" style="margin: 20px 0; background-color: #f8f9fa; border-radius: 8px; padding: 20px 15px;">
                                                <tr>
                                                    <td>
                                                        <p style="margin: 0 0 12px 0; font-size: 16px; color: #333333; font-weight: bold;">
                                                            Das erwartet Sie bei %s:
                                                        </p>
                                                        <p style="margin: 10px 0; font-size: 14px; color: #666666; line-height: 1.5;">
                                                            🎯 Interaktive Radiologie-Quizzes für alle Schwierigkeitsgrade
                                                        </p>
                                                        <p style="margin: 10px 0; font-size: 14px; color: #666666; line-height: 1.5;">
                                                            📊 Detailliertes Fortschritts-Tracking und Statistiken
                                                        </p>
                                                        <p style="margin: 10px 0; font-size: 14px; color: #666666; line-height: 1.5;">
                                                            🏆 Achievements und Lernziele für zusätzliche Motivation
                                                        </p>
                                                        <p style="margin: 10px 0; font-size: 14px; color: #666666; line-height: 1.5;">
                                                            📚 Expertenwissen in Röntgen, CT, MRT und Ultraschall
                                                        </p>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- CTA Button -->
                                            <table width="100%%" cellpadding="0" cellspacing="0" style="margin: 25px 0;">
                                                <tr>
                                                    <td align="center">
                                                        <a href="%s" class="cta-button" style="display: inline-block; background: linear-gradient(135deg, #645CBB, #A084DC); color: #ffffff; padding: 16px 40px; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 16px;">
                                                            Jetzt loslegen!
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <p style="margin: 30px 0 0 0; font-size: 15px; color: #666666; line-height: 1.6;">
                                                Bei Fragen oder Problemen stehen wir Ihnen jederzeit zur Verfügung. 
                                                Viel Erfolg beim Lernen!
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e0e0e0;">
                                            <p style="margin: 0; font-size: 14px; color: #333333; font-weight: bold;">
                                                © 2025 %s
                                            </p>
                                            <p style="margin: 5px 0 0 0; font-size: 13px; color: #999999;">
                                                Medizinisches Lernportal für Radiologie
                                            </p>
                                        </td>
                                    </tr>
                                    
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """, appName, fullname, appName, baseUrl, appName);
    }

    /**
     * ✅ Email-client-compatible HTML template for password reset email
     */
    private String createPasswordResetEmailHtml(String fullname, String resetUrl) {
        return String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style type="text/css">
                        @media only screen and (max-width: 600px) {
                            .email-container { width: 100%% !important; }
                            .header-title { font-size: 24px !important; }
                            .content-padding { padding: 30px 15px !important; }
                            .cta-button { padding: 14px 30px !important; font-size: 15px !important; }
                        }
                    </style>
                </head>
                <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f4f4f4;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f4f4f4; padding: 20px 0;">
                        <tr>
                            <td align="center">
                                <table class="email-container" width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1); max-width: 600px;">
                                    
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #dc2626 0%%, #ef4444 100%%); padding: 35px 20px; text-align: center;">
                                            <h1 class="header-title" style="margin: 0; color: #ffffff; font-size: 26px; font-weight: bold;">
                                                Passwort zurücksetzen
                                            </h1>
                                        </td>
                                    </tr>
                                    
                                    <!-- Content -->
                                    <tr>
                                        <td class="content-padding" style="padding: 35px 25px;">
                                            <p style="margin: 0 0 18px 0; font-size: 17px; color: #333333;">
                                                Hallo <strong style="color: #dc2626;">%s</strong>!
                                            </p>
                                            
                                            <p style="margin: 0 0 25px 0; font-size: 15px; color: #666666; line-height: 1.6;">
                                                Sie haben eine Passwort-Zurücksetzung für Ihr %s-Konto angefordert. 
                                                Klicken Sie auf den Button unten, um ein neues Passwort zu setzen:
                                            </p>
                                            
                                            <!-- CTA Button -->
                                            <table width="100%%" cellpadding="0" cellspacing="0" style="margin: 25px 0;">
                                                <tr>
                                                    <td align="center">
                                                        <a href="%s" class="cta-button" style="display: inline-block; background: linear-gradient(135deg, #dc2626, #ef4444); color: #ffffff; padding: 16px 40px; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 16px;">
                                                            Neues Passwort setzen
                                                        </a>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Warning Box -->
                                            <table width="100%%" cellpadding="0" cellspacing="0" style="margin: 20px 0;">
                                                <tr>
                                                    <td style="background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 12px; border-radius: 4px;">
                                                        <p style="margin: 0; font-size: 13px; color: #856404; font-weight: bold;">
                                                            ⏱️ Dieser Link ist nur 30 Minuten gültig
                                                        </p>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <!-- Security Tips -->
                                            <table width="100%%" cellpadding="0" cellspacing="0" style="margin: 20px 0; background-color: #fee2e2; border-radius: 8px; padding: 15px;">
                                                <tr>
                                                    <td>
                                                        <p style="margin: 0 0 10px 0; font-size: 15px; color: #7f1d1d; font-weight: bold;">
                                                            🔒 Sicherheitshinweise
                                                        </p>
                                                        <p style="margin: 7px 0; font-size: 13px; color: #991b1b; line-height: 1.4;">
                                                            ⚠️ Falls Sie keine Zurücksetzung angefordert haben, ignorieren Sie diese E-Mail
                                                        </p>
                                                        <p style="margin: 7px 0; font-size: 13px; color: #991b1b; line-height: 1.4;">
                                                            ⚠️ Teilen Sie diesen Link niemals mit anderen Personen
                                                        </p>
                                                        <p style="margin: 7px 0; font-size: 13px; color: #991b1b; line-height: 1.4;">
                                                            ⚠️ Nach 30 Minuten müssen Sie eine neue Anfrage stellen
                                                        </p>
                                                    </td>
                                                </tr>
                                            </table>
                                            
                                            <p style="margin: 25px 0 0 0; font-size: 14px; color: #999999; line-height: 1.6;">
                                                <em>Wenn der Button nicht funktioniert, kopieren Sie diesen Link in Ihren Browser:</em><br>
                                                <a href="%s" style="color: #645CBB; word-break: break-all;">%s</a>
                                            </p>
                                        </td>
                                    </tr>
                                    
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f8f9fa; padding: 30px; text-align: center; border-top: 1px solid #e0e0e0;">
                                            <p style="margin: 0; font-size: 14px; color: #333333; font-weight: bold;">
                                                © 2025 %s
                                            </p>
                                            <p style="margin: 5px 0 0 0; font-size: 13px; color: #999999;">
                                                Medizinisches Lernportal für Radiologie
                                            </p>
                                        </td>
                                    </tr>
                                    
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """, fullname, appName, resetUrl, resetUrl, resetUrl, appName);
    }

    /**
     * Checks if email service is properly configured and available
     */
    public boolean isEmailServiceAvailable() {
        return mailEnabled
                && mailSender != null
                && fromEmail != null
                && !fromEmail.isEmpty();
    }
}