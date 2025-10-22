package com.nrstudio.portail.domaine;

import jakarta.persistence.*;

@Entity
@Table(name = "CompanyPARC")
public class CompanyPARC {

    @Id
    @Column(name = "parc_PARCid")
    private Integer parcId;

    @Column(name = "parc_name")
    private String parcName;

    @Column(name = "parc_companyid")
    private Integer parcCompanyId;

    @Column(name = "comp_companyid")
    private Integer compCompanyId;

    @Column(name = "comp_name")
    private String compName;

    // Getters and Setters
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
}
