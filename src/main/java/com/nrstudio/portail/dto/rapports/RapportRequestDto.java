package com.nrstudio.portail.dto.rapports;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class RapportRequestDto {
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String typeRapport; // "activite", "performance", "satisfaction", "credits"
}

