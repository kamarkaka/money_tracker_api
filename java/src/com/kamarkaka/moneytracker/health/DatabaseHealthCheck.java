package com.kamarkaka.moneytracker.health;

import com.codahale.metrics.health.HealthCheck;
import com.kamarkaka.moneytracker.db.DSLContextFactory;
import org.jooq.DSLContext;

public class DatabaseHealthCheck extends HealthCheck {
    public DatabaseHealthCheck() {}

    @Override
    protected Result check() throws Exception {
        boolean isValid = DSLContextFactory.getDSLContext().diagnosticsConnection().isValid(5);
        if (isValid) {
            return Result.healthy();
        }

        return Result.unhealthy("cannot connect to database");
    }
}
