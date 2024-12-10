package com.example.budgetest;

public class CategorySummary {
    private String categoryName;
    private double totalAmount;

    public CategorySummary(String categoryName, double totalAmount) {
        this.categoryName = categoryName;
        this.totalAmount = totalAmount;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }
}