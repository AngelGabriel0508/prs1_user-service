package pe.edu.vallegrande.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetLink(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("🔒 Restablece tu contraseña");
        message.setText("Hola,\n\nHaz clic en el siguiente enlace para restablecer tu contraseña:\n\n" + resetLink + "\n\nSi no solicitaste esto, ignora este mensaje.");

        String senderEmail = ((JavaMailSenderImpl) mailSender).getUsername();

        System.out.println("📧 Enviando desde: " + senderEmail);
        System.out.println("📨 Enviando a: " + to);
        System.out.println("🔗 Link: " + resetLink);

        mailSender.send(message);
        System.out.println("✅ Correo enviado correctamente.");
    }
}
