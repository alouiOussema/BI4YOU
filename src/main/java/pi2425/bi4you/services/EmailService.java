package pi2425.bi4you.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private final JavaMailSender mailSender;
    
    public void sendPasswordResetEmail(String to, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Réinitialisation de mot de passe - BI4YOU");
            message.setText(buildPasswordResetEmailContent(token));
            message.setFrom("noreply@bi4you.com");
            
            mailSender.send(message);
            logger.info("Email de réinitialisation envoyé à: {}", to);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email à {}: {}", to, e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email de réinitialisation");
        }
    }
    
    public void sendWelcomeEmail(String to, String username, String temporaryPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Bienvenue sur BI4YOU - Vos identifiants de connexion");
            message.setText(buildWelcomeEmailContent(username, temporaryPassword));
            message.setFrom("noreply@bi4you.com");
            
            mailSender.send(message);
            logger.info("Email de bienvenue envoyé à: {}", to);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email de bienvenue à {}: {}", to, e.getMessage());
            throw new RuntimeException("Impossible d'envoyer l'email de bienvenue");
        }
    }
    
    private String buildPasswordResetEmailContent(String token) {
        return String.format(
            "Bonjour,\n\n" +
            "Vous avez demandé la réinitialisation de votre mot de passe pour votre compte BI4YOU.\n\n" +
            "Votre token de réinitialisation est: %s\n\n" +
            "Ce token expire dans 24 heures.\n\n" +
            "Si vous n'avez pas demandé cette réinitialisation, veuillez ignorer cet email.\n\n" +
            "Cordialement,\n" +
            "L'équipe BI4YOU",
            token
        );
    }
    
    private String buildWelcomeEmailContent(String username, String temporaryPassword) {
        return String.format(
            "Bonjour,\n\n" +
            "Bienvenue sur BI4YOU ! Votre compte a été créé avec succès.\n\n" +
            "Vos identifiants de connexion sont:\n" +
            "Nom d'utilisateur: %s\n" +
            "Mot de passe temporaire: %s\n\n" +
            "IMPORTANT: Veuillez changer votre mot de passe lors de votre première connexion.\n\n" +
            "Cordialement,\n" +
            "L'équipe BI4YOU",
            username, temporaryPassword
        );
    }
}

