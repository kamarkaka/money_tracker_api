package com.kamarkaka.moneytracker;

import com.kamarkaka.moneytracker.db.dao.*;
import com.kamarkaka.moneytracker.health.DatabaseHealthCheck;
import com.kamarkaka.moneytracker.resources.AccountResource;
import com.kamarkaka.moneytracker.resources.BudgetResource;
import com.kamarkaka.moneytracker.resources.CategoryResource;
import com.kamarkaka.moneytracker.resources.TransactionResource;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.forms.MultiPartBundle;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.jooq.DSLContext;

import java.util.EnumSet;

public class MoneyTrackerApplication extends Application<MoneyTrackerConfiguration> {
    public static void main(String[] args) throws Exception {
        new MoneyTrackerApplication().run(args);
    }

    @Override
    public String getName() {
        return "money tracker";
    }

    @Override
    public void initialize(Bootstrap<MoneyTrackerConfiguration> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
            new SubstitutingSourceProvider(
                bootstrap.getConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor()
            )
        );

        bootstrap.addBundle(new MultiPartBundle());

    }

    @Override
    public void run(MoneyTrackerConfiguration configuration, Environment environment) {
        configuration.getDSLContextFactory().build(environment);
        AccountDAO accountDAO = new AccountDAO();
        BudgetDAO budgetDAO = new BudgetDAO();
        CategoryDAO categoryDAO = new CategoryDAO();
        LabelDAO labelDAO = new LabelDAO();
        RuleDAO ruleDAO = new RuleDAO();
        TransactionDAO transactionDAO = new TransactionDAO(labelDAO);

        // resources
        AccountResource accountResource = new AccountResource(accountDAO);
        BudgetResource budgetResource = new BudgetResource(budgetDAO);
        CategoryResource categoryResource = new CategoryResource(categoryDAO);
        TransactionResource transactionResource =  new TransactionResource(accountDAO, budgetDAO, categoryDAO, labelDAO, ruleDAO, transactionDAO);

        environment.jersey().register(accountResource);
        environment.jersey().register(budgetResource);
        environment.jersey().register(categoryResource);
        environment.jersey().register(transactionResource);

        // health checks
        DatabaseHealthCheck dbHealthCheck = new DatabaseHealthCheck();
        environment.healthChecks().register("database", dbHealthCheck);

        // Enable CORS headers
        final FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);

        // Configure CORS parameters
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "GET,POST");

        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
}
