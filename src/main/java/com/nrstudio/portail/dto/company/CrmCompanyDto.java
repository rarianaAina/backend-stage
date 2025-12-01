package com.nrstudio.portail.dto.company;

public class CrmCompanyDto {
    private Integer companyId;
    private String name;
    private String type;
    private boolean deleted;

    // Constructeur par défaut
    public CrmCompanyDto() {}
    
    // Builder pattern
    public static CrmCompanyDtoBuilder builder() {
        return new CrmCompanyDtoBuilder();
    }
    
    // Getters et Setters
    public Integer getCompanyId() { return companyId; }
    public void setCompanyId(Integer companyId) { this.companyId = companyId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    
    // Builder class
    public static class CrmCompanyDtoBuilder {
        private Integer companyId;
        private String name;
        private String type;
        private boolean deleted;
        
        public CrmCompanyDtoBuilder companyId(Integer companyId) {
            this.companyId = companyId;
            return this;
        }
        
        public CrmCompanyDtoBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public CrmCompanyDtoBuilder type(String type) {
            this.type = type;
            return this;
        }
        
        public CrmCompanyDtoBuilder deleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }
        
        public CrmCompanyDto build() {
            CrmCompanyDto dto = new CrmCompanyDto();
            dto.setCompanyId(companyId);
            dto.setName(name);
            dto.setType(type);
            dto.setDeleted(deleted);
            return dto;
        }
    }
}