# Intégration CRM - État Final

## ✅ Corrections Appliquées

### 1. Structure des Entités selon le Schéma SQL

#### Utilisateur
- ✅ Aligné avec le schéma SQL
- ✅ Ajout de `mot_de_passe_salt`, `whatsapp_numero`, `date_derniere_connexion`
- ✅ `id_externe_crm` en VARCHAR(100)
- ⚠️ Champs supprimés : `typeCompte`, `companyId`, `companyNom`, `role` (utiliser tables de relation)

#### Client (NOUVEAU)
- ✅ Entité créée selon le schéma SQL
- ✅ Représente les sociétés clientes (Company du CRM)
- ✅ Repository créé

#### Produit
- ✅ Renommé `nom` → `libelle`
- ✅ Ajout de `code_produit`
- ✅ `id_externe_crm` en VARCHAR(100)

### 2. Services de Synchronisation CRM

#### CrmCompanySyncService ✅
```
Company (CRM) → Client (Portail)
```
- Synchronise les sociétés clientes (Company.Comp_Type = 'Customer')
- Crée des entités **Client** dans la base portail
- S'exécute tous les jours à 2h00

#### CrmPersonSyncService ✅
```
Person (CRM) → Utilisateur (Portail)
```
- Synchronise les contacts des sociétés clientes
- Crée des **Utilisateur** liés aux **Client** (via CompanyId)
- Vérifie l'existence du Client avant de créer l'utilisateur
- Génère des mots de passe temporaires
- S'exécute tous les jours à 2h10

#### CrmUsersSyncService ✅
```
Users (CRM) → Utilisateur (Portail)
```
- Synchronise les utilisateurs internes (Consultants/Admin)
- Détermine automatiquement le rôle (ADMIN ou CONSULTANT)
- Génère des mots de passe temporaires
- S'exécute tous les jours à 2h20

#### CrmProductSyncService ✅
```
NewProduct (CRM) → Produit (Portail)
```
- Synchronise les produits
- S'exécute tous les jours à 2h30

#### CrmTicketSyncService ⚠️
```
Cases (CRM) → Ticket (Portail)
```
- Import des tickets depuis le CRM
- ⚠️ **À ADAPTER** : Utiliser Client au lieu de Utilisateur pour client_id

#### CrmInterventionSyncService ✅
```
Appointments (CRM) → Intervention (Portail)
```
- Synchronisation bidirectionnelle
- Import toutes les 15 min, Export toutes les 20 min

### 3. Mapping CRM ↔ Portail

#### ID Externes
```
Company.Comp_CompanyId → Client.id_externe_crm = "123"
Person.Pers_PersonId → Utilisateur.id_externe_crm = "PERSON-456"
Users.User_UserId → Utilisateur.id_externe_crm = "USER-789"
```

#### Relations
```
CRM:
- Company (1) ← (N) Person
- Users (table séparée pour consultants/admin)

Portail:
- Client (1) ← (N) Utilisateur (via CompanyId dans Person)
- Utilisateur (interne : consultants/admin, pas de Client)
- Ticket.client_id → Client.id
```

## ⚠️ Adaptations Restantes Nécessaires

### 1. CrmTicketSyncService
```java
// AVANT (incorrect)
t.setClientId(clientIdPortail); // clientIdPortail = Utilisateur.id

// APRÈS (correct)
// Trouver le Client via le CompanyId du CRM
Client client = clients.findByIdExterneCrm(String.valueOf(companyId)).orElse(null);
if (client != null) {
  t.setClientId(client.getId()); // client.id référence la table client
}
```

### 2. TicketService
```java
// Adapter mapClientIdToCrmCompanyId
private Integer mapClientIdToCrmCompanyId(Integer clientId) {
  Client client = clients.findById(clientId).orElse(null);
  if (client != null && client.getIdExterneCrm() != null) {
    return Integer.valueOf(client.getIdExterneCrm());
  }
  return null;
}
```

### 3. Tables Relationnelles à Créer

#### utilisateur_role
```java
@Entity
public class UtilisateurRole {
  @Id @GeneratedValue
  private Integer id;

  @ManyToOne
  private Utilisateur utilisateur;

  @ManyToOne
  private Role role;

  @ManyToOne
  private Client client; // Optionnel : pour lier utilisateur à un client
}
```

#### client_contact
```java
@Entity
public class ClientContact {
  @Id @GeneratedValue
  private Integer id;

  @ManyToOne
  private Client client;

  @ManyToOne
  private Utilisateur utilisateur;

  private String fonction;
  private boolean principal;
}
```

## 📋 Ordre de Synchronisation

```
1. CrmCompanySyncService (2h00) → Crée les Client
2. CrmPersonSyncService (2h10) → Crée les Utilisateur clients
3. CrmUsersSyncService (2h20) → Crée les Utilisateur internes
4. CrmProductSyncService (2h30) → Crée les Produit
5. CrmTicketSyncService (toutes les 30 min) → Import Ticket
6. CrmInterventionSyncService (15-20 min) → Sync Intervention
```

## 🔑 Points Clés

1. **Company → Client** : Les sociétés sont dans la table `client`
2. **Person → Utilisateur** : Les contacts sont dans `utilisateur`, liés au `client`
3. **Users → Utilisateur** : Les consultants/admin sont aussi dans `utilisateur`
4. **Ticket.client_id** : Référence `client.id` (pas `utilisateur.id`)
5. **Différenciation** : Utiliser `utilisateur_role` pour gérer les rôles

## 🚀 Prochaines Étapes

1. Adapter CrmTicketSyncService pour utiliser Client
2. Créer les entités utilisateur_role et client_contact
3. Adapter TicketService et InterventionService
4. Créer les tables référentielles (role, statut_ticket, etc.)
5. Tester la synchronisation complète
