package com.nrstudio.portail.depots.solution;

import com.nrstudio.portail.depots.solution.SolutionRepository;
import com.nrstudio.portail.domaine.solution.Solution;
import com.nrstudio.portail.services.synchronisations.configurations.mappers.SolutionDataMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
@Transactional
public class SolutionSyncRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(SolutionSyncRepository.class);
    
    private final SolutionRepository solutionRepository;
    private final SolutionDataMapper dataMapper;
    
    public SolutionSyncRepository(SolutionRepository solutionRepository,
                                SolutionDataMapper dataMapper) {
        this.solutionRepository = solutionRepository;
        this.dataMapper = dataMapper;
    }
    
    public Solution trouverOuCreerSolution(String idExterneCrm) {
        Optional<Solution> solutionExistante = solutionRepository.findByIdExterneCrm(idExterneCrm);
        
        if (solutionExistante.isPresent()) {
            Solution solution = solutionExistante.get();
            solution.setDateSynchronisation(LocalDateTime.now());
            return solution;
        } else {
            Solution nouvelleSolution = new Solution();
            nouvelleSolution.setIdExterneCrm(idExterneCrm);
            nouvelleSolution.setDateCreation(LocalDateTime.now());
            nouvelleSolution.setDateSynchronisation(LocalDateTime.now());
            nouvelleSolution.setSupprime(false);
            return nouvelleSolution;
        }
    }
    
    public void sauvegarder(Solution solution) {
        solutionRepository.save(solution);
        logger.debug("Solution sauvegardée: {}", solution.getIdExterneCrm());
    }
    
    public void marquerCommeSupprimee(Map<String, Object> donneesCrm) {
        Integer solutionId = dataMapper.extraireSolutionId(donneesCrm);
        if (solutionId != null) {
            solutionRepository.findByIdExterneCrm(solutionId.toString())
                .ifPresent(solution -> {
                    solution.setSupprime(true);
                    solution.setDateSynchronisation(LocalDateTime.now());
                    solutionRepository.save(solution);
                    logger.info("Solution marquée comme supprimée: {}", solutionId);
                });
        }
    }
}