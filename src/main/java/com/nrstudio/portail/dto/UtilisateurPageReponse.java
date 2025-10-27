package com.nrstudio.portail.dto;

import com.nrstudio.portail.domaine.Utilisateur;
import lombok.Data;
import java.util.List;

@Data
public class UtilisateurPageReponse {
    private List<Utilisateur> utilisateurs;
    private int pageCourante;
    private int totalPages;
    private long totalElements;
    private int taillePage;

    public UtilisateurPageReponse(List<Utilisateur> utilisateurs, int pageCourante, int totalPages, long totalElements, int taillePage) {
        this.utilisateurs = utilisateurs;
        this.pageCourante = pageCourante;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.taillePage = taillePage;
    }
}