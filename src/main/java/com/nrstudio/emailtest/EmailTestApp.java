package com.nrstudio.emailtest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.*;

import java.util.Properties;

// Scanner UNIQUEMENT ce package
@SpringBootApplication(
        scanBasePackages = "com.nrstudio.emailtest",
        exclude = {
                DataSourceAutoConfiguration.class,
                HibernateJpaAutoConfiguration.class
        }
)
@RestController
@RequestMapping("/api/email-test")
public class EmailTestApp {

    public static void main(String[] args) {
        SpringApplication.run(EmailTestApp.class, args);
    }

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername("rarianamiadana@gmail.com");
        mailSender.setPassword("qcuw wsrj dfen bkhz");

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

    @PostMapping("/send")
    public String sendTestEmail() {
        try {
            JavaMailSender mailSender = javaMailSender();

            var message = new org.springframework.mail.SimpleMailMessage();
            message.setFrom("rarianamiadana@gmail.com");
            message.setTo("rarianamiadana@gmail.com");
            message.setSubject("🎉 TEST RÉUSSI - " + System.currentTimeMillis());
            message.setText("Félicitations ! Email envoyé avec succès sans base de données !");

            mailSender.send(message);
            return "✅ Email envoyé ! Vérifiez Gmail.";
        } catch (Exception e) {
            return "❌ Erreur : " + e.getMessage();
        }
    }

    @GetMapping("/status")
    public String status() {
        return "🚀 Service Email Test - " + java.time.LocalDateTime.now();
    }
}