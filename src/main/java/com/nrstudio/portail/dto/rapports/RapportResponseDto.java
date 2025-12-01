package com.nrstudio.portail.dto.rapports;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.nrstudio.portail.dto.rapports.*;

@Data
public class RapportResponseDto {
    private StatistiquesRapportDto statistiques;
    private DonneesGraphiqueDto donneesGraphique;
    private List<ConsultantPerformanceDto> performancesConsultants;
    private DonneesSatisfactionDto satisfaction;
    private Map<String, Object> donneesBrutes;
}
