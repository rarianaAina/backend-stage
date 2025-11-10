package com.nrstudio.portail.controleurs;

import com.nrstudio.portail.domaine.UtilisateurRole;
import com.nrstudio.portail.depots.UtilisateurRoleRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/utilisateur-role")
@CrossOrigin
public class UtilisateurRoleControleur {

    private final UtilisateurRoleRepository utilisateurRoleRepository;

    public UtilisateurRoleControleur(UtilisateurRoleRepository utilisateurRoleRepository) {
        this.utilisateurRoleRepository = utilisateurRoleRepository;
    }

    // @GetMapping("/roles/{utilisateurId}")
    // public List<String> getRolesByUtilisateur(@PathVariable("utilisateurId") Integer utilisateurId) {
    //     System.out.println(utilisateurId);
    //     List<UtilisateurRole> roles = utilisateurRoleRepository.findAll()
    //         .stream()
    //         .filter(ur -> ur.getUtilisateur().getId().equals(utilisateurId))
    //         .collect(Collectors.toList());
    //     return roles.stream()
    //         .map(ur -> ur.getRole().getCode())
    //         .collect(Collectors.toList());
    // }

    @GetMapping("/roles/{utilisateurId}")
    public List<String> getRolesByUtilisateur(@PathVariable("utilisateurId") Integer utilisateurId) {
        return utilisateurRoleRepository.findRoleCodesByUtilisateurId(utilisateurId);
    }
}