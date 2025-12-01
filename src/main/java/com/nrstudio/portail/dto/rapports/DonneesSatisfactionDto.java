package com.nrstudio.portail.dto.rapports;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
public class DonneesSatisfactionDto {
    private Map<String, Integer> repartition;
    private double moyenne;
    private int totalAvis;
}