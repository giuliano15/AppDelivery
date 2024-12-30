package com.example.project1732.Activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import com.bumptech.glide.Glide;
import com.example.project1732.Domain.Foods;
import com.example.project1732.Helper.ManagmentCart;
import com.example.project1732.databinding.ActivityDetailBinding;

import eightbitlab.com.blurview.RenderScriptBlur;

public class DetailActivity extends BaseActivity {
    private ActivityDetailBinding binding;
    private Foods object;
    private int num = 1;
    private ManagmentCart managmentCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        managmentCart = new ManagmentCart(this);

        binding.favorite.setOnClickListener(v -> {
            Intent intent = new Intent(this, Favoritos.class);
            intent.putExtra("favorite", object);
            // binding.favorite.setImageResource(R.drawable.favorite_red);
            startActivity(intent);

        });

        getBundleExtra();
        setVariable();
        setBlurEffect();
    }

    private void setBlurEffect() {
        float radius = 10f;
        View decorView = (this).getWindow().getDecorView();
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();

        binding.blurView.setupWith(rootView, new RenderScriptBlur(this)) // or RenderEffectBlur
                .setFrameClearDrawable(windowBackground) // Optional
                .setBlurRadius(radius);
        binding.blurView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        binding.blurView.setClipToOutline(true);

        binding.blurView2.setupWith(rootView, new RenderScriptBlur(this)) // or RenderEffectBlur
                .setFrameClearDrawable(windowBackground) // Optional
                .setBlurRadius(radius);
        binding.blurView2.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        binding.blurView2.setClipToOutline(true);
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> finish());

        Glide.with(DetailActivity.this)
                .load(object.getImagePath())
                .into(binding.pic);

        binding.priceTxt.setText("R$ " + object.getPrice());
        binding.titleTxt.setText(object.getTitle());
        binding.descriptionTxt.setText(object.getDescription());
        binding.starTxt.setText(object.getStar() + " Rating");
        binding.ratingBar.setRating((float) object.getStar());
        binding.totalTxt.setText(("R$ " + num * object.getPrice()));
        int timeValue = Integer.parseInt(String.valueOf(object.getTimeValue()));
        binding.timeTxt.setText(object.getTimeValue() + "min");

        binding.plusBtn.setOnClickListener(v -> {
            num = num + 1;
            binding.numTxt.setText(num + " ");
            binding.totalTxt.setText(("R$ " + num * object.getPrice()));
        });

        binding.minusBtn.setOnClickListener(v -> {
            if (num > 1) {
                num = num - 1;
                binding.numTxt.setText(num + "");
                binding.totalTxt.setText("R$" + num * object.getPrice());
            }
        });

        binding.addBtn.setOnClickListener(v -> {
            object.setNumberInCart(num);
            managmentCart.insertFood(object);
            Intent intent = new Intent(this, CartActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void getBundleExtra() {
        object = (Foods) getIntent().getSerializableExtra("object");
    }
}