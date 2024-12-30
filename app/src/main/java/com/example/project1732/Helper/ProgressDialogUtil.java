package com.example.project1732.Helper;

import android.app.Activity;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.project1732.Activity.ProgressDialogFragment;

public class ProgressDialogUtil {

    private static ProgressDialogFragment progressDialogFragment;

    public static void showProgressDialog(Activity activity) {
        if (progressDialogFragment == null) {
            progressDialogFragment = new ProgressDialogFragment();
        }
        if (!progressDialogFragment.isAdded()) {
            FragmentManager fragmentManager = ((FragmentActivity) activity).getSupportFragmentManager();
            progressDialogFragment.show(fragmentManager, "progress_dialog");
        }
    }

    public static void hideProgressDialog(Activity activity) {
        if (progressDialogFragment != null && progressDialogFragment.isAdded()) {
            progressDialogFragment.dismiss();
            progressDialogFragment = null; // Clean up reference
        }
    }
}
