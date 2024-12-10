package com.example.budgetest;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.budgetest.databinding.ActivityBudgetDetailsBinding;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ArrayList;
import androidx.recyclerview.widget.LinearLayoutManager;


public class BudgetDetailsActivity extends AppCompatActivity {
    private ActivityBudgetDetailsBinding binding;
    private com.example.budgetest.DatabaseHelper dbHelper;
    private ExpenseAdapter expenseAdapter;
    private long budgetId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBudgetDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new com.example.budgetest.DatabaseHelper(this);

        // Get budget ID from intent
        budgetId = getIntent().getLongExtra("BUDGET_ID", -1);
        if (budgetId == -1L) {
            Toast.makeText(this, "Invalid budget selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        loadBudgetDetails();
    }

    private void setupUI() {
        // Setup RecyclerView for expenses
        expenseAdapter = new ExpenseAdapter(
                new ArrayList<>(),
                expense -> showEditExpenseDialog(expense),
                expense -> confirmDeleteExpense(expense)
        );
        binding.rvExpenses.setLayoutManager(new LinearLayoutManager(this));
        binding.rvExpenses.setAdapter(expenseAdapter);

        // Add Expense Button
        binding.btnAddExpense.setOnClickListener(v -> {
            String expenseName = binding.edtExpenseName.getText().toString();
            Double expenseAmount = null;
            try {
                expenseAmount = Double.parseDouble(binding.edtExpenseAmount.getText().toString());
            } catch (NumberFormatException e) {
                // Handle exception
            }

            if (!expenseName.isEmpty() && expenseAmount != null && expenseAmount > 0) {
                // Get the total spent and budget details
                double totalSpent = dbHelper.getTotalExpensesForBudget(budgetId);
                com.example.budgetest.Budget budget = dbHelper.getBudget(budgetId);

                if (budget != null) {
                    double newTotalSpent = totalSpent + expenseAmount;

                    if (newTotalSpent > budget.amount) {
                        // Call warning dialog if expense exceeds the budget
                        showExceedBudgetWarning(expenseName, expenseAmount, newTotalSpent, budget.amount);
                    } else {
                        // Add expense directly if within the budget
                        addExpenseToDatabase(expenseName, expenseAmount);
                    }
                }
            } else {
                Toast.makeText(this, "Please enter valid expense details", Toast.LENGTH_SHORT).show();
            }
        });

        // Delete Budget Button with confirmation
        binding.btnDeleteBudget.setOnClickListener(v -> confirmDeleteBudget());

        // Edit Budget Button
        binding.btnEditBudget.setOnClickListener(v -> showEditBudgetDialog());
    }

    private void confirmDeleteBudget() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Budget")
                .setMessage("Are you sure you want to delete this budget? All associated expenses will also be deleted.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteBudget(budgetId); // Delete the budget
                    Toast.makeText(this, "Budget deleted successfully", Toast.LENGTH_SHORT).show();

                    // Notify MainActivity to refresh the budgets list
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("REFRESH_BUDGETS", true);
                    setResult(RESULT_OK, resultIntent);

                    finish(); // Close the BudgetDetailsActivity
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditExpenseDialog(Expense expense) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_edit_expense, null);
        EditText edtExpenseName = dialogView.findViewById(R.id.edtExpenseName);
        EditText edtExpenseAmount = dialogView.findViewById(R.id.edtExpenseAmount);

        // Pre-fill the expense details
        edtExpenseName.setText(expense.name);
        edtExpenseAmount.setText(String.valueOf(expense.amount));

        new AlertDialog.Builder(this)
                .setTitle("Edit Expense")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = edtExpenseName.getText().toString();
                    Double newAmount = null;
                    try {
                        newAmount = Double.parseDouble(edtExpenseAmount.getText().toString());
                    } catch (NumberFormatException e) {
                        // Handle exception
                    }

                    if (!newName.isEmpty() && newAmount != null && newAmount > 0) {
                        Expense updatedExpense = new Expense(expense.id, newName, newAmount, expense.budgetId, expense.createdAt);
                        dbHelper.updateExpense(updatedExpense); // Update the expense in the DB

                        loadBudgetDetails(); // Refresh details page
                        Toast.makeText(this, "Expense updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Please enter valid expense details", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void confirmDeleteExpense(com.example.budgetest.Expense expense) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteExpense(expense.id); // Delete the expense from the database
                    loadBudgetDetails(); // Refresh the list
                    Toast.makeText(this, "Expense deleted successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadBudgetDetails() {
        com.example.budgetest.Budget budget = dbHelper.getBudget(budgetId);
        if (budget == null) {
            Toast.makeText(this, "Budget not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Currency formatter for Indonesian Rupiah
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));

        // Update UI with budget data
        binding.tvBudgetName.setText(budget.name);
        binding.tvBudgetAmount.setText(currencyFormatter.format(budget.amount).replace("Rp", "Rp "));

        double totalSpent = dbHelper.getTotalExpensesForBudget(budgetId);
        binding.tvSpentAmount.setText(currencyFormatter.format(totalSpent).replace("Rp", "Rp "));
        binding.tvRemainingAmount.setText(currencyFormatter.format(budget.amount - totalSpent).replace("Rp", "Rp "));

        int progress = (int) ((totalSpent / budget.amount) * 100);
        progress = Math.max(0, Math.min(progress, 100));
        binding.progressBar.setProgress(progress);
        binding.tvProgressPercentage.setText(progress + "%");

        // Load expenses and update adapter
        expenseAdapter.updateExpenses(dbHelper.getExpensesForBudget(budgetId));
    }

    private void clearExpenseInputs() {
        binding.edtExpenseName.getText().clear();
        binding.edtExpenseAmount.getText().clear();
    }

    private void addExpenseToDatabase(String expenseName, double expenseAmount) {
        com.example.budgetest.Expense expense = new com.example.budgetest.Expense(-1, expenseName, expenseAmount, budgetId, "");
        dbHelper.addExpense(expense);
        loadBudgetDetails(); // Refresh details
        clearExpenseInputs();
        Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show();
    }

    private void showExceedBudgetWarning(String expenseName, double expenseAmount, double newTotalSpent, double budgetAmount) {
        String warningMessage = String.format(
                "Warning: Adding this expense will exceed the budget!\n\n" +
                        "Budget Amount: Rp %.2f\n" +
                        "Current Total Spent: Rp %.2f\n" +
                        "Expense Amount: Rp %.2f\n" +
                        "New Total Spent: Rp %.2f",
                budgetAmount, newTotalSpent - expenseAmount, expenseAmount, newTotalSpent
        );

        new AlertDialog.Builder(this)
                .setTitle("Lu Bokek Anjay")
                .setMessage(warningMessage)
                .setPositiveButton("Proceed", (dialog, which) -> {
                    // User confirms to proceed
                    addExpenseToDatabase(expenseName, expenseAmount);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditBudgetDialog() {
        com.example.budgetest.Budget budget = dbHelper.getBudget(budgetId);
        if (budget == null) {
            Toast.makeText(this, "Budget not found", Toast.LENGTH_SHORT).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_edit_budget, null);
        EditText edtBudgetName = dialogView.findViewById(R.id.edtBudgetName);
        EditText edtBudgetAmount = dialogView.findViewById(R.id.edtBudgetAmount);

        // Pre-fill budget details
        edtBudgetName.setText(budget.name);
        edtBudgetAmount.setText(String.valueOf(budget.amount));

        new AlertDialog.Builder(this)
                .setTitle("Edit Budget")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = edtBudgetName.getText().toString();
                    Double newAmount = null;
                    try {
                        newAmount = Double.parseDouble(edtBudgetAmount.getText().toString());
                    } catch (NumberFormatException e) {
                        // Handle exception
                    }

                    if (!newName.isEmpty() && newAmount != null && newAmount > 0) {
                        Budget updatedBudget = new Budget(budget.id, newName, newAmount);
                        dbHelper.updateBudget(updatedBudget); // Update in DB

                        // Notify MainActivity to refresh data
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("REFRESH_BUDGETS", true);
                        setResult(RESULT_OK, resultIntent);

                        loadBudgetDetails(); // Refresh details page
                        Toast.makeText(this, "Budget updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Please enter valid details", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();

    }
}

