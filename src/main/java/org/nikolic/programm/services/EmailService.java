package org.nikolic.programm.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.internet.MimeMessage;

/**
 * ✅ Email Service - Professional & Clean
 * Handles all email communications for RadBrain
 */
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
            helper.setSubject("🎯 " + appName + " - E-Mail-Verifikation");

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
            helper.setSubject("🎉 Willkommen bei " + appName + "!");

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
            helper.setSubject("🔒 " + appName + " - Passwort zurücksetzen");

            String resetUrl = baseUrl + "/reset-password.html?token=" + resetToken;
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
     * ✅ Modern HTML template for verification email
     */
    private String createVerificationEmailHtml(String fullname, String verificationCode) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="de">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body { 
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
                            line-height: 1.6;
                            color: #1f2937;
                            background-color: #f9fafb;
                        }
                        .email-wrapper {
                            max-width: 600px;
                            margin: 40px auto;
                            background: white;
                            border-radius: 16px;
                            overflow: hidden;
                            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                        }
                        .header {
                            background: linear-gradient(135deg, #645CBB 0%%, #A084DC 100%%);
                            padding: 40px 20px;
                            text-align: center;
                            color: white;
                        }
                        .header h1 {
                            font-size: 28px;
                            font-weight: 800;
                            margin-bottom: 8px;
                        }
                        .header p {
                            font-size: 16px;
                            opacity: 0.95;
                        }
                        .content {
                            padding: 40px 30px;
                        }
                        .greeting {
                            font-size: 18px;
                            margin-bottom: 20px;
                            color: #374151;
                        }
                        .greeting strong {
                            color: #645CBB;
                        }
                        .message {
                            font-size: 15px;
                            color: #4b5563;
                            margin-bottom: 30px;
                            line-height: 1.7;
                        }
                        .code-container {
                            background: linear-gradient(135deg, #f0edff 0%%, #f8f9ff 100%%);
                            border: 3px dashed #645CBB;
                            border-radius: 12px;
                            padding: 30px;
                            text-align: center;
                            margin: 30px 0;
                        }
                        .code-label {
                            font-size: 14px;
                            color: #6b7280;
                            margin-bottom: 10px;
                            text-transform: uppercase;
                            letter-spacing: 1px;
                            font-weight: 600;
                        }
                        .code {
                            font-size: 42px;
                            font-weight: 800;
                            color: #645CBB;
                            letter-spacing: 8px;
                            font-family: 'Courier New', monospace;
                        }
                        .info-box {
                            background: #fef3c7;
                            border-left: 4px solid #f59e0b;
                            padding: 16px;
                            border-radius: 8px;
                            margin: 25px 0;
                        }
                        .info-box p {
                            font-size: 14px;
                            color: #92400e;
                            margin: 0;
                            font-weight: 600;
                        }
                        .security-tips {
                            background: #f9fafb;
                            border-radius: 8px;
                            padding: 20px;
                            margin: 25px 0;
                        }
                        .security-tips h3 {
                            font-size: 16px;
                            color: #374151;
                            margin-bottom: 12px;
                            display: flex;
                            align-items: center;
                            gap: 8px;
                        }
                        .security-tips ul {
                            list-style: none;
                            padding: 0;
                        }
                        .security-tips li {
                            padding: 8px 0;
                            padding-left: 24px;
                            position: relative;
                            font-size: 14px;
                            color: #6b7280;
                        }
                        .security-tips li:before {
                            content: "✓";
                            position: absolute;
                            left: 0;
                            color: #10b981;
                            font-weight: bold;
                        }
                        .footer {
                            background: #f9fafb;
                            padding: 30px;
                            text-align: center;
                            border-top: 1px solid #e5e7eb;
                        }
                        .footer p {
                            font-size: 13px;
                            color: #9ca3af;
                            margin: 4px 0;
                        }
                        .footer a {
                            color: #645CBB;
                            text-decoration: none;
                        }
                    </style>
                </head>
                <body>
                    <div class="email-wrapper">
                        <div class="header">
                            <h1>🎯 %s</h1>
                            <p>E-Mail-Verifikation</p>
                        </div>
                        
                        <div class="content">
                            <p class="greeting">Hallo <strong>%s</strong>!</p>
                            
                            <p class="message">
                                Willkommen bei %s! Um Ihre Registrierung abzuschließen, 
                                geben Sie bitte den folgenden Verifikationscode ein:
                            </p>
                            
                            <div class="code-container">
                                <div class="code-label">Ihr Verifikationscode</div>
                                <div class="code">%s</div>
                            </div>
                            
                            <div class="info-box">
                                <p>⏱️ Dieser Code ist nur 10 Minuten gültig</p>
                            </div>
                            
                            <div class="security-tips">
                                <h3>🔒 Sicherheitshinweise</h3>
                                <ul>
                                    <li>Verwenden Sie diesen Code nur auf unserer offiziellen Website</li>
                                    <li>Teilen Sie diesen Code niemals mit anderen Personen</li>
                                    <li>Unser Team wird Sie niemals nach diesem Code fragen</li>
                                </ul>
                            </div>
                            
                            <p class="message" style="margin-top: 25px;">
                                Falls Sie sich nicht registriert haben, können Sie diese E-Mail 
                                ignorieren. Ihr Konto wird nicht aktiviert.
                            </p>
                        </div>
                        
                        <div class="footer">
                            <p><strong>© 2025 %s</strong></p>
                            <p>Medizinisches Lernportal für Radiologie</p>
                        </div>
                    </div>
                </body>
                </html>
                """, appName, fullname, appName, verificationCode, appName);
    }

    /**
     * ✅ Modern HTML template for welcome email
     */
    private String createWelcomeEmailHtml(String fullname) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="de">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body { 
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
                            line-height: 1.6;
                            color: #1f2937;
                            background-color: #f9fafb;
                        }
                        .email-wrapper {
                            max-width: 600px;
                            margin: 40px auto;
                            background: white;
                            border-radius: 16px;
                            overflow: hidden;
                            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                        }
                        .header {
                            background: linear-gradient(135deg, #645CBB 0%%, #A084DC 100%%);
                            padding: 50px 20px;
                            text-align: center;
                            color: white;
                        }
                        .header h1 {
                            font-size: 32px;
                            font-weight: 800;
                            margin-bottom: 8px;
                        }
                        .content {
                            padding: 40px 30px;
                        }
                        .greeting {
                            font-size: 18px;
                            margin-bottom: 20px;
                            color: #374151;
                        }
                        .greeting strong {
                            color: #645CBB;
                        }
                        .message {
                            font-size: 15px;
                            color: #4b5563;
                            margin-bottom: 30px;
                            line-height: 1.7;
                        }
                        .features {
                            background: #f9fafb;
                            border-radius: 12px;
                            padding: 25px;
                            margin: 25px 0;
                        }
                        .features h3 {
                            font-size: 16px;
                            color: #374151;
                            margin-bottom: 15px;
                        }
                        .features ul {
                            list-style: none;
                            padding: 0;
                        }
                        .features li {
                            padding: 12px 0;
                            padding-left: 35px;
                            position: relative;
                            font-size: 15px;
                            color: #4b5563;
                        }
                        .features li:before {
                            content: attr(data-icon);
                            position: absolute;
                            left: 0;
                            font-size: 20px;
                        }
                        .cta-button {
                            display: inline-block;
                            background: linear-gradient(135deg, #645CBB, #A084DC);
                            color: white;
                            padding: 16px 40px;
                            text-decoration: none;
                            border-radius: 12px;
                            font-weight: 700;
                            font-size: 16px;
                            margin: 25px 0;
                            box-shadow: 0 4px 12px rgba(100,92,187,0.3);
                        }
                        .cta-container {
                            text-align: center;
                        }
                        .footer {
                            background: #f9fafb;
                            padding: 30px;
                            text-align: center;
                            border-top: 1px solid #e5e7eb;
                        }
                        .footer p {
                            font-size: 13px;
                            color: #9ca3af;
                            margin: 4px 0;
                        }
                    </style>
                </head>
                <body>
                    <div class="email-wrapper">
                        <div class="header">
                            <h1>🎉 Willkommen bei %s!</h1>
                        </div>
                        
                        <div class="content">
                            <p class="greeting">Hallo <strong>%s</strong>!</p>
                            
                            <p class="message">
                                Ihre Registrierung war erfolgreich! Wir freuen uns, Sie in unserer 
                                Lern-Community begrüßen zu dürfen.
                            </p>
                            
                            <div class="features">
                                <h3>Das erwartet Sie bei %s:</h3>
                                <ul>
                                    <li data-icon="🎯">Interaktive Radiologie-Quizzes für alle Schwierigkeitsgrade</li>
                                    <li data-icon="📊">Detailliertes Fortschritts-Tracking und Statistiken</li>
                                    <li data-icon="🏆">Achievements und Lernziele für zusätzliche Motivation</li>
                                    <li data-icon="📚">Expertenwissen in Röntgen, CT, MRT und Ultraschall</li>
                                </ul>
                            </div>
                            
                            <div class="cta-container">
                                <a href="%s" class="cta-button">Jetzt loslegen! 🚀</a>
                            </div>
                            
                            <p class="message" style="margin-top: 30px;">
                                Bei Fragen oder Problemen stehen wir Ihnen jederzeit zur Verfügung. 
                                Viel Erfolg beim Lernen!
                            </p>
                        </div>
                        
                        <div class="footer">
                            <p><strong>© 2025 %s</strong></p>
                            <p>Medizinisches Lernportal für Radiologie</p>
                        </div>
                    </div>
                </body>
                </html>
                """, appName, fullname, appName, baseUrl, appName);
    }

    /**
     * ✅ Modern HTML template for password reset email
     */
    private String createPasswordResetEmailHtml(String fullname, String resetUrl) {
        return String.format("""
                <!DOCTYPE html>
                <html lang="de">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body { 
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
                            line-height: 1.6;
                            color: #1f2937;
                            background-color: #f9fafb;
                        }
                        .email-wrapper {
                            max-width: 600px;
                            margin: 40px auto;
                            background: white;
                            border-radius: 16px;
                            overflow: hidden;
                            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
                        }
                        .header {
                            background: linear-gradient(135deg, #dc2626 0%%, #ef4444 100%%);
                            padding: 40px 20px;
                            text-align: center;
                            color: white;
                        }
                        .header h1 {
                            font-size: 28px;
                            font-weight: 800;
                            margin-bottom: 8px;
                        }
                        .content {
                            padding: 40px 30px;
                        }
                        .greeting {
                            font-size: 18px;
                            margin-bottom: 20px;
                            color: #374151;
                        }
                        .greeting strong {
                            color: #dc2626;
                        }
                        .message {
                            font-size: 15px;
                            color: #4b5563;
                            margin-bottom: 30px;
                            line-height: 1.7;
                        }
                        .cta-button {
                            display: inline-block;
                            background: linear-gradient(135deg, #dc2626, #ef4444);
                            color: white;
                            padding: 16px 40px;
                            text-decoration: none;
                            border-radius: 12px;
                            font-weight: 700;
                            font-size: 16px;
                            margin: 25px 0;
                            box-shadow: 0 4px 12px rgba(220,38,38,0.3);
                        }
                        .cta-container {
                            text-align: center;
                        }
                        .info-box {
                            background: #fef3c7;
                            border-left: 4px solid #f59e0b;
                            padding: 16px;
                            border-radius: 8px;
                            margin: 25px 0;
                        }
                        .info-box p {
                            font-size: 14px;
                            color: #92400e;
                            margin: 0;
                            font-weight: 600;
                        }
                        .security-tips {
                            background: #fee2e2;
                            border-radius: 8px;
                            padding: 20px;
                            margin: 25px 0;
                        }
                        .security-tips h3 {
                            font-size: 16px;
                            color: #7f1d1d;
                            margin-bottom: 12px;
                        }
                        .security-tips ul {
                            list-style: none;
                            padding: 0;
                        }
                        .security-tips li {
                            padding: 8px 0;
                            padding-left: 24px;
                            position: relative;
                            font-size: 14px;
                            color: #991b1b;
                        }
                        .security-tips li:before {
                            content: "⚠️";
                            position: absolute;
                            left: 0;
                        }
                        .footer {
                            background: #f9fafb;
                            padding: 30px;
                            text-align: center;
                            border-top: 1px solid #e5e7eb;
                        }
                        .footer p {
                            font-size: 13px;
                            color: #9ca3af;
                            margin: 4px 0;
                        }
                    </style>
                </head>
                <body>
                    <div class="email-wrapper">
                        <div class="header">
                            <h1>🔒 Passwort zurücksetzen</h1>
                        </div>
                        
                        <div class="content">
                            <p class="greeting">Hallo <strong>%s</strong>!</p>
                            
                            <p class="message">
                                Sie haben eine Passwort-Zurücksetzung für Ihr %s-Konto angefordert. 
                                Klicken Sie auf den Button unten, um ein neues Passwort zu setzen:
                            </p>
                            
                            <div class="cta-container">
                                <a href="%s" class="cta-button">Neues Passwort setzen</a>
                            </div>
                            
                            <div class="info-box">
                                <p>⏱️ Dieser Link ist nur 30 Minuten gültig</p>
                            </div>
                            
                            <div class="security-tips">
                                <h3>🔒 Sicherheitshinweise</h3>
                                <ul>
                                    <li>Falls Sie keine Zurücksetzung angefordert haben, ignorieren Sie diese E-Mail</li>
                                    <li>Teilen Sie diesen Link niemals mit anderen Personen</li>
                                    <li>Nach 30 Minuten müssen Sie eine neue Anfrage stellen</li>
                                    <li>Bei verdächtigen Aktivitäten kontaktieren Sie uns sofort</li>
                                </ul>
                            </div>
                            
                            <p class="message" style="margin-top: 25px; color: #6b7280; font-size: 14px;">
                                <em>Wenn der Button nicht funktioniert, kopieren Sie diesen Link in Ihren Browser:</em><br>
                                <a href="%s" style="color: #645CBB; word-break: break-all;">%s</a>
                            </p>
                        </div>
                        
                        <div class="footer">
                            <p><strong>© 2025 %s</strong></p>
                            <p>Medizinisches Lernportal für Radiologie</p>
                        </div>
                    </div>
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