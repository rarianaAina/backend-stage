package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.depots.piecesjointes.PieceJointeRepository;
import com.nrstudio.portail.domaine.PieceJointe;
import com.nrstudio.portail.dto.piecejointe.PieceJointeRequest;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pieces-jointes")
@CrossOrigin
public class PieceJointeControleur {

  private final PieceJointeRepository repo;
  private final String repertoireUpload = "uploads/";

  public PieceJointeControleur(PieceJointeRepository repo) {
    this.repo = repo;

    try {
      Files.createDirectories(Paths.get(repertoireUpload));
    } catch (IOException e) {
      throw new RuntimeException("Impossible de créer le répertoire d'upload", e);
    }
  }

  @GetMapping
  public List<PieceJointe> lister() {
    return repo.findAll();
  }

  @GetMapping("/{id}")
  public PieceJointe obtenir(@PathVariable("id") Integer id) {
    return repo.findById(id).orElseThrow();
  }

  @GetMapping("/ticket/{ticketId}")
  public List<PieceJointe> listerParTicket(@PathVariable("ticketId") Integer ticketId) {
    return repo.findByTicketId(ticketId);
  }

  @GetMapping("/intervention/{interventionId}")
  public List<PieceJointe> listerParIntervention(@PathVariable("interventionId") Integer interventionId) {
    return repo.findByInterventionId(interventionId);
  }

  @PostMapping(value = "/{ticketId}/rajouter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public PieceJointe televerser(
    @PathVariable("ticketId") Integer ticketId,
    @RequestParam("fichier") MultipartFile fichier,
    @RequestParam(value = "commentaires", required = false) String commentaires,
    @RequestParam("utilisateurId") String utilisateurIdStr // Recevoir comme String
  ) {
    try {
      // Convertir l'ID utilisateur en Integer
      Integer utilisateurId = Integer.parseInt(utilisateurIdStr);
      
      System.out.println("=== UPLOAD FICHIER ===");
      System.out.println("Ticket ID: " + ticketId);
      System.out.println("Utilisateur ID: " + utilisateurId);
      System.out.println("Commentaires: " + commentaires);
      System.out.println("Nom fichier: " + fichier.getOriginalFilename());
      System.out.println("Taille: " + fichier.getSize());

      // Validation
      if (fichier.isEmpty()) {
        throw new RuntimeException("Le fichier est vide");
      }

      // Générer un nom de fichier unique
      String nomOriginal = fichier.getOriginalFilename();
      String extension = nomOriginal != null && nomOriginal.contains(".")
        ? nomOriginal.substring(nomOriginal.lastIndexOf("."))
        : "";
      String nomUnique = UUID.randomUUID().toString() + extension;
      Path cheminFichier = Paths.get(repertoireUpload + nomUnique);

      // Créer le dossier si nécessaire
      Files.createDirectories(cheminFichier.getParent());

      // Sauvegarder le fichier
      Files.copy(fichier.getInputStream(), cheminFichier, StandardCopyOption.REPLACE_EXISTING);

      // Créer et sauvegarder l'entité
      PieceJointe pj = new PieceJointe();
      pj.setTicketId(ticketId);
      pj.setNomFichier(nomOriginal);
      pj.setCheminFichier(cheminFichier.toString());
      pj.setUrlContenu("/api/pieces-jointes/telecharger/" + nomUnique);
      pj.setTypeMime(fichier.getContentType());
      pj.setTailleOctets(fichier.getSize());
      pj.setAjouteParUtilisateurId(utilisateurId);
      pj.setDateAjout(LocalDateTime.now());
      pj.setCommentaires(commentaires);

      return repo.save(pj);
      
    } catch (NumberFormatException e) {
      throw new RuntimeException("ID utilisateur invalide: " + utilisateurIdStr);
    } catch (IOException e) {
      throw new RuntimeException("Erreur lors du téléversement du fichier: " + e.getMessage(), e);
    }
  }
  @GetMapping("/telecharger/{id}")
  public ResponseEntity<Resource> telecharger(@PathVariable("id") Integer id) {
    try {
      PieceJointe pj = repo.findById(id).orElseThrow();
      Path cheminFichier = Paths.get(pj.getCheminFichier());
      Resource resource = new UrlResource(cheminFichier.toUri());

      if (resource.exists() || resource.isReadable()) {
        return ResponseEntity.ok()
          .contentType(MediaType.parseMediaType(pj.getTypeMime()))
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pj.getNomFichier() + "\"")
          .body(resource);
      } else {
        throw new RuntimeException("Fichier introuvable");
      }
    } catch (Exception e) {
      throw new RuntimeException("Erreur lors du téléchargement du fichier", e);
    }
  }

  @DeleteMapping("/{id}")
  public void supprimer(@PathVariable("id") Integer id) {
    try {
      PieceJointe pj = repo.findById(id).orElseThrow();
      Path cheminFichier = Paths.get(pj.getCheminFichier());
      Files.deleteIfExists(cheminFichier);
      repo.deleteById(id);
    } catch (IOException e) {
      throw new RuntimeException("Erreur lors de la suppression du fichier", e);
    }
  }
}
