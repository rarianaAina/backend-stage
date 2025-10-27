package com.nrstudio.portail.dto;

public class CodeValidationResponse {
    private boolean valid;
    private String message;
    private String code;

    // Constructeurs
    public CodeValidationResponse() {}

    public CodeValidationResponse(boolean valid, String message, String code) {
        this.valid = valid;
        this.message = message;
        this.code = code;
    }

    // Getters et Setters
    public boolean isValid() { return valid; }
    public void setValid(boolean valid) { this.valid = valid; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}