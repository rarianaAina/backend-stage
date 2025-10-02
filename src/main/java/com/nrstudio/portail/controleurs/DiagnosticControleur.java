package com.nrstudio.portail.controleurs;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@RestController
public class DiagnosticControleur {

    private final DataSource dataSource;

    public DiagnosticControleur(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/diagnostic/tables")
    public List<String> listerTables() throws Exception {
        List<String> tables = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            // Afficher la base connectée
            try (ResultSet rs = conn.createStatement().executeQuery("SELECT DB_NAME()")) {
                if (rs.next()) {
                    tables.add("Base courante = " + rs.getString(1));
                }
            }

            DatabaseMetaData meta = conn.getMetaData();
            try (ResultSet rs = meta.getTables(null, "dbo", "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String schema = rs.getString("TABLE_SCHEM");
                    String name = rs.getString("TABLE_NAME");
                    tables.add(schema + "." + name);
                }
            }
        }
        return tables;
    }
}
