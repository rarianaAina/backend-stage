package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.depots.PieceJointeRepository;
import com.nrstudio.portail.domaine.PieceJointe;
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

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public PieceJointe televerser(
    @RequestParam("fichier") MultipartFile fichier,
    @RequestParam(value = "ticketId", required = false) Integer ticketId,
    @RequestParam(value = "interventionId", required = false) Integer interventionId,
    @RequestParam("utilisateurId") Integer utilisateurId
  ) {
    try {
      String nomOriginal = fichier.getOriginalFilename();
      String extension = nomOriginal != null && nomOriginal.contains(".")
        ? nomOriginal.substring(nomOriginal.lastIndexOf("."))
        : "";
      String nomUnique = UUID.randomUUID().toString() + extension;
      Path cheminFichier = Paths.get(repertoireUpload + nomUnique);

      Files.copy(fichier.getInputStream(), cheminFichier, StandardCopyOption.REPLACE_EXISTING);

      PieceJointe pj = new PieceJointe();
      pj.setTicketId(ticketId);
      pj.setInterventionId(interventionId);
      pj.setNomFichier(nomOriginal);
      pj.setCheminFichier(cheminFichier.toString());
      pj.setTypeFichier(fichier.getContentType());
      pj.setTailleFichier(fichier.getSize());
      pj.setTeleverseParUtilisateurId(utilisateurId);
      pj.setDateTelechargement(LocalDateTime.now());

      return repo.save(pj);
    } catch (IOException e) {
      throw new RuntimeException("Erreur lors du téléversement du fichier", e);
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
          .contentType(MediaType.parseMediaType(pj.getTypeFichier()))
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
