package payup.payup.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableJpaRepositories(basePackages = "payup.repository")
public class DatabaseConfig {

    /**
     * Configures a data source for development environment using H2.
     * 
     * @return DataSource for development.
     */
    @Bean
    @Profile("!production")
    public DataSource dataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.h2.Driver")
                .url("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1")
                .username("sa")
                .password("payup##")
                .build();
    }

    /**
     * Configures a data source for the production environment using PostgreSQL.
     * 
     * @return DataSource for production.
     */
    @Bean
    @Profile("production")
    public DataSource productionDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url("${spring.datasource.url}")
                .username("${spring.datasource.username}")
                .password("${spring.datasource.password}")
                .build();
    }

    /**
     * Creates an EntityManagerFactory bean for managing JPA entities.
     * 
     * @return LocalContainerEntityManagerFactoryBean for JPA entity management.
     */
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource()); // or use developmentDataSource based on profile
        em.setPackagesToScan("payup.payup.model");

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        em.setJpaProperties(additionalProperties());

        return em;
    }

    /**
     * Additional Hibernate properties for database configuration.
     * 
     * @return Properties object with Hibernate configurations.
     */
    Properties additionalProperties() {
        Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "update"); // Change to 'update' for development
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect"); // or H2Dialect
        properties.setProperty("hibernate.encrypt.enabled", "true");
        return properties;
    }

    /**
     * Configures transaction management for JPA.
     * 
     * @return PlatformTransactionManager for managing transactions.
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }
}