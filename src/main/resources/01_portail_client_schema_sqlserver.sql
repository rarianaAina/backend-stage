/*
Fichier: 01_portail_client_schema_sqlserver.sql
Cible   : Microsoft SQL Server (T-SQL)
But     : Schéma complet et normalisé pour le portail client (tickets, interventions, interactions, notifications, etc.).
Notes   :
- Colonnes en français (sans accents) pour éviter les soucis d’identifiants.
- Relations explicites (1‑N, N‑N) avec clés étrangères et index.
- Tables de reference (statuts, types) pour remplacer les énumérations.
- Champs *_externe_crm pour gérer une éventuelle synchro avec Sage CRM via API.
- Envoi d’e-mails/WhatsApp à gérer au niveau applicatif (Spring Boot); la table notification trace l’historique.
*/

/* =========================================================
   Préambule & paramètres
========================================================= */
SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================================================
   SCHEMA (optionnel) : décommentez si vous voulez un schéma dédié
   CREATE SCHEMA portail AUTHORIZATION dbo;
========================================================= */

/* =========================================================
   Tables de référence (codes & libellés)
========================================================= */

CREATE TABLE dbo.role (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    code             VARCHAR(50) NOT NULL UNIQUE,      -- CLIENT, CONSULTANT, ADMIN
    libelle          NVARCHAR(150) NOT NULL
);



CREATE TABLE dbo.priorite_ticket (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    code             VARCHAR(50) NOT NULL UNIQUE,      -- URGENT, HAUTE, NORMALE, BASSE
    libelle          NVARCHAR(150) NOT NULL,
    niveau           INT NOT NULL CHECK (niveau >= 0)  -- 0=le plus bas; plus grand = priorite plus haute
);

CREATE TABLE dbo.type_ticket (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    code             VARCHAR(50) NOT NULL UNIQUE,      -- INCIDENT, DEMANDE, EVOLUTION, QUESTION, etc.
    libelle          NVARCHAR(150) NOT NULL
);

CREATE TABLE dbo.statut_ticket (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    code             VARCHAR(50) NOT NULL UNIQUE,      -- OUVERT, EN_COURS, EN_ATTENTE, EN_ATTENTE_CLIENT, PLANIFIE, RESOLU, CLOTURE
    libelle          NVARCHAR(150) NOT NULL,
    ordre_affichage  INT NOT NULL DEFAULT(0)
);

CREATE TABLE dbo.statut_intervention (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    code             VARCHAR(50) NOT NULL UNIQUE,      -- PROPOSEE, PLANIFIEE, EN_COURS, A_VALIDER_CLIENT, REFUSEE, CLOTUREE
    libelle          NVARCHAR(150) NOT NULL,
    ordre_affichage  INT NOT NULL DEFAULT(0)
);

CREATE TABLE dbo.type_interaction (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    code             VARCHAR(50) NOT NULL UNIQUE,      -- MESSAGE, SYSTEME, RELANCE
    libelle          NVARCHAR(150) NOT NULL
);

CREATE TABLE dbo.canal_interaction (
    id               INT IDENTITY(1,1) PRIMARY KEY,
    code             VARCHAR(50) NOT NULL UNIQUE,      -- PORTAIL, EMAIL, WHATSAPP
    libelle          NVARCHAR(150) NOT NULL
);

/* =========================================================
   Donnees "metier" externes/mirror (optionnel selon intégration CRM)
========================================================= */
CREATE TABLE dbo.company (
    id                      INT IDENTITY(1,1) PRIMARY KEY,
    id_externe_crm          VARCHAR(100) NULL,         -- clé externe Sage CRM (Comp_CompanyId)
    code_company            VARCHAR(100) NULL,
    nom                     NVARCHAR(250) NOT NULL,
    nif                     VARCHAR(100) NULL,
    stat                    VARCHAR(100) NULL,
    adresse                 NVARCHAR(500) NULL,
    telephone               VARCHAR(50) NULL,
    whatsapp_numero         VARCHAR(50) NULL,
    email                   VARCHAR(320) NULL,
    actif                   BIT NOT NULL DEFAULT(1),
    date_creation           DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    date_mise_a_jour        DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME())
);

-- CREATE TABLE dbo.client (
--     id                      INT IDENTITY(1,1) PRIMARY KEY,
--     company_id              INT NOT NULL,              -- référence vers company
--     id_externe_crm          VARCHAR(100) NULL,         -- clé externe Sage CRM (Pers_PersonId)
--     nom                     NVARCHAR(150) NOT NULL,
--     prenom                  NVARCHAR(150) NULL,
--     email                   VARCHAR(320) NULL,
--     telephone               VARCHAR(50) NULL,
--     whatsapp_numero         VARCHAR(50) NULL,
--     fonction                NVARCHAR(150) NULL,
--     principal               BIT NOT NULL DEFAULT(0),
--     actif                   BIT NOT NULL DEFAULT(1),
--     date_creation           DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
--     date_mise_a_jour        DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
--     CONSTRAINT FK_client_company FOREIGN KEY (company_id) REFERENCES dbo.company(id)
-- );

CREATE TABLE dbo.produit (
    id                      INT IDENTITY(1,1) PRIMARY KEY,
    id_externe_crm          VARCHAR(100) NULL,         -- clé externe Sage CRM
    code_produit            VARCHAR(100) NULL,
    libelle                 NVARCHAR(250) NOT NULL,
    description             NVARCHAR(MAX) NULL,
    actif                   BIT NOT NULL DEFAULT(1),
    date_creation           DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    date_mise_a_jour        DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME())
);

/* liaison N‑N pour gerer les produits possedes par chaque company (avec meta) */
CREATE TABLE dbo.company_produit (
    id                      INT IDENTITY(1,1) PRIMARY KEY,
    company_id              INT NOT NULL,
    produit_id              INT NOT NULL,
    numero_serie            NVARCHAR(150) NULL,
    date_debut_contrat      DATE NULL,
    date_fin_contrat        DATE NULL,
    actif                   BIT NOT NULL DEFAULT(1),
    CONSTRAINT UQ_company_produit UNIQUE (company_id, produit_id, numero_serie),
    CONSTRAINT FK_company_produit_company  FOREIGN KEY (company_id)  REFERENCES dbo.company(id),
    CONSTRAINT FK_company_produit_produit FOREIGN KEY (produit_id) REFERENCES dbo.produit(id)
);

/* Crédits horaires (forfaits de support) */
CREATE TABLE dbo.credit_horaire (
    id                      INT IDENTITY(1,1) PRIMARY KEY,
    company_id              INT NOT NULL,
    produit_id              INT NULL,                      -- si lié à un produit spécifique
    periode_debut           DATE NOT NULL,
    periode_fin             DATE NOT NULL,
    heures_allouees         INT NOT NULL CHECK (heures_allouees >= 0),
    heures_consommees       INT NOT NULL DEFAULT(0) CHECK (heures_consommees >= 0),
    heures_restantes        INT NOT NULL,
    actif                   BIT NOT NULL DEFAULT(1),
    reporte            BIT NOT NULL DEFAULT(0),
    date_creation           DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    date_mise_a_jour        DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    CONSTRAINT FK_credit_horaire_company FOREIGN KEY (company_id) REFERENCES dbo.company(id),
    CONSTRAINT FK_credit_horaire_produit FOREIGN KEY (produit_id) REFERENCES dbo.produit(id)
);

CREATE INDEX IX_credit_horaire_company ON dbo.credit_horaire(company_id, actif);
CREATE INDEX IX_credit_horaire_periode ON dbo.credit_horaire(periode_debut, periode_fin);

/* =========================================================
   Utilisateurs & roles
========================================================= */
-- CREATE TABLE dbo.utilisateur (
--     id                      INT IDENTITY(1,1) PRIMARY KEY,
--     id_externe_crm          VARCHAR(100) NULL,     -- ex: id contact CRM
--     identifiant             VARCHAR(150) NOT NULL UNIQUE,  -- fourni par la societe
--     mot_de_passe_hash       VARBINARY(512) NULL,   -- si auth interne; sinon NULL si SSO
--     mot_de_passe_salt       VARBINARY(128) NULL,
--     nom                     NVARCHAR(150) NOT NULL,
--     prenom                  NVARCHAR(150) NULL,
--     email                   VARCHAR(320) NULL,
--     telephone               VARCHAR(50) NULL,
--     whatsapp_numero         VARCHAR(50) NULL,
--     actif                   BIT NOT NULL DEFAULT(1),
--     date_derniere_connexion DATETIME2(0) NULL,
--     date_creation           DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
--     date_mise_a_jour        DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME())
-- );

CREATE TABLE dbo.utilisateur (
    id                      INT IDENTITY(1,1) PRIMARY KEY,
    -- Champs de Company (ajoutés depuis Client)
    company_id              INT NULL,               -- Référence à l'entreprise
    -- Champs CRM
    id_externe_crm          VARCHAR(100) NULL,     -- ex: id contact CRM
    -- Champs d'authentification
    identifiant             VARCHAR(150) NOT NULL UNIQUE,  -- fourni par la societe
    mot_de_passe_hash       VARBINARY(512) NULL,   -- si auth interne; sinon NULL si SSO
    mot_de_passe_salt       VARBINARY(128) NULL,
    -- Champs personnels
    nom                     NVARCHAR(150) NOT NULL,
    prenom                  NVARCHAR(150) NULL,
    email                   VARCHAR(320) NULL,
    telephone               VARCHAR(50) NULL,
    whatsapp_numero         VARCHAR(50) NULL,
    -- Statut
    actif                   BIT NOT NULL DEFAULT(1),
    -- Dates
    date_derniere_connexion DATETIME2(0) NULL,
    date_creation           DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    date_mise_a_jour        DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    
    -- Contrainte de clé étrangère pour company_id
    CONSTRAINT FK_utilisateur_company 
        FOREIGN KEY (company_id) 
        REFERENCES dbo.company(id)
);

-- Création d'un index sur company_id pour les performances
CREATE INDEX IX_utilisateur_company_id ON dbo.utilisateur(company_id);

-- Index sur l'email pour les recherches
CREATE INDEX IX_utilisateur_email ON dbo.utilisateur(email);


--  Utilisateur roles


/* Contacts visibles pour le client (annuaire de la societe) */
CREATE TABLE dbo.contact_societe (
    id                      INT IDENTITY(1,1) PRIMARY KEY,
    nom                     NVARCHAR(150) NOT NULL,
    prenom                  NVARCHAR(150) NULL,
    email                   VARCHAR(320) NULL,
    telephone               VARCHAR(50) NULL,
    whatsapp_numero         VARCHAR(50) NULL,
    fonction                NVARCHAR(150) NULL,
    visible_clients         BIT NOT NULL DEFAULT(1),
    ordre_affichage         INT NOT NULL DEFAULT(0),
    actif                   BIT NOT NULL DEFAULT(1)
);

/* =========================================================
   Tickets
========================================================= */
CREATE TABLE dbo.ticket (
    id                      INT IDENTITY(1,1) PRIMARY KEY,
    id_externe_crm          INT NULL,                    -- clé externe Sage CRM (Case_CaseId)
    client_id               INT NOT NULL,                -- client demandeur (utilisateur)
    reference               VARCHAR(50) NOT NULL UNIQUE, -- ex: TCK-2025-000123 (generer cote backend)
    company_id              INT NOT NULL,
    produit_id              INT NULL,                    -- si rattachement a un produit
    type_ticket_id          INT NOT NULL,
    priorite_ticket_id      INT NOT NULL,
    statut_ticket_id        INT NOT NULL,
    titre                   NVARCHAR(250) NOT NULL,
    description             NVARCHAR(MAX) NULL,
    raison                  NVARCHAR(500) NULL,
    politique_acceptee      BIT NOT NULL DEFAULT(0),
    cree_par_utilisateur_id INT NULL,                -- createur (client ou staff)
    affecte_a_utilisateur_id INT NULL,                   -- consultant principal (peut changer)
    date_creation           DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    date_mise_a_jour        DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    date_cloture            DATETIME2(0) NULL,
    cloture_par_utilisateur_id INT NULL,
    CONSTRAINT FK_ticket_company           FOREIGN KEY (company_id)          REFERENCES dbo.company(id),
    CONSTRAINT FK_ticket_produit           FOREIGN KEY (produit_id)         REFERENCES dbo.produit(id),
    CONSTRAINT FK_ticket_type              FOREIGN KEY (type_ticket_id)     REFERENCES dbo.type_ticket(id),
    CONSTRAINT FK_ticket_priorite          FOREIGN KEY (priorite_ticket_id) REFERENCES dbo.priorite_ticket(id),
    CONSTRAINT FK_ticket_statut            FOREIGN KEY (statut_ticket_id)   REFERENCES dbo.statut_ticket(id),
    CONSTRAINT FK_ticket_cree_par          FOREIGN KEY (cree_par_utilisateur_id) REFERENCES dbo.utilisateur(id),
    CONSTRAINT FK_ticket_affecte_a         FOREIGN KEY (affecte_a_utilisateur_id) REFERENCES dbo.utilisateur(id),
    CONSTRAINT FK_ticket_cloture_par       FOREIGN KEY (cloture_par_utilisateur_id) REFERENCES dbo.utilisateur(id)
);

CREATE INDEX IX_ticket_company_statut      ON dbo.ticket(company_id, statut_ticket_id);
CREATE INDEX IX_ticket_affecte_a          ON dbo.ticket(affecte_a_utilisateur_id, statut_ticket_id);
CREATE INDEX IX_ticket_dates              ON dbo.ticket(date_creation, date_cloture);

/* Historique des statuts de ticket */
CREATE TABLE dbo.historique_statut_ticket (
    id                      INT IDENTITY(1,1) PRIMARY KEY,
    ticket_id               INT NOT NULL,
    ancien_statut_id        INT NULL,
    nouveau_statut_id       INT NOT NULL,
    change_par_utilisateur_id INT NOT NULL,
    commentaire             NVARCHAR(1000) NULL,
    date_changement         DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    CONSTRAINT FK_hist_ticket_ticket   FOREIGN KEY (ticket_id)        REFERENCES dbo.ticket(id),
    CONSTRAINT FK_hist_ticket_old      FOREIGN KEY (ancien_statut_id) REFERENCES dbo.statut_ticket(id),
    CONSTRAINT FK_hist_ticket_new      FOREIGN KEY (nouveau_statut_id)REFERENCES dbo.statut_ticket(id),
    CONSTRAINT FK_hist_ticket_user     FOREIGN KEY (change_par_utilisateur_id) REFERENCES dbo.utilisateur(id)
);

/* Multi-assignations (N‑N) consultant <-> ticket si besoin */
CREATE TABLE dbo.ticket_assignation (
    id                      INT IDENTITY(1,1) PRIMARY KEY,
    ticket_id               INT NOT NULL,
    consultant_utilisateur_id INT NOT NULL,
    date_debut              DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    date_fin                DATETIME2(0) NULL,
    actif                   BIT NOT NULL DEFAULT(1),
    CONSTRAINT UQ_ticket_assignation UNIQUE (ticket_id, consultant_utilisateur_id, date_debut),
    CONSTRAINT FK_ta_ticket     FOREIGN KEY (ticket_id)               REFERENCES dbo.ticket(id),
    CONSTRAINT FK_ta_consultant FOREIGN KEY (consultant_utilisateur_id) REFERENCES dbo.utilisateur(id)
);

/* =========================================================
   Interventions
========================================================= */
CREATE TABLE dbo.intervention (
    id                          INT IDENTITY(1,1) PRIMARY KEY,
    id_externe_crm              INT NULL,                    -- clé externe Sage CRM (inte_INTERVENTIONid)
    ticket_id                   INT NOT NULL,
    reference                   VARCHAR(50) NOT NULL,        -- ex: INT-2025-000123
    raison                      NVARCHAR(MAX) NULL,
    date_intervention           DATETIME2(0) NOT NULL,       -- date prévue/planifiée
    date_proposee_client        DATETIME2(0) NULL,
    type_intervention           VARCHAR(50) NULL,
    statut_intervention_id      INT NOT NULL,
    cree_par_utilisateur_id     INT NOT NULL,
    date_creation               DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    date_mise_a_jour            DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    date_cloture                DATETIME2(0) NULL,
    cloture_par_utilisateur_id  INT NULL,
    fiche_intervention          NVARCHAR(MAX) NULL,
    validee_par_client          BIT NULL,
    CONSTRAINT FK_inter_ticket       FOREIGN KEY (ticket_id)                REFERENCES dbo.ticket(id),
    CONSTRAINT FK_inter_statut       FOREIGN KEY (statut_intervention_id)   REFERENCES dbo.statut_intervention(id),
    CONSTRAINT FK_inter_cree_par     FOREIGN KEY (cree_par_utilisateur_id)  REFERENCES dbo.utilisateur(id),
    CONSTRAINT FK_inter_cloture_par  FOREIGN KEY (cloture_par_utilisateur_id) REFERENCES dbo.utilisateur(id)
);

CREATE INDEX IX_intervention_ticket_statut ON dbo.intervention(ticket_id, statut_intervention_id);

/* =========================================================
   Interactions & PJ
========================================================= */
CREATE TABLE dbo.interaction (
    id                      INT IDENTITY(1,1) PRIMARY KEY,
    ticket_id               INT NOT NULL,
    intervention_id         INT NULL, -- interaction rattachee a l'intervention specifique (sinon discussion ticket)
    auteur_utilisateur_id   INT NOT NULL,
    type_interaction_id     INT NOT NULL,  -- MESSAGE / SYSTEME / RELANCE
    canal_interaction_id    INT NOT NULL,  -- PORTAIL / EMAIL / WHATSAPP
    contenu                 NVARCHAR(MAX) NOT NULL,
    date_creation           DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    CONSTRAINT FK_interact_ticket    FOREIGN KEY (ticket_id)            REFERENCES dbo.ticket(id),
    CONSTRAINT FK_interact_interv    FOREIGN KEY (intervention_id)      REFERENCES dbo.intervention(id),
    CONSTRAINT FK_interact_auteur    FOREIGN KEY (auteur_utilisateur_id)REFERENCES dbo.utilisateur(id),
    CONSTRAINT FK_interact_type      FOREIGN KEY (type_interaction_id)  REFERENCES dbo.type_interaction(id),
    CONSTRAINT FK_interact_canal     FOREIGN KEY (canal_interaction_id) REFERENCES dbo.canal_interaction(id)
);

CREATE INDEX IX_interaction_ticket_date ON dbo.interaction(ticket_id, date_creation);

/* PJ pouvant appartenir a un ticket OU a une intervention OU a une interaction (1 seul) */
CREATE TABLE dbo.piece_jointe (
    id                          INT IDENTITY(1,1) PRIMARY KEY,
    nom_fichier                 NVARCHAR(255) NOT NULL,
    url_contenu                 NVARCHAR(1000) NULL,
    chemin_fichier              NVARCHAR(1000) NULL,
    type_mime                   NVARCHAR(200) NULL,
    taille_octets               BIGINT NULL,
    ajoute_par_utilisateur_id   INT NOT NULL,
    ticket_id                   INT NULL,
    intervention_id             INT NULL,
    interaction_id              INT NULL,
    date_ajout                  DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    CONSTRAINT CK_pj_cible UNIQUE (id, ticket_id, intervention_id, interaction_id),
    CONSTRAINT CK_pj_exactement_un CHECK (
        (CASE WHEN ticket_id IS NOT NULL THEN 1 ELSE 0 END) +
        (CASE WHEN intervention_id IS NOT NULL THEN 1 ELSE 0 END) +
        (CASE WHEN interaction_id IS NOT NULL THEN 1 ELSE 0 END) = 1
    ),
    CONSTRAINT FK_pj_ajoute_par   FOREIGN KEY (ajoute_par_utilisateur_id) REFERENCES dbo.utilisateur(id),
    CONSTRAINT FK_pj_ticket       FOREIGN KEY (ticket_id)       REFERENCES dbo.ticket(id),
    CONSTRAINT FK_pj_intervention FOREIGN KEY (intervention_id) REFERENCES dbo.intervention(id),
    CONSTRAINT FK_pj_interaction  FOREIGN KEY (interaction_id)  REFERENCES dbo.interaction(id)
);

CREATE INDEX IX_pj_ticket        ON dbo.piece_jointe(ticket_id);
CREATE INDEX IX_pj_intervention  ON dbo.piece_jointe(intervention_id);
CREATE INDEX IX_pj_interaction   ON dbo.piece_jointe(interaction_id);

/* =========================================================
   Notifications & templates
========================================================= */
CREATE TABLE dbo.notification_template (
    id                  INT IDENTITY(1,1) PRIMARY KEY,
    code                VARCHAR(100) NOT NULL UNIQUE,  -- TICKET_STATUT_CHANGE, INTERVENTION_CREE, INTERVENTION_DATE_VALIDEE, NOUVELLE_DEMANDE, etc.
    libelle             NVARCHAR(200) NOT NULL,
    canal               VARCHAR(20) NOT NULL CHECK (canal IN ('EMAIL','WHATSAPP')),
    sujet               NVARCHAR(200) NULL,           -- pour EMAIL
    contenu_html        NVARCHAR(MAX) NULL,
    actif               BIT NOT NULL DEFAULT(1),
    date_creation       DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    date_mise_a_jour    DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME())
);

CREATE TABLE dbo.notification (
    id                      INT IDENTITY(1,1) PRIMARY KEY,
    notification_template_id INT NOT NULL,
    destinataire_utilisateur_id INT NOT NULL,
    ticket_id               INT NULL,
    intervention_id         INT NULL,
    canal                   VARCHAR(20) NOT NULL CHECK (canal IN ('EMAIL','WHATSAPP')),
    sujet_envoye            NVARCHAR(200) NULL,
    contenu_envoye          NVARCHAR(MAX) NULL,
    statut_envoi            VARCHAR(20) NOT NULL DEFAULT('EN_ATTENTE') CHECK (statut_envoi IN ('EN_ATTENTE','ENVOYE','ECHEC')),
    tentative               INT NOT NULL DEFAULT(0),
    erreur_message          NVARCHAR(1000) NULL,
    date_creation           DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    date_envoi              DATETIME2(0) NULL,
    CONSTRAINT FK_notif_template  FOREIGN KEY (notification_template_id) REFERENCES dbo.notification_template(id),
    CONSTRAINT FK_notif_dest      FOREIGN KEY (destinataire_utilisateur_id) REFERENCES dbo.utilisateur(id),
    CONSTRAINT FK_notif_ticket    FOREIGN KEY (ticket_id)        REFERENCES dbo.ticket(id),
    CONSTRAINT FK_notif_interv    FOREIGN KEY (intervention_id)  REFERENCES dbo.intervention(id)
);

CREATE INDEX IX_notification_statut ON dbo.notification(statut_envoi, date_creation);

/* =========================================================
   Journal d'evenements (audit)
========================================================= */
CREATE TABLE dbo.journal_evenement (
    id                      INT IDENTITY(1,1) PRIMARY KEY,
    entite_type             VARCHAR(50) NOT NULL,  -- TICKET, INTERVENTION, INTERACTION, UTILISATEUR, etc.
    entite_id               INT NOT NULL,
    evenement_type          VARCHAR(100) NOT NULL, -- ex: TICKET_RELANCE, INTERVENTION_VALIDEE, PIECE_JOINTE_AJOUTEE
    donnees_json            NVARCHAR(MAX) NULL,    -- JSON libre
    utilisateur_id          INT NULL,
    date_evenement          DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    CONSTRAINT FK_journal_user FOREIGN KEY (utilisateur_id) REFERENCES dbo.utilisateur(id)
);

CREATE INDEX IX_journal_entite ON dbo.journal_evenement(entite_type, entite_id, date_evenement);

/* =========================================================
   Index additionnels pour la recherche multicritere
========================================================= */
CREATE INDEX IX_ticket_prio_type_date ON dbo.ticket(priorite_ticket_id, type_ticket_id, date_creation);

CREATE TABLE dbo.utilisateur_role (

    utilisateur_id          INT NOT NULL,
    role_id                 INT NOT NULL,
    company_id              INT NULL,
    PRIMARY KEY (utilisateur_id, role_id),
    CONSTRAINT FK_utilisateur_role_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES dbo.utilisateur(id),
    CONSTRAINT FK_utilisateur_role_role        FOREIGN KEY (role_id)        REFERENCES dbo.role(id),
    CONSTRAINT FK_utilisateur_role_company     FOREIGN KEY (company_id)     REFERENCES dbo.company(id)
);

CREATE TABLE dbo.modalite_intervention (
    id      INT IDENTITY(1,1) PRIMARY KEY,
    code    VARCHAR(50) NOT NULL UNIQUE,
    libelle NVARCHAR(150) NOT NULL
);

CREATE TABLE shedlock (
    name NVARCHAR(64) NOT NULL PRIMARY KEY,
    lock_until DATETIME2 NOT NULL,
    locked_at DATETIME2 NOT NULL,
    locked_by NVARCHAR(255) NOT NULL
);

CREATE TABLE dbo.CompanyPARC (
    parc_PARCid INT PRIMARY KEY,               -- clé primaire pour `parcId`
    parc_name NVARCHAR(255),                   -- champ pour `parcName`
    parc_companyid INT,                        -- champ pour `parcCompanyId`
    comp_companyid INT,                        -- champ pour `compCompanyId`
    comp_name NVARCHAR(255),                   -- champ pour `compName`
    date_obtention DATETIME2(0) NOT NULL DEFAULT SYSDATETIME()
);

CREATE TABLE ValidationCodes (
    Id BIGINT IDENTITY(1,1) PRIMARY KEY,
    utilisateur_id NVARCHAR(128) NOT NULL, -- ou INT selon votre structure d'utilisateurs
    Code CHAR(4) NOT NULL, -- Code à 4 chiffres
    CreatedAt DATETIME2 NOT NULL DEFAULT GETUTCDATE(),
    ExpiresAt DATETIME2 NOT NULL,
    IsUsed BIT NOT NULL DEFAULT 0,
    UsedAt DATETIME2 NULL,
    Attempts INT NOT NULL DEFAULT 0,
    MaxAttempts INT NOT NULL DEFAULT 3
);

-- Index pour optimiser les recherches
CREATE INDEX IX_ValidationCodes_utilisateur_id ON ValidationCodes(utilisateur_id);
CREATE INDEX IX_ValidationCodes_Code ON ValidationCodes(Code);
CREATE INDEX IX_ValidationCodes_ExpiresAt ON ValidationCodes(ExpiresAt);

-- Création de la table type_notification
CREATE TABLE type_notification (
    id INT IDENTITY(1,1) PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    libelle NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX),
    template_id INT NULL, -- Lien vers le template de notification
    est_actif BIT NOT NULL DEFAULT 1,
    date_creation DATETIME2 NOT NULL DEFAULT GETDATE(),
    date_modification DATETIME2 NULL
);

CREATE TABLE solutions (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    id_externe_crm NVARCHAR(255) UNIQUE NOT NULL,
    titre NVARCHAR(500),
    description NVARCHAR(MAX),
    zone NVARCHAR(100),
    statut NVARCHAR(50),
    etape NVARCHAR(50),
    reference NVARCHAR(255),
    secteur NVARCHAR(100),
    cloture BIT DEFAULT 0,
    supprime BIT DEFAULT 0,
    date_creation DATETIME2,
    date_mise_a_jour DATETIME2,
    date_externalisation DATETIME2,
    date_synchronisation DATETIME2,
    cree_par INT,
    mis_a_jour_par INT,
    utilisateur_attribue INT,
    workflow_id INT,
    canal_id INT,
    cle_externe_talend NVARCHAR(255)
);
ALTER TABLE solutions ADD COLUMN date_cloture DATETIME;
-- Création des index
CREATE INDEX idx_solutions_id_externe_crm ON solutions(id_externe_crm);
CREATE INDEX idx_solutions_statut ON solutions(statut);
CREATE INDEX idx_solutions_zone ON solutions(zone);
CREATE INDEX idx_solutions_cloture ON solutions(cloture);
CREATE INDEX idx_solutions_date_creation ON solutions(date_creation);
CREATE INDEX idx_solutions_date_synchronisation ON solutions(date_synchronisation);
-- Création de la table workflow_notification_mail
CREATE TABLE workflow_notification_mail (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ordre INT NOT NULL,
    utilisateur_id INT NOT NULL,
    type_notification_id INT NOT NULL,
    est_actif BIT NOT NULL DEFAULT 1,
    date_creation DATETIME2 NOT NULL DEFAULT GETDATE(),
    date_modification DATETIME2 NULL,
    
    -- Contraintes d'unicité
    CONSTRAINT UK_workflow_ordre_type UNIQUE (ordre, type_notification_id),
    
    -- Contraintes de clé étrangère
    CONSTRAINT FK_workflow_type_notification 
        FOREIGN KEY (type_notification_id) 
        REFERENCES type_notification(id),
    
    -- Contrainte pour éviter les ordres négatifs
    CONSTRAINT CHK_workflow_ordre_positif 
        CHECK (ordre > 0)
);

-- Ajout de la clé étrangère vers notification_template
ALTER TABLE type_notification
ADD CONSTRAINT FK_type_notification_template
    FOREIGN KEY (template_id) 
    REFERENCES notification_template(id);

-- Création d'index pour améliorer les performances
CREATE INDEX IX_workflow_ordre ON workflow_notification_mail(ordre);
CREATE INDEX IX_workflow_type_notification ON workflow_notification_mail(type_notification_id);
CREATE INDEX IX_workflow_utilisateur ON workflow_notification_mail(utilisateur_id);
CREATE INDEX IX_type_notification_code ON type_notification(code);
CREATE INDEX IX_type_notification_template ON type_notification(template_id);

CREATE TABLE configuration_smtp (
    id INT IDENTITY(1,1) PRIMARY KEY,
    host VARCHAR(255) NOT NULL,
    port INT NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(500) NOT NULL, -- Chiffré de préférence
    protocol VARCHAR(50) DEFAULT 'smtp',
    auth_required BIT DEFAULT 1,
    starttls_enabled BIT DEFAULT 1,
    ssl_trust VARCHAR(255),
    est_actif BIT DEFAULT 1,
    date_creation DATETIME2 DEFAULT GETDATE(),
    date_modification DATETIME2
);

CREATE TABLE configuration_whatsapp (
    id INT IDENTITY(1,1) PRIMARY KEY,
    api_base_url VARCHAR(255) NOT NULL DEFAULT 'https://waba.360dialog.io/v1/messages', -- URL par défaut de l'API
    api_key VARCHAR(500) NOT NULL, -- Clé API 360dialog (à chiffrer)
    phone_number_id VARCHAR(100) NOT NULL, -- ID du numéro WhatsApp configuré
    business_account_id VARCHAR(100) NULL, -- ID du compte WhatsApp Business (optionnel)
    webhook_url VARCHAR(255) NULL, -- URL où ton app reçoit les statuts de message
    webhook_token VARCHAR(255) NULL, -- jeton de vérification du webhook (si utilisé)
    est_actif BIT DEFAULT 1, -- pour activer/désactiver la config
    nom_configuration VARCHAR(100) DEFAULT 'Défaut',
    description NVARCHAR(500) NULL,
    date_creation DATETIME2 DEFAULT GETDATE(),
    date_modification DATETIME2
);

-- Vue pour faciliter la consultation du workflow avec toutes les informations
CREATE VIEW v_workflow_notification_complet AS
SELECT 
    wn.id,
    wn.ordre,
    wn.utilisateur_id,
    wn.type_notification_id,
    tn.code AS type_notification_code,
    tn.libelle AS type_notification_libelle,
    tn.template_id,
    nt.code AS template_code,
    nt.libelle AS template_libelle,
    nt.canal AS template_canal,
    nt.sujet AS template_sujet,
    wn.est_actif,
    wn.date_creation,
    wn.date_modification
FROM workflow_notification_mail wn
INNER JOIN type_notification tn ON wn.type_notification_id = tn.id
LEFT JOIN notification_template nt ON tn.template_id = nt.id;

-- Vue pour récupérer le workflow actif par type de notification
CREATE VIEW v_workflow_actif_par_type AS
SELECT 
    tn.code AS type_notification_code,
    tn.libelle AS type_notification_libelle,
    wn.ordre,
    wn.utilisateur_id,
    nt.sujet AS template_sujet,
    nt.contenu_html AS template_contenu
FROM workflow_notification_mail wn
INNER JOIN type_notification tn ON wn.type_notification_id = tn.id
LEFT JOIN notification_template nt ON tn.template_id = nt.id
WHERE wn.est_actif = 1 AND tn.est_actif = 1
ORDER BY tn.code, wn.ordre;



GO
