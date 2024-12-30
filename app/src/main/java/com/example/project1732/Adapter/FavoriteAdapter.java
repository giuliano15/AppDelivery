package com.example.project1732.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.project1732.Activity.DetailActivity;
import com.example.project1732.Activity.Principal;
import com.example.project1732.Domain.Foods;
import com.example.project1732.Helper.FavoriteManager;
import com.example.project1732.R;

import java.util.ArrayList;
import java.util.List;

import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {
    Activity activity;
    private List<Foods> favoriteList;
    private List<Foods> originalFavoriteList; // Lista original para não perder os dados
    private Context context;
    private FavoriteManager favoriteManager;

    public FavoriteAdapter(Activity activity, List<Foods> favoriteList, Context context, FavoriteManager favoriteManager) {
        this.activity = activity;
        this.favoriteList = favoriteList;
        this.originalFavoriteList = new ArrayList<>(favoriteList); // Inicializa com a lista original
        this.context = context;
        this.favoriteManager = favoriteManager;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_item_favorite, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.titleTxt.setText(favoriteList.get(position).getTitle());
        holder.priceTxt.setText("R$ " + favoriteList.get(position).getPrice());
        holder.timeTxt.setText(favoriteList.get(position).getTimeValue() + " min");
        holder.starTxt.setText("" + favoriteList.get(position).getStar());

        float radius = 10f;
        View decorView = ((Activity) holder.itemView.getContext()).getWindow().getDecorView();
        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();

        holder.blurView.setupWith(rootView, new RenderScriptBlur(holder.itemView.getContext())) // or RenderEffectBlur
                .setFrameClearDrawable(windowBackground) // Optional
                .setBlurRadius(radius);
        holder.blurView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
        holder.blurView.setClipToOutline(true);

        Glide.with(context)
                .load(favoriteList.get(position).getImagePath())
                .transform(new CenterCrop(), new RoundedCorners(30))
                .into(holder.pic);

        holder.btnDelete.setOnClickListener(v -> {
            removeItem(position);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("object", favoriteList.get(position));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            activity.finish();
        });
    }

    // Método para atualizar a lista de favoritos
    public void updateFavorites(List<Foods> newFavorites) {
        this.favoriteList = newFavorites;
        notifyDataSetChanged();
    }

    // Método para filtrar a lista
    public void filterFavorites(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            favoriteList = new ArrayList<>(originalFavoriteList); // Restaura a lista original
        } else {
            favoriteList = new ArrayList<>();
            String lowerCaseSearchText = searchText.toLowerCase();
            for (Foods food : originalFavoriteList) {
                if (food.getTitle().toLowerCase().contains(lowerCaseSearchText)) {
                    favoriteList.add(food); // Adiciona itens que correspondem ao filtro
                }
            }
        }
        notifyDataSetChanged(); // Atualiza a lista exibida
    }

    public void removeItem(int position) {
        if (position >= 0 && position < favoriteList.size()) {
            // Primeiro, obtenha o item da posição
            Foods food = favoriteList.get(position);

            // Remova o item da lista exibida
            favoriteList.remove(position);

            // Notifique o RecyclerView sobre a remoção
            notifyItemRemoved(position);

            // Remova o item da lista de favoritos gerenciada
            favoriteManager.removeFavorite(food);

            // Atualize os itens subsequentes para manter a lista em ordem
            notifyItemRangeChanged(position, favoriteList.size());

            if (favoriteList.isEmpty()) {
                Toast.makeText(context, "Sua lista de favoritos está vazia.", Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(context, Principal.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                    if (context instanceof Activity) {
                        ((Activity) context).finish();
                    }
                }, 1000);
            }
        }
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView favoriteImage;
        TextView titleTxt, priceTxt, starTxt, timeTxt;
        ImageView btnDelete, pic;
        BlurView blurView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            priceTxt = itemView.findViewById(R.id.priceTxt);
            starTxt = itemView.findViewById(R.id.starTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
            pic = itemView.findViewById(R.id.img);
            blurView = itemView.findViewById(R.id.blurView);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}


//public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.ViewHolder> {
//    Activity activity;
//    private List<Foods> favoriteList;
//    private Context context;
//    private FavoriteManager favoriteManager;
//
//    public FavoriteAdapter(Activity activity, List<Foods> favoriteList, Context context, FavoriteManager favoriteManager) {
//        this.activity = activity;
//        this.favoriteList = favoriteList;
//        this.context = context;
//        this.favoriteManager = favoriteManager;
//    }
//
//    @NonNull
//    @Override
//    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_holder_item_favorite, parent, false);
//        return new ViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//        holder.titleTxt.setText(favoriteList.get(position).getTitle());
//        holder.priceTxt.setText("R$ " + favoriteList.get(position).getPrice());
//        holder.timeTxt.setText(favoriteList.get(position).getTimeValue() + " min");
//        holder.starTxt.setText("" + favoriteList.get(position).getStar());
//
//        float radius = 10f;
//        View decorView = ((Activity) holder.itemView.getContext()).getWindow().getDecorView();
//        ViewGroup rootView = (ViewGroup) decorView.findViewById(android.R.id.content);
//        Drawable windowBackground = decorView.getBackground();
//
//        holder.blurView.setupWith(rootView, new RenderScriptBlur(holder.itemView.getContext())) // or RenderEffectBlur
//                .setFrameClearDrawable(windowBackground) // Optional
//                .setBlurRadius(radius);
//        holder.blurView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
//        holder.blurView.setClipToOutline(true);
//
//        Glide.with(context)
//                .load(favoriteList.get(position).getImagePath())
//                .transform(new CenterCrop(), new RoundedCorners(30))
//                .into(holder.pic);
//
//        holder.btnDelete.setOnClickListener(v -> {
//            removeItem(position);
//        });
//
//
//        holder.itemView.setOnClickListener(v -> {
//            Intent intent = new Intent(context, DetailActivity.class);
//            intent.putExtra("object", favoriteList.get(position));
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
//            activity.finish();
//
//        });
//        // Glide.with(context).load(food.getImagePath()).into(holder.favoriteImage);
//    }
//
//    public void updateFavorites(List<Foods> newFavorites) {
//        this.favoriteList = newFavorites;
//        notifyDataSetChanged();
//    }
//
//    public void removeItem(int position) {
//        if (position >= 0 && position < favoriteList.size()) {
//            // Primeiro, obtenha o item da posição
//            Foods food = favoriteList.get(position);
//
//            // Remova o item da lista exibida
//            favoriteList.remove(position);
//
//
//            // Notifique o RecyclerView sobre a remoção
//            notifyItemRemoved(position);
//
//            // Remova o item da lista de favoritos gerenciada
//            favoriteManager.removeFavorite(food);
//
//            // Atualize os itens subsequentes para manter a lista em ordem
//            notifyItemRangeChanged(position, favoriteList.size());
//
//            if (favoriteList.isEmpty()) {
//                Toast.makeText(context, "Sua lista de favoritos esta vazia.", Toast.LENGTH_SHORT).show();
//                // Adiciona um atraso de 3 segundos antes de voltar para a tela inicial
//                new Handler().postDelayed(() -> {
//                    // Inicia a tela inicial ou qualquer outra atividade desejada
//                    Intent intent = new Intent(context, Principal.class); // Substitua MainActivity pela sua tela inicial
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpa a pilha de atividades
//                    context.startActivity(intent);
//                    // Finaliza a atividade atual
//                    if (context instanceof Activity) {
//                        ((Activity) context).finish();
//                    }
//                }, 1000); // 3000 milissegundos
//
//            }
//        }
//    }
//
//
//    @Override
//    public int getItemCount() {
//        return favoriteList.size();
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        ImageView favoriteImage;
//        TextView titleTxt, priceTxt, starTxt, timeTxt;
//        ImageView btnDelete, pic;
//        BlurView blurView;
//
//        public ViewHolder(@NonNull View itemView) {
//            super(itemView);
//            titleTxt = itemView.findViewById(R.id.titleTxt);
//            priceTxt = itemView.findViewById(R.id.priceTxt);
//            starTxt = itemView.findViewById(R.id.starTxt);
//            timeTxt = itemView.findViewById(R.id.timeTxt);
//            pic = itemView.findViewById(R.id.img);
//            blurView = itemView.findViewById(R.id.blurView);
//            btnDelete = itemView.findViewById(R.id.btnDelete);
//        }
//    }
//}
