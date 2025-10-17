package com.nrstudio.portail.services;

import org.springframework.stereotype.Service;
import com.nrstudio.portail.domaine.UtilisateurRole;
import com.nrstudio.portail.depots.UtilisateurRoleRepository;

@Service
public class UtilisateurRoleService {
    public final UtilisateurRoleRepository repo;
    public UtilisateurRoleService(UtilisateurRoleRepository repo) {
        this.repo = repo;
    }

    public void enregistrerUtilisateurRole(UtilisateurRole ur) {
        repo.save(ur);
    }
}
