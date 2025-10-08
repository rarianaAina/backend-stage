# ANALYSE DÉTAILLÉE DES STRUCTURES CRM SAGE

## 📊 TABLES PRINCIPALES

### 1. **Cases** (Tickets CRM)
```
Case_CaseId                 int      PK
Case_PrimaryCompanyId       int      → Company
Case_PrimaryPersonId        int      → Person  
Case_AssignedUserId         int      → Users
Case_Description            nvarchar (titre court)
Case_CustomerRef            nvarchar (référence client)
Case_Product                nvarchar (nom produit en texte)
Case_ProductId              int      (ID produit)
Case_ProblemNote            nvarchar (description détaillée)
Case_Priority               nvarchar ("Low", "Normal", "High", "Urgent")
Case_Status                 nvarchar ("Open", "In Progress", "Closed", etc.)
Case_Opened                 datetime
Case_OpenedBy               int      → Users
Case_Closed                 datetime
Case_ClosedBy               int      → Users
Case_CreatedDate            datetime
Case_UpdatedDate            datetime
Case_Deleted                tinyint
Case_INTERVENTIONId         int      → INTERVENTION
```

**POINTS CLÉS** :
- ✅ `Case_PrimaryCompanyId` → Lien vers Company (pas Person)
- ✅ `Case_PrimaryPersonId` → Contact optionnel
- ✅ `Case_ProductId` existe en INT
- ⚠️ `Case_Product` est en texte (nvarchar) pas INT
- ✅ `Case_INTERVENTIONId` → Lien vers intervention

### 2. **Company** (Sociétés)
```
Comp_CompanyId              int      PK
Comp_Name                   nvarchar
Comp_Status                 nvarchar
Comp_Type                   nvarchar
Comp_WebSite                nvarchar
Comp_Deleted                tinyint
Comp_CreatedDate            datetime
Comp_UpdatedDate            datetime
comp_nif_iltx               nvarchar (NIF Madagascar)
comp_stat_iltx              nvarchar (STAT Madagascar)
comp_reference              nvarchar
```

**POINTS CLÉS** :
- ✅ Structure simple et cohérente
- ✅ Champs personnalisés Madagascar (_iltx)

### 3. **Person** (Contacts)
```
Pers_PersonId               int      PK
Pers_CompanyId              int      → Company
Pers_FirstName              nvarchar
Pers_LastName               nvarchar
Pers_Title                  nvarchar
Pers_EmailAddress           nvarchar (N'EXISTE PAS DIRECTEMENT!)
Pers_Status                 nvarchar
Pers_Deleted                tinyint
```

**⚠️ ATTENTION** :
- Person n'a PAS de champ email direct dans cette table
- Les emails sont dans une table séparée (Email)
- La vue `vEmailCompanyAndPerson` fait la jointure

### 4. **Products** (Produits)
```
Prod_ProductId              int      PK
Prod_Name                   nvarchar
Prod_Description            nvarchar
Prod_Deleted                tinyint
Prod_CreatedDate            datetime
```

**POINTS CLÉS** :
- ✅ Très simple
- ⚠️ Pas de code produit séparé

### 5. **Users** (Utilisateurs internes)
```
User_UserId                 int      PK
User_Logon                  nvarchar
User_FirstName              nvarchar
User_LastName               nvarchar
User_EmailAddress           nvarchar
User_MobilePhone            nvarchar
User_Deleted                int
User_Disabled               nchar
```

### 6. **INTERVENTION**
```
inte_INTERVENTIONid         int      PK
inte_companyid              int      → Company
inte_product                int      → Products
inte_date                   datetime (date prévue)
inte_datedebut              datetime (début réel)
inte_detefin                datetime (fin réelle)
inte_details                nvarchar
inte_interlocuteur          int      → Person
inte_UserId                 int      → Users (consultant)
inte_type_intervention      nchar
inte_Deleted                int
```

## 🔍 INCOHÉRENCES DÉTECTÉES

### A. Dans nos Services de Sync

#### 1. **CrmTicketSyncService** ⚠️
```java
// INCORRECT : On lit "Case_Product" comme INT
String produitStr = row.get("Case_Product");
// Mais Case_Product est nvarchar, pas int!

// CORRECT : Devrait être
Integer produitId = (Integer) row.get("Case_ProductId");
```

#### 2. **CrmProductSyncService** ⚠️
```java
// On utilise "NewProduct" mais la table est "Products"
SELECT Prod_ProductId, Prod_Name FROM NewProduct

// CORRECT : Devrait être
SELECT Prod_ProductId, Prod_Name FROM Products
```

#### 3. **CrmPersonSyncService** ⚠️
```java
// Person n'a PAS de champ email direct
// Les emails sont dans une table Email séparée
// Devrait utiliser la vue vEmailCompanyAndPerson
```

#### 4. **InterventionService** ⚠️
```java
// Création appointment au lieu d'INTERVENTION
INSERT INTO dbo.Appointments...

// CORRECT : Devrait insérer dans INTERVENTION
INSERT INTO dbo.INTERVENTION...
```

### B. Dans notre Script SQL

#### 1. **Table Intervention** ⚠️
Notre structure ne correspond PAS à INTERVENTION du CRM :
- Manque : `inte_datedebut`, `inte_detefin`
- Mauvais nom : `date_prevue` au lieu de `inte_date`
- Manque : `inte_interlocuteur`, `inte_details`

### C. Champs Email/Téléphone

⚠️ **Person n'a PAS** :
- `Pers_EmailAddress` (n'existe pas directement)
- `Pers_PhoneNumber` (n'existe pas directement)

Ces infos sont dans des tables séparées (Email, Phone)

## ✅ CE QUI EST CORRECT

1. ✅ Company → company (mapping OK)
2. ✅ Person → client (mapping OK)
3. ✅ Users → utilisateur (mapping OK)
4. ✅ Cases utilise Case_PrimaryCompanyId
5. ✅ INTERVENTION utilise inte_companyid

## 🔧 CORRECTIONS À FAIRE

### PRIORITÉ 1 - Services Sync

1. **CrmTicketSyncService** :
   - Utiliser `Case_ProductId` (INT) au lieu de `Case_Product` (nvarchar)
   - Vérifier `Case_PrimaryCompanyId` existe

2. **CrmProductSyncService** :
   - Changer `NewProduct` → `Products`
   - Utiliser `Prod_ProductId`, `Prod_Name`

3. **CrmPersonSyncService** :
   - Utiliser vue `vEmailCompanyAndPerson` pour les emails
   - Ne pas supposer que Person a email direct

4. **CrmInterventionSyncService** :
   - Mapper vers table `INTERVENTION` (pas Appointments)
   - Utiliser les bons champs CRM

### PRIORITÉ 2 - Entités Java

1. **Intervention.java** :
   - Ajouter tous les champs manquants
   - Aligner avec structure CRM

### PRIORITÉ 3 - Script SQL

1. Corriger table `intervention` du portail

## 📋 PLAN D'ACTION

1. ✅ Analyser structures CRM
2. ⏳ Corriger CrmProductSyncService
3. ⏳ Corriger CrmTicketSyncService  
4. ⏳ Corriger CrmPersonSyncService
5. ⏳ Corriger InterventionService
6. ⏳ Corriger Intervention.java
7. ⏳ Nettoyer code inutile
