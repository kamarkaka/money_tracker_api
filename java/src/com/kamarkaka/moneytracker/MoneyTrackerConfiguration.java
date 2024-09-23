package com.kamarkaka.moneytracker;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kamarkaka.moneytracker.db.DSLContextFactory;
import io.dropwizard.core.Configuration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class MoneyTrackerConfiguration extends Configuration {
    @Valid
    @NotNull
    private DSLContextFactory database = new DSLContextFactory();

    @JsonProperty("database")
    public DSLContextFactory getDSLContextFactory() { return database; }

    @JsonProperty("database")
    public void setDSLContextFactory(DSLContextFactory database) { this.database = database; }
}
