package com.nrstudio.portail.services;

import com.nrstudio.portail.depots.*;
import com.nrstudio.portail.domaine.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import com.nrstudio.portail.depots.utilisateur.UtilisateurInterneRepository;
import com.nrstudio.portail.domaine.utilisateur.UtilisateurInterne;
import com.nrstudio.portail.services.notification.EmailNotificationService;

@Service
public class NotificationWorkflowService {
    
    private final WorkflowNotificationMailRepository workflowRepository;
    private final TypeNotificationRepository typeNotificationRepository;
    private final NotificationTemplateRepository templateRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurInterneRepository utilisateurInterneRepository; // Nouveau repository
    private final EmailNotificationService emailService;
    private final WhatsAppNotificationService whatsAppService;
    
    public NotificationWorkflowService(WorkflowNotificationMailRepository workflowRepository,
                                     TypeNotificationRepository typeNotificationRepository,
                                     NotificationTemplateRepository templateRepository,
                                     UtilisateurRepository utilisateurRepository,
                                     UtilisateurInterneRepository utilisateurInterneRepository, // Nouveau
                                     EmailNotificationService emailService,
                                     WhatsAppNotificationService whatsAppService) {
        this.workflowRepository = workflowRepository;
        this.typeNotificationRepository = typeNotificationRepository;
        this.templateRepository = templateRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurInterneRepository = utilisateurInterneRepository; // Initialisation
        this.emailService = emailService;
        this.whatsAppService = whatsAppService;
    }
    
    /**
     * Exécute le workflow de notification pour un type donné
     * 1. Notifie toujours le créateur du ticket (dans table utilisateur)
     * 2. Notifie les utilisateurs supplémentaires selon le workflow configuré (dans table utilisateur_interne)
     */
    public void executerWorkflowNotification(String typeNotificationCode, Ticket ticket, Object... parametres) {
        
        System.out.println("=== DÉBUT exécuterWorkflowNotification ===");
        
        // 1. TOUJOURS notifier le créateur du ticket (OBLIGATOIRE) - Table utilisateur
        notifierCreateurTicket(ticket, typeNotificationCode, parametres);
        System.out.println("Notifié le créateur du ticket: " + ticket.getCreeParUtilisateurId());
        
        // 2. Notifications supplémentaires selon le workflow configuré - Table utilisateur_interne
        List<WorkflowNotificationMail> workflowSteps = workflowRepository
            .findByTypeNotificationCodeActif(typeNotificationCode);
        System.out.println("Nombre d'étapes dans le workflow: " + workflowSteps.size());
        
        for (WorkflowNotificationMail step : workflowSteps) {
            // Éviter les doublons avec le créateur
            if (!step.getUtilisateurId().equals(ticket.getCreeParUtilisateurId())) {
                System.out.println("Notifiant l'utilisateur interne workflow: " + step.getUtilisateurId());
                envoyerNotificationSelonWorkflow(step, ticket, parametres);
                System.out.println("Notification envoyée à l'utilisateur interne workflow: " + step.getUtilisateurId());
            }
        }
    }
    
    /**
     * Notification obligatoire au créateur du ticket (table utilisateur)
     */
    private void notifierCreateurTicket(Ticket ticket, String typeNotificationCode, Object... parametres) {
        // Récupérer le créateur du ticket depuis la table utilisateur
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
                envoyerNotificationCreationTicket(createur, null, ticket, templateOpt);
                break;
                
            case "MODIFICATION_STATUT_TICKET":
                if (parametres.length >= 2) {
                    Integer ancienStatutId = (Integer) parametres[0];
                    Integer nouveauStatutId = (Integer) parametres[1];
                    envoyerNotificationChangementStatut(createur, null, ticket, ancienStatutId, nouveauStatutId, templateOpt);
                }
                break;
                
            case "AJOUT_SOLUTION":
                envoyerNotificationAjoutSolution(createur, null, ticket, templateOpt);
                break;
                
            case "CLOTURE_TICKET":
                envoyerNotificationClotureTicket(createur, null, ticket, templateOpt);
                break;
                
            default:
                System.err.println("Type de notification non géré: " + typeNotificationCode);
        }
    }
    
    /**
     * Notification pour les utilisateurs internes du workflow (table utilisateur_interne)
     */
    private void envoyerNotificationSelonWorkflow(WorkflowNotificationMail step, Ticket ticket, Object... parametres) {
        // Rechercher d'abord dans utilisateur_interne
        Optional<UtilisateurInterne> utilisateurInterneOpt = utilisateurInterneRepository.findById(step.getUtilisateurId());
        
        if (utilisateurInterneOpt.isPresent()) {
            // Utilisateur trouvé dans la table utilisateur_interne
            UtilisateurInterne utilisateurInterne = utilisateurInterneOpt.get();
            TypeNotification typeNotification = step.getTypeNotification();
            Optional<NotificationTemplate> templateOpt = getTemplateForNotification(typeNotification.getCode());
            
            switch (typeNotification.getCode()) {
                case "CREATION_TICKET":
                    envoyerNotificationCreationTicket(null, utilisateurInterne, ticket, templateOpt);
                    break;
                
                case "CREATION_REPONSE_SOLUTION":
                    envoyerNotificationCreationReponseSolution(utilisateurInterne, ticket, templateOpt);
                    break;
                    
                case "MODIFICATION_STATUT_TICKET":
                    if (parametres.length >= 2) {
                        Integer ancienStatutId = (Integer) parametres[0];
                        Integer nouveauStatutId = (Integer) parametres[1];
                        envoyerNotificationChangementStatut(null, utilisateurInterne, ticket, ancienStatutId, nouveauStatutId, templateOpt);
                    }
                    break;
                    
                case "AJOUT_SOLUTION":
                    envoyerNotificationAjoutSolution(null, utilisateurInterne, ticket, templateOpt);
                    break;
                    
                case "CLOTURE_TICKET":
                    envoyerNotificationClotureTicket(null, utilisateurInterne, ticket, templateOpt);
                    break;
                    
                default:
                    System.err.println("Type de notification non géré: " + typeNotification.getCode());
            }
        } else {
            // Fallback : chercher dans utilisateur (au cas où)
            Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(step.getUtilisateurId());
            if (utilisateurOpt.isPresent()) {
                Utilisateur utilisateur = utilisateurOpt.get();
                TypeNotification typeNotification = step.getTypeNotification();
                Optional<NotificationTemplate> templateOpt = getTemplateForNotification(typeNotification.getCode());
                
                switch (typeNotification.getCode()) {
                    case "CREATION_TICKET":
                        envoyerNotificationCreationTicket(utilisateur, null, ticket, templateOpt);
                        break;
                    // ... autres cas similaires
                }
            } else {
                System.err.println("Utilisateur workflow non trouvé (ni dans utilisateur_interne ni dans utilisateur): " + step.getUtilisateurId());
            }
        }
    }
    
    /**
     * Envoi notification création de ticket (version avec les deux types d'utilisateurs)
     */
    private void envoyerNotificationCreationTicket(Utilisateur utilisateur, UtilisateurInterne utilisateurInterne, 
                                                  Ticket ticket, Optional<NotificationTemplate> templateOpt) {
        
        String email = utilisateur != null ? utilisateur.getEmail() : 
                      (utilisateurInterne != null ? utilisateurInterne.getEmail() : null);
        String telephone = utilisateur != null ? utilisateur.getTelephone() : 
                          (utilisateurInterne != null ? utilisateurInterne.getTelephone() : null);
        String nom = utilisateur != null ? utilisateur.getNom() : 
                    (utilisateurInterne != null ? utilisateurInterne.getNom() : "Utilisateur");
        
        if (email != null) {
            if (templateOpt.isPresent()) {
                NotificationTemplate template = templateOpt.get();
                emailService.envoyerEmailAvecTemplate(
                    email,
                    template.getSujet().replace("{reference}", ticket.getReference()),
                    template.getContenuHtml()
                        .replace("{reference}", ticket.getReference())
                        .replace("{titre}", ticket.getTitre())
                        .replace("{utilisateur}", nom)
                );
            } else {
                emailService.envoyerNotificationTicketCree(
                    email,
                    ticket.getReference(),
                    ticket.getTitre()
                );
            }
        }
        
        if (telephone != null) {
            whatsAppService.envoyerNotificationTicketCree(
                telephone,
                ticket.getReference(),
                ticket.getTitre()
            );
        }
    }
    
    /**
     * Envoi notification création de réponse solution (version avec les deux types d'utilisateurs)
     */
    private void envoyerNotificationCreationReponseSolution(UtilisateurInterne utilisateurInterne, 
                                                  Ticket ticket, Optional<NotificationTemplate> templateOpt) {
        
        String email = utilisateurInterne != null ? utilisateurInterne.getEmail() : null;
        String telephone = utilisateurInterne != null ? utilisateurInterne.getTelephone() : null;
        if (email != null) {
            if (templateOpt.isPresent()) {
                NotificationTemplate template = templateOpt.get();
                emailService.envoyerEmailAvecTemplate(
                    email,
                    template.getSujet().replace("{reference}", ticket.getReference()),
                    template.getContenuHtml()
                        .replace("{reference}", ticket.getReference())
                        .replace("{titre}", ticket.getTitre())

                );
            } 
        }
        
        if (telephone != null) {
            whatsAppService.envoyerNotificationTicketCree(
                telephone,
                ticket.getReference(),
                ticket.getTitre()
            );
        }
    }
    /**
     * Envoi notification changement de statut (version avec les deux types d'utilisateurs)
     */
    private void envoyerNotificationChangementStatut(Utilisateur utilisateur, UtilisateurInterne utilisateurInterne, 
                                                    Ticket ticket, Integer ancienStatutId, Integer nouveauStatutId,
                                                    Optional<NotificationTemplate> templateOpt) {
        
        String ancienStatut = mapStatutIdToString(ancienStatutId);
        String nouveauStatut = mapStatutIdToString(nouveauStatutId);
        
        String email = utilisateur != null ? utilisateur.getEmail() : 
                      (utilisateurInterne != null ? utilisateurInterne.getEmail() : null);
        String telephone = utilisateur != null ? utilisateur.getTelephone() : 
                          (utilisateurInterne != null ? utilisateurInterne.getTelephone() : null);
        String nom = utilisateur != null ? utilisateur.getNom() : 
                    (utilisateurInterne != null ? utilisateurInterne.getNom() : "Utilisateur");
        
        if (email != null) {
            if (templateOpt.isPresent()) {
                NotificationTemplate template = templateOpt.get();
                emailService.envoyerEmailAvecTemplate(
                    email,
                    template.getSujet().replace("{reference}", ticket.getReference()),
                    template.getContenuHtml()
                        .replace("{reference}", ticket.getReference())
                        .replace("{ancienStatut}", ancienStatut)
                        .replace("{nouveauStatut}", nouveauStatut)
                        .replace("{utilisateur}", nom)
                );
            } else {
                emailService.envoyerNotificationChangementStatut(
                    email,
                    ticket.getReference(),
                    ancienStatut,
                    nouveauStatut
                );
            }
        }
        
        if (telephone != null) {
            whatsAppService.envoyerNotificationChangementStatut(
                telephone,
                ticket.getReference(),
                nouveauStatut
            );
        }
    }
    
    /**
     * Envoi notification ajout de solution (version avec les deux types d'utilisateurs)
     */
    private void envoyerNotificationAjoutSolution(Utilisateur utilisateur, UtilisateurInterne utilisateurInterne, 
                                                 Ticket ticket, Optional<NotificationTemplate> templateOpt) {
        
        String email = utilisateur != null ? utilisateur.getEmail() : 
                      (utilisateurInterne != null ? utilisateurInterne.getEmail() : null);
        String telephone = utilisateur != null ? utilisateur.getTelephone() : 
                          (utilisateurInterne != null ? utilisateurInterne.getTelephone() : null);
        String nom = utilisateur != null ? utilisateur.getNom() : 
                    (utilisateurInterne != null ? utilisateurInterne.getNom() : "Utilisateur");
        
        if (email != null) {
            if (templateOpt.isPresent()) {
                NotificationTemplate template = templateOpt.get();
                emailService.envoyerEmailAvecTemplate(
                    email,
                    template.getSujet().replace("{reference}", ticket.getReference()),
                    template.getContenuHtml()
                        .replace("{reference}", ticket.getReference())
                        .replace("{titre}", ticket.getTitre())
                        .replace("{utilisateur}", nom)
                );
            } else {
                // Template par défaut pour ajout de solution
                String sujet = "✅ Solution ajoutée - " + ticket.getReference();
                String contenu = "<p>Une solution a été ajoutée à votre ticket <strong>" + ticket.getReference() + "</strong></p>" +
                               "<p><strong>Titre:</strong> " + ticket.getTitre() + "</p>" +
                               "<div style=\"text-align: center; margin: 20px 0;\">" +
                               "<a href=\"#\" class=\"action-button\">📖 Voir la Solution</a>" +
                               "</div>";
                
                emailService.envoyerEmailAvecTemplate(email, sujet, contenu);
            }
        }
    }
    
    /**
     * Envoi notification clôture de ticket (version avec les deux types d'utilisateurs)
     */
    private void envoyerNotificationClotureTicket(Utilisateur utilisateur, UtilisateurInterne utilisateurInterne, 
                                                 Ticket ticket, Optional<NotificationTemplate> templateOpt) {
        
        String email = utilisateur != null ? utilisateur.getEmail() : 
                      (utilisateurInterne != null ? utilisateurInterne.getEmail() : null);
        String telephone = utilisateur != null ? utilisateur.getTelephone() : 
                          (utilisateurInterne != null ? utilisateurInterne.getTelephone() : null);
        String nom = utilisateur != null ? utilisateur.getNom() : 
                    (utilisateurInterne != null ? utilisateurInterne.getNom() : "Utilisateur");
        
        if (email != null) {
            if (templateOpt.isPresent()) {
                NotificationTemplate template = templateOpt.get();
                emailService.envoyerEmailAvecTemplate(
                    email,
                    template.getSujet().replace("{reference}", ticket.getReference()),
                    template.getContenuHtml()
                        .replace("{reference}", ticket.getReference())
                        .replace("{utilisateur}", nom)
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
                
                emailService.envoyerEmailAvecTemplate(email, sujet, contenu);
            }
        }
    }

        
        // if (utilisateur.getTelephone() != null) {
        //     whatsAppService.envoyerNotificationClotureTicket(
        //         utilisateur.getTelephone(),
        //         ticket.getReference()
        //     );
        // }
    
    
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

    /**
     * Récupère le template associé à un type de notification
     */
    private Optional<NotificationTemplate> getTemplateForNotification(String typeNotificationCode) {
        Optional<TypeNotification> typeOpt = typeNotificationRepository.findByCode(typeNotificationCode);
        if (typeOpt.isPresent() && typeOpt.get().getTemplate() != null) {
            return Optional.of(typeOpt.get().getTemplate());
        }
        return Optional.empty();
    }
}