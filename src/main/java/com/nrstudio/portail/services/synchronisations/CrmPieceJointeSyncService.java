package com.nrstudio.portail.services.synchronisations;

import com.nrstudio.portail.depots.piecesjointes.PieceJointeRepository;
import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.depots.InterventionRepository;
import com.nrstudio.portail.depots.InteractionRepository;
import com.nrstudio.portail.domaine.PieceJointe;
import com.nrstudio.portail.domaine.Ticket;
import com.nrstudio.portail.domaine.Intervention;
import com.nrstudio.portail.domaine.Interaction;
import com.nrstudio.portail.domaine.Utilisateur;
import com.nrstudio.portail.depots.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CrmPieceJointeSyncService {

    private final JdbcTemplate crmJdbc;
    private final PieceJointeRepository pieceJointeRepository;
    private final TicketRepository ticketRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SynchronisationManager synchronisationManager;

    private boolean synchronisationManuelleEnCours = false;

    public CrmPieceJointeSyncService(@Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                                    PieceJointeRepository pieceJointeRepository,
                                    TicketRepository ticketRepository,
                                    UtilisateurRepository utilisateurRepository,
                                    SynchronisationManager synchronisationManager) {
        this.crmJdbc = crmJdbc;
        this.pieceJointeRepository = pieceJointeRepository;
        this.ticketRepository = ticketRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.synchronisationManager = synchronisationManager;
    }

    // Synchronisation planifiée
    //@Scheduled(cron = "${scheduling.crm-piece-jointe-sync-cron:0 0 3 * * *}")
    @Transactional
    public void synchroniserPiecesJointes() {
        System.out.println("🚀 Début de la synchronisation planifiée des pièces jointes");
        synchronisationManuelleEnCours = false;
        executerSynchronisationPlanifiee();
    }

    // Synchronisation manuelle
    @Transactional
    public void synchroniserPiecesJointesManuellement() {
        System.out.println("🚀 Début de la synchronisation manuelle des pièces jointes");
        synchronisationManuelleEnCours = true;
        executerSynchronisationManuelle();
    }

    // private void executerSynchronisationPlanifiee() {
    //     final String sql =
    //         "SELECT Libr_LibraryId, Libr_FileName, Libr_FilePath, Libr_FileSize, " +
    //                "Libr_CaseId, Libr_INTERVENTIONId, libr_communicationId, " +
    //                "Libr_CreatedBy, Libr_CreatedDate, Libr_Note, Libr_Type " +
    //         "FROM dbo.Library " +
    //         "WHERE Libr_FileName IS NOT NULL ";
    //     List<Map<String, Object>> rows = crmJdbc.queryForList(sql);
    //     int nouvellesPiecesJointes = 0;
    //     int misesAJour = 0;
    //     int erreurs = 0;

    //     for (Map<String, Object> row : rows) {
    //         try {
    //             SyncResult result = traiterPieceJointe(row);
    //             if (result == SyncResult.NOUVEAU) {
    //                 nouvellesPiecesJointes++;
    //             } else if (result == SyncResult.MIS_A_JOUR) {
    //                 misesAJour++;
    //             }
    //         } catch (Exception e) {
    //             System.err.println("❌ Erreur lors du traitement de la pièce jointe: " + e.getMessage());
    //             e.printStackTrace();
    //             erreurs++;
    //         }
    //     }
        
    //     if (nouvellesPiecesJointes > 0 || misesAJour > 0 || erreurs > 0) {
    //         System.out.println("✅ Synchronisation planifiée terminée - " + 
    //             nouvellesPiecesJointes + " nouvelle(s), " + 
    //             misesAJour + " mise(s) à jour, " + 
    //             erreurs + " erreur(s)");
    //     }
    // }
    private void executerSynchronisationPlanifiee() {
        final String sql =
            "SELECT Libr_LibraryId, Libr_FileName, Libr_FilePath, Libr_FileSize, " +
                "Libr_CaseId, Libr_INTERVENTIONId, libr_communicationId, " +
                "Libr_CreatedBy, Libr_CreatedDate, Libr_Note, Libr_Type " +
            "FROM dbo.Library " +
            "WHERE Libr_FileName IS NOT NULL ";
        
        List<Map<String, Object>> rows = crmJdbc.queryForList(sql);
        int total = rows.size();
        int nouvellesPiecesJointes = 0;
        int misesAJour = 0;
        int erreurs = 0;
        
        System.out.println("📊 Synchronisation de " + total + " pièces jointes");

        for (int i = 0; i < total; i++) {
            Map<String, Object> row = rows.get(i);
            
            // Afficher le pourcentage tous les 10% ou pour les 10 premiers et derniers
            if (i == 0 || i == total - 1 || (i + 1) % Math.max(1, total / 10) == 0) {
                int pourcentage = (int) ((i + 1) * 100.0 / total);
                System.out.println("📈 Progression: " + pourcentage + "% (" + (i + 1) + "/" + total + ")");
            }
            
            try {
                SyncResult result = traiterPieceJointe(row);
                if (result == SyncResult.NOUVEAU) {
                    nouvellesPiecesJointes++;
                } else if (result == SyncResult.MIS_A_JOUR) {
                    misesAJour++;
                }
            } catch (Exception e) {
                System.err.println("❌ Erreur lors du traitement de la pièce jointe: " + e.getMessage());
                erreurs++;
            }
        }
        
        System.out.println("✅ Synchronisation planifiée terminée - " + 
            nouvellesPiecesJointes + " nouvelle(s), " + 
            misesAJour + " mise(s) à jour, " + 
            erreurs + " erreur(s)");
    }
    private void executerSynchronisationManuelle() {
        final String typeSync = "pieces-jointes";
        
        if (synchronisationManager.estEnCours(typeSync)) {
            throw new IllegalStateException("Une synchronisation des pièces jointes est déjà en cours");
        }

        synchronisationManager.demarrerSynchronisation(typeSync);
        
        Thread syncThread = new Thread(() -> {
            try {
                synchronisationManager.enregistrerThread(typeSync, Thread.currentThread());
                
                final String sql =
                    "SELECT Libr_LibraryId, Libr_FileName, Libr_FilePath, Libr_FileSize, " +
                        "Libr_CaseId, Libr_INTERVENTIONId, libr_communicationId, " +
                        "Libr_CreatedBy, Libr_CreatedDate, Libr_Note, Libr_Type " +
                    "FROM dbo.Library " +
                    "WHERE Libr_FileName IS NOT NULL ";
                
                List<Map<String, Object>> rows = crmJdbc.queryForList(sql);
                int total = rows.size();
                int nouvellesPiecesJointes = 0;
                int misesAJour = 0;
                int erreurs = 0;

                System.out.println("📊 Synchronisation de " + total + " pièces jointes");

                for (int i = 0; i < total; i++) {
                    if (synchronisationManager.doitArreter(typeSync)) {
                        System.out.println("🛑 Synchronisation manuelle des pièces jointes arrêtée à la demande");
                        return;
                    }

                    Map<String, Object> row = rows.get(i);
                    
                    // Afficher le pourcentage tous les 10% ou pour les 10 premiers et derniers
                    if (i == 0 || i == total - 1 || (i + 1) % Math.max(1, total / 10) == 0) {
                        int pourcentage = (int) ((i + 1) * 100.0 / total);
                        System.out.println("📈 Progression: " + pourcentage + "% (" + (i + 1) + "/" + total + ")");
                    }

                    try {
                        SyncResult result = traiterPieceJointe(row);
                        if (result == SyncResult.NOUVEAU) {
                            nouvellesPiecesJointes++;
                        } else if (result == SyncResult.MIS_A_JOUR) {
                            misesAJour++;
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Erreur lors du traitement de la pièce jointe");
                        erreurs++;
                    }
                    
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        System.out.println("🛑 Synchronisation manuelle interrompue");
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                
                System.out.println("✅ Synchronisation manuelle terminée - " + 
                    nouvellesPiecesJointes + " nouvelle(s), " + 
                    misesAJour + " mise(s) à jour, " + 
                    erreurs + " erreur(s)");
                
            } catch (Exception e) {
                System.err.println("❌ Erreur lors de la synchronisation manuelle des pièces jointes: " + e.getMessage());
            } finally {
                synchronisationManager.supprimerThread(typeSync);
                synchronisationManuelleEnCours = false;
            }
        });
        
        syncThread.start();
    }
    private enum SyncResult {
        NOUVEAU,
        MIS_A_JOUR,
        EXISTANT,
        IGNORE
    }

    private SyncResult traiterPieceJointe(Map<String, Object> row) {
        Integer libraryId = toInt(row.get("Libr_LibraryId"));
        String nomFichier = toString(row.get("Libr_FileName"));
        String cheminFichier = toString(row.get("Libr_FilePath"));
        Long tailleOctets = toLong(row.get("Libr_FileSize"));
        Integer caseId = toInt(row.get("Libr_CaseId"));
        Integer interventionId = toInt(row.get("Libr_INTERVENTIONId"));
        Integer communicationId = toInt(row.get("libr_communicationId"));
        Integer createdBy = toInt(row.get("Libr_CreatedBy"));
        LocalDateTime createdDate = toLocalDateTime(row.get("Libr_CreatedDate"));
        String commentaires = toString(row.get("Libr_Note"));
        String typeMime = toString(row.get("Libr_Type"));

        if (libraryId == null || nomFichier == null) {
            System.out.println("⚠️ Pièce jointe ignorée - ID ou nom de fichier manquant: LibraryId=" + libraryId + ", NomFichier=" + nomFichier);
            return SyncResult.IGNORE;
        }

        // Vérifier si la pièce jointe existe déjà via son ID externe CRM
        Optional<PieceJointe> pieceJointeExistante = 
            pieceJointeRepository.findByIdExterneCrm(libraryId.toString());

        PieceJointe pieceJointe;
        boolean estNouveau = false;

        if (pieceJointeExistante.isPresent()) {
            pieceJointe = pieceJointeExistante.get();
            System.out.println("📝 Pièce jointe existante trouvée - ID: " + libraryId + ", Nom: " + nomFichier);
            
            // Vérifier si une mise à jour est nécessaire
            boolean besoinMiseAJour = !nomFichier.equals(pieceJointe.getNomFichier()) ||
                !equalsSafe(cheminFichier, pieceJointe.getCheminFichier()) ||
                !equalsSafe(tailleOctets, pieceJointe.getTailleOctets()) ||
                !equalsSafe(typeMime, pieceJointe.getTypeMime()) ||
                !equalsSafe(commentaires, pieceJointe.getCommentaires());

            if (besoinMiseAJour) {
                pieceJointe.setNomFichier(nomFichier);
                pieceJointe.setCheminFichier(cheminFichier);
                pieceJointe.setTailleOctets(tailleOctets);
                pieceJointe.setTypeMime(typeMime);
                pieceJointe.setCommentaires(commentaires);
                
                pieceJointeRepository.save(pieceJointe);
                System.out.println("🔄 Pièce jointe mise à jour - ID: " + libraryId);
                return SyncResult.MIS_A_JOUR;
            }
            return SyncResult.EXISTANT;
        } else {
            // Créer une nouvelle pièce jointe
            pieceJointe = new PieceJointe();
            pieceJointe.setIdExterneCrm(libraryId.toString());
            estNouveau = true;
            System.out.println("🆕 Nouvelle pièce jointe détectée - ID: " + libraryId + ", Nom: " + nomFichier);
        }

        // Définir les propriétés de base
        pieceJointe.setNomFichier(nomFichier);
        pieceJointe.setCheminFichier(cheminFichier);
        pieceJointe.setTailleOctets(tailleOctets);
        pieceJointe.setTypeMime(typeMime);
        pieceJointe.setCommentaires(commentaires);
        pieceJointe.setDateAjout(createdDate != null ? createdDate : LocalDateTime.now());

        // Gérer l'utilisateur qui a ajouté la pièce jointe
        if (createdBy != null) {
            // Si vous avez un repository Utilisateur avec id_externe_crm
            Optional<Utilisateur> utilisateur = utilisateurRepository.findByIdExterneCrm(createdBy.toString());
            if (utilisateur.isPresent()) {
                pieceJointe.setAjouteParUtilisateurId(utilisateur.get().getId());
            } else {
                System.out.println("⚠️ Utilisateur non trouvé avec id_externe_crm: " + createdBy);
                pieceJointe.setAjouteParUtilisateurId(null);
            }
        } else {
            pieceJointe.setAjouteParUtilisateurId(null);
        }

        // Lier au ticket si disponible
        if (caseId != null) {
            Optional<Ticket> ticket = ticketRepository.findByIdExterneCrm(caseId);
            if (ticket.isPresent()) {
                pieceJointe.setTicketId(ticket.get().getId());
                System.out.println("🔗 Pièce jointe liée au ticket ID: " + ticket.get().getId());
            } else {
                System.out.println("⚠️ Ticket non trouvé avec id_externe_crm: " + caseId);
                pieceJointe.setTicketId(null);
            }
        } else {
            pieceJointe.setTicketId(null);
        }

        // Construire l'URL du contenu si le chemin est disponible
        if (cheminFichier != null && !cheminFichier.isEmpty()) {
            String urlContenu = construireUrlContenu(cheminFichier, nomFichier);
            pieceJointe.setUrlContenu(urlContenu);
        } else {
            pieceJointe.setUrlContenu(null);
        }

        pieceJointeRepository.save(pieceJointe);

        if (estNouveau) {
            System.out.println("✅ Pièce jointe créée - ID CRM: " + libraryId + ", Fichier: " + nomFichier);
            return SyncResult.NOUVEAU;
        }

        return SyncResult.EXISTANT;
    }

    private String construireUrlContenu(String cheminFichier, String nomFichier) {
        // Implémentez la logique pour construire l'URL d'accès au fichier
        // Cela dépend de votre système de stockage (système de fichiers, S3, etc.)
        
        // Exemple basique :
        if (cheminFichier.startsWith("http")) {
            return cheminFichier;
        } else {
            // Adapter selon votre configuration
            // Vous pouvez utiliser le libraryId pour construire une URL cohérente
            return "/api/fichiers/" + cheminFichier;
        }
    }

    // Méthodes utilitaires pour la conversion des types
    private Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.valueOf(o.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        try {
            return Long.valueOf(o.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private String toString(Object o) {
        if (o == null) return null;
        return o.toString().trim();
    }

    private LocalDateTime toLocalDateTime(Object o) {
        if (o == null) return null;
        if (o instanceof LocalDateTime) return (LocalDateTime) o;
        if (o instanceof java.sql.Timestamp) {
            return ((java.sql.Timestamp) o).toLocalDateTime();
        }
        if (o instanceof java.util.Date) {
            return new java.sql.Timestamp(((java.util.Date) o).getTime()).toLocalDateTime();
        }
        try {
            return LocalDateTime.parse(o.toString().replace(" ", "T"));
        } catch (Exception e) {
            System.err.println("❌ Erreur conversion date: " + o + " - " + e.getMessage());
            return null;
        }
    }

    private boolean equalsSafe(Object o1, Object o2) {
        if (o1 == null && o2 == null) return true;
        if (o1 == null || o2 == null) return false;
        return o1.equals(o2);
    }
}