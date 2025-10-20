// package com.nrstudio.portail.config;

// import net.javacrumbs.shedlock.core.LockProvider;
// import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
// import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.jdbc.core.JdbcTemplate;

// import javax.sql.DataSource;

// @Configuration
// @EnableSchedulerLock(defaultLockAtMostFor = "PT5M") // lock max : 5 minutes
// public class ShedLockConfig {

//     @Bean
//     public LockProvider lockProvider(DataSource dataSource) {
//         return new JdbcTemplateLockProvider(
//             JdbcTemplateLockProvider.Configuration.builder()
//                 .withJdbcTemplate(new JdbcTemplate(dataSource))
//                 .usingDbTime() // évite les erreurs liées au temps système
//                 .build()
//         );
//     }
// }
