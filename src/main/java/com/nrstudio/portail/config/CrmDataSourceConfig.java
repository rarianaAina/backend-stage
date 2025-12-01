package com.nrstudio.portail.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

// @Configuration
// public class CrmDataSourceConfig {

//   // ---- Datasource principale (PORTAIL_CLIENT) ----
//   @Bean
//   @ConfigurationProperties("spring.datasource")
//   public DataSourceProperties portailProps() {
//     return new DataSourceProperties();
//   }

//   @Primary
//   @Bean(name = "dataSource") // <- nom standard attendu par Spring/JPA
//   public DataSource portailDataSource() {
//     return portailProps().initializeDataSourceBuilder().build();
//   }

//   // ---- Datasource CRM (lecture seule) ----
//   @Bean
//   @ConfigurationProperties("crm.datasource")
//   public DataSourceProperties crmProps() {
//     return new DataSourceProperties();
//   }

//   @Bean(name = "crmDataSource")
//   public DataSource crmDataSource() {
//     return crmProps().initializeDataSourceBuilder().build();
//   }

//   @Bean(name = "crmJdbc")
//   public JdbcTemplate crmJdbc(@Qualifier("crmDataSource") DataSource ds) {
//     return new JdbcTemplate(ds);
//   }
// }

@Configuration
public class CrmDataSourceConfig {

  @Bean
  @ConfigurationProperties("spring.datasource")
  public DataSourceProperties portailProps() {
    return new DataSourceProperties();
  }

  @Primary
  @Bean(name = "dataSource")
  public DataSource portailDataSource() {
    return portailProps().initializeDataSourceBuilder().build();
  }

  // JdbcTemplate principal (PORTAIL_CLIENT)
  @Bean
  @Primary
  public JdbcTemplate jdbcTemplate(@Qualifier("dataSource") DataSource ds) {
      return new JdbcTemplate(ds);
  }

  @Bean
  @ConfigurationProperties("crm.datasource")
  public DataSourceProperties crmProps() {
    return new DataSourceProperties();
  }

  @Bean(name = "crmDataSource")
  public DataSource crmDataSource() {
    return crmProps().initializeDataSourceBuilder().build();
  }

  @Bean(name = "crmJdbc")
  public JdbcTemplate crmJdbc(@Qualifier("crmDataSource") DataSource ds) {
    return new JdbcTemplate(ds);
  }
}

