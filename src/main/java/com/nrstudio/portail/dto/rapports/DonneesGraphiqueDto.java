package com.nrstudio.portail.dto.rapports;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.nrstudio.portail.dto.rapports.*;

@Data
public class DonneesGraphiqueDto {
    private List<String> labels;
    private List<DatasetGraphiqueDto> datasets;
}