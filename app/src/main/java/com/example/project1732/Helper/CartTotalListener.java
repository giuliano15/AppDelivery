package com.example.project1732.Helper;

public interface CartTotalListener {
    void onTotalCalculated(double totalFee);
    void onTotalCalculationFailed(Exception e);
}
