# Architecture Finale - Structure Company/Client

## 🎯 Structure des Tables

### Tables Principales

```
company (sociétés clientes)
├── id (PK)
├── id_externe_crm (Comp_CompanyId du CRM)
├── code_company
├── nom
├── nif, stat, adresse
├── telephone, whatsapp_numero, email
├── actif
└── date_creation, date_mise_a_jour

client (contacts des sociétés)
├── id (PK)
├── company_id (FK → company.id)
├── id_externe_crm (Pers_PersonId du CRM)
├── nom, prenom
├── email, telephone, whatsapp_numero
├── fonction
├── principal
├── actif
└── date_creation, date_mise_a_jour

utilisateur (utilisateurs internes + authentification)
├── id (PK)
├── id_externe_crm (User_UserId ou Pers_PersonId du CRM)
├── identifiant (login)
├── mot_de_passe_hash, mot_de_passe_salt
├── nom, prenom
├── email, telephone, whatsapp_numero
├── actif
├── date_derniere_connexion
└── date_creation, date_mise_a_jour

ticket
├── id (PK)
├── company_id (FK → company.id)  ✅ IMPORTANT
├── produit_id (FK → produit.id)
├── type_ticket_id, priorite_ticket_id, statut_ticket_id
├── titre, description, raison
├── politique_acceptee
├── cree_par_utilisateur_id (FK → utilisateur.id)
├── affecte_a_utilisateur_id (FK → utilisateur.id)
├── date_creation, date_mise_a_jour, date_cloture
└── id_externe_crm (Case_CaseId du CRM)
```

## 🔄 Synchronisation CRM

### Mapping CRM → Portail

```
CRM Table         →  Portail Table    | id_externe_crm
─────────────────────────────────────────────────────────
Company           →  company          | "123" (Comp_CompanyId)
Person            →  client           | "PERSON-456" (Pers_PersonId)
Users (internal)  →  utilisateur      | "USER-789" (User_UserId)
NewProduct        →  produit          | "789" (Prod_ProductId)
Cases             →  ticket           | INT (Case_CaseId)
```

### Services de Synchronisation

**CrmCompanySyncService** (2h00 quotidien)
```java
Company (CRM) → Company (Portail)
- Synchronise les sociétés clientes (Comp_Type = 'Customer')
- Crée/met à jour la table company
- id_externe_crm = String.valueOf(Comp_CompanyId)
```

**CrmPersonSyncService** (2h10 quotidien)
```java
Person (CRM) → Client (Portail)
- Synchronise les contacts des sociétés
- Vérifie l'existence de Company avant création
- Crée les entités dans la table client
- Lie chaque client à sa company via company_id
- id_externe_crm = "PERSON-" + Pers_PersonId
```

**CrmUsersSyncService** (2h20 quotidien)
```java
Users (CRM) → Utilisateur (Portail)
- Synchronise les utilisateurs internes (Consultants/Admin)
- Pas de lien avec Company
- Détecte le rôle automatiquement (ADMIN ou CONSULTANT)
- id_externe_crm = "USER-" + User_UserId
```

**CrmProductSyncService** (2h30 quotidien)
```java
NewProduct (CRM) → Produit (Portail)
- Synchronise les produits
- Utilise setLibelle(), setCodeProduit()
- id_externe_crm = String.valueOf(Prod_ProductId)
```

**CrmTicketSyncService** (toutes les 30 min)
```java
Cases (CRM) → Ticket (Portail)
- Import des tickets depuis le CRM
- ticket.company_id = company.id (trouvé via Case_PrimaryCompanyId)
- id_externe_crm = Case_CaseId
```

## 📊 Relations

```
Company (1) ────< (N) Client
  │
  └────< (N) Ticket

Utilisateur (1) ────< (N) Ticket (cree_par)
                └────< (N) Ticket (affecte_a)

Company (N) ────< (N) Produit (via company_produit)
```

## 🎯 Différences Clés

### AVANT (Incorrect)
```
❌ Client = Société (raison_sociale)
❌ Ticket.client_id → Client.id (société)
❌ Confusion entre société et contact
```

### APRÈS (Correct)
```
✅ Company = Société (nom)
✅ Client = Contact de société (nom, prenom)
✅ Ticket.company_id → Company.id
✅ Client.company_id → Company.id
✅ Séparation claire société/contact
```

## 📝 Entités Java

### Company.java ✅
```java
@Entity
@Table(name = "company", schema = "dbo")
public class Company {
  private Integer id;
  private String idExterneCrm;
  private String codeCompany;
  private String nom;
  private String nif;
  private String stat;
  private String adresse;
  private String telephone;
  private String whatsappNumero;
  private String email;
  private boolean actif;
  private LocalDateTime dateCreation;
  private LocalDateTime dateMiseAJour;
}
```

### Client.java ✅
```java
@Entity
@Table(name = "client", schema = "dbo")
public class Client {
  private Integer id;
  private Integer companyId;

  @ManyToOne
  @JoinColumn(name="company_id", ...)
  private Company company;

  private String idExterneCrm;
  private String nom;
  private String prenom;
  private String email;
  private String telephone;
  private String whatsappNumero;
  private String fonction;
  private boolean principal;
  private boolean actif;
  private LocalDateTime dateCreation;
  private LocalDateTime dateMiseAJour;
}
```

### Ticket.java ✅
```java
@Entity
@Table(name = "ticket", schema = "dbo")
public class Ticket {
  private Integer id;
  private Integer companyId;  // ✅ Référence Company
  private Integer produitId;
  private Integer typeTicketId;
  private Integer prioriteTicketId;
  private Integer statutTicketId;
  private String titre;
  private String description;
  private String raison;
  private boolean politiqueAcceptee;
  private Integer creeParUtilisateurId;
  private Integer affecteAUtilisateurId;
  private LocalDateTime dateCreation;
  private LocalDateTime dateMiseAJour;
  private LocalDateTime dateCloture;
  private Integer clotureParUtilisateurId;
  private Integer idExterneCrm;
}
```

## 🚀 Utilisation

### Créer un Ticket
```java
TicketCreationRequete requete = new TicketCreationRequete();
requete.setCompanyId(companyId);  // ✅ ID de la société
requete.setProduitId(produitId);
requete.setTypeTicketId(1);
requete.setPrioriteTicketId(2);
requete.setStatutTicketId(1);
requete.setTitre("Problème XYZ");
requete.setDescription("...");
requete.setPolitiqueAcceptee(true);
requete.setCreeParUtilisateurId(userId);

Ticket ticket = ticketService.creerEtSynchroniser(requete);
```

### Récupérer les Clients d'une Company
```java
List<Client> clients = clientRepository.findByCompanyId(companyId);
```

### Récupérer la Company d'un Ticket
```java
Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
Company company = companyRepository.findById(ticket.getCompanyId()).orElseThrow();
```

## ✅ Checklist de Cohérence

- [x] Table `company` créée dans le script SQL
- [x] Table `client` modifiée avec `company_id`
- [x] Table `ticket` modifiée avec `company_id`
- [x] Entité `Company.java` créée
- [x] Entité `Client.java` modifiée
- [x] Entité `Ticket.java` modifiée (companyId)
- [x] Repository `CompanyRepository` créé
- [x] `CrmCompanySyncService` adapté
- [x] `CrmPersonSyncService` adapté
- [x] `CrmTicketSyncService` adapté
- [x] `TicketService` adapté
- [x] DTO `TicketCreationRequete` adapté
- [x] Relations N-N `company_produit` créée
- [x] Table `utilisateur_role` utilise `company_id`
- [x] Index SQL mis à jour

## ⚠️ Points à Vérifier

1. **InterventionService** : Vérifier les méthodes obsolètes
2. **CrmUtilisateurSyncService** : Supprimer (doublon avec CrmUsersSyncService)
3. **Tables référentielles** : Créer les entités (Role, StatutTicket, etc.)
4. **Tests** : Tester la synchronisation complète

## 🎉 Architecture Finale

```
                    ┌─────────────┐
                    │  CRM Sage   │
                    └──────┬──────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ┌────▼────┐       ┌────▼────┐       ┌────▼────┐
   │ Company │       │ Person  │       │  Users  │
   └────┬────┘       └────┬────┘       └────┬────┘
        │                 │                  │
        │ Sync            │ Sync             │ Sync
        │ 2h00            │ 2h10             │ 2h20
        │                 │                  │
   ┌────▼────┐       ┌────▼────┐       ┌────▼─────────┐
   │ company │◄──┐   │ client  │       │ utilisateur  │
   └────┬────┘   │   └─────────┘       └──────────────┘
        │        │                            │
        │        └────────────────┐           │
        │                         │           │
   ┌────▼────┐              ┌────▼───────────▼──┐
   │ ticket  │              │ ticket (créé_par,  │
   └─────────┘              │       affecté_à)   │
                            └────────────────────┘
```
