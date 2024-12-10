// dbhelper

package com.example.budgetest;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BudgetTrackerDB";
    private static final int DATABASE_VERSION = 2; // Increment version to trigger upgrade

    // Table Names
    private static final String TABLE_BUDGETS = "budgets";
    private static final String TABLE_EXPENSES = "expenses";
    private static final String TABLE_CATEGORIES = "categories";

    // Common Columns
    private static final String KEY_ID = "id";
    private static final String KEY_CREATED_AT = "created_at";

    // Budgets Table Columns
    private static final String KEY_BUDGET_NAME = "name";
    private static final String KEY_BUDGET_AMOUNT = "amount";

    // Expenses Table Columns
    private static final String KEY_EXPENSE_NAME = "name";
    private static final String KEY_EXPENSE_AMOUNT = "amount";
    private static final String KEY_BUDGET_ID = "budget_id";
    private static final String KEY_CATEGORY_ID = "category_id";

    // Categories Table Columns
    private static final String KEY_CATEGORY_NAME = "name";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Categories Table
        String createCategoriesTable = "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_CATEGORY_NAME + " TEXT NOT NULL UNIQUE   " +
                ")";

        // Budgets Table
        String createBudgetsTable = "CREATE TABLE " + TABLE_BUDGETS + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_BUDGET_NAME + " TEXT NOT NULL, " +
                KEY_BUDGET_AMOUNT + " REAL NOT NULL, " +
                KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";

        // Expenses Table
        String createExpensesTable = "CREATE TABLE " + TABLE_EXPENSES + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_EXPENSE_NAME + " TEXT NOT NULL, " +
                KEY_EXPENSE_AMOUNT + " REAL NOT NULL, " +
                KEY_BUDGET_ID + " INTEGER NOT NULL, " +
                KEY_CATEGORY_ID + " INTEGER NOT NULL, " +
                KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(" + KEY_BUDGET_ID + ") REFERENCES " + TABLE_BUDGETS + "(" + KEY_ID + "), " +
                "FOREIGN KEY(" + KEY_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + KEY_ID + ")" +
                ")";

        // Execute table creation
        db.execSQL(createCategoriesTable);
        db.execSQL(createBudgetsTable);
        db.execSQL(createExpensesTable);

        // Insert predefined categories
        insertPredefinedCategories(db);
    }

    private void insertPredefinedCategories(SQLiteDatabase db) {
        // category names
        String[] categories = {
                "Shopping",
                "Transportation",
                "Lifestyle",
                "Bills",
                "Grocery",
                "Food & Drink",
                "Entertainment",
                "Health",
                "Education",
                "Personal Care",
                "Technology",
                "Home",
                "Utilities",
                "Insurance",
                "Subscriptions"
        };

        for (String category : categories) {
            ContentValues values = new ContentValues();
            values.put(KEY_CATEGORY_NAME, category);
            db.insert(TABLE_CATEGORIES, null, values);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop existing tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);

        // Recreate tables
        onCreate(db);
    }

    // budget CRUD
    public long addBudget(Budget budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_BUDGET_NAME, budget.getName());
        values.put(KEY_BUDGET_AMOUNT, budget.getAmount());
        return db.insert(TABLE_BUDGETS, null, values);
    }

    public Budget getBudget(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_BUDGETS,
                new String[]{KEY_ID, KEY_BUDGET_NAME, KEY_BUDGET_AMOUNT, KEY_CREATED_AT},
                KEY_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        Budget budget = null;
        if (cursor != null && cursor.moveToFirst()) {
            budget = new Budget(
                    cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(KEY_BUDGET_NAME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_BUDGET_AMOUNT))
            );
            cursor.close();
        }
        return budget;
    }

    public List<Budget> getAllBudgets() {
        List<Budget> budgets = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_BUDGETS + " ORDER BY " + KEY_CREATED_AT + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Budget budget = new Budget(
                        cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_BUDGET_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_BUDGET_AMOUNT))
                );
                budgets.add(budget);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return budgets;
    }


    // Expense CRUD
    public List<Expense> getAllExpenses() {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Modify query to join the expenses table with categories table
        String query = "SELECT e.id, e.name AS expense_name, e.amount AS expense_amount, e.budget_id, " +
                "e.category_id, e.created_at, c.name AS category_name " +
                "FROM " + TABLE_EXPENSES + " e " +
                "JOIN " + TABLE_CATEGORIES + " c ON e.category_id = c.id";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Expense expense = new Expense(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("expense_name")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("expense_amount")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("budget_id")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("category_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category_name"))
                );
                expenses.add(expense);
                cursor.moveToNext();
            }
            cursor.close();
        }

        return expenses;
    }


    public long addExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EXPENSE_NAME, expense.getName());
        values.put(KEY_EXPENSE_AMOUNT, expense.getAmount());
        values.put(KEY_BUDGET_ID, expense.getBudgetId());
        values.put(KEY_CATEGORY_ID, expense.getCategoryId());
        return db.insert(TABLE_EXPENSES, null, values);
    }

    public List<Expense> getExpensesForBudget(long budgetId) {
        List<Expense> expenses = new ArrayList<>();

        // SQL query to fetch expenses and their corresponding category names
        String query = "SELECT e.id, e.name, e.amount, e.category_id, e.created_at, c.name AS category_name " +
                "FROM " + TABLE_EXPENSES + " e " +
                "JOIN " + TABLE_CATEGORIES + " c ON e.category_id = c.id " +
                "WHERE e.budget_id = ? " +
                "ORDER BY e.created_at DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(budgetId)});

        // Check if the cursor is not empty and start iterating through the rows
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Create an Expense object with all 7 arguments
                Expense expense = new Expense(
                        cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        budgetId,
                        cursor.getLong(cursor.getColumnIndexOrThrow("category_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("created_at")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category_name"))
                );

                // Add the expense object to the list
                expenses.add(expense);
            } while (cursor.moveToNext());

            cursor.close();  // Close the cursor after processing
        }

        return expenses;  // Return the list of expenses
    }


    public double getTotalExpensesForBudget(long budgetId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + KEY_EXPENSE_AMOUNT + ") FROM " + TABLE_EXPENSES + " WHERE " + KEY_BUDGET_ID + " = ?",
                new String[]{String.valueOf(budgetId)}
        );

        double total = 0.0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    public int updateBudget(Budget budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_BUDGET_NAME, budget.getName());
        values.put(KEY_BUDGET_AMOUNT, budget.getAmount());
        return db.update(
                TABLE_BUDGETS,
                values,
                KEY_ID + " = ?",
                new String[]{String.valueOf(budget.getId())}
        );
    }

    public int deleteBudget(long budgetId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete expenses linked to the budget
        db.delete(TABLE_EXPENSES, KEY_BUDGET_ID + " = ?", new String[]{String.valueOf(budgetId)});
        // Delete the budget
        return db.delete(TABLE_BUDGETS, KEY_ID + " = ?", new String[]{String.valueOf(budgetId)});
    }

    public int updateExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EXPENSE_NAME, expense.getName());
        values.put(KEY_EXPENSE_AMOUNT, expense.getAmount());
        values.put(KEY_CATEGORY_ID, expense.getCategoryId());

        return db.update(
                TABLE_EXPENSES,
                values,
                KEY_ID + " = ?",
                new String[]{String.valueOf(expense.getId())}
        );
    }


    public int deleteExpense(long expenseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_EXPENSES, KEY_ID + " = ?", new String[]{String.valueOf(expenseId)});
    }

    //categories

    // Method to get all categories (read-only)
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES,
                new String[]{KEY_ID, KEY_CATEGORY_NAME},
                null, null, null, null, KEY_CATEGORY_NAME);

        if (cursor.moveToFirst()) {
            do {
                Category category = new Category(
                        cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_NAME))
                );
                categories.add(category);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categories;
    }

    public Category getCategoryById(long categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_CATEGORIES, // The table to query
                new String[] { "id", "name" }, // The columns to return
                "id = ?", // The column for the WHERE clause
                new String[] { String.valueOf(categoryId) }, // The value for the WHERE clause
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
            String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            cursor.close();
            return new Category(id, name);
        }
        cursor.close();
        return null; // Return null if category not found
    }


    public String getCategoryName(long categoryId) {
        String categoryName = null;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                "categories",  // Table name
                new String[]{"name"},  // Columns to select
                "id = ?",  // WHERE clause
                new String[]{String.valueOf(categoryId)},  // WHERE arguments
                null,  // GROUP BY
                null,  // HAVING
                null   // ORDER BY
        );

        if (cursor != null && cursor.moveToFirst()) {
            categoryName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            cursor.close();
        }
        return categoryName != null ? categoryName : "Unknown"; // Return "Unknown" if no name is found
    }

    public List<CategorySummary> getCategoryExpenseSummary() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<CategorySummary> categorySummaries = new ArrayList<>();
        String query = "SELECT " +
                KEY_CATEGORY_NAME + ", " +
                "SUM(" + KEY_EXPENSE_AMOUNT + ") AS total_amount " +
                "FROM " + TABLE_EXPENSES +
                " GROUP BY " + KEY_CATEGORY_NAME;

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String categoryName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CATEGORY_NAME));
                double totalAmount = cursor.getDouble(cursor.getColumnIndexOrThrow("total_amount"));
                categorySummaries.add(new CategorySummary(categoryName, totalAmount));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return categorySummaries;
    }


}


class Budget {
    public long id;
    public String name;
    public double amount;

    public Budget(long id, String name, double amount) {
        this.id = id;
        this.name = name;
        this.amount = amount;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }
}

class Expense {
    public long id;
    public String name;
    public double amount;
    public long budgetId;
    public long categoryId;
    public String createdAt;
    public String categoryName;


    public Expense(long id, String name, double amount, long budgetId, long categoryId, String createdAt, String categoryName) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.budgetId = budgetId;
        this.categoryId = categoryId;
        this.createdAt = createdAt;
        this.categoryName = categoryName;
    }

    // Getters for all fields
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getAmount() {
        return amount;
    }

    public long getBudgetId() {
        return budgetId;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getCategoryName() {
        return categoryName;
    }

}



class Category {
    public long id;
    public String name;

    public Category(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getCategoryId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

}


