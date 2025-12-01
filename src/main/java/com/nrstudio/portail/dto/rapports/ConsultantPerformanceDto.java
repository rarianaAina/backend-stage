package com.nrstudio.portail.dto.rapports;

import lombok.Data;

@Data
public class ConsultantPerformanceDto {
    private Integer consultantId;
    private String consultantNom;
    private Integer ticketsEnCours;
    private Integer ticketsClotures;
    private Double tauxResolution;
    
    public ConsultantPerformanceDto() {}
}