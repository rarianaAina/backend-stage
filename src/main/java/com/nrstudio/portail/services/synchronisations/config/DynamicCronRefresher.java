package com.nrstudio.portail.services.synchronisations.config;

import java.util.concurrent.ScheduledFuture;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import com.nrstudio.portail.services.solution.CrmSolutionTicketSyncService;
import com.nrstudio.portail.services.solution.CrmSolutionsSyncService;
import com.nrstudio.portail.services.synchronisations.CrmCompanySyncService;
import com.nrstudio.portail.services.synchronisations.CrmPersonSyncService;
import com.nrstudio.portail.services.synchronisations.CrmTicketSyncService;
import com.nrstudio.portail.services.synchronisations.CrmCreditHoraireSyncService;

import jakarta.annotation.PostConstruct;

//@Component
@EnableScheduling
public class DynamicCronRefresher {

    private final SchedulingWatcherService watcher;
    private final ThreadPoolTaskScheduler scheduler;
    private final CrmSolutionTicketSyncService solutickSync;
    private final CrmSolutionsSyncService solutionsSync;
    private final CrmTicketSyncService ticketSync;
    private final CrmCompanySyncService companySync;
    private final CrmPersonSyncService  personSync;
    private final CrmCreditHoraireSyncService creditHoraireSync;

    private ScheduledFuture<?> solutickTask;
    private ScheduledFuture<?> solutionsTask;
    private ScheduledFuture<?> ticketTask;
    private ScheduledFuture<?> companyTask;
    private ScheduledFuture<?> personTask;
    private ScheduledFuture<?> creditHoraireTask;

    public DynamicCronRefresher(
            SchedulingWatcherService watcher,
            ThreadPoolTaskScheduler scheduler,
            CrmSolutionTicketSyncService solutickSync,
            CrmSolutionsSyncService solutionsSync,
            CrmTicketSyncService ticketSync,
            CrmCompanySyncService companySync,
            CrmPersonSyncService personSync,
            CrmCreditHoraireSyncService creditHoraireSync
    ) {
        this.watcher = watcher;
        this.scheduler = scheduler;
        this.solutickSync = solutickSync;
        this.solutionsSync = solutionsSync;
        this.ticketSync = ticketSync;
        this.companySync = companySync;
        this.personSync = personSync;
        this.creditHoraireSync = creditHoraireSync;
    }

    @PostConstruct
    public void init() {
        refreshTasks();
    }

    @Scheduled(fixedDelay = 5000)
    public void watchForChanges() {
        if (watcher.hasChanged()) {
            System.out.println("🔄 Modification détectée → rechargement des tâches…");
            refreshTasks();
        }
    }

    private void refreshTasks() {

        // === crm-solutick-sync ===
        if (solutickTask != null) solutickTask.cancel(false);
        String cronSolutick = watcher.getCron("crm-solutick-sync");

        if (cronSolutick != null) {
            solutickTask = scheduler.schedule(
                () -> solutickSync.synchroniserLiaisonsSolutionsTicketsDynamique(),
                new CronTrigger(cronSolutick)
            );
            System.out.println("🟢 cron-solutick-sync : " + cronSolutick);
        } else {
            System.out.println("⛔ crm-solutick-sync désactivé");
        }

        // === crm-solution-sync ===
        if (solutionsTask != null) solutionsTask.cancel(false);
        String cronSolutions = watcher.getCron("crm-solution-sync");

        if (cronSolutions != null) {
            solutionsTask = scheduler.schedule(
                () -> solutionsSync.synchroniserSolutions(),
                new CronTrigger(cronSolutions)
            );
            System.out.println("🟢 crm-solution-sync : " + cronSolutions);
        } else {
            System.out.println("⛔ crm-solution-sync désactivé");
        }

        // === crm-ticket-sync ===
        if (ticketTask != null) ticketTask.cancel(false);
        String cronTickets = watcher.getCron("crm-ticket-sync");
        if (cronTickets != null) {
            ticketTask = scheduler.schedule(
                () -> ticketSync.importerDepuisCrm(),
                new CronTrigger(cronTickets)
            );
            System.out.println("🟢 crm-ticket-sync : " + cronTickets);
        } else {
            System.out.println("⛔ crm-ticket-sync désactivé");
        }

        // === crm-company-sync ===
        if (companyTask != null) companyTask.cancel(false);
        String cronCompany = watcher.getCron("crm-company-sync");
        if (cronCompany != null) {
            companyTask = scheduler.schedule(
                () -> companySync.synchroniserCompanies(),
                new CronTrigger(cronCompany)
            );
            System.out.println("🟢 crm-company-sync : " + cronCompany);
        } else {
            System.out.println("⛔ crm-company-sync désactivé");
        }

        // === crm-person-sync ===
        if (personTask != null) personTask.cancel(false);
        String cronPerson = watcher.getCron("crm-person-sync");
        if (cronPerson != null) {
            personTask = scheduler.schedule(
                () -> personSync.synchroniserPersons(),
                new CronTrigger(cronPerson)
            );
            System.out.println("🟢 crm-person-sync : " + cronPerson);
        } else {
            System.out.println("⛔ crm-person-sync désactivé");
        }
        // === crm-credithoraire-sync ===
        if (creditHoraireTask != null) creditHoraireTask.cancel(false);
        String cronCH = watcher.getCron("crm-ch-sync");
        if (cronCH != null) {
            creditHoraireTask = scheduler.schedule(
                () -> creditHoraireSync.synchroniserCreditHoraire(),
                new CronTrigger(cronCH)
            );
            System.out.println("🟢 crm-credithoraire-sync : " + cronCH);
        } else {
            System.out.println("⛔ crm-credithoraire-sync désactivé");
        }
    }
}
