package com.kamarkaka.moneytracker.resources;

import com.kamarkaka.moneytracker.core.model.Account;
import com.kamarkaka.moneytracker.core.model.Budget;
import com.kamarkaka.moneytracker.core.model.Category;
import com.kamarkaka.moneytracker.core.model.Label;
import com.kamarkaka.moneytracker.core.model.Rule;
import com.kamarkaka.moneytracker.core.model.Transaction;
import com.kamarkaka.moneytracker.db.dao.AccountDAO;
import com.kamarkaka.moneytracker.db.dao.BudgetDAO;
import com.kamarkaka.moneytracker.db.dao.CategoryDAO;
import com.kamarkaka.moneytracker.db.dao.LabelDAO;
import com.kamarkaka.moneytracker.db.dao.RuleDAO;
import com.kamarkaka.moneytracker.db.dao.TransactionDAO;
import com.kamarkaka.moneytracker.db.TransactionFilter;
import com.kamarkaka.moneytracker.db.TransactionFilterBuilder;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jooq.tools.csv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Path("/transactions")
@Produces(MediaType.APPLICATION_JSON)
public class TransactionResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionResource.class);

    private final AccountDAO accountDAO;
    private final BudgetDAO budgetDAO;
    private final CategoryDAO categoryDAO;
    private final LabelDAO labelDAO;
    private final RuleDAO ruleDAO;
    private final TransactionDAO transactionDAO;

    public TransactionResource(
            AccountDAO accountDAO,
            BudgetDAO budgetDAO,
            CategoryDAO categoryDAO,
            LabelDAO labelDAO,
            RuleDAO ruleDAO,
            TransactionDAO transactionDAO) {
        this.accountDAO = accountDAO;
        this.budgetDAO = budgetDAO;
        this.categoryDAO = categoryDAO;
        this.labelDAO = labelDAO;
        this.ruleDAO = ruleDAO;
        this.transactionDAO = transactionDAO;
    }

    @GET
    @Path("/list")
    public Response list(
            @NotNull
            @QueryParam("begin-date")
            String beginDate,

            @NotNull
            @QueryParam("end-date")
            String endDate,

            @QueryParam("account-ids")
            String accountIdsStr,

            @DefaultValue("-1")
            @QueryParam("amount-min")
            Float amountMin,

            @DefaultValue("-1")
            @QueryParam("amount-max")
            Float amountMax,

            @DefaultValue("0")
            @QueryParam("budget-id")
            int budgetId,

            @QueryParam("category-ids")
            String categoryIdsStr,

            @QueryParam("query")
            String descriptionSearchQueryStr,

            @DefaultValue("false")
            @QueryParam("show-duplicated")
            boolean showDuplicated,

            @DefaultValue("false")
            @QueryParam("show-hidden")
            boolean showHidden,

            @DefaultValue("false")
            @QueryParam("show-uncategorized")
            boolean showUncategorized
    ) {
        TransactionFilterBuilder builder = new TransactionFilterBuilder();
        builder.setDateRange(LocalDate.parse(beginDate, DateTimeFormatter.ofPattern("yyyyMMdd")),
                             LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyyMMdd")));

        if (accountIdsStr != null && !accountIdsStr.isBlank()) {
            List<Integer> accountIds = Arrays.stream(accountIdsStr.split(",")).map(Integer::parseInt).toList();
            builder.setAccountIds(accountIds);
        }

        builder.setAmountMin(amountMin);
        builder.setAmountMax(amountMax);

        // If a valid budget id is present, use the budget id to fetch its associated category ids. In the generated
        // query, it will ONLY search for transactions with those category ids, NOT their child categories.
        // If budget id is set to -1, it will use all category ids that are NOT in ANY of the budgets, and similarly,
        // it will only search for transactions with those category ids in the generated query.
        // If there is no budget id present, just a list of category ids, the generated query will search in a
        // category's child category as well.
        // If there is no budget id and no category ids, but show-uncategorized flag is on, the generated query will
        // ONLY fetch those transactions without categories
        if (budgetId > 0) {
            Optional<Budget> budget = budgetDAO.getById(budgetId);

            if (budget.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            builder.setBudgetId(budget.get().getId(), budget.get().getCategoryIds());
        } else if (budgetId == -1) {
            // others
            Set<Integer> categoryIdSet = categoryDAO.read().stream().map(Category::getId).collect(Collectors.toSet());
            List<Budget> budgets = budgetDAO.read();
            budgets.forEach(b -> {
                b.getCategoryIds().forEach(categoryIdSet::remove);
            });
            builder.setBudgetId(-1, categoryIdSet.stream().toList());
        } else if (categoryIdsStr != null && !categoryIdsStr.isBlank()) {
            List<Integer> categoryIds = Arrays.stream(categoryIdsStr.split(",")).map(Integer::parseInt).toList();
            builder.setCategoryIds(categoryIds);
        } else if (showUncategorized) {
            builder.setShowTransactionWithNoCategory();
        }

        builder.setDescriptionQuery(descriptionSearchQueryStr);

        if (showDuplicated) builder.setShowDuplicated();
        if (showHidden) builder.setShowHidden();

        TransactionFilter filter = builder.build();
        List<Transaction> transactions = transactionDAO.listByFilter(filter);
        return Response.ok(transactions).build();
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    public int processUploadedFile(InputStream inputStream) {
        int numInserted = 0;
        CSVReader reader = new CSVReader(new InputStreamReader(inputStream));

        // skip header
        reader.next();

        while (reader.hasNext()) {
            String[] line = reader.next();
            if (line == null || line.length < 1) {
                continue;
            }
            LOGGER.info(Arrays.toString(line));

            boolean isPending = line[6].equalsIgnoreCase("yes");
            if (isPending) {
                LOGGER.info("Pending transaction, skip...");
                continue;
            }

            LocalDate date = LocalDate.parse(line[0], DateTimeFormatter.ofPattern("M/d/yyyy"));
            String description = line[1];
            Optional<Account> account = accountDAO.getByName(line[3].trim());
            boolean isHidden = line[5].equalsIgnoreCase("yes");
            boolean isDuplicated = false;

            boolean isNegative = line[7].contains("(");
            String amountStr = line[7].replaceAll("[$()]", "");
            double amountValue = Double.parseDouble(amountStr);

            BigDecimal amount;
            if (isNegative) {
                amount = BigDecimal.valueOf(-amountValue);
            } else {
                amount = BigDecimal.valueOf(amountValue);
            }

            LOGGER.info("Date: {}, Description: {}, Account Id: {}, Is Hidden: {}, Is Duplicated: {}, Amount: {}", date, description, account.orElse(new Account(-1, line[3])).getId(), isHidden, isDuplicated, amount);
            Transaction transaction = new Transaction(
                    -1,
                    date,
                    description,
                    account.orElse(null),
                    null,
                    new HashSet<>(),
                    isHidden,
                    false,
                    isDuplicated,
                    amount
            );
            int inserted = transactionDAO.create(transaction);
            if (inserted == 0) {
                LOGGER.info("Duplication detected, transaction not inserted");
            } else {
                LOGGER.info("Transaction inserted!");
            }

            numInserted += inserted;

        }
        LOGGER.info("{} transaction inserted", numInserted);
        return numInserted;
    }

    @GET
    @Path("/match-category")
    public Response matchCategory() {
        int numMatched = 0;
        List<Transaction> transactions = transactionDAO.listUncategorized();
        LOGGER.info("{} uncategorized transactions fetched", transactions.size());
        List<Rule> rules = ruleDAO.read();
        LOGGER.info("{} rules fetched", rules.size());

        for (Transaction transaction : transactions) {
            for (Rule rule : rules) {
                if (transaction.getDescription().toLowerCase().contains(
                        rule.getDescription().toLowerCase()
                )) {
                    LOGGER.info("Match found! {}", rule.getDescription());
                    Optional<Category> category = categoryDAO.getById(rule.getCategoryId());
                    if (category.isPresent()) {
                        Transaction updatedTransaction = new Transaction(
                                transaction.getId(),
                                transaction.getDate(),
                                transaction.getDescription(),
                                transaction.getAccount(),
                                category.get(),
                                transaction.getLabels(),
                                transaction.isHidden(),
                                transaction.isPending(),
                                transaction.isDuplicated(),
                                transaction.getAmount()
                        );

                        numMatched += transactionDAO.update(updatedTransaction);
                    }
                    break;
                }
            }
        }

        return Response.ok(numMatched).build();
    }

    @POST
    @Path("/update-category")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateCategory(
            @NotNull
            @FormDataParam("id")
            long id,

            @NotNull
            @FormDataParam("category-id")
            int categoryId) {

        Optional<Transaction> transaction = transactionDAO.getById(id);
        Optional<Category> category = categoryDAO.getById(categoryId);

        if (transaction.isPresent() && category.isPresent()) {
            Transaction updatedTransaction = new Transaction(
                    transaction.get().getId(),
                    transaction.get().getDate(),
                    transaction.get().getDescription(),
                    transaction.get().getAccount(),
                    category.get(),
                    transaction.get().getLabels(),
                    transaction.get().isHidden(),
                    transaction.get().isPending(),
                    transaction.get().isDuplicated(),
                    transaction.get().getAmount()
            );

            int numUpdated = transactionDAO.update(updatedTransaction);
            return Response.ok(numUpdated).build();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }


    @POST
    @Path("/add-label")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response addLabel(
            @NotNull
            @FormDataParam("id")
            long id,

            @DefaultValue("-1")
            @FormDataParam("label-id")
            int labelId,

            @FormDataParam("label-name")
            String labelName) {

        LOGGER.info("tid: {}, lid: {}, lname: {}", id, labelId, labelName);

        Optional<Transaction> transaction = transactionDAO.getById(id);
        if (transaction.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Optional<Label> label;

        if (labelId <= 0) {
            if (labelName == null || labelName.isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            labelId = labelDAO.create(new Label(labelId, labelName));
        }

        label = labelDAO.getById(labelId);
        if (label.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Set<Label> labels = transaction.get().getLabels();
        labels.add(label.get());

        Transaction updatedTransaction = new Transaction(
                transaction.get().getId(),
                transaction.get().getDate(),
                transaction.get().getDescription(),
                transaction.get().getAccount(),
                transaction.get().getCategory(),
                labels,
                transaction.get().isHidden(),
                transaction.get().isPending(),
                transaction.get().isDuplicated(),
                transaction.get().getAmount()
        );

        int numUpdated = transactionDAO.update(updatedTransaction);
        return Response.ok(numUpdated).build();
    }

    @POST
    @Path("/delete-label")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response deleteLabel(
            @NotNull
            @FormDataParam("id")
            long id,

            @NotNull
            @FormDataParam("label-id")
            int labelId) {

        int numUpdated = 0;

        Optional<Transaction> transaction = transactionDAO.getById(id);
        Optional<Label> label = labelDAO.getById(labelId);
        if (transaction.isEmpty() || label.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Set<Label> labels = transaction.get().getLabels();
        boolean removed = labels.remove(label.get());

        if (removed) {
            Transaction updatedTransaction = new Transaction(
                    transaction.get().getId(),
                    transaction.get().getDate(),
                    transaction.get().getDescription(),
                    transaction.get().getAccount(),
                    transaction.get().getCategory(),
                    labels,
                    transaction.get().isHidden(),
                    transaction.get().isPending(),
                    transaction.get().isDuplicated(),
                    transaction.get().getAmount()
            );

            numUpdated = transactionDAO.update(updatedTransaction);
        }

        LOGGER.info("numUpdated: {}", numUpdated);
        return Response.ok(numUpdated).build();
    }
}
