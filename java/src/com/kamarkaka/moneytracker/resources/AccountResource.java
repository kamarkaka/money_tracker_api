package com.kamarkaka.moneytracker.resources;

import com.kamarkaka.moneytracker.core.model.Account;
import com.kamarkaka.moneytracker.db.dao.AccountDAO;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Optional;

@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
public class AccountResource {
    private final AccountDAO accountDAO;

    public AccountResource(AccountDAO accountDAO) {
        this.accountDAO = accountDAO;
    }

    @GET
    @Path("/list")
    public Response list() {
        return Response.ok(accountDAO.read()).build();
    }

    @GET
    @Path("/get")
    public Response getById(
            @QueryParam("id")
            @NotNull
            int id) {
        Optional<Account> account = accountDAO.getById(id);
        if (account.isPresent()) {
            return Response.ok(account.get()).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }
}