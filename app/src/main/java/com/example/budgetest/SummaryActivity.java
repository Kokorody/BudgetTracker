//summaryactivity / piechart

package com.example.budgetest;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummaryActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;

    private PieChart pieChart;
    private RecyclerView expenseListView;
    private AllExpenseAdapter expenseAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart); // Ensure your layout includes RecyclerView

        // Initialize UI components
        pieChart = findViewById(R.id.pieGraph);
        expenseListView = findViewById(R.id.expenseListView); // Make sure this ID exists in your layout

        // Set up RecyclerView
        expenseListView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Bottom Navigation View
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_Summary);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_Summary) {
                return true;
            } else if (itemId == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                finish();
                return true;
            }
            return false;
        });

        // Fetch expenses and load data
        dbHelper = new DatabaseHelper(this);
        loadPieChartData();
        loadExpenseList();
    }

    private void loadPieChartData() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        List<Expense> allExpenses = dbHelper.getAllExpenses();

        // Calculate total expenses per category
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Expense expense : allExpenses) {
            String category = expense.getCategoryName();
            double amount = expense.getAmount();
            categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
        }

        // Add data to the pie chart
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            String categoryName = entry.getKey();
            double amount = entry.getValue();

            int color = getCategoryColor(categoryName);
            pieChart.addPieSlice(new PieModel(categoryName, (float) amount, color));
        }

        pieChart.startAnimation();
    }

    private void loadExpenseList() {
        List<Expense> allExpenses = dbHelper.getAllExpenses();

        // Initialize the adapter with the expense list
        expenseAdapter = new AllExpenseAdapter(allExpenses);
        expenseListView.setAdapter(expenseAdapter);
    }

    private int getCategoryColor(String category) {
        switch (category) {
            case "Shopping":
                return Color.parseColor("#FFA726");
            case "Transportation":
                return Color.parseColor("#66BB6A");
            case "Lifestyle":
                return Color.parseColor("#EF5350");
            case "Bills":
                return Color.parseColor("#29B6F6");
            case "Grocery":
                return Color.parseColor("#AB47BC");
            case "Food & Drink":
                return Color.parseColor("#FF7043");
            case "Entertainment":
                return Color.parseColor("#8D6E63");
            case "Health":
                return Color.parseColor("#4CAF50");
            case "Education":
                return Color.parseColor("#FFEB3B");
            case "Personal Care":
                return Color.parseColor("#00ACC1");
            case "Technology":
                return Color.parseColor("#F44336");
            case "Home":
                return Color.parseColor("#9E9D24");
            case "Utilities":
                return Color.parseColor("#03A9F4");
            case "Insurance":
                return Color.parseColor("#9C27B0");
            case "Subscriptions":
                return Color.parseColor("#FF9800");
            default:
                return Color.parseColor("#607D8B");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        overridePendingTransition(0, 0); // Disable transition animation
    }
}
