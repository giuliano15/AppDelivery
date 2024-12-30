package com.example.project1732.Adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.project1732.Domain.Foods;
import com.example.project1732.Helper.ChangeNumberItemsListener;
import com.example.project1732.Helper.ManagmentCart;
import com.example.project1732.Helper.Utils;
import com.example.project1732.R;

import java.util.ArrayList;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.Viewholder> {
    ArrayList<Foods> listItemSelected;
    private ManagmentCart managmentCart;
    ChangeNumberItemsListener changeNumberItemsListener;

    private Runnable updateItemCount;

    public CartAdapter(ArrayList<Foods> listItemSelected, Context context, ChangeNumberItemsListener changeNumberItemsListener, Runnable updateItemCount) {
        this.listItemSelected = listItemSelected;
        this.managmentCart = new ManagmentCart(context);
        this.changeNumberItemsListener = changeNumberItemsListener;
        this.updateItemCount = updateItemCount;
    }

    @NonNull
    @Override
    public CartAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_cart, parent, false);
        return new Viewholder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull CartAdapter.Viewholder holder, int position) {
        float radius = 10f;
        View decorView = ((Activity) holder.itemView.getContext()).getWindow().getDecorView();
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();

        holder.blurView.setupWith(rootView, new RenderScriptBlur(holder.itemView.getContext())) // or RenderEffectBlur
                .setFrameClearDrawable(windowBackground) // Optional
                .setBlurRadius(radius);
        holder.blurView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        holder.blurView.setClipToOutline(true);

        Glide.with(holder.itemView.getContext())
                .load(listItemSelected.get(position).getImagePath())
                .transform(new CenterCrop(), new RoundedCorners(30))
                .into(holder.pic);

        // Limita o título a 3 palavras
        String limitedTitle = Utils.limitToThreeWords(listItemSelected.get(position).getTitle());

        holder.title.setText(limitedTitle);
        holder.feeEachItem.setText("R$ " + listItemSelected.get(position).getPrice());
        holder.totalEachItem.setText(listItemSelected.get(position).getNumberInCart() + "* R$"
                + (listItemSelected.get(position).getNumberInCart() * listItemSelected.get(position).getPrice()));

        holder.num.setText(String.valueOf(listItemSelected.get(position).getNumberInCart()));

        holder.plusItem.setOnClickListener(v ->
                managmentCart.plusNumberItem(listItemSelected, position, () -> {
                    changeNumberItemsListener.change();
                    notifyDataSetChanged();
                    updateItemCount.run();
                }));

        holder.minusItem.setOnClickListener(v ->
                managmentCart.minusNumberItem(listItemSelected, position, () -> {
                    changeNumberItemsListener.change();
                    notifyDataSetChanged();
                    updateItemCount.run();

                }));
    }

    @Override
    public int getItemCount() {
        return listItemSelected.size();
    }

    public class Viewholder extends RecyclerView.ViewHolder {
        TextView title, feeEachItem, plusItem, minusItem;
        ImageView pic;
        TextView totalEachItem, num;
        BlurView blurView;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleTxt);//titulo
            pic = itemView.findViewById(R.id.pic);//foto
            feeEachItem = itemView.findViewById(R.id.feeEachItem);//preço unidade
            totalEachItem = itemView.findViewById(R.id.totalEachItem);// total
            plusItem = itemView.findViewById(R.id.plusBtn);
            minusItem = itemView.findViewById(R.id.minusBtn);
            num = itemView.findViewById(R.id.numTxt);//qtdade de unidades
            blurView = itemView.findViewById(R.id.blurView);
        }
    }

    public void updateCartItems(ArrayList<Foods> newItems) {
        this.listItemSelected.clear();
        this.listItemSelected.addAll(newItems);
        notifyDataSetChanged();
    }

    public double calculateCartTotal() {
        double total = 0.0;
        for (Foods item : listItemSelected) {
            total += item.getNumberInCart() * item.getPrice();
        }
        return total;
    }
}
