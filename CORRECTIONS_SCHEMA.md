# Corrections appliquées selon le schéma SQL

## Entités corrigées

### 1. Utilisateur ✓
- Ajouté `mot_de_passe_salt`
- Ajouté `whatsapp_numero`
- Ajouté `date_derniere_connexion`
- Ajouté `date_creation`
- Changé `idExterneCrm` de Integer vers String (VARCHAR(100))
- Supprimé `typeCompte`, `companyId`, `companyNom`, `role` (utiliser tables de relation)

### 2. Client ✓
- Nouvelle entité créée avec tous les champs du schéma

### 3. Produit ✓
- Renommé `nom` → `libelle`
- Ajouté `code_produit`
- Supprimé `reference`, `categorie`, `version`
- Changé `idExterneCrm` de Integer vers String

## À corriger

### Ticket
- `id_externe_crm` devrait être INT (pas String)
- Le reste semble OK

### Intervention
- Renommer `reference` (n'existe pas dans le schéma)
- Renommer `raison` → `motif`
- Renommer `dateIntervention` → `date_prevue`
- Ajouter `date_debut_reel`, `date_fin_reelle`, `date_validee`
- Renommer `statutInterventionId` → `statut_intervention_id`
- Ajouter `modalite_intervention_id`
- Supprimer `ficheIntervention`, `valideeParClient` (table séparée)

### Interaction
- Renommer `message` → `contenu`
- Ajouter `type_interaction_id`, `canal_interaction_id`
- Supprimer `typeInteraction`, `visibleClient`

### PieceJointe
- Renommer champs selon schéma
- Ajouter `url_contenu`, `type_mime`, `taille_octets`
- Renommer `televerseParUtilisateurId` → `ajoute_par_utilisateur_id`
