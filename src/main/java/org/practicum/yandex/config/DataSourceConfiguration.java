package org.practicum.yandex.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfiguration {

    @Bean
    public HikariConfig hikariConfig(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password
    ) {
        final var config = new HikariConfig();

        config.setDriverClassName(Driver.class.getName());
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);

        return config;
    }

    @Bean
    public DataSource hikariDataSource(HikariConfig hikariConfig) {
        return new HikariDataSource(hikariConfig);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @EventListener(ContextRefreshedEvent.class)
    public void prepareDatabase(ContextRefreshedEvent event) {
        final var dataSource = event.getApplicationContext().getBean(DataSource.class);

        final var populator = new ResourceDatabasePopulator();

        populator.addScript(new ClassPathResource("sql/schema.sql"));
        populator.execute(dataSource);
    }

}
