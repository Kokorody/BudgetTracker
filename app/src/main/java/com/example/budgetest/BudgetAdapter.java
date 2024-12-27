
package com.example.budgetest;


import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import com.example.budgetest.databinding.ItemBudgetBinding;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private final List<com.example.budgetest.Budget> budgets;
    private final DatabaseHelper dbHelper;
    private final OnItemClickListener onItemClick;

    public interface OnItemClickListener {
        void onItemClick(com.example.budgetest.Budget budget);
    }

    public BudgetAdapter(List<com.example.budgetest.Budget> budgets, com.example.budgetest.DatabaseHelper dbHelper, OnItemClickListener onItemClick) {
        this.budgets = budgets;
        this.dbHelper = dbHelper;
        this.onItemClick = onItemClick;
    }

    public static class BudgetViewHolder extends RecyclerView.ViewHolder {
        private final ItemBudgetBinding binding;

        public BudgetViewHolder(ItemBudgetBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(com.example.budgetest.Budget budget, double totalSpent, OnItemClickListener onItemClick) {
            binding.tvBudgetName.setText(budget.getName());
            // budget format RP indo
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
            binding.tvBudgetAmount.setText(currencyFormatter.format(budget.getAmount()).replace("Rp", "Rp "));
            binding.tvSpentAmount.setText("Spent: " + currencyFormatter.format(totalSpent).replace("Rp", "Rp "));

            int progress = (int) Math.min(Math.max((totalSpent / budget.getAmount()) * 100, 0), 100);
            binding.progressBar.setProgress(progress);
            binding.tvProgressPercentage.setText(progress + "%");

            binding.btnViewDetails.setOnClickListener(v -> onItemClick.onItemClick(budget));
        }
    }

    @Override
    public BudgetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemBudgetBinding binding = ItemBudgetBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new BudgetViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(BudgetViewHolder holder, int position) {
        com.example.budgetest.Budget budget = budgets.get(position);
        double totalSpent = dbHelper.getTotalExpensesForBudget(budget.getId());
        holder.bind(budget, totalSpent, onItemClick);
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    public void updateBudgets(List<com.example.budgetest.Budget> newBudgets) {
        budgets.clear();
        budgets.addAll(newBudgets);
        notifyDataSetChanged();
    }
}

