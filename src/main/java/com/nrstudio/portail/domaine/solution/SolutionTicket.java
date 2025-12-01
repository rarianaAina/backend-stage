package com.nrstudio.portail.domaine.solution;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.nrstudio.portail.domaine.Ticket;

@Entity
@Table(name = "solution_ticket", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"solution_id", "ticket_id"})
})
public class SolutionTicket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "solution_id", nullable = false)
    private Solution solution;
    
    @ManyToOne
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;
    
    @Column(name = "date_liaison")
    private LocalDateTime dateLiaison;
    
    // Constructeurs
    public SolutionTicket() {}
    
    public SolutionTicket(Solution solution, Ticket ticket) {
        this.solution = solution;
        this.ticket = ticket;
        this.dateLiaison = LocalDateTime.now();
    }
    
    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Solution getSolution() { return solution; }
    public void setSolution(Solution solution) { this.solution = solution; }
    
    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }
    
    public LocalDateTime getDateLiaison() { return dateLiaison; }
    public void setDateLiaison(LocalDateTime dateLiaison) { this.dateLiaison = dateLiaison; }
}