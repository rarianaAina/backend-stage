package com.nrstudio.portail.services;

import com.nrstudio.portail.depots.CompanyRepository;
import com.nrstudio.portail.depots.ProduitRepository;
import com.nrstudio.portail.depots.TicketRepository;
import com.nrstudio.portail.depots.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.nrstudio.portail.domaine.Produit;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProduitService {
    
    @Autowired
    private ProduitRepository produitsRepository;

    public List<Produit> listerProduitsActifs() {
        return produitsRepository.findByActif(true);
    }
}

