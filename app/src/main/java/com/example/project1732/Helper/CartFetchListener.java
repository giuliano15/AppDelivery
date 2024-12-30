package com.example.project1732.Helper;

import com.example.project1732.Domain.Foods;

import java.util.ArrayList;

public interface CartFetchListener {
    void onCartFetchSuccess(ArrayList<Foods> cartItems);
    void onCartFetchFailed(Exception e);
}
