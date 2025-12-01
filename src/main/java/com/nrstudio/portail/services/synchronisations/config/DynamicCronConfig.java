package com.nrstudio.portail.services.synchronisations.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

import com.nrstudio.portail.services.solution.CrmSolutionTicketSyncService;
import com.nrstudio.portail.services.solution.CrmSolutionsSyncService;
import com.nrstudio.portail.services.synchronisations.CrmCompanySyncService;
import com.nrstudio.portail.services.synchronisations.CrmCreditHoraireSyncService;
import com.nrstudio.portail.services.synchronisations.CrmPersonSyncService;
import com.nrstudio.portail.services.synchronisations.CrmTicketSyncService;

@Configuration
@EnableScheduling
public class DynamicCronConfig implements SchedulingConfigurer {

    private final DynamicSchedulerService schedulerService;
    private final CrmSolutionTicketSyncService syncService;
    private final CrmSolutionsSyncService solutionsSyncService;
    private final CrmTicketSyncService  ticketSyncService;
    private final CrmCompanySyncService companySyncService;
    private final CrmPersonSyncService personSyncService;
    private final CrmCreditHoraireSyncService creditHoraireSyncService;

    public DynamicCronConfig(DynamicSchedulerService schedulerService,
                             CrmSolutionTicketSyncService syncService,
                             CrmSolutionsSyncService solutionsSyncService,
                             CrmTicketSyncService ticketSyncService,
                             CrmCompanySyncService companySyncService,
                             CrmPersonSyncService personSyncService,
                             CrmCreditHoraireSyncService creditHoraireSyncService) {
        this.schedulerService = schedulerService;
        this.syncService = syncService;
        this.solutionsSyncService = solutionsSyncService;
        this.ticketSyncService = ticketSyncService;
        this.companySyncService = companySyncService;
        this.personSyncService = personSyncService;
        this.creditHoraireSyncService = creditHoraireSyncService;
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(
                // tâche à exécuter
                () -> syncService.synchroniserLiaisonsSolutionsTicketsDynamique(),

                // trigger dynamique
                triggerContext -> {
                    String cron = schedulerService.getCronExpression("crm-solutick-sync");

                    if (!schedulerService.isEnabled("crm-solutick-sync")) {
                        // Désactiver la tâche -> renvoie une date "null"
                        return null;
                    }

                    return new CronTrigger(cron).nextExecution(triggerContext);
                }
        );
        taskRegistrar.addTriggerTask(
                () -> solutionsSyncService.synchroniserSolutions(),
                triggerContext -> {
                    String cron = schedulerService.getCronExpression("crm-solution-sync");
                    if (!schedulerService.isEnabled("crm-solution-sync")) return null;
                    return new CronTrigger(cron).nextExecution(triggerContext);
                }
        );
        taskRegistrar.addTriggerTask(
                () -> ticketSyncService.importerDepuisCrm(),
                triggerContext -> {
                    String cron = schedulerService.getCronExpression("crm-ticket-sync");
                    if (!schedulerService.isEnabled("crm-ticket-sync")) return null;
                    return new CronTrigger(cron).nextExecution(triggerContext);
                }
        );
        taskRegistrar.addTriggerTask(
                () -> companySyncService.synchroniserCompanies(),
                triggerContext -> {
                    String cron = schedulerService.getCronExpression("crm-company-sync");
                    if (!schedulerService.isEnabled("crm-company-sync")) return null;
                    return new CronTrigger(cron).nextExecution(triggerContext);
                }
        );
        taskRegistrar.addTriggerTask(
                () -> personSyncService.synchroniserPersons(),
                triggerContext -> {
                    String cron = schedulerService.getCronExpression("crm-person-sync");
                    if (!schedulerService.isEnabled("crm-person-sync")) return null;
                    return new CronTrigger(cron).nextExecution(triggerContext);
                }
        );
        taskRegistrar.addTriggerTask(
                () -> creditHoraireSyncService.synchroniserCreditHoraire(),
                triggerContext -> {
                    String cron = schedulerService.getCronExpression("crm-ch-sync");
                    if (!schedulerService.isEnabled("crm-credithoraire-sync")) return null;
                    return new CronTrigger(cron).nextExecution(triggerContext);
                }
        );
    }
}

