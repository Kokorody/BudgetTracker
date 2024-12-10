package com.example.budgetest;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.budgetest.databinding.ItemExpenseBinding;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private final List<Expense> expenses;
    private final Consumer<Expense> onEditClick;
    private final Consumer<Expense> onDeleteClick;

    public ExpenseAdapter(List<Expense> expenses, Consumer<Expense> onEditClick, Consumer<Expense> onDeleteClick) {
        this.expenses = new ArrayList<>(expenses);
        this.onEditClick = onEditClick;
        this.onDeleteClick = onDeleteClick;
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final ItemExpenseBinding binding;

        public ExpenseViewHolder(ItemExpenseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Expense expense, Consumer<Expense> onEditClick, Consumer<Expense> onDeleteClick) {
            binding.tvExpenseName.setText(expense.getName());
            binding.tvExpenseAmount.setText(NumberFormat.getCurrencyInstance(new Locale("in", "ID"))
                    .format(expense.getAmount())
                    .replace("Rp", "Rp "));

            // Format the date (assuming it's in the format "yyyy-MM-dd HH:mm:ss")
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = "";
            try {
                formattedDate = outputFormat.format(inputFormat.parse(expense.getCreatedAt()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Bind the date to the TextView
            binding.tvExpenseDate.setText(formattedDate);

            binding.btnEditExpense.setOnClickListener(v -> onEditClick.accept(expense));
            binding.btnDeleteExpense.setOnClickListener(v -> onDeleteClick.accept(expense));
        }
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemExpenseBinding binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ExpenseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.bind(expense, onEditClick, onDeleteClick);
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public void updateExpenses(List<Expense> newExpenses) {
        expenses.clear();
        expenses.addAll(newExpenses);
        notifyDataSetChanged();
    }
}

