package br.com.redemaisfarma.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import java.util.Collections;
import java.util.Map;
import javax.sql.DataSource;

@Configuration
public class JpaBuilderConfig {

    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder(
            ObjectProvider<PersistenceUnitManager> pumProvider) {

        final HibernateJpaVendorAdapter vendor = new HibernateJpaVendorAdapter();
        return new EntityManagerFactoryBuilder(
                vendor,
                this::buildJpaProperties,
                pumProvider.getIfAvailable()
        );
    }

    private Map<String, ?> buildJpaProperties(
            final DataSource ignoredDataSource
    ) {
        return Collections.emptyMap();
    }
}
