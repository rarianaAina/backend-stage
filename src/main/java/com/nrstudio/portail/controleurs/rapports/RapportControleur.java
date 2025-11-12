package com.nrstudio.portail.controleurs.rapports;

import com.nrstudio.portail.dto.rapports.RapportRequestDto;
import com.nrstudio.portail.dto.rapports.RapportResponseDto;
import com.nrstudio.portail.services.rapports.RapportService;
import com.nrstudio.portail.services.export.ExportService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rapports")
@CrossOrigin(origins = "*")
public class RapportControleur {

    private final RapportService rapportService;
    private final ExportService exportService;

    public RapportControleur(RapportService rapportService, ExportService exportService) {
        this.rapportService = rapportService;
        this.exportService = exportService;
    }

    @PostMapping("/generer")
    public ResponseEntity<RapportResponseDto> genererRapport(@RequestBody RapportRequestDto request) {
        try {
            System.out.println("Generation du rapport: " + request.getTypeRapport() + 
                             " du " + request.getDateDebut() + " au " + request.getDateFin());
            
            RapportResponseDto response = rapportService.genererRapport(request);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Erreur generation rapport: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/export/pdf")
    public ResponseEntity<Resource> exporterPDF(@RequestBody RapportRequestDto request) {
        try {
            System.out.println("Export PDF: " + request.getTypeRapport());
            
            Resource pdfResource = exportService.genererPDF(request);
            
            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"rapport-" + request.getTypeRapport() + ".pdf\"")
                .body(pdfResource);
            
        } catch (Exception e) {
            System.err.println("Erreur export PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/export/excel")
    public ResponseEntity<Resource> exporterExcel(@RequestBody RapportRequestDto request) {
        try {
            System.out.println("Export Excel: " + request.getTypeRapport());
            
            Resource excelResource = exportService.genererExcel(request);
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"rapport-" + request.getTypeRapport() + ".xlsx\"")
                .body(excelResource);
            
        } catch (Exception e) {
            System.err.println("Erreur export Excel: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}