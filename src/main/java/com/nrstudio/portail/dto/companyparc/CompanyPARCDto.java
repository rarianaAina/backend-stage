package com.nrstudio.portail.dto.companyparc;

import java.time.LocalDateTime;

public class CompanyPARCDto {
    private Integer parcId;
    private String parcName;
    private Integer parcCompanyId;
    private Integer compCompanyId;
    private String compName;
    private LocalDateTime dateObtention;
    private Integer userId;
    private String userFullName; 

    // Constructeurs
    public CompanyPARCDto() {}

    public CompanyPARCDto(Integer parcId, String parcName, Integer parcCompanyId, 
                         Integer compCompanyId, String compName, LocalDateTime dateObtention, 
                         Integer userId, String userFullName) {
        this.parcId = parcId;
        this.parcName = parcName;
        this.parcCompanyId = parcCompanyId;
        this.compCompanyId = compCompanyId;
        this.compName = compName;
        this.dateObtention = dateObtention;
        this.userId = userId;
        this.userFullName = userFullName;
    }

    // Getters et Setters
    public Integer getParcId() {
        return parcId;
    }

    public void setParcId(Integer parcId) {
        this.parcId = parcId;
    }

    public String getParcName() {
        return parcName;
    }

    public void setParcName(String parcName) {
        this.parcName = parcName;
    }

    public Integer getParcCompanyId() {
        return parcCompanyId;
    }

    public void setParcCompanyId(Integer parcCompanyId) {
        this.parcCompanyId = parcCompanyId;
    }

    public Integer getCompCompanyId() {
        return compCompanyId;
    }

    public void setCompCompanyId(Integer compCompanyId) {
        this.compCompanyId = compCompanyId;
    }

    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName;
    }

    public LocalDateTime getDateObtention() {
        return dateObtention;
    }

    public void setDateObtention(LocalDateTime dateObtention) {
        this.dateObtention = dateObtention;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }
}