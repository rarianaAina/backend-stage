package com.nrstudio.portail.dto;

import java.util.List;

public class TicketAvecProduitPageReponse {
    private List<TicketAvecProduitDto> tickets;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
    
    public TicketAvecProduitPageReponse(List<TicketAvecProduitDto> tickets, int currentPage, int totalPages, long totalElements, int pageSize) {
        this.tickets = tickets;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.pageSize = pageSize;
    }
    
    // Getters et Setters
    public List<TicketAvecProduitDto> getTickets() { return tickets; }
    public void setTickets(List<TicketAvecProduitDto> tickets) { this.tickets = tickets; }
    
    public int getCurrentPage() { return currentPage; }
    public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
    
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
    
    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }
    
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}