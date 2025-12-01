package com.nrstudio.portail.services.synchronisations.configurations.constants;

public final class SyncConstants {
    public static final String ENTITE_COMPANY = "companies";
    public static final String ENTITE_SOLUTION = "solutions";
    public static final String ENTITE_CH = "ch";
    public static final String ENTITE_PERSON = "persons";
    public static final String ENTITE_PRODUCT = "products";
    public static final String ENTITE_TICKET = "tickets";
    public static final String ENTITE_SOLUTICK = "soluticks";
    
    public static final String CRON_DEFAULT = "0 0 2 * * *";
    public static final int BATCH_SIZE_DEFAULT = 100;
    public static final int DELAY_MS_DEFAULT = 10;
    
    private SyncConstants() {}
}