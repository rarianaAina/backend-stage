package com.nrstudio.portail.dto.rapports;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class StatistiquesRapportDto {
    private int totalDemandes;
    private int demandesCreees;
    private int demandesResolues;
    private double tauxResolution;
    private double tempsMoyenReponse;
    private double satisfactionMoyenne;
    private Map<String, Integer> evolutionMensuelle;
}


