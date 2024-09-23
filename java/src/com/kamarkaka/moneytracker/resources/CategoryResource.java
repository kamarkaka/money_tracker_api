package com.kamarkaka.moneytracker.resources;

import com.kamarkaka.moneytracker.core.model.Category;
import com.kamarkaka.moneytracker.db.dao.CategoryDAO;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Optional;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {
    private final CategoryDAO categoryDAO;

    public CategoryResource(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    @GET
    @Path("/list")
    public Response list() {
        return Response.ok(categoryDAO.read()).build();
    }

    @GET
    @Path("/get")
    public Response getById(
            @QueryParam("id")
            @NotNull
            int id) {
        Optional<Category> category = categoryDAO.getById(id);
        if (category.isPresent()) {
            return Response.ok(category.get()).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }
}