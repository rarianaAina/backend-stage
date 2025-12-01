// Fichiers fournis dans ce seul document — sépare-les en plusieurs fichiers dans ton projet.

// 1) CrmCompanyDto.java
package com.nrstudio.portail.synchronisations.dto;

import java.time.LocalDateTime;

public class CrmCompanyDto {
    private Integer companyId;
    private String name;
    private Integer type;
    private boolean deleted;

    public Integer getCompanyId() { return companyId; }
    public void setCompanyId(Integer companyId) { this.companyId = companyId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}

// 2) CrmCompanyReader.java
package com.nrstudio.portail.synchronisations.reader;

import com.nrstudio.portail.synchronisations.dto.CrmCompanyDto;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

@Repository
public class CrmCompanyReader {
    private final JdbcTemplate crmJdbc;
    private static final String SQL_ALL = "SELECT Comp_CompanyId, Comp_Name, Comp_Type, COALESCE(Comp_Deleted,0) AS Comp_Deleted FROM dbo.Company";

    public CrmCompanyReader(JdbcTemplate crmJdbc) {
        this.crmJdbc = crmJdbc;
    }

    /**
     * Lit toutes les lignes et appelle le consumer pour chaque ligne. Cette méthode évite
     * d'allouer une grande liste en mémoire.
     */
    public void streamAll(Consumer<CrmCompanyDto> consumer) {
        crmJdbc.query(SQL_ALL, (ResultSetExtractor<Void>) rs -> {
            while (rs.next()) {
                CrmCompanyDto dto = mapRow(rs);
                consumer.accept(dto);
            }
            return null;
        });
    }

    private CrmCompanyDto mapRow(ResultSet rs) throws SQLException {
        CrmCompanyDto dto = new CrmCompanyDto();
        dto.setCompanyId(getIntNullable(rs, "Comp_CompanyId"));
        dto.setName(rs.getString("Comp_Name"));
        dto.setType(getIntNullable(rs, "Comp_Type"));
        dto.setDeleted(rs.getInt("Comp_Deleted") == 1);
        return dto;
    }

    private Integer getIntNullable(ResultSet rs, String col) throws SQLException {
        int val = rs.getInt(col);
        return rs.wasNull() ? null : Integer.valueOf(val);
    }
}

// 3) CompanyUpsertService.java
package com.nrstudio.portail.synchronisations.service;

import com.nrstudio.portail.depots.CompanyRepository;
import com.nrstudio.portail.domaine.Company;
import com.nrstudio.portail.synchronisations.dto.CrmCompanyDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class CompanyUpsertService {
    private final CompanyRepository companies;

    public CompanyUpsertService(CompanyRepository companies) {
        this.companies = companies;
    }

    /**
     * Méthode transactionnelle appelée depuis le thread d'exécution (executor).
     * Chaque upsert est encapsulé dans sa propre transaction pour limiter la durée.
     */
    @Transactional
    public void upsertFromCrm(CrmCompanyDto dto) {
        if (dto == null || dto.getCompanyId() == null || dto.isDeleted()) return;

        String idExterneCrm = String.valueOf(dto.getCompanyId());
        Company existing = companies.findByIdExterneCrm(idExterneCrm).orElse(null);

        String nom = Objects.toString(dto.getName(), "Société " + dto.getCompanyId());

        if (existing != null) {
            existing.setNom(nom);
            existing.setDateMiseAJour(LocalDateTime.now());
            companies.save(existing);
        } else {
            Company nouvelle = new Company();
            nouvelle.setIdExterneCrm(idExterneCrm);
            nouvelle.setCodeCompany("COMP-" + dto.getCompanyId());
            nouvelle.setNom(nom);
            nouvelle.setActif(true);
            nouvelle.setDateCreation(LocalDateTime.now());
            nouvelle.setDateMiseAJour(LocalDateTime.now());
            companies.save(nouvelle);
        }
    }
}

// 4) CompanySyncService.java (orchestrator)
package com.nrstudio.portail.synchronisations;

import com.nrstudio.portail.synchronisations.dto.CrmCompanyDto;
import com.nrstudio.portail.synchronisations.reader.CrmCompanyReader;
import com.nrstudio.portail.synchronisations.service.CompanyUpsertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class CompanySyncService {
    private final CrmCompanyReader crmReader;
    private final CompanyUpsertService upsertService;
    private final TaskExecutor taskExecutor;
    private final SynchronisationManager synchronisationManager;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicBoolean scheduledRunning = new AtomicBoolean(false);

    public CompanySyncService(CrmCompanyReader crmReader,
                              CompanyUpsertService upsertService,
                              TaskExecutor taskExecutor,
                              SynchronisationManager synchronisationManager) {
        this.crmReader = crmReader;
        this.upsertService = upsertService;
        this.taskExecutor = taskExecutor;
        this.synchronisationManager = synchronisationManager;
    }

    @Scheduled(cron = "${scheduling.crm-company-sync-cron:0 * * * * *}")
    public void scheduledSync() {
        if (synchronisationManager.estEnCours("companies")) {
            logger.info("Sync planifiée ignorée - Sync manuelle en cours");
            return;
        }
        if (!scheduledRunning.compareAndSet(false, true)) {
            logger.info("Sync planifiée ignorée - déjà en cours");
            return;
        }

        try {
            logger.info("Début de la synchronisation planifiée des companies");
            executeSyncLoop();
        } finally {
            scheduledRunning.set(false);
        }
    }

    public void startManualSync() {
        final String type = "companies";
        if (synchronisationManager.estEnCours(type)) throw new IllegalStateException("Une synchronisation manuelle est déjà en cours");
        synchronisationManager.demarrerSynchronisation(type);

        taskExecutor.execute(() -> {
            Thread current = Thread.currentThread();
            synchronisationManager.enregistrerThread(type, current);
            try {
                executeSyncLoopWithStopCheck(type);
            } catch (Exception e) {
                logger.error("Erreur pendant la sync manuelle", e);
            } finally {
                synchronisationManager.supprimerThread(type);
            }
        });
    }

    private void executeSyncLoop() {
        // exécution simple (planifiée) sans contrôle d'arrêt
        final int[] counter = {0};
        crmReader.streamAll(dto -> {
            try {
                upsertService.upsertFromCrm(dto);
                counter[0]++;
            } catch (Exception e) {
                logger.error("Erreur traitement company {}", dto.getCompanyId(), e);
            }
        });
        logger.info("Synchronisation planifiée terminée. {} companies traitées", counter[0]);
    }

    private void executeSyncLoopWithStopCheck(String type) {
        final int[] counter = {0};
        crmReader.streamAll(dto -> {
            if (synchronisationManager.doitArreter(type)) {
                logger.info("Synchronisation manuelle arrêtée à la demande");
                throw new RuntimeException("Arrêt demandé");
            }
            try {
                upsertService.upsertFromCrm(dto);
                counter[0]++;
            } catch (RuntimeException re) {
                // si l'arrêt a été demandé on remonte
                throw re;
            } catch (Exception e) {
                logger.error("Erreur traitement company {}", dto.getCompanyId(), e);
            }
        });
        logger.info("Synchronisation manuelle terminée. {} companies traitées", counter[0]);
    }
}

// 5) TaskExecutor Configuration (AsyncConfig.java)
package com.nrstudio.portail.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig {
    @Bean("syncTaskExecutor")
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(2);
        exec.setMaxPoolSize(4);
        exec.setQueueCapacity(1000);
        exec.setThreadNamePrefix("sync-exec-");
        exec.initialize();
        return exec;
    }
}

// 6) Mise à jour de CompanySyncService pour injecter le bean nommé (exemple d'utilisation)
// Dans la classe CompanySyncService, change le constructeur pour recevoir @Qualifier("syncTaskExecutor") TaskExecutor taskExecutor

// 7) Notes d'utilisation :
// - Sépare les fichiers dans les packages indiqués.
// - Assure-toi que ton bean SynchronisationManager expose les méthodes utilisées :
//   estEnCours(String), demarrerSynchronisation(String), enregistrerThread(String, Thread), supprimerThread(String), doitArreter(String)
// - Si tu veux utiliser des batchs, considère l'ajout d'un CompanyBatchUpsertService qui regroupe les entités par chunck et appelle companyRepo.saveAll(chunk) puis flush.
// - Teste avec un jeu de données réduit d'abord. Ajoute metrics (Micrometer) si besoin.

// FIN
