package org.nikolic.programm.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessagePreparator;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.util.Properties;

/**
 * Mail-Konfiguration:
 * - Aktiviert MailProperties als ConfigurationProperties-Bean
 * - Erzeugt JavaMailSenderImpl, wenn MailProperties.host gesetzt ist
 * - Sonst: NoOpMailSender (loggt Mails nur), damit App auch ohne SMTP-Daten startet
 */
@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig {

    private static final Logger logger = LoggerFactory.getLogger(MailConfig.class);

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender(MailProperties mailProperties) {
        String host = mailProperties.getHost();
        if (host == null || host.isBlank()) {
            logger.warn("Keine SMTP-Konfiguration gefunden (spring.mail.host). Verwende NoOpMailSender (Mails werden nur geloggt).");
            return new NoOpMailSender();
        }

        JavaMailSenderImpl impl = new JavaMailSenderImpl();
        impl.setHost(mailProperties.getHost());
        if (mailProperties.getPort() != null) impl.setPort(mailProperties.getPort());
        impl.setUsername(mailProperties.getUsername());
        impl.setPassword(mailProperties.getPassword());
        if (mailProperties.getProtocol() != null) impl.setProtocol(mailProperties.getProtocol());

        Properties props = new Properties();
        mailProperties.getProperties().forEach(props::put);
        props.putIfAbsent("mail.smtp.auth", String.valueOf(mailProperties.getProperties().getOrDefault("mail.smtp.auth", "true")));
        props.putIfAbsent("mail.smtp.starttls.enable", String.valueOf(mailProperties.getProperties().getOrDefault("mail.smtp.starttls.enable", "true")));
        impl.setJavaMailProperties(props);

        logger.info("JavaMailSenderImpl konfiguriert mit host={}", mailProperties.getHost());
        return impl;
    }

    // Minimal implementierter JavaMailSender, der keine Mails verschickt, sondern nur loggt.
    private static class NoOpMailSender implements JavaMailSender {

        private final Logger log = LoggerFactory.getLogger(NoOpMailSender.class);
        private final Session session = Session.getInstance(new Properties());

        @Override
        public MimeMessage createMimeMessage() {
            return new MimeMessage(session);
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
            try {
                return new MimeMessage(session, contentStream);
            } catch (Exception e) {
                throw new MailException("Failed to create MimeMessage from stream", e) {};
            }
        }

        @Override
        public void send(MimeMessage mimeMessage) throws MailException {
            log.info("[NoOpMailSender] send(MimeMessage) — kein Versand (subject={})", safeSubject(mimeMessage));
        }

        @Override
        public void send(MimeMessage... mimeMessages) throws MailException {
            log.info("[NoOpMailSender] send(MimeMessage[]) — {} Mails nicht versendet", mimeMessages.length);
        }

        @Override
        public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
            log.info("[NoOpMailSender] send(MimeMessagePreparator) — kein Versand");
        }

        @Override
        public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
            log.info("[NoOpMailSender] send(MimeMessagePreparator[]) — {} Preparators nicht ausgeführt", mimeMessagePreparators.length);
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {
            log.info("[NoOpMailSender] send(SimpleMailMessage) — kein Versand an={} subject={}", simpleMessage.getTo(), simpleMessage.getSubject());
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) throws MailException {
            log.info("[NoOpMailSender] send(SimpleMailMessage[]) — {} Nachrichten nicht versendet", simpleMessages.length);
        }

        private String safeSubject(MimeMessage m) {
            try { return m.getSubject(); } catch (Exception e) { return "<unbekannt>"; }
        }
    }
}