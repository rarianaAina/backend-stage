-- Script de réinitialisation de la base de données COURANTE uniquement
-- Préserve les tables spécifiées et réinitialise toutes les autres

DECLARE @PreservedTables TABLE (TableName VARCHAR(255));
DECLARE @Sql NVARCHAR(MAX);
DECLARE @CurrentDB VARCHAR(255) = DB_NAME();

PRINT 'Opération sur la base de données : ' + @CurrentDB;
PRINT 'Êtes-vous sûr de vouloir continuer? Cette action est IRREVERSIBLE!';
PRINT '';

-- LISTE DES TABLES À PRÉSERVER
INSERT INTO @PreservedTables (TableName) VALUES
('role'),
('priorite_ticket'),
('statut_ticket'),
('type_ticket'),
('type_interaction'),
('canal_interaction'),
('statut_intervention'),
('typenotification'),
('configuration_smtp'),
('modalite_intervention');

-- AFFICHER LES TABLES QUI SERONT AFFECTÉES
PRINT 'Tables qui seront VIDÉES :';
SELECT TABLE_NAME 
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_TYPE = 'BASE TABLE'
AND TABLE_NAME NOT IN (SELECT TableName FROM @PreservedTables)
ORDER BY TABLE_NAME;

PRINT '';
PRINT 'Tables qui seront PRÉSERVÉES :';
SELECT TableName FROM @PreservedTables ORDER BY TableName;

PRINT '';
PRINT 'Pour annuler : Arrêtez le script maintenant!';
PRINT 'Pour continuer : Exécutez le reste du script';
-- PAUSE VISUELLE - ARRÊTEZ ICI SI VOUS VOULEZ ANNULER

-- DÉSACTIVER LES CONTRAINTES
SET @Sql = '';
SELECT @Sql = @Sql + 
'ALTER TABLE ' + QUOTENAME(TABLE_SCHEMA) + '.' + QUOTENAME(TABLE_NAME) + ' NOCHECK CONSTRAINT ALL; '
FROM INFORMATION_SCHEMA.TABLES T
WHERE TABLE_TYPE = 'BASE TABLE'
AND TABLE_NAME NOT IN (SELECT TableName FROM @PreservedTables);

EXEC sp_executesql @Sql;
PRINT 'Contraintes désactivées';

-- VIDER LES TABLES
SET @Sql = '';
SELECT @Sql = @Sql + 
'DELETE FROM ' + QUOTENAME(TABLE_SCHEMA) + '.' + QUOTENAME(TABLE_NAME) + '; '
FROM INFORMATION_SCHEMA.TABLES T
WHERE TABLE_TYPE = 'BASE TABLE'
AND TABLE_NAME NOT IN (SELECT TableName FROM @PreservedTables);

EXEC sp_executesql @Sql;
PRINT 'Tables vidées';

-- RÉACTIVER LES CONTRAINTES
SET @Sql = '';
SELECT @Sql = @Sql + 
'ALTER TABLE ' + QUOTENAME(TABLE_SCHEMA) + '.' + QUOTENAME(TABLE_NAME) + ' CHECK CONSTRAINT ALL; '
FROM INFORMATION_SCHEMA.TABLES T
WHERE TABLE_TYPE = 'BASE TABLE'
AND TABLE_NAME NOT IN (SELECT TableName FROM @PreservedTables);

EXEC sp_executesql @Sql;
PRINT 'Contraintes réactivées';

-- RÉINITIALISER LES IDENTITIES
SET @Sql = '';
SELECT @Sql = @Sql + 
'DBCC CHECKIDENT (''' + QUOTENAME(TABLE_SCHEMA) + '.' + QUOTENAME(TABLE_NAME) + ''', RESEED, 0); '
FROM INFORMATION_SCHEMA.TABLES T
WHERE TABLE_TYPE = 'BASE TABLE'
AND TABLE_NAME NOT IN (SELECT TableName FROM @PreservedTables)
AND EXISTS (
    SELECT 1 FROM sys.columns C 
    JOIN sys.tables TB ON C.object_id = TB.object_id 
    WHERE C.is_identity = 1 
    AND TB.name = T.TABLE_NAME
    AND TB.schema_id = SCHEMA_ID(T.TABLE_SCHEMA)
);

IF LEN(@Sql) > 0
    EXEC sp_executesql @Sql;

PRINT 'Identités réinitialisées';
PRINT 'Réinitialisation TERMINÉE pour la base : ' + @CurrentDB;




CREATE PROCEDURE sp_ajouter_regle_workflow
    @ordre INT,
    @utilisateur_id INT,
    @type_notification_code VARCHAR(50),
    @est_actif BIT = 1
AS
BEGIN
    DECLARE @type_notification_id INT
    
    SELECT @type_notification_id = id 
    FROM type_notification 
    WHERE code = @type_notification_code
    
    IF @type_notification_id IS NOT NULL
    BEGIN
        INSERT INTO workflow_notification_mail (ordre, utilisateur_id, type_notification_id, est_actif)
        VALUES (@ordre, @utilisateur_id, @type_notification_id, @est_actif)
    END
    ELSE
    BEGIN
        RAISERROR('Type de notification non trouvé', 16, 1)
    END
END;

-- Créer un nouveau code
CREATE PROCEDURE sp_CreateValidationCode
    @utilisateur_id NVARCHAR(128),
    @Code CHAR(4),
    @ExpiryMinutes INT = 10
AS
BEGIN
    INSERT INTO ValidationCodes (utilisateur_id, Code, ExpiresAt)
    VALUES (@utilisateur_id, @Code, DATEADD(MINUTE, @ExpiryMinutes, GETUTCDATE()));
    
    SELECT SCOPE_IDENTITY() AS CodeId;
END

-- Valider un code
CREATE PROCEDURE sp_ValidateCode
    @utilisateur_id NVARCHAR(128),
    @Code CHAR(4)
AS
BEGIN
    DECLARE @CodeId BIGINT, @IsValid BIT = 0, @Message NVARCHAR(100);

    SELECT @CodeId = Id 
    FROM ValidationCodes 
    WHERE utilisateur_id = @utilisateur_id 
      AND Code = @Code
      AND ExpiresAt > GETUTCDATE()
      AND IsUsed = 0
      AND Attempts < MaxAttempts;

    IF @CodeId IS NOT NULL
    BEGIN
        UPDATE ValidationCodes 
        SET IsUsed = 1, UsedAt = GETUTCDATE()
        WHERE Id = @CodeId;
        
        SET @IsValid = 1;
        SET @Message = 'Code valide';
    END
    ELSE
    BEGIN
        -- Incrémenter les tentatives si code existe mais invalide
        UPDATE ValidationCodes 
        SET Attempts = Attempts + 1
        WHERE utilisateur_id = @utilisateur_id AND Code = @Code AND IsUsed = 0;
        
        SET @Message = 'Code invalide ou expiré';
    END

    SELECT @IsValid AS IsValid, @Message AS Message;
END
/* =========================================================
   Donnees de base (seeds) minimales
========================================================= */
INSERT INTO dbo.role(code, libelle) VALUES
('CLIENT','Client'),
('CONSULTANT','Consultant'),
('ADMIN','Administrateur');

INSERT INTO dbo.priorite_ticket(code, libelle, niveau) VALUES
('URGENT','Urgent', 3),('HAUTE','Haute',2),('NORMALE','Normale',1),('BASSE','Basse',0);

INSERT INTO dbo.type_ticket(code, libelle) VALUES
('INCIDENT','Incident'),('DEMANDE','Demande'),('EVOLUTION','Evolution'),('QUESTION','Question');

INSERT INTO dbo.statut_ticket(code, libelle, ordre_affichage) VALUES
('OUVERT','Ouvert',10),
('EN_COURS','En cours',20),
('EN_ATTENTE','En attente',30),
('EN_ATTENTE_CLIENT','En attente du client',40),
('PLANIFIE','Planifie',50),
('RESOLU','Resolue',60),
('CLOTURE','Cloture',70);

INSERT INTO dbo.statut_intervention(code, libelle, ordre_affichage) VALUES
('PROPOSEE','Proposee',10),
('PLANIFIEE','Planifiee',20),
('EN_COURS','En cours',30),
('A_VALIDER_CLIENT','A valider par le client',40),
('REFUSEE','Refusee',50),
('CLOTUREE','Cloturee',60);

INSERT INTO dbo.modalite_intervention(code, libelle) VALUES
('SITE','Sur site'),
('DISTANCE','A distance');

INSERT INTO dbo.type_interaction(code, libelle) VALUES
('MESSAGE','Message'),
('SYSTEME','Systeme'),
('RELANCE','Relance');

INSERT INTO dbo.canal_interaction(code, libelle) VALUES
('PORTAIL','Portail'),
('EMAIL','Email'),
('WHATSAPP','WhatsApp');

-- Insertion des types de notification spécifiques
INSERT INTO type_notification (code, libelle, description) VALUES
('CREATION_TICKET', 'Création de ticket', 'Notification envoyée lors de la création d''un nouveau ticket'),
('MODIFICATION_STATUT_TICKET', 'Modification statut ticket', 'Notification envoyée lors du changement de statut d''un ticket'),
('AJOUT_SOLUTION', 'Ajout d''une solution', 'Notification envoyée lors de l''ajout d''une solution à un ticket'),
('CLOTURE_TICKET', 'Clôture d''un ticket', 'Notification envoyée lors de la clôture d''un ticket');

-- Insertion de données exemple pour le workflow
-- Exemple pour la création de ticket : envoi d'abord au créateur, puis au responsable
INSERT INTO workflow_notification_mail (ordre, utilisateur_id, type_notification_id) VALUES
(1, 1870, 1),  -- Premier envoi pour création ticket (créateur)
(2, 1914, 1),  -- Deuxième envoi pour création ticket (responsable)