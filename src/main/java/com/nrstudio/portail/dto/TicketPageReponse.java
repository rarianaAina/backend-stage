package com.nrstudio.portail.dto;

import java.util.List;

import com.nrstudio.portail.domaine.Ticket;

public class TicketPageReponse {
    
    private List<Ticket> tickets;
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int pageSize;
  
    public TicketPageReponse(List<Ticket> tickets, int currentPage, int totalPages, long totalElements, int pageSize) {
        this.tickets = tickets;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
        this.pageSize = pageSize;
    }

    public TicketPageReponse() {
    }

    public List<Ticket> getTickets() {
        return tickets;
    }
    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }
    public int getCurrentPage() {
        return currentPage;
    }
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }
    public int getTotalPages() {
        return totalPages;
    }
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    public long getTotalElements() {
        return totalElements;
    }
    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }
    public int getPageSize() {
        return pageSize;
    }
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

}
