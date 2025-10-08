package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.depots.ProduitRepository;
import com.nrstudio.portail.domaine.Produit;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produits")
@CrossOrigin
public class ProduitControleur {

  private final ProduitRepository repo;

  public ProduitControleur(ProduitRepository repo) {
    this.repo = repo;
  }

  @GetMapping
  public List<Produit> lister() {
    return repo.findAll();
  }

  @GetMapping("/actifs")
  public List<Produit> listerActifs() {
    return repo.findByActif(true);
  }

  @GetMapping("/{id}")
  public Produit obtenir(@PathVariable("id") Integer id) {
    return repo.findById(id).orElseThrow();
  }

  @GetMapping("/company/{companyId}")
  public List<Produit> listerParCompany(@PathVariable("companyId") Integer companyId) {
    return repo.findByActif(true);
  }
}
