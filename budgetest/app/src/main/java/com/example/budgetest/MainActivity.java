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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);
        setupUI();
        loadBudgets();

// Bottom Navigation Setup
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Set selected item to 'Home' programmatically
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                // Stay in Home (MainActivity)
                return true;
            } else if (itemId == R.id.nav_Summary) {
                // Navigate to Profile
                startActivity(new Intent(this, SummaryActivity.class));
                finish();  // Finish MainActivity to avoid back navigation to it
                return true;
            } else if (itemId == R.id.nav_settings) {
                // Navigate to Settings
                startActivity(new Intent(this, SettingsActivity.class));
                finish();  // Finish MainActivity to avoid back navigation to it
                return true;
            }

            return false;
        });
    }

    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0);
        loadBudgets();
    }

    private void setupUI() {
        // Setup RecyclerView
        AtomicReference<List<Budget>> budgets = new AtomicReference<>(dbHelper.getAllBudgets());
        budgetAdapter = new BudgetAdapter(budgets.get(), dbHelper, budget -> {
            // Handle budget item click
            Intent intent = new Intent(this, BudgetDetailsActivity.class);
            intent.putExtra("BUDGET_ID", budget.getId());
            startActivity(intent);
        });

        binding.rvBudgets.setAdapter(budgetAdapter);

        // Setup Create Budget Button
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
                loadBudgets();
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

            if (!name.isEmpty() && amount != null && amount > 0 && !selectedBudget.isEmpty()) {
                // Find the budget ID from the selected budget name
                budgets.set(dbHelper.getAllBudgets());
                Budget budget = budgets.get().stream()
                        .filter(b -> b.getName().equals(selectedBudget))
                        .findFirst()
                        .orElse(null);

                if (budget != null) {
                    long expenseId = 0;
                    Expense expense = new Expense(expenseId, name, amount, budget.getId(), "");
                    dbHelper.addExpense(expense);
                    clearExpenseInputs();
                    loadBudgets(); // Refresh the budget list to show updated progress
                }
            } else {
                Toast.makeText(this, "Please enter valid expense details", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup Budget Category Spinner
        updateBudgetCategorySpinner();
    }

    private void loadBudgets() {
        List<Budget> budgets = dbHelper.getAllBudgets();
        budgetAdapter.updateBudgets(budgets);
        updateBudgetCategorySpinner();
    }

    private void updateBudgetCategorySpinner() {
        List<Budget> budgets = dbHelper.getAllBudgets();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                budgets.stream().map(Budget::getName).collect(Collectors.toList())
        );
        binding.spinnerBudgetCategory.setAdapter(adapter);
    }

    private void clearBudgetInputs() {
        binding.edtBudgetName.getText().clear();
        binding.edtBudgetAmount.getText().clear();
    }

    private void clearExpenseInputs() {
        binding.edtExpenseName.getText().clear();
        binding.edtExpenseAmount.getText().clear();
        binding.spinnerBudgetCategory.getText().clear();
    }
}

