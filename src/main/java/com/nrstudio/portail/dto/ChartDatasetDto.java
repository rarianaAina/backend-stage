package com.nrstudio.portail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartDatasetDto {
  private String label;
  private List<Integer> data;
  private String backgroundColor;
  private String borderColor;
}
