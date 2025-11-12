package com.nrstudio.portail.dto.rapports;

import lombok.Data;
import java.util.List;

@Data
public class DatasetGraphiqueDto {
    private String label;
    private List<Integer> data;
    private String backgroundColor; // Reste String pour couleur unique
    private String borderColor;     // Reste String pour couleur unique
    
    // Pour les graphiques avec couleurs multiples (comme Doughnut)
    private List<String> backgroundColors;
    
    public DatasetGraphiqueDto() {}
}