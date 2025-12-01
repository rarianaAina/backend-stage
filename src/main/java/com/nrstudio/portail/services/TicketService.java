package com.nrstudio.portail.services;

import com.nrstudio.portail.depots.CompanyRepository;
import com.nrstudio.portail.depots.ProduitRepository;
import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.depots.UtilisateurRepository;
import com.nrstudio.portail.depots.piecesjointes.PieceJointeRepository;
import com.nrstudio.portail.domaine.Company;
import com.nrstudio.portail.domaine.PieceJointe;
import com.nrstudio.portail.domaine.Produit;
import com.nrstudio.portail.domaine.Ticket;
import com.nrstudio.portail.domaine.Utilisateur;
import com.nrstudio.portail.dto.TicketAvecProduitDto;
import com.nrstudio.portail.dto.TicketAvecProduitPageReponse;
import com.nrstudio.portail.dto.TicketCreationRequete;
import com.nrstudio.portail.dto.TicketPageReponse;
import com.nrstudio.portail.services.notification.EmailNotificationService;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class TicketService {

  @Value("${app.upload.dir:uploads/fichiers}")
  private String uploadDir;
  
  private final TicketRepository tickets;
  private final JdbcTemplate crmJdbc;
  private final EmailNotificationService emailService;
  private final WhatsAppNotificationService whatsAppService;
  private final CompanyRepository companies;
  private final UtilisateurRepository utilisateurs;
  private final ProduitRepository produitRepository;
  private final NotificationWorkflowService notificationWorkflowService;
  private final PieceJointeRepository pieceJointeRepository;

  public TicketService(TicketRepository tickets,
                       @Qualifier("crmJdbc") JdbcTemplate crmJdbc,
                       EmailNotificationService emailService,
                       WhatsAppNotificationService whatsAppService,
                       CompanyRepository companies,
                       UtilisateurRepository utilisateurs,
                       ProduitRepository produitRepository,
                       NotificationWorkflowService notificationWorkflowService,
                       PieceJointeRepository pieceJointeRepository) {
    this.tickets = tickets;
    this.crmJdbc = crmJdbc;
    this.emailService = emailService;
    this.whatsAppService = whatsAppService;
    this.companies = companies;
    this.utilisateurs = utilisateurs;
    this.produitRepository = produitRepository;
    this.notificationWorkflowService = notificationWorkflowService;
    this.pieceJointeRepository = pieceJointeRepository;
  }

// @Transactional
// public Ticket creerEtSynchroniser(TicketCreationRequete r) {

//     // Pour le produit
//     String produitIdExterneCrm = String.valueOf(r.getProduitId());
//     Optional<Produit> produitOpt = produitRepository.findByIdExterneCrm(produitIdExterneCrm);
    
//     Integer produitIdLocal;
//     if (produitOpt.isPresent()) {
//         produitIdLocal = produitOpt.get().getId();
//     } else {
//         throw new IllegalArgumentException("Produit introuvable pour idExterneCrm: " + produitIdExterneCrm);
//     }

//     // Pour le client - récupérer l'idExterneCrm à partir de l'ID local
//     Integer clientIdLocal = r.getClientId();
//     Optional<Utilisateur> utilisateurOpt = utilisateurs.findById(clientIdLocal); // Chercher par ID local
    
//     String clientIdExterne;
//     if (utilisateurOpt.isPresent()) {
//         clientIdExterne = utilisateurOpt.get().getIdExterneCrm();
//         // Si vous voulez un Integer, convertissez-le
//         // clientIdExterne = Integer.valueOf(utilisateurOpt.get().getIdExterneCrm());
//     } else {
//         throw new IllegalArgumentException("Utilisateur introuvable pour id: " + clientIdLocal);
//     }

//     Ticket t = new Ticket();
//     t.setCompanyId(r.getCompanyId());
//     t.setClientId(Integer.valueOf(clientIdExterne)); // ou Integer.valueOf(clientIdExterne) si besoin
//     t.setProduitId(produitIdLocal);
//     t.setTypeTicketId(r.getTypeTicketId());
//     t.setPrioriteTicketId(r.getPrioriteTicketId());
//     t.setStatutTicketId(1);
//     t.setTitre(r.getRaison());
//     t.setDescription(r.getDescription());
//     t.setRaison(r.getRaison());
//     t.setPolitiqueAcceptee(true);
//     t.setCreeParUtilisateurId(r.getCreeParUtilisateurId());
//     t.setAffecteAUtilisateurId(r.getAffecteAUtilisateurId());
//     t.setDateCreation(LocalDateTime.now());
//     t.setDateMiseAJour(LocalDateTime.now());

//     t.setReference("TCK-" + System.currentTimeMillis());
//     t = tickets.save(t);

//     // 2) Créer le Case dans le CRM (dbo.Cases)
//     String caseDescription = truncate(t.getTitre(), 40);
//     String caseProblemNote = t.getDescription() != null ? t.getDescription() : "";
//     String casePriority = mapPrioriteIdToCrmString(t.getPrioriteTicketId()); 
//     String caseStatus   = mapStatutIdToCrmString(t.getStatutTicketId());     
//     String caseProduct  = mapProduitIdToCrmString(t.getProduitId());
//     System.out.println("Mapping produitId " + t.getProduitId() + " to CRM product: " + caseProduct); 
//     String caseStage = "Logged";
//     String caseSource = "Portail";
//     Integer caseCreatedBy = 2074;
//     String caseReferenceId = 
//     // Company côté CRM
//     Integer crmCompanyId = mapCompanyIdToCrmCompanyId(t.getCompanyId());

//     Integer caseId = crmJdbc.queryForObject(
//       "INSERT INTO dbo.Cases " +
//       " (Case_PrimaryCompanyId, Case_PrimaryPersonId, Case_Description, Case_ProblemNote, Case_Priority, Case_CreatedDate, Case_Status, Case_Stage, " +
//       "  Case_PARCId, Case_CreatedBy, Case_Source, Case_CustomerRef, Case_ReferenceId) " +
//       " VALUES (?,?,?,?,?,GETDATE(),?,?,?,?,?,?,?) ; " +
//       " SELECT CAST(SCOPE_IDENTITY() AS INT);",
//       Integer.class,
//       crmCompanyId, clientIdExterne, caseDescription, caseProblemNote, casePriority, caseStatus, caseStage,
//       caseProduct, caseCreatedBy, caseSource, t.getReference()
//     );

//     if (caseId != null) {
//       t.setIdExterneCrm(caseId);
//       t.setDateMiseAJour(LocalDateTime.now());
//       t = tickets.save(t);
//     }

//     envoyerNotificationsCreation(t);

//     return t;
// }

@Transactional
public Ticket creerEtSynchroniser(TicketCreationRequete r) {

    // Pour le produit
    String produitIdExterneCrm = String.valueOf(r.getProduitId());
    Optional<Produit> produitOpt = produitRepository.findByIdExterneCrm(produitIdExterneCrm);
    
    Integer produitIdLocal;
    if (produitOpt.isPresent()) {
        produitIdLocal = produitOpt.get().getId();
    } else {
        throw new IllegalArgumentException("Produit introuvable pour idExterneCrm: " + produitIdExterneCrm);
    }

    // Pour le client - récupérer l'idExterneCrm à partir de l'ID local
    Integer clientIdLocal = r.getClientId();
    Optional<Utilisateur> utilisateurOpt = utilisateurs.findById(clientIdLocal); // Chercher par ID local
    
    String clientIdExterne;
    if (utilisateurOpt.isPresent()) {
        clientIdExterne = utilisateurOpt.get().getIdExterneCrm();
    } else {
        throw new IllegalArgumentException("Utilisateur introuvable pour id: " + clientIdLocal);
    }
    String caseReferenceId = genererNouveauCaseReferenceId();

    Ticket t = new Ticket();
    t.setCompanyId(r.getCompanyId());
    t.setClientId(Integer.valueOf(clientIdExterne));
    t.setProduitId(produitIdLocal);
    t.setTypeTicketId(r.getTypeTicketId());
    t.setPrioriteTicketId(r.getPrioriteTicketId());
    t.setStatutTicketId(1);
    t.setTitre(r.getRaison());
    t.setDescription(r.getDescription());
    t.setRaison(r.getRaison());
    t.setPolitiqueAcceptee(true);
    t.setCreeParUtilisateurId(r.getCreeParUtilisateurId());
    t.setAffecteAUtilisateurId(r.getAffecteAUtilisateurId());
    t.setDateCreation(LocalDateTime.now());
    t.setDateMiseAJour(LocalDateTime.now());

    t.setReference("TCK-" + System.currentTimeMillis());
    t.setReferenceId(caseReferenceId);
    t = tickets.save(t);

    // 2) Créer le Case dans le CRM (dbo.Cases)
    String caseDescription = truncate(t.getTitre(), 40);
    String caseProblemNote = t.getDescription() != null ? t.getDescription() : "";
    String casePriority = mapPrioriteIdToCrmString(t.getPrioriteTicketId()); 
    String caseStatus   = "In progress ";     
    String caseProduct  = mapProduitIdToCrmString(t.getProduitId());
    System.out.println("Mapping produitId " + t.getProduitId() + " to CRM product: " + caseProduct); 
    String caseStage = "Logged";
    String caseSource = "Portail";
    Integer caseCreatedBy = 2074;
    String caseTargetVer = "CaseTargetVer2";
    String caseFoundVer = "CaseFoundVer1";
    Integer caseSecTerr = -2147483640;
    String caseObject = caseProblemNote;
    // Générer le Case_ReferenceId automatiquement

    Integer crmCompanyId = mapCompanyIdToCrmCompanyId(t.getCompanyId());
    Integer caseId = crmJdbc.queryForObject(
      "INSERT INTO dbo.Cases " +
      " (Case_PrimaryCompanyId, Case_PrimaryPersonId, Case_Description, Case_ProblemNote, Case_Priority, Case_CreatedDate, Case_Status, Case_Stage, " +
      "  Case_PARCId, Case_CreatedBy, Case_Source, Case_Object, Case_TargetVer, Case_FoundVer, Case_SecTerr, Case_CustomerRef, Case_ReferenceId) " +
      " VALUES (?,?,?,?,?,GETDATE(),?,?,?,?,?,?,?,?,?,?,?) ; " +
      " SELECT CAST(SCOPE_IDENTITY() AS INT);",
      Integer.class,
      crmCompanyId, clientIdExterne, caseDescription, caseProblemNote, casePriority, caseStatus, caseStage,
      caseProduct, caseCreatedBy, caseSource, caseObject, caseTargetVer, caseFoundVer, caseSecTerr, t.getReference(), caseReferenceId
    );

    if (caseId != null) {
      t.setIdExterneCrm(caseId);
      t.setDateMiseAJour(LocalDateTime.now());
      t = tickets.save(t);
    }
    System.out.println(r.getFichiers().isEmpty());
    if (r.getFichiers() != null && !r.getFichiers().isEmpty()) {
      System.out.println("Ato");
      System.out.println(r.getFichiers());
      System.out.println(clientIdExterne);
        sauvegarderFichiers(r.getFichiers(), t.getId(), Integer.parseInt(clientIdExterne));
    }

    envoyerNotificationsCreation(t);

    return t;
}

private void sauvegarderFichiers(List<MultipartFile> fichiers, Integer ticketId, Integer utilisateurId) {
    System.out.println("=== DÉBUT SAUVEGARDE FICHIERS ===");
    System.out.println("Nombre de fichiers reçus: " + fichiers.size());
    
    Ticket ticket = tickets.findById(ticketId)
        .orElseThrow(() -> new IllegalArgumentException("Ticket introuvable"));
    for (int i = 0; i < fichiers.size(); i++) {
        MultipartFile fichier = fichiers.get(i);
        System.out.println("Fichier " + i + ":");
        System.out.println("  - Nom original: '" + fichier.getOriginalFilename() + "'");
        System.out.println("  - Taille: " + fichier.getSize());
        System.out.println("  - Type MIME: " + fichier.getContentType());
        System.out.println("  - Nom: " + fichier.getName());
        System.out.println("  - Vide: " + fichier.isEmpty());
        System.out.println("  - Resource: " + fichier.getResource());
        
        // Vérification plus détaillée
        if (fichier.isEmpty()) {
            System.out.println("  → Fichier vide, ignoré");
            continue;
        }
        
        if (fichier.getOriginalFilename() == null || fichier.getOriginalFilename().trim().isEmpty()) {
            System.out.println("  → Fichier sans nom, ignoré");
            continue;
        }
        
        try {
            // Vérifier si le fichier a du contenu
            byte[] bytes = fichier.getBytes();
            System.out.println("  - Bytes lus: " + bytes.length);
            
            if (bytes.length == 0) {
                System.out.println("  → Fichier sans contenu, ignoré");
                continue;
            }
            
            // Générer un nom de fichier unique
            String nomOriginal = fichier.getOriginalFilename();
            String nomFichier = genererNomFichierUnique(nomOriginal);
            
            // Utiliser le chemin configuré
            String cheminComplet = uploadDir + File.separator + nomFichier;
            
            // Créer le dossier s'il n'existe pas
            File dossier = new File(uploadDir);
            if (!dossier.exists()) {
                boolean created = dossier.mkdirs();
                System.out.println("  - Dossier créé: " + created + " - Chemin: " + dossier.getAbsolutePath());
            }
            
            // Sauvegarder le fichier sur le disque
            Path chemin = Paths.get(cheminComplet);
            Files.copy(fichier.getInputStream(), chemin, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("  - Fichier sauvegardé: " + cheminComplet);
            
            // Créer l'entité PieceJointe
            PieceJointe pieceJointe = new PieceJointe();
            pieceJointe.setNomFichier(nomOriginal);
            pieceJointe.setCheminFichier(cheminComplet);
            pieceJointe.setUrlContenu("/api/fichiers/" + nomFichier);
            pieceJointe.setTypeMime(fichier.getContentType());
            pieceJointe.setTailleOctets(fichier.getSize());
            pieceJointe.setAjouteParUtilisateurId(utilisateurId);
            pieceJointe.setTicketId(ticketId);
            pieceJointe.setDateAjout(LocalDateTime.now());
            
            // Sauvegarder en base
            PieceJointe saved = pieceJointeRepository.save(pieceJointe);
            System.out.println("  → PieceJointe sauvegardée en base avec ID: " + saved.getId());
            insererDansLibraryCRM(fichier, ticket, utilisateurId, nomFichier, cheminComplet);
        } catch (IOException e) {
            System.err.println("  → Erreur lors de la sauvegarde du fichier: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("  → Erreur inattendue: " + e.getMessage());
            e.printStackTrace();
        }
    }
    System.out.println("=== FIN SAUVEGARDE FICHIERS ===");
}

  private void insererDansLibraryCRM(MultipartFile fichier, Ticket ticket, Integer utilisateurId, 
                                    String nomFichierSauvegarde, String cheminComplet) {
      try {
          // Vérifier que le ticket a un ID externe CRM
          if (ticket.getIdExterneCrm() == null) {
              System.out.println("⚠️ Ticket sans ID externe CRM, impossible d'insérer dans Library");
              return;
          }
          
          // Préparer les valeurs pour l'insertion
          String nomFichierOriginal = fichier.getOriginalFilename();
          String typeMime = fichier.getContentType();
          Long tailleFichier = fichier.getSize();
          Integer caseId = ticket.getIdExterneCrm(); // L'ID du case dans le CRM
          Integer createdBy = 2074; // ID utilisateur CRM par défaut (à adapter)
          
          // Valeurs pour les nouvelles colonnes
          String entity = "Case";
          Integer secTerr = -2147483640;
          String global = "N";
          String mergetemplate = "N";
          String category = "Sales";
          String librType = "Proposal";
          String status = "Final";
          //String active = "Y";
          //String librPrivate = "N";
          //Integer deleted = 0;
          
          // Insérer dans la table Library avec TOUTES les colonnes
          String sql = 
              "INSERT INTO dbo.Library (" +
              "Libr_FileName, Libr_FilePath, Libr_FileSize, Libr_CaseId, " +
              "Libr_CreatedBy, Libr_Entity, Libr_CreatedDate, Libr_Type, Libr_Note, " +
              "Libr_Status, Libr_Global, Libr_Category, Libr_SecTerr, " +
              "Libr_Mergetemplate" +
              ") VALUES (?, ?, ?, ?, ?, ?, GETDATE(), ?, ?, ?, ?, ?, ?, ?)";
          
          int rowsAffected = crmJdbc.update(sql,
              nomFichierOriginal,        // Libr_FileName
              cheminComplet,             // Libr_FilePath
              tailleFichier,             // Libr_FileSize  
              caseId,                    // Libr_CaseId (liaison avec le ticket)
              createdBy,                 // Libr_CreatedBy
              entity,                    // Libr_Entity
              librType,                  // Libr_Type
              "Document attaché via Portail Client",  // Libr_Note
              status,                    // Libr_Status
              //active,                    // Libr_Active
              global,                    // Libr_Global
              category,                  // Libr_Category
              secTerr,                   // Libr_SecTerr
              mergetemplate             // Libr_Mergetemplate
              //librPrivate,               // Libr_Private
              //deleted                    // Libr_Deleted
          );
          
          if (rowsAffected > 0) {
              System.out.println("✅ Fichier inséré dans Library CRM - CaseId: " + caseId + ", Fichier: " + nomFichierOriginal);
          } else {
              System.out.println("❌ Échec de l'insertion dans Library CRM");
          }
          
      } catch (Exception e) {
          System.err.println("❌ Erreur lors de l'insertion dans Library CRM: " + e.getMessage());
          e.printStackTrace();
      }
  }

  private String determinerTypeDocument(String nomFichier, String typeMime) {
    if (nomFichier == null) return "Document";
    
    String extension = "";
    if (nomFichier.contains(".")) {
        extension = nomFichier.substring(nomFichier.lastIndexOf(".") + 1).toLowerCase();
    }
    
    switch (extension) {
        case "pdf":
            return "PDF";
        case "doc":
        case "docx":
            return "Word";
        case "xls":
        case "xlsx":
            return "Excel";
        case "jpg":
        case "jpeg":
        case "png":
        case "gif":
            return "Image";
        case "txt":
            return "Text";
        default:
            return "Document";
    }
}
    private String genererNomFichierUnique(String nomOriginal) {
        if (nomOriginal == null || nomOriginal.isEmpty()) {
            nomOriginal = "fichier";
        }
        
        String timestamp = String.valueOf(System.currentTimeMillis());
        String extension = "";
        
        if (nomOriginal.contains(".")) {
            extension = nomOriginal.substring(nomOriginal.lastIndexOf("."));
        }
        
        // Nettoyer le nom de fichier
        String nomBase = nomOriginal.replaceAll("[^a-zA-Z0-9.-]", "_");
        if (nomBase.length() > 100) {
            nomBase = nomBase.substring(0, 100);
        }
        
        return timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }

private String determinerCheminStockage() {
    // Vous pouvez configurer cela dans application.properties
    return "uploads/fichiers"; // Chemin relatif ou absolu
}
// Méthode pour générer le nouveau Case_ReferenceId
private String genererNouveauCaseReferenceId() {
    try {
        // Récupérer le dernier Case_ReferenceId de la table dbo.Cases
        String dernierReferenceId = crmJdbc.queryForObject(
            "SELECT TOP 1 Case_ReferenceId FROM dbo.Cases WHERE Case_ReferenceId LIKE '2074-%' ORDER BY Case_CaseId DESC",
            String.class
        );
        
        if (dernierReferenceId == null) {
            // Si aucun enregistrement n'existe, commencer à 2074-11300
            return "2074-11300";
        }
        
        // Extraire la partie numérique après le "2074-"
        String[] parties = dernierReferenceId.split("-");
        if (parties.length == 2) {
            try {
                int dernierNumero = Integer.parseInt(parties[1]);
                int nouveauNumero = dernierNumero + 1;
                return "2074-" + nouveauNumero;
            } catch (NumberFormatException e) {
                // En cas d'erreur de format, retourner une valeur par défaut
                System.err.println("Erreur de format du Case_ReferenceId: " + dernierReferenceId);
                return "2074-11300";
            }
        } else {
            // Format inattendu, retourner une valeur par défaut
            System.err.println("Format inattendu du Case_ReferenceId: " + dernierReferenceId);
            return "2074-11300";
        }
    } catch (Exception e) {
        System.err.println("Erreur lors de la génération du Case_ReferenceId: " + e.getMessage());
        // En cas d'erreur, retourner une valeur basée sur le timestamp
        return "2074-" + (11300 + (System.currentTimeMillis() % 1000));
    }
}
  @Transactional
  public Ticket changerStatut(Integer ticketId, Integer nouveauStatutId, Integer utilisateurId) {
    Ticket t = tickets.findById(ticketId)
      .orElseThrow(() -> new IllegalArgumentException("Ticket introuvable"));

    Integer ancienStatutId = t.getStatutTicketId();
    t.setStatutTicketId(nouveauStatutId);
    t.setDateMiseAJour(LocalDateTime.now());

    if (nouveauStatutId == 4) {
      t.setDateCloture(LocalDateTime.now());
      t.setClotureParUtilisateurId(utilisateurId);
    }

    t = tickets.save(t);

    if (t.getIdExterneCrm() != null) {
      String nouveauStatutCrm = mapStatutIdToCrmString(nouveauStatutId);
      crmJdbc.update(
        "UPDATE dbo.Cases SET Case_Status = ?, Case_Closed = ? WHERE Case_CaseId = ?",
        nouveauStatutCrm,
        nouveauStatutId == 4 ? LocalDateTime.now() : null,
        t.getIdExterneCrm()
      );
    }

    envoyerNotificationsChangementStatut(t, ancienStatutId, nouveauStatutId);

    return t;
  }

  @Transactional
  public List<Ticket> listerTicketsCompany(Integer companyId) {
    return tickets.findAll().stream()
      .filter(ticket -> ticket.getCompanyId().equals(companyId))
      .toList();
  }

  //Tickets par utilisateur
  public List<Ticket> listerTicketsUtilisateur(Integer utilisateurId) {
    return tickets.findAll().stream()
      .filter(ticket -> utilisateurId.equals(ticket.getCreeParUtilisateurId()))
      .toList();
  }

  @Transactional
  public List<Ticket> listerTicketsConsultant(Integer consultantId) {
    return tickets.findAll().stream()
      .filter(ticket -> consultantId.equals(ticket.getAffecteAUtilisateurId()))
      .toList();
  }

  //Ticket par utilisateur avec pagination
  // public List<Ticket> listerTicketsUtilisateurAvecPagination(Integer utilisateurId, int page, int size) {
  //   return tickets.findAll().stream()
  //     .filter(ticket -> utilisateurId.equals(ticket.getCreeParUtilisateurId()))
  //     .skip(page * size)
  //     .limit(size)
  //     .toList();
  // }

  public List<Ticket> listerTicketsUtilisateurAvecPagination(Integer utilisateurId, int page, int size) {
      // trouver l'id_externe_crm et non l'utilisateur par rapport à utilisateurId
      Utilisateur utilisateur = utilisateurs.findById(utilisateurId)
          .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
      Integer utilisateurIdClient = utilisateur.getIdExterneCrm() != null ? Integer.valueOf(utilisateur.getIdExterneCrm()) : null;  
    return tickets.findAll().stream()
      .filter(ticket -> utilisateurIdClient.equals(ticket.getClientId()))
      .skip(page * size)
      .limit(size)
      .toList();
  }

public TicketAvecProduitPageReponse listerTicketsUtilisateurAvecPaginationEtFiltres(
    Integer utilisateurId,
    int page,
    int size,
    String statutTicketIdStr,
    String reference,
    String produitIdStr,
    String dateDebut,
    String dateFin) {

    Utilisateur utilisateur = utilisateurs.findById(utilisateurId)
        .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
    Integer utilisateurIdClient = utilisateur.getIdExterneCrm() != null ? 
        Integer.valueOf(utilisateur.getIdExterneCrm()) : null;

    Stream<Ticket> ticketStream = tickets.findAll().stream()
        .filter(ticket -> utilisateurIdClient != null && 
                         utilisateurIdClient.equals(ticket.getClientId()));

    // Filtre statut
    if (statutTicketIdStr != null && !statutTicketIdStr.isEmpty()) {
      try {
        Integer statutTicketId = Integer.valueOf(statutTicketIdStr);
        ticketStream = ticketStream.filter(ticket -> 
            statutTicketId.equals(ticket.getStatutTicketId()));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("statutTicketId invalide : " + statutTicketIdStr);
      }
    }

    // Filtre référence
    if (reference != null && !reference.isEmpty()) {
      ticketStream = ticketStream.filter(ticket -> 
          ticket.getReference() != null &&
          ticket.getReference().toLowerCase().contains(reference.toLowerCase()));
    }

    // Filtre produit - VÉRIFICATION AMÉLIORÉE
    if (produitIdStr != null && !produitIdStr.isEmpty()) {
      try {
        Integer produitId = Integer.valueOf(produitIdStr);
        
        // Vérifier que le produit existe
        Optional<Produit> produitOpt = produitRepository.findByIdExterneCrm(produitIdStr);
        System.out.println("Produit id str " + produitIdStr);
        System.out.println("Id produit" + produitOpt.get().getId());
        if (produitOpt.isEmpty()) {
          throw new IllegalArgumentException("Produit introuvable avec ID: " + produitId);
        }
        
        ticketStream = ticketStream.filter(ticket -> 
            ticket.getProduitId() != null &&
            ticket.getProduitId().equals(produitOpt.get().getId()));
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("produitId invalide : " + produitIdStr);
      }
    }

    // Filtres dates (garder votre code existant)
    if (dateDebut != null && !dateDebut.isEmpty()) {
      LocalDate debut = LocalDate.parse(dateDebut);
      ticketStream = ticketStream.filter(ticket -> {
        if (ticket.getDateCreation() == null) return false;
        return !ticket.getDateCreation().toLocalDate().isBefore(debut);
      });
    }

    if (dateFin != null && !dateFin.isEmpty()) {
      LocalDate fin = LocalDate.parse(dateFin);
      ticketStream = ticketStream.filter(ticket -> {
        if (ticket.getDateCreation() == null) return false;
        return !ticket.getDateCreation().toLocalDate().isAfter(fin);
      });
    }

    // Pagination et conversion
    List<TicketAvecProduitDto> ticketDtos = ticketStream
        .sorted((t1, t2) -> t2.getDateCreation().compareTo(t1.getDateCreation())) // Tri par date décroissante
        .skip((long) page * size)
        .limit(size)
        .map(ticket -> convertirEnAvecProduitDto(ticket))
        .toList();

    long totalElements = countTicketsUtilisateurAvecFiltres(utilisateurId, statutTicketIdStr, reference, produitIdStr, dateDebut, dateFin);
    int totalPages = (int) Math.ceil((double) totalElements / size);

    return new TicketAvecProduitPageReponse(ticketDtos, page, totalPages, totalElements, size);
}

// Méthode utilitaire pour convertir Ticket en TicketAvecProduitDto
private TicketAvecProduitDto convertirEnAvecProduitDto(Ticket ticket) {
    TicketAvecProduitDto dto = new TicketAvecProduitDto();
    dto.setId(ticket.getId().toString());
    dto.setReference(ticket.getReference());
    dto.setProduitId(ticket.getProduitId());
    dto.setDescription(ticket.getDescription());
    dto.setTitre(ticket.getTitre());
    dto.setPrioriteTicketId(ticket.getPrioriteTicketId().toString());
    dto.setDateCreation(ticket.getDateCreation());
    dto.setDateCloture(ticket.getDateCloture());
    dto.setEtat(ticket.getStatutTicketId().toString()); // Adaptez selon votre logique d'état
    dto.setCompanyName(ticket.getCompanyId() != null ? 
        companies.findById(ticket.getCompanyId())
                 .map(Company::getNom)
                 .orElse("Entreprise inconnue") 
        : "Entreprise inconnue");

    
    
    // Récupérer le nom du produit
    String produitNom = "Produit inconnu";
    if (ticket.getProduitId() != null) {
        Optional<Produit> produit = produitRepository.findById(ticket.getProduitId());
        if (produit.isPresent()) {
            // Adaptez selon le champ qui contient le nom dans votre entité Produit
            produitNom = produit.get().getCodeProduit(); // ou getLibelle(), getDescription(), etc.
        }
    }
    dto.setProduitNom(produitNom);
    
    return dto;
}
  // Méthode pour obtenir le nombre total de tickets (pour la pagination)
  public long countTicketsUtilisateurAvecFiltres(
    Integer utilisateurId,
    String statutTicketIdStr,
    String reference,
    String produitIdStr,
    String dateDebut,
    String dateFin) {

  Utilisateur utilisateur = utilisateurs.findById(utilisateurId)
      .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable"));
  Integer utilisateurIdClient = utilisateur.getIdExterneCrm() != null ? Integer.valueOf(utilisateur.getIdExterneCrm()) : null;

  Stream<Ticket> ticketStream = tickets.findAll().stream()
      .filter(ticket -> utilisateurIdClient.equals(ticket.getClientId()));

  // Filtres
  if (statutTicketIdStr != null && !statutTicketIdStr.isEmpty()) {
    try {
      Integer statutTicketId = Integer.valueOf(statutTicketIdStr);
      ticketStream = ticketStream.filter(ticket -> statutTicketId.equals(ticket.getStatutTicketId()));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("statutTicketId invalide : " + statutTicketIdStr);
    }
  }

  if (reference != null && !reference.isEmpty()) {
    ticketStream = ticketStream.filter(ticket ->
        ticket.getReference() != null &&
        ticket.getReference().toLowerCase().contains(reference.toLowerCase()));
  }

  if (produitIdStr != null && !produitIdStr.isEmpty()) {
    try {
      Integer produitId = Integer.valueOf(produitIdStr);
      ticketStream = ticketStream.filter(ticket ->
          ticket.getProduitId() != null &&
          ticket.getProduitId().equals(produitId));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("produitId invalide : " + produitIdStr);
    }
  }

  if (dateDebut != null && !dateDebut.isEmpty()) {
    LocalDate debut = LocalDate.parse(dateDebut);
    ticketStream = ticketStream.filter(ticket -> {
      if (ticket.getDateCreation() == null) return false;
      return !ticket.getDateCreation().toLocalDate().isBefore(debut);
    });
  }

  if (dateFin != null && !dateFin.isEmpty()) {
    LocalDate fin = LocalDate.parse(dateFin);
    ticketStream = ticketStream.filter(ticket -> {
      if (ticket.getDateCreation() == null) return false;
      return !ticket.getDateCreation().toLocalDate().isAfter(fin);
    });
  }

  return ticketStream.count();
}


  // private void envoyerNotificationsCreation(Ticket t) {
  //     Integer clientId = t.getClientId();
  //     System.out.println("Client ID: " + clientId);
  //   try {
  //     Utilisateur createur = utilisateurs.findByIdExterneCrm(t.getClientId().toString()).orElse(null);

  //     System.out.println(createur.getEmail());
  //     if (createur != null && createur.getEmail() != null) {
  //       emailService.envoyerNotificationTicketCree(
  //         createur.getEmail(),
  //         t.getReference(),
  //         t.getTitre()
  //       );

  //       // if (createur.getTelephone() != null) {
  //       //   whatsAppService.envoyerNotificationTicketCree(
  //       //     createur.getTelephone(),
  //       //     t.getReference(),
  //       //     t.getTitre()
  //       //   );
  //       // }
  //     }

  //     // if (t.getAffecteAUtilisateurId() != null) {
  //     //   Utilisateur consultant = utilisateurs.findById(t.getAffecteAUtilisateurId()).orElse(null);
  //     //   if (consultant != null && consultant.getEmail() != null) {
  //     //     emailService.envoyerNotificationTicketCree(
  //     //       consultant.getEmail(),
  //     //       t.getReference(),
  //     //       t.getTitre()
  //     //     );
  //     //   }
  //     // }
  //   } catch (Exception e) {
  //     System.err.println("Erreur lors de l'envoi des notifications : " + e.getMessage());
  //   }
  // }

  private void envoyerNotificationsCreation(Ticket t) {
      try {
          notificationWorkflowService.executerWorkflowNotification("CREATION_TICKET", t);
      } catch (Exception e) {
          System.err.println("Erreur lors de l'envoi des notifications : " + e.getMessage());
          e.printStackTrace();
      }
  }

  public void envoyerNotificationsChangementStatut(Ticket t, Integer ancienStatutId, Integer nouveauStatutId) {
    try {
        notificationWorkflowService.executerWorkflowNotification("MODIFICATION_STATUT_TICKET", t, ancienStatutId, nouveauStatutId);
    } catch (Exception e) {
        System.err.println("Erreur lors de l'envoi des notifications : " + e.getMessage());
        e.printStackTrace();
    }
  }

    public void envoyerNotificationsAjoutSolution(Ticket t) {
      try {
          notificationWorkflowService.executerWorkflowNotification("AJOUT_SOLUTION", t);
      } catch (Exception e) {
          System.err.println("Erreur lors de l'envoi des notifications : " + e.getMessage());
          e.printStackTrace();
      }
    }
  // private void envoyerNotificationsChangementStatut(Ticket t, Integer ancienStatutId, Integer nouveauStatutId) {
  //   try {
  //     String ancienStatut = mapStatutIdToCrmString(ancienStatutId);
  //     String nouveauStatut = mapStatutIdToCrmString(nouveauStatutId);

  //     Utilisateur createur = utilisateurs.findById(t.getCreeParUtilisateurId()).orElse(null);
  //     System.out.println(createur.getTelephone());
  //     if (createur != null && createur.getEmail() != null) {
  //       emailService.envoyerNotificationChangementStatut(
  //         createur.getEmail(),
  //         t.getReference(),
  //         ancienStatut,
  //         nouveauStatut
  //       );


  //       if (createur.getTelephone() != null) {
  //         whatsAppService.envoyerNotificationChangementStatut(
  //           createur.getTelephone(),
  //           t.getReference(),
  //           nouveauStatut
  //         );
  //       }
  //     }

  //     if (t.getAffecteAUtilisateurId() != null) {
  //       Utilisateur consultant = utilisateurs.findById(t.getAffecteAUtilisateurId()).orElse(null);
  //       if (consultant != null && consultant.getEmail() != null) {
  //         emailService.envoyerNotificationChangementStatut(
  //           consultant.getEmail(),
  //           t.getReference(),
  //           ancienStatut,
  //           nouveauStatut
  //         );
  //       }
  //     }
  //   } catch (Exception e) {
  //     System.err.println("Erreur lors de l'envoi des notifications : " + e.getMessage());
  //   }
  // }

public TicketAvecProduitPageReponse listerTicketsAdminAvecPaginationEtFiltres(
    int page,
    int size,
    String etat,
    String reference,
    String produit,
    String dateDebut,
    String dateFin,
    String societe,
    String priorite) {

    Stream<Ticket> ticketStream = tickets.findAll().stream();

    System.out.println("Etat: " + etat);
    // Filtre par statut
    if (etat != null && !etat.isEmpty()) {
        try {
            Integer statutId = Integer.valueOf(etat);
            ticketStream = ticketStream.filter(ticket -> statutId.equals(ticket.getStatutTicketId()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("statut invalide : " + etat);
        }
    }

    // Filtre par référence
    if (reference != null && !reference.isEmpty()) {
        ticketStream = ticketStream.filter(ticket -> 
            ticket.getReference() != null &&
            ticket.getReference().toLowerCase().contains(reference.toLowerCase()));
    }

    // Filtre par produit
    if (produit != null && !produit.isEmpty()) {
        ticketStream = ticketStream.filter(ticket -> {
            if (ticket.getProduitId() == null) return false;
            try {
                Produit produitEntity = produitRepository.findById(ticket.getProduitId()).orElse(null);
                return produitEntity != null && 
                       produitEntity.getCodeProduit().toLowerCase().contains(produit.toLowerCase());
            } catch (Exception e) {
                return false;
            }
        });
    }

    // Filtre par date début
    if (dateDebut != null && !dateDebut.isEmpty()) {
        LocalDate debut = LocalDate.parse(dateDebut);
        ticketStream = ticketStream.filter(ticket -> {
            if (ticket.getDateCreation() == null) return false;
            return !ticket.getDateCreation().toLocalDate().isBefore(debut);
        });
    }

    // Filtre par date fin
    if (dateFin != null && !dateFin.isEmpty()) {
        LocalDate fin = LocalDate.parse(dateFin);
        ticketStream = ticketStream.filter(ticket -> {
            if (ticket.getDateCreation() == null) return false;
            return !ticket.getDateCreation().toLocalDate().isAfter(fin);
        });
    }

    // Filtre par société
    if (societe != null && !societe.isEmpty()) {
        ticketStream = ticketStream.filter(ticket -> {
            if (ticket.getCompanyId() == null) return false;
            try {
                Company company = companies.findById(ticket.getCompanyId()).orElse(null);
                return company != null && 
                       company.getNom().toLowerCase().contains(societe.toLowerCase());
            } catch (Exception e) {
                return false;
            }
        });
    }

    // Filtre par priorité
    if (priorite != null && !priorite.isEmpty()) {
        try {
            Integer prioriteId = Integer.valueOf(priorite);
            ticketStream = ticketStream.filter(ticket -> prioriteId.equals(ticket.getPrioriteTicketId()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("priorite invalide : " + priorite);
        }
    }

    // Trier par date de création décroissante
    List<Ticket> ticketsFiltres = ticketStream
        .sorted((t1, t2) -> t2.getDateCreation().compareTo(t1.getDateCreation()))
        .toList();

    // Pagination
    long totalElements = ticketsFiltres.size();
    int totalPages = (int) Math.ceil((double) totalElements / size);

    // Convertir les tickets en TicketAvecProduitDto
    List<TicketAvecProduitDto> ticketDtos = ticketsFiltres.stream()
        .skip(page * size)
        .limit(size)
        .map(this::convertirEnAvecProduitDto)
        .toList();

    return new TicketAvecProduitPageReponse(ticketDtos, page, totalPages, totalElements, size);
}
  private String truncate(String s, int max) {
    if (s == null) return null;
    return s.length() <= max ? s : s.substring(0, max);
  }

  // ====== MAPPINGS À ADAPTER à tes tables référentielles ======
  private String mapPrioriteIdToCrmString(Integer prioriteId) {
    if (prioriteId == null) return null;
    // exemple : 1=Low, 2=Normal, 3=High, 4=Urgent
    switch (prioriteId) {
      case 1: return "High";
      case 2: return "Normal";
      case 3: return "Low";
      default: return "Normal";
    }
  }

  private String mapStatutIdToCrmString(Integer statutId) {
    if (statutId == null) return "Open";
    // exemple : 1=Open, 2=In Progress, 3=Pending, 4=Closed
    switch (statutId) {
      case 4: return "Closed";
      case 3: return "Pending";
      case 2: return "In Progress";
      default: return "Open";
    }
  }

  //Retourner l'idexternecrm du produit
  private String mapProduitIdToCrmString(Integer produitId) {
    if (produitId == null) return null;
    try {
      Produit produit = produitRepository.findById(produitId).orElse(null);
      if (produit != null) {
        return produit.getIdExterneCrm(); 
      } else {
        return null;
      }
    } catch (Exception e) {
      return null;
    }
  }

  private Integer mapCompanyIdToCrmCompanyId(Integer companyId) {
    try {
      Company company = companies.findById(companyId).orElse(null);
      if (company != null && company.getIdExterneCrm() != null) {
        return Integer.valueOf(company.getIdExterneCrm());
      }
      return null;
    } catch (Exception e) {
      return null;
    }
  }
}
