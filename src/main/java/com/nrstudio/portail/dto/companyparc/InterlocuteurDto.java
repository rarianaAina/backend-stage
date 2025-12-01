package com.nrstudio.portail.dto.companyparc;

public class InterlocuteurDto {
    private Integer userId;
    private String userFullName;
    private String email;
    private String telephone;
    private String whatsappNumero;

    // Constructeurs
    public InterlocuteurDto() {}

    public InterlocuteurDto(Integer userId, String userFullName) {
        this.userId = userId;
        this.userFullName = userFullName;
    }

    public InterlocuteurDto(Integer userId, String userFullName, String email) {
        this.userId = userId;
        this.userFullName = userFullName;
        this.email = email;
    }

    public InterlocuteurDto(Integer userId, String userFullName, String email, String telephone, String whatsappNumero) {
        this.userId = userId;
        this.userFullName = userFullName;
        this.email = email;
        this.telephone = telephone;
        this.whatsappNumero = whatsappNumero;
    }

    // Getters et Setters
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getWhatsappNumero() {
        return whatsappNumero;
    }

    public void setWhatsappNumero(String whatsappNumero) {
        this.whatsappNumero = whatsappNumero;
    }
}