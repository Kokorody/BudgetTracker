// test 

package com.example.budgetest;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.budgetest.databinding.ItemAllExpenseBinding;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AllExpenseAdapter extends RecyclerView.Adapter<AllExpenseAdapter.ExpenseViewHolder> {

    private final List<Expense> expenses;

    public AllExpenseAdapter(List<Expense> expenses) {
        this.expenses = expenses;
    }

    public static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private final ItemAllExpenseBinding binding;

        public ExpenseViewHolder(ItemAllExpenseBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Expense expense) {
            binding.tvExpenseName.setText(expense.getName());
            binding.tvExpenseCategory.setText(expense.getCategoryName());
            binding.tvExpenseAmount.setText(NumberFormat.getCurrencyInstance(new Locale("in", "ID"))
                    .format(expense.getAmount())
                    .replace("Rp", "Rp "));

            // Format the date
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = "";
            try {
                formattedDate = outputFormat.format(inputFormat.parse(expense.getCreatedAt()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            binding.tvExpenseDate.setText(formattedDate);
        }
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAllExpenseBinding binding = ItemAllExpenseBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ExpenseViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.bind(expense);
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
