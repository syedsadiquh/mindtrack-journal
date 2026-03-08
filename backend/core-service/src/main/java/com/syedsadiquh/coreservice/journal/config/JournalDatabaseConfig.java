package com.syedsadiquh.coreservice.journal.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Objects;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.syedsadiquh.coreservice.journal.repository",
        entityManagerFactoryRef = "journalEntityManager",
        transactionManagerRef = "journalTransactionManager"
)
public class JournalDatabaseConfig {

    @Bean(name = "journalDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.journal")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "journalEntityManager")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("journalDataSource") DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("com.syedsadiquh.coreservice.journal.entity")
                .persistenceUnit("user")
                .build();
    }

    @Bean(name = "journalTransactionManager")
    public PlatformTransactionManager transactionManager(
            @Qualifier("journalEntityManager") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject()));
    }
}