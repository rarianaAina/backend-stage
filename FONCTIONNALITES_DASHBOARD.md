# 📊 FONCTIONNALITÉS DASHBOARD & CRÉDITS HORAIRES

## ✅ Nouveaux Composants Créés

### 1. Modèle (Entité)
- ✅ **CreditHoraire.java** - Gestion des forfaits horaires de support

### 2. DTOs (11 nouveaux)
- ✅ **DashboardClientDto** - Dashboard complet pour client
- ✅ **DashboardAdminDto** - Dashboard complet pour admin
- ✅ **StatistiquesTicketsDto** - Statistiques tickets client
- ✅ **StatistiquesGlobalesDto** - Statistiques globales admin
- ✅ **CreditHoraireDto** - Informations crédit horaire
- ✅ **TicketRecentDto** - Ticket récent formaté
- ✅ **InterventionProchaine** - Intervention à venir
- ✅ **ConsultantPerformanceDto** - Performance consultant
- ✅ **DureeTraitementDto** - Durées de traitement
- ✅ **ChartDataDto** - Données pour graphiques
- ✅ **ChartDatasetDto** - Dataset pour graphiques

### 3. Repository
- ✅ **CreditHoraireRepository** - Accès BDD crédits horaires

### 4. Services
- ✅ **CreditHoraireService** - Gestion crédits horaires
- ✅ **DashboardService** - Agrégation données dashboard

### 5. Contrôleurs (2 nouveaux)
- ✅ **DashboardControleur** - API dashboard
- ✅ **CreditHoraireControleur** - API crédits horaires

### 6. Base de Données
- ✅ **Table credit_horaire** ajoutée au script SQL

---

## 🎯 API ENDPOINTS

### Dashboard Client

#### GET /api/dashboard/client/{userId}
**Description** : Données complètes du dashboard client

**Réponse** :
```json
{
  "statistiquesTickets": {
    "totalTickets": 45,
    "ticketsOuverts": 5,
    "ticketsEnCours": 12,
    "ticketsClotures": 28,
    "ticketsUrgents": 2
  },
  "creditsHoraires": [
    {
      "id": 1,
      "nomCompany": "Entreprise ABC",
      "nomProduit": "Logiciel ERP",
      "periodeDebut": "2025-01-01",
      "periodeFin": "2025-12-31",
      "heuresAllouees": 100,
      "heuresConsommees": 45,
      "heuresRestantes": 55,
      "pourcentageUtilisation": 45.0,
      "actif": true,
      "expire": false
    }
  ],
  "ticketsRecents": [...],
  "interventionsProchaines": [...],
  "ticketsParStatut": {
    "Ouvert": 5,
    "En cours": 10,
    "Cloturé": 30
  },
  "ticketsParPriorite": {
    "Basse": 10,
    "Normale": 25,
    "Haute": 8,
    "Urgente": 2
  },
  "ticketsParProduit": {
    "ERP": 20,
    "CRM": 15,
    "Site Web": 10
  },
  "dureesMoyennes": {
    "dureeMoyenneHeures": 48.5,
    "dureeMoyenneJours": 2.02,
    "ticketsTraitesRapidement": 15,
    "ticketsTraitesNormalement": 20,
    "ticketsTraitesLentement": 10
  }
}
```

#### GET /api/dashboard/client/{userId}/chart
**Description** : Données graphique pour visualisation

**Réponse** :
```json
{
  "labels": ["Ouvert", "En cours", "En attente", "Planifié", "Résolu", "Cloturé"],
  "datasets": [
    {
      "label": "Tickets",
      "data": [5, 10, 3, 2, 5, 30],
      "backgroundColor": "rgba(54, 162, 235, 0.5)",
      "borderColor": "rgba(54, 162, 235, 1)"
    }
  ]
}
```

### Dashboard Admin

#### GET /api/dashboard/admin
**Description** : Dashboard complet administrateur

**Réponse** :
```json
{
  "statistiquesGlobales": {
    "totalTickets": 250,
    "ticketsOuverts": 35,
    "ticketsEnCours": 80,
    "ticketsClotures": 135,
    "totalCompanies": 25,
    "totalConsultants": 8,
    "interventionsPlanifiees": 15
  },
  "ticketsParStatut": {...},
  "ticketsParPriorite": {...},
  "performancesConsultants": {
    "Jean Dupont": {
      "consultantId": 1,
      "consultantNom": "Jean Dupont",
      "ticketsEnCours": 12,
      "ticketsClotures": 45,
      "interventionsRealisees": 30,
      "tauxResolution": 78.95,
      "dureeMoyenneTraitement": 36.5
    }
  },
  "ticketsRecents": [...],
  "ticketsParCompany": {
    "Entreprise A": 45,
    "Entreprise B": 32,
    "Entreprise C": 28
  },
  "ticketsParProduit": {...},
  "dureesMoyennes": {...}
}
```

#### GET /api/dashboard/admin/chart
**Description** : Données graphique admin

---

### Crédits Horaires

#### GET /api/credits-horaires/client/{userId}
**Description** : Crédits horaires pour un utilisateur

**Réponse** :
```json
[
  {
    "id": 1,
    "nomCompany": "Entreprise ABC",
    "nomProduit": "Logiciel ERP",
    "periodeDebut": "2025-01-01",
    "periodeFin": "2025-12-31",
    "heuresAllouees": 100,
    "heuresConsommees": 45,
    "heuresRestantes": 55,
    "pourcentageUtilisation": 45.0,
    "actif": true,
    "expire": false
  }
]
```

#### GET /api/credits-horaires/company/{companyId}
**Description** : Tous les crédits d'une company

#### GET /api/credits-horaires/company/{companyId}/actifs
**Description** : Crédits actifs uniquement

#### GET /api/credits-horaires/company/{companyId}/restant
**Description** : Total heures restantes

**Réponse** :
```json
120
```

---

## 📊 DONNÉES FOURNIES

### Pour le Client

1. **Statistiques Tickets**
   - Total tickets
   - Tickets ouverts
   - Tickets en cours
   - Tickets clôturés
   - Tickets urgents

2. **Crédits Horaires**
   - Heures allouées
   - Heures consommées
   - Heures restantes
   - Pourcentage utilisation
   - Statut (actif/expiré)

3. **Répartitions**
   - Tickets par statut
   - Tickets par priorité
   - Tickets par produit

4. **Durées de Traitement**
   - Durée moyenne (heures/jours)
   - Tickets traités rapidement (< 24h)
   - Tickets traités normalement (24-72h)
   - Tickets traités lentement (> 72h)

5. **Tickets Récents** (10 derniers)
6. **Interventions Prochaines** (10 prochaines)

### Pour l'Admin

Toutes les données client PLUS :

1. **Statistiques Globales**
   - Total companies
   - Total consultants
   - Interventions planifiées

2. **Performances Consultants**
   - Tickets en cours par consultant
   - Tickets clôturés par consultant
   - Interventions réalisées
   - Taux de résolution
   - Durée moyenne de traitement

3. **Répartitions Supplémentaires**
   - Tickets par company
   - Répartition par consultant

---

## 🗄️ TABLE CREDIT_HORAIRE

```sql
CREATE TABLE dbo.credit_horaire (
    id                      INT IDENTITY(1,1) PRIMARY KEY,
    company_id              INT NOT NULL,
    produit_id              INT NULL,
    periode_debut           DATE NOT NULL,
    periode_fin             DATE NOT NULL,
    heures_allouees         INT NOT NULL CHECK (heures_allouees >= 0),
    heures_consommees       INT NOT NULL DEFAULT(0) CHECK (heures_consommees >= 0),
    heures_restantes        INT NOT NULL CHECK (heures_restantes >= 0),
    actif                   BIT NOT NULL DEFAULT(1),
    renouvelable            BIT NOT NULL DEFAULT(0),
    remarques               NVARCHAR(1000) NULL,
    date_creation           DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    date_mise_a_jour        DATETIME2(0) NOT NULL DEFAULT(SYSDATETIME()),
    CONSTRAINT FK_credit_horaire_company FOREIGN KEY (company_id) REFERENCES dbo.company(id),
    CONSTRAINT FK_credit_horaire_produit FOREIGN KEY (produit_id) REFERENCES dbo.produit(id)
);
```

**Fonctionnalités** :
- Gestion des forfaits horaires par company
- Optionnellement liés à un produit spécifique
- Période de validité (début/fin)
- Suivi automatique des heures (allouées/consommées/restantes)
- Statut actif/inactif
- Renouvelable ou non
- Remarques libres

---

## 🔧 UTILISATION

### Consommer des Heures

```java
// Dans un service
creditHoraireService.consommerHeures(companyId, produitId, nbHeures);
```

### Désactiver Crédits Expirés

```java
// Job automatique à planifier
@Scheduled(cron = "0 0 1 * * *") // Tous les jours à 1h
public void desactiverCreditsExpires() {
    creditHoraireService.desactiverCreditsExpires();
}
```

---

## 📈 EXEMPLES FRONTEND

### Dashboard Client (React/Vue)

```javascript
// Récupérer dashboard
const response = await fetch(`/api/dashboard/client/${userId}`);
const dashboard = await response.json();

// Afficher statistiques
console.log(`Total tickets: ${dashboard.statistiquesTickets.totalTickets}`);
console.log(`Heures restantes: ${dashboard.creditsHoraires[0].heuresRestantes}`);

// Récupérer données graphique
const chartResponse = await fetch(`/api/dashboard/client/${userId}/chart`);
const chartData = await chartResponse.json();

// Utiliser avec Chart.js, ApexCharts, etc.
```

### Dashboard Admin

```javascript
// Récupérer dashboard admin
const response = await fetch('/api/dashboard/admin');
const adminDashboard = await response.json();

// Afficher performances consultants
Object.values(adminDashboard.performancesConsultants).forEach(perf => {
  console.log(`${perf.consultantNom}: ${perf.tauxResolution}% résolution`);
});
```

---

## ✅ STATUT

**Toutes les fonctionnalités dashboard et crédits horaires sont implémentées et prêtes à l'emploi.**

- ✅ Modèle de données complet
- ✅ API REST complète
- ✅ Logique métier implémentée
- ✅ Agrégation des données
- ✅ Calculs statistiques
- ✅ Performances consultants
- ✅ Gestion crédits horaires

**Date : 2025-10-08**
