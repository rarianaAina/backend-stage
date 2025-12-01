package com.nrstudio.portail.services.synchronisations.configurations.fetchers;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CrmDataFetcher {
    private final JdbcTemplate crmJdbc;
    
    // CORRECTION : Utiliser le bon nom de qualifier
    public CrmDataFetcher(@Qualifier("crmJdbc") JdbcTemplate crmJdbc) {
        this.crmJdbc = crmJdbc;
    }
    
    public List<Map<String, Object>> recupererCompaniesCrm() {
        final String sql = """
            SELECT Comp_CompanyId, Comp_Name, Comp_Type, 
                   ISNULL(Comp_Deleted,0) AS Comp_Deleted 
            FROM dbo.Company 
            """;
        
        return crmJdbc.queryForList(sql);
    }

    public List<Map<String, Object>> recupererSolutionsCrm() {
        final String sql = """
            SELECT Soln_SolutionId, Soln_CreatedBy, Soln_CreatedDate, Soln_UpdatedBy, 
                Soln_UpdatedDate, Soln_TimeStamp, ISNULL(Soln_Deleted,0) AS Soln_Deleted, 
                Soln_AssignedUserId, Soln_Area, Soln_SolutionDetails, Soln_Description, 
                Soln_Stage, Soln_Status, Soln_ReferenceId, Soln_Closed, 
                Soln_Secterr, Soln_WorkflowId, soln_ChannelId, soln_TalendExterKey 
            FROM dbo.Solutions
            """;
        
        return crmJdbc.queryForList(sql);
    }

    public List<Map<String, Object>> recupererLiaisonsSolutionsTicketsCrm() {
        final String sql =
            "SELECT SLnk_Soln_SolutionId, SLnk_Case_CaseId " +
            "FROM dbo.vSolutionCaseLinkReport " +
            "WHERE SLnk_Soln_SolutionId IS NOT NULL AND SLnk_Case_CaseId IS NOT NULL";
        
        return crmJdbc.queryForList(sql);
    }
}