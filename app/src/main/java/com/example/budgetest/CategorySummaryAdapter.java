package com.example.budgetest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;

public class CategorySummaryAdapter extends RecyclerView.Adapter<CategorySummaryAdapter.CategorySummaryViewHolder> {
    private List<CategorySummary> categorySummaries;
    private NumberFormat currencyFormatter;

    public CategorySummaryAdapter(List<CategorySummary> categorySummaries) {
        this.categorySummaries = categorySummaries;
        this.currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
    }

    @NonNull
    @Override
    public CategorySummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_summary, parent, false);
        return new CategorySummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategorySummaryViewHolder holder, int position) {
        CategorySummary summary = categorySummaries.get(position);
        holder.categoryNameTextView.setText(summary.getCategoryName());
        holder.categoryAmountTextView.setText(currencyFormatter.format(summary.getTotalAmount()));
    }

    @Override
    public int getItemCount() {
        return categorySummaries.size();
    }

    static class CategorySummaryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryNameTextView;
        TextView categoryAmountTextView;

        CategorySummaryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryNameTextView = itemView.findViewById(R.id.category_name);
            categoryAmountTextView = itemView.findViewById(R.id.category_amount);
        }
    }
}