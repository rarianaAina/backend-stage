package com.nrstudio.portail.dto.piecejointe;

import org.springframework.web.multipart.MultipartFile;

public class PieceJointeRequest {
    private MultipartFile fichier;
    private String commentaires;
    private Integer utilisateurId;
    
    // Getters et setters
    public MultipartFile getFichier() { return fichier; }
    public void setFichier(MultipartFile fichier) { this.fichier = fichier; }
    
    public String getCommentaires() { return commentaires; }
    public void setCommentaires(String commentaires) { this.commentaires = commentaires; }
    
    public Integer getUtilisateurId() { return utilisateurId; }
    public void setUtilisateurId(Integer utilisateurId) { this.utilisateurId = utilisateurId; }
}