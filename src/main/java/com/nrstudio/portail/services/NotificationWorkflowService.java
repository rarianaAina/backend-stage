package com.nrstudio.portail.services;

import com.nrstudio.portail.depots.*;
import com.nrstudio.portail.domaine.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationWorkflowService {
    
    private final WorkflowNotificationMailRepository workflowRepository;
    private final TypeNotificationRepository typeNotificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final EmailNotificationService emailService;
    private final WhatsAppNotificationService whatsAppService;
    
    public NotificationWorkflowService(WorkflowNotificationMailRepository workflowRepository,
                                     TypeNotificationRepository typeNotificationRepository,
                                     NotificationTemplateRepository templateRepository,
                                     UtilisateurRepository utilisateurRepository,
                                     EmailNotificationService emailService,
                                     WhatsAppNotificationService whatsAppService) {
        this.workflowRepository = workflowRepository;
        this.typeNotificationRepository = typeNotificationRepository;
        this.templateRepository = templateRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.emailService = emailService;
        this.whatsAppService = whatsAppService;
    }
    
    /**
     * Exécute le workflow de notification pour un type donné
     * 1. Notifie toujours le créateur du ticket
     * 2. Notifie les utilisateurs supplémentaires selon le workflow configuré
     */
    public void executerWorkflowNotification(String typeNotificationCode, Ticket ticket, Object... parametres) {
        
        System.out.println("=== DÉBUT exécuterWorkflowNotification ===");
        // 1. TOUJOURS notifier le créateur du ticket (OBLIGATOIRE)
        notifierCreateurTicket(ticket, typeNotificationCode, parametres);
        System.out.println("Notifié le créateur du ticket: " + ticket.getCreeParUtilisateurId());
        // 2. Notifications supplémentaires selon le workflow configuré
        List<WorkflowNotificationMail> workflowSteps = workflowRepository
            .findByTypeNotificationCodeActif(typeNotificationCode);
        System.out.println("Nombre d'étapes dans le workflow: " + workflowSteps.size());
        for (WorkflowNotificationMail step : workflowSteps) {
            // Éviter les doublons avec le créateur
            if (!step.getUtilisateurId().equals(ticket.getCreeParUtilisateurId())) {
                System.out.println("Notifiant l'utilisateur workflow: " + step.getUtilisateurId());
                envoyerNotificationSelonWorkflow(step, ticket, parametres);
                System.out.println("Notification envoyée à l'utilisateur workflow: " + step.getUtilisateurId());
            }
        }
    }
    
    /**
     * Notification obligatoire au créateur du ticket
     */
    private void notifierCreateurTicket(Ticket ticket, String typeNotificationCode, Object... parametres) {
        // Récupérer le créateur du ticket
        Optional<Utilisateur> createurOpt = utilisateurRepository.findByIdExterneCrm(ticket.getClientId().toString());

        if (createurOpt.isEmpty()) {
            System.err.println("Créateur du ticket non trouvé: " + ticket.getClientId());
            return;
        }
        System.out.println("Client: " + ticket.getClientId());
        Utilisateur createur = createurOpt.get();
        System.out.println("Créateur du ticket trouvé: " + createur.getEmail());
        // Récupérer le template si disponible
        Optional<NotificationTemplate> templateOpt = getTemplateForNotification(typeNotificationCode);
        
        // Appliquer la logique de notification selon le type
        switch (typeNotificationCode) {
            case "CREATION_TICKET":
                envoyerNotificationCreationTicket(createur, ticket, templateOpt);
                break;
                
            case "MODIFICATION_STATUT_TICKET":
                if (parametres.length >= 2) {
                    Integer ancienStatutId = (Integer) parametres[0];
                    Integer nouveauStatutId = (Integer) parametres[1];
                    envoyerNotificationChangementStatut(createur, ticket, ancienStatutId, nouveauStatutId, templateOpt);
                }
                break;
                
            case "AJOUT_SOLUTION":
                envoyerNotificationAjoutSolution(createur, ticket, templateOpt);
                break;
                
            case "CLOTURE_TICKET":
                envoyerNotificationClotureTicket(createur, ticket, templateOpt);
                break;
                
            default:
                System.err.println("Type de notification non géré: " + typeNotificationCode);
        }
    }
    
    /**
     * Notification pour les utilisateurs du workflow
     */
    private void envoyerNotificationSelonWorkflow(WorkflowNotificationMail step, Ticket ticket, Object... parametres) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(step.getUtilisateurId());
        if (utilisateurOpt.isEmpty()) {
            System.err.println("Utilisateur workflow non trouvé: " + step.getUtilisateurId());
            return;
        }
        
        Utilisateur utilisateur = utilisateurOpt.get();
        TypeNotification typeNotification = step.getTypeNotification();
        Optional<NotificationTemplate> templateOpt = getTemplateForNotification(typeNotification.getCode());
        
        switch (typeNotification.getCode()) {
            case "CREATION_TICKET":
                envoyerNotificationCreationTicket(utilisateur, ticket, templateOpt);
                break;
                
            case "MODIFICATION_STATUT_TICKET":
                if (parametres.length >= 2) {
                    Integer ancienStatutId = (Integer) parametres[0];
                    Integer nouveauStatutId = (Integer) parametres[1];
                    envoyerNotificationChangementStatut(utilisateur, ticket, ancienStatutId, nouveauStatutId, templateOpt);
                }
                break;
                
            case "AJOUT_SOLUTION":
                envoyerNotificationAjoutSolution(utilisateur, ticket, templateOpt);
                break;
                
            case "CLOTURE_TICKET":
                envoyerNotificationClotureTicket(utilisateur, ticket, templateOpt);
                break;
                
            default:
                System.err.println("Type de notification non géré: " + typeNotification.getCode());
        }
    }
    
    /**
     * Récupère le template associé à un type de notification
     */
    private Optional<NotificationTemplate> getTemplateForNotification(String typeNotificationCode) {
        Optional<TypeNotification> typeOpt = typeNotificationRepository.findByCode(typeNotificationCode);
        if (typeOpt.isPresent() && typeOpt.get().getTemplateId() != null) {
            return templateRepository.findById(typeOpt.get().getTemplateId());
        }
        return Optional.empty();
    }
    
    /**
     * Envoi notification création de ticket
     */
    private void envoyerNotificationCreationTicket(Utilisateur utilisateur, Ticket ticket, Optional<NotificationTemplate> templateOpt) {
        if (utilisateur.getEmail() != null) {
            if (templateOpt.isPresent()) {
                NotificationTemplate template = templateOpt.get();
                emailService.envoyerEmailAvecTemplate(
                    utilisateur.getEmail(),
                    template.getSujet().replace("{reference}", ticket.getReference()),
                    template.getContenuHtml()
                        .replace("{reference}", ticket.getReference())
                        .replace("{titre}", ticket.getTitre())
                        .replace("{utilisateur}", utilisateur.getNom())
                );
            } else {
                emailService.envoyerNotificationTicketCree(
                    utilisateur.getEmail(),
                    ticket.getReference(),
                    ticket.getTitre()
                );
            }
        }
        
        if (utilisateur.getTelephone() != null) {
            whatsAppService.envoyerNotificationTicketCree(
                utilisateur.getTelephone(),
                ticket.getReference(),
                ticket.getTitre()
            );
        }
    }
    
    /**
     * Envoi notification changement de statut
     */
    private void envoyerNotificationChangementStatut(Utilisateur utilisateur, Ticket ticket, 
                                                    Integer ancienStatutId, Integer nouveauStatutId,
                                                    Optional<NotificationTemplate> templateOpt) {
        String ancienStatut = mapStatutIdToString(ancienStatutId);
        String nouveauStatut = mapStatutIdToString(nouveauStatutId);
        
        if (utilisateur.getEmail() != null) {
            if (templateOpt.isPresent()) {
                NotificationTemplate template = templateOpt.get();
                emailService.envoyerEmailAvecTemplate(
                    utilisateur.getEmail(),
                    template.getSujet().replace("{reference}", ticket.getReference()),
                    template.getContenuHtml()
                        .replace("{reference}", ticket.getReference())
                        .replace("{ancienStatut}", ancienStatut)
                        .replace("{nouveauStatut}", nouveauStatut)
                        .replace("{utilisateur}", utilisateur.getNom())
                );
            } else {
                emailService.envoyerNotificationChangementStatut(
                    utilisateur.getEmail(),
                    ticket.getReference(),
                    ancienStatut,
                    nouveauStatut
                );
            }
        }
        
        if (utilisateur.getTelephone() != null) {
            whatsAppService.envoyerNotificationChangementStatut(
                utilisateur.getTelephone(),
                ticket.getReference(),
                nouveauStatut
            );
        }
    }
    
    /**
     * Envoi notification ajout de solution
     */
    private void envoyerNotificationAjoutSolution(Utilisateur utilisateur, Ticket ticket, Optional<NotificationTemplate> templateOpt) {
        if (utilisateur.getEmail() != null) {
            if (templateOpt.isPresent()) {
                NotificationTemplate template = templateOpt.get();
                emailService.envoyerEmailAvecTemplate(
                    utilisateur.getEmail(),
                    template.getSujet().replace("{reference}", ticket.getReference()),
                    template.getContenuHtml()
                        .replace("{reference}", ticket.getReference())
                        .replace("{utilisateur}", utilisateur.getNom())
                );
            } else {
                // Template par défaut pour ajout de solution
                String sujet = "✅ Solution ajoutée - " + ticket.getReference();
                String contenu = "<p>Une solution a été ajoutée à votre ticket <strong>" + ticket.getReference() + "</strong></p>" +
                               "<p><strong>Titre:</strong> " + ticket.getTitre() + "</p>" +
                               "<div style=\"text-align: center; margin: 20px 0;\">" +
                               "<a href=\"#\" class=\"action-button\">📖 Voir la Solution</a>" +
                               "</div>";
                
                emailService.envoyerEmailAvecTemplate(utilisateur.getEmail(), sujet, contenu);
            }
        }
        
        // if (utilisateur.getTelephone() != null) {
        //     whatsAppService.envoyerNotificationAjoutSolution(
        //         utilisateur.getTelephone(),
        //         ticket.getReference()
        //     );
        // }
    }
    
    /**
     * Envoi notification clôture de ticket
     */
    private void envoyerNotificationClotureTicket(Utilisateur utilisateur, Ticket ticket, Optional<NotificationTemplate> templateOpt) {
        if (utilisateur.getEmail() != null) {
            if (templateOpt.isPresent()) {
                NotificationTemplate template = templateOpt.get();
                emailService.envoyerEmailAvecTemplate(
                    utilisateur.getEmail(),
                    template.getSujet().replace("{reference}", ticket.getReference()),
                    template.getContenuHtml()
                        .replace("{reference}", ticket.getReference())
                        .replace("{utilisateur}", utilisateur.getNom())
                );
            } else {
                // Template par défaut pour clôture
                String sujet = "🏁 Ticket clôturé - " + ticket.getReference();
                String contenu = "<p>Votre ticket <strong>" + ticket.getReference() + "</strong> a été clôturé.</p>" +
                               "<p><strong>Titre:</strong> " + ticket.getTitre() + "</p>" +
                               "<div style=\"background: linear-gradient(135deg, #10B981 0%, #34D399 100%); color: white; padding: 20px; border-radius: 12px; text-align: center; margin: 20px 0;\">" +
                               "<h3 style=\"margin: 0 0 10px 0;\">✅ Résolu</h3>" +
                               "<p style=\"margin: 0; opacity: 0.9;\">Votre demande a été traitée avec succès</p>" +
                               "</div>";
                
                emailService.envoyerEmailAvecTemplate(utilisateur.getEmail(), sujet, contenu);
            }
        }
        
        // if (utilisateur.getTelephone() != null) {
        //     whatsAppService.envoyerNotificationClotureTicket(
        //         utilisateur.getTelephone(),
        //         ticket.getReference()
        //     );
        // }
    }
    
    /**
     * Conversion ID statut → libellé
     */
    private String mapStatutIdToString(Integer statutId) {
        if (statutId == null) return "Open";
        switch (statutId) {
            case 4: return "Closed";
            case 3: return "Pending";
            case 2: return "In Progress";
            default: return "Open";
        }
    }
    
    /**
     * Méthode utilitaire pour récupérer tous les workflows actifs (pour l'admin)
     */
    public List<WorkflowNotificationMail> getWorkflowsActifs() {
        return workflowRepository.findAllActifs();
    }
    
    /**
     * Méthode utilitaire pour récupérer les workflows par type (pour l'admin)
     */
    public List<WorkflowNotificationMail> getWorkflowsParType(String typeNotificationCode) {
        return workflowRepository.findByTypeNotificationCodeActif(typeNotificationCode);
    }
}