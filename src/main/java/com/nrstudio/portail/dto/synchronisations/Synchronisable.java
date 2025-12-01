package com.nrstudio.portail.dto.synchronisations;



public interface Synchronisable {
    String getEntite();
    void synchroniserPlanifie();
    void synchroniserManuellement();
    SyncStats getStats();
}