package com.kamarkaka.moneytracker.resources;

import com.kamarkaka.moneytracker.core.model.Budget;
import com.kamarkaka.moneytracker.db.dao.BudgetDAO;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Optional;

@Path("/budgets")
@Produces(MediaType.APPLICATION_JSON)
public class BudgetResource {
    private final BudgetDAO budgetDAO;

    public BudgetResource(BudgetDAO budgetDAO) {
        this.budgetDAO = budgetDAO;
    }

    @GET
    @Path("/list")
    public Response list() {
        return Response.ok(budgetDAO.read()).build();
    }

    @GET
    @Path("/get")
    public Response getById(
            @QueryParam("id")
            @NotNull
            int id) {
        Optional<Budget> budget = budgetDAO.getById(id);
        if (budget.isPresent()) {
            return Response.ok(budget.get()).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
