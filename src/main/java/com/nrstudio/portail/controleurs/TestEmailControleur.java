// package com.nrstudio.portail.controleurs;

// import com.nrstudio.portail.services.EmailNotificationService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/test")
// public class TestEmailControleur {

//     @Autowired
//     private EmailNotificationService emailService;

//     @GetMapping("/email-encoding")
//     public String testEmailEncoding() {
//         emailService.testerEncodageUTF8("rarianamiadana@gmail.com");
//         return "Test d'encodage UTF-8 envoyé ! Vérifiez votre boite Gmail.";
//     }

//     @GetMapping("/email-text-simple")
//     public String testEmailTextSimple() {
//         emailService.testerTexteSimple("rarianamiadana@gmail.com");
//         return "Test email texte simple envoyé ! Vérifiez votre boite Gmail.";
//     }

//     @GetMapping("/email-ticket")
//     public String testEmailTicket() {
//         emailService.envoyerNotificationTicketCree(
//             "rarianamiadana@gmail.com", 
//             "TCK-TEST-123", 
//             "Test de ticket avec accents - Élément créé"
//         );
//         return "Test notification ticket envoyé !";
//     }
// }