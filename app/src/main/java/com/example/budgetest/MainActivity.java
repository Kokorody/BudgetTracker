//mainactivity

package com.example.budgetest;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.budgetest.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private DatabaseHelper dbHelper;
    private BudgetAdapter budgetAdapter;
    private List<Category> categories;
    private List<Budget> budgets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);
        setupUI();
        loadData();

        // Bottom Navigation Setup (unchanged)
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_Summary) {
                startActivity(new Intent(this, SummaryActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            }

            return false;
        });
    }

    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
        loadData();
    }

    private void loadData() {
        // Load budgets
        budgets = dbHelper.getAllBudgets();
        budgetAdapter.updateBudgets(budgets);

        // Load categories
        categories = dbHelper.getAllCategories();

        // Update spinners
        updateBudgetCategorySpinner();
        updateExpenseCategorySpinner();
    }

    private void setupUI() {
        // Setup RecyclerView
        AtomicReference<List<Budget>> budgetsRef = new AtomicReference<>(dbHelper.getAllBudgets());
        budgetAdapter = new BudgetAdapter(budgetsRef.get(), dbHelper, budget -> {
            // Handle budget item click
            Intent intent = new Intent(this, BudgetDetailsActivity.class);
            intent.putExtra("BUDGET_ID", budget.getId());
            startActivity(intent);
        });

        binding.rvBudgets.setAdapter(budgetAdapter);

        // Setup Create Budget Button (unchanged)
        binding.btnCreateBudget.setOnClickListener(v -> {
            String name = binding.edtBudgetName.getText().toString();
            Double amount = null;
            try {
                amount = Double.parseDouble(binding.edtBudgetAmount.getText().toString());
            } catch (NumberFormatException e) {
                // Handle parsing error
            }

            if (!name.isEmpty() && amount != null && amount > 0) {
                long id = 0;
                Budget budget = new Budget(id,  name, amount);
                dbHelper.addBudget(budget);
                clearBudgetInputs();
                loadData();
            } else {
                Toast.makeText(this, "Please enter valid budget details", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup Add Expense Button
        binding.btnAddExpense.setOnClickListener(v -> {
            String name = binding.edtExpenseName.getText().toString();
            Double amount = null;
            try {
                amount = Double.parseDouble(binding.edtExpenseAmount.getText().toString());
            } catch (NumberFormatException e) {
                // Handle parsing error
            }

            String selectedBudget = binding.spinnerBudgetCategory.getText().toString();
            String selectedCategory = binding.spinnerExpenseCategory.getText().toString();

            if (!name.isEmpty() && amount != null && amount > 0 &&
                    !selectedBudget.isEmpty() && !selectedCategory.isEmpty()) {

                // Find the budget ID from the selected budget name
                Budget budget = budgets.stream()
                        .filter(b -> b.getName().equals(selectedBudget))
                        .findFirst()
                        .orElse(null);

                // Find the category ID from the selected category name
                Category category = categories.stream()
                        .filter(c -> c.getName().equals(selectedCategory))
                        .findFirst()
                        .orElse(null);

                if (budget != null && category != null) {
                    long expenseId = 0;  // or any other logic to set the expenseId
                    String categoryName = category.getName();  // Assuming the Category object has a getName() method

                    // Create the Expense object with all required parameters
                    Expense expense = new Expense(expenseId, name, amount, budget.getId(), category.getId(), "", categoryName);

                    // Add the expense to the database
                    dbHelper.addExpense(expense);

                    // Clear the input fields and reload the data
                    clearExpenseInputs();
                    loadData(); // Refresh the budget list to show updated progress
                }

            } else {
                Toast.makeText(this, "Please enter valid expense details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBudgetCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                budgets.stream().map(Budget::getName).collect(Collectors.toList())
        );
        binding.spinnerBudgetCategory.setAdapter(adapter);
    }

    private void updateExpenseCategorySpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories.stream().map(Category::getName).collect(Collectors.toList())
        );
        binding.spinnerExpenseCategory.setAdapter(adapter);
    }

    private void clearBudgetInputs() {
        binding.edtBudgetName.getText().clear();
        binding.edtBudgetAmount.getText().clear();
    }

    private void clearExpenseInputs() {
        binding.edtExpenseName.getText().clear();
        binding.edtExpenseAmount.getText().clear();
        binding.spinnerBudgetCategory.getText().clear();
        binding.spinnerExpenseCategory.getText().clear();
    }
}