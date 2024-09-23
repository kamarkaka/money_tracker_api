package com.kamarkaka.moneytracker.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kamarkaka.moneytracker.db.dao.AccountDAO;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.lifecycle.Managed;
import jakarta.validation.constraints.NotNull;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DSLContextFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DSLContextFactory.class);

    @NotNull
    private String url;

    @NotNull
    private String name;

    @NotNull
    private String user;

    @NotNull
    private String password;

    @JsonProperty
    public String getUrl() {
        return url;
    }

    @JsonProperty
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public String getUser() {
        return user;
    }

    @JsonProperty
    public void setUser(String user) {
        this.user = user;
    }

    @JsonProperty
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }

    private static HikariDataSource dataSource = null;

    public void build(Environment environment) {
        LOGGER.info("building hikari config");
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getUrl() + getName());
        config.setUsername(getUser());
        config.setPassword(getPassword());

        dataSource = new HikariDataSource(config);

        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() {}

            @Override
            public void stop() throws Exception {
                dataSource.close();
            }
        });
    }

    public static DSLContext getDSLContext() {
        if (dataSource == null) throw new RuntimeException("Build DSLContextFactory before getting DSLContext");
        return DSL.using(dataSource, SQLDialect.POSTGRES);
    }
}
