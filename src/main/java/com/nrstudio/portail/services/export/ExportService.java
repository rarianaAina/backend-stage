package com.nrstudio.portail.services.export;

import com.nrstudio.portail.dto.rapports.RapportRequestDto;
import com.nrstudio.portail.dto.rapports.RapportResponseDto;
import com.nrstudio.portail.services.rapports.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class ExportService {

    private final RapportService rapportService;

    public ExportService(RapportService rapportService) {
        this.rapportService = rapportService;
    }

    public Resource genererPDF(RapportRequestDto request) {
        try {
            // Générer le rapport
            RapportResponseDto rapport = rapportService.genererRapport(request);
            
            // Simuler la génération d'un PDF (à implémenter avec une librairie comme iText ou Apache PDFBox)
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // Exemple simple - À remplacer par une vraie génération PDF
            String pdfContent = genererContenuPDF(rapport, request);
            outputStream.write(pdfContent.getBytes());
            
            return new ByteArrayResource(outputStream.toByteArray());
            
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    public Resource genererExcel(RapportRequestDto request) {
        try {
            // Générer le rapport
            RapportResponseDto rapport = rapportService.genererRapport(request);
            
            // Simuler la génération d'un Excel (à implémenter avec Apache POI)
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            
            // Exemple simple - À remplacer par une vraie génération Excel
            String excelContent = genererContenuExcel(rapport, request);
            outputStream.write(excelContent.getBytes());
            
            return new ByteArrayResource(outputStream.toByteArray());
            
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du Excel", e);
        }
    }

    private String genererContenuPDF(RapportResponseDto rapport, RapportRequestDto request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        StringBuilder content = new StringBuilder();
        content.append("RAPPORT ").append(request.getTypeRapport().toUpperCase()).append("\n");
        content.append("Période: ").append(request.getDateDebut().format(formatter))
               .append(" - ").append(request.getDateFin().format(formatter)).append("\n\n");
        
        content.append("STATISTIQUES:\n");
        content.append("- Total demandes: ").append(rapport.getStatistiques().getTotalDemandes()).append("\n");
        content.append("- Demandes créées: ").append(rapport.getStatistiques().getDemandesCreees()).append("\n");
        content.append("- Demandes résolues: ").append(rapport.getStatistiques().getDemandesResolues()).append("\n");
        content.append("- Taux de résolution: ").append(rapport.getStatistiques().getTauxResolution()).append("%\n");
        content.append("- Temps moyen réponse: ").append(rapport.getStatistiques().getTempsMoyenReponse()).append("h\n");
        
        return content.toString();
    }

    private String genererContenuExcel(RapportResponseDto rapport, RapportRequestDto request) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        StringBuilder content = new StringBuilder();
        content.append("Type Rapport\tPériode Début\tPériode Fin\tTotal Demandes\tDemandes Créées\tDemandes Résolues\tTaux Résolution\tTemps Moyen Réponse\n");
        content.append(request.getTypeRapport()).append("\t")
               .append(request.getDateDebut().format(formatter)).append("\t")
               .append(request.getDateFin().format(formatter)).append("\t")
               .append(rapport.getStatistiques().getTotalDemandes()).append("\t")
               .append(rapport.getStatistiques().getDemandesCreees()).append("\t")
               .append(rapport.getStatistiques().getDemandesResolues()).append("\t")
               .append(rapport.getStatistiques().getTauxResolution()).append("\t")
               .append(rapport.getStatistiques().getTempsMoyenReponse()).append("\n");
        
        return content.toString();
    }
}