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
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_BUDGETS = "budgets";
    private static final String TABLE_EXPENSES = "expenses";

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

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // budget
        String createBudgetsTable = "CREATE TABLE " + TABLE_BUDGETS + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_BUDGET_NAME + " TEXT NOT NULL, " +
                KEY_BUDGET_AMOUNT + " REAL NOT NULL, " +
                KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")";

        // pengeluaran
        String createExpensesTable = "CREATE TABLE " + TABLE_EXPENSES + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_EXPENSE_NAME + " TEXT NOT NULL, " +
                KEY_EXPENSE_AMOUNT + " REAL NOT NULL, " +
                KEY_BUDGET_ID + " INTEGER NOT NULL, " +
                KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY(" + KEY_BUDGET_ID + ") REFERENCES " + TABLE_BUDGETS + "(" + KEY_ID + ")" +
                ")";

        db.execSQL(createBudgetsTable);
        db.execSQL(createExpensesTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
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

    // Expense CRUD operations
    public long addExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EXPENSE_NAME, expense.getName());
        values.put(KEY_EXPENSE_AMOUNT, expense.getAmount());
        values.put(KEY_BUDGET_ID, expense.getBudgetId());
        return db.insert(TABLE_EXPENSES, null, values);
    }

    public List<Expense> getExpensesForBudget(long budgetId) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_EXPENSES,
                new String[]{KEY_ID, KEY_EXPENSE_NAME, KEY_EXPENSE_AMOUNT, KEY_CREATED_AT},
                KEY_BUDGET_ID + " = ?",
                new String[]{String.valueOf(budgetId)},
                null, null, KEY_CREATED_AT + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                Expense expense = new Expense(
                        cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_EXPENSE_NAME)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_EXPENSE_AMOUNT)),
                        budgetId,
                        cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREATED_AT))
                );
                expenses.add(expense);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return expenses;
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
    public String createdAt;

    public Expense(long id, String name, double amount, long budgetId, String createdAt) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.budgetId = budgetId;
        this.createdAt = createdAt;
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

    public long getBudgetId() {
        return budgetId;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}

