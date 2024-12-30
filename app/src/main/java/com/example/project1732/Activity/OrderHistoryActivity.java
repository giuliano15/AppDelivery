package com.example.project1732.Activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project1732.Adapter.OrderAdapter;
import com.example.project1732.Domain.Order;
import com.example.project1732.Domain.OrderCallback;
import com.example.project1732.Domain.OrderManager;
import com.example.project1732.Helper.ManagmentCart;
import com.example.project1732.Helper.UserManager;
import com.example.project1732.databinding.ActivityOrderHistoryBinding;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends BaseActivity implements OrderCallback {
    private ActivityOrderHistoryBinding binding;
    private ManagmentCart managmentCart;
    private RecyclerView.Adapter adapter;

    private OrderManager orderManager;

    String userName = "";

    private ArrayList<Order> orderHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // double taxaDelivery = TaxaDelivery.getInstance().getGlobalPrice();

        managmentCart = new ManagmentCart(this);

        // Instancia o OrderManager
        orderManager = new OrderManager();

        binding.backBtn.setOnClickListener(
                v -> {
                    managmentCart.saveOrderHistory(orderHistory);
                    finish();
                });

        binding.backBtn.setOnClickListener(v -> finish());


        // Recupera o nome do usuário (por exemplo, a partir do UserManager)
        userName = UserManager.getInstance(this).getUserName();

        // Carrega o histórico de pedidos do Firebase
        loadOrderHistory(userName);


//        orderHistory = managmentCart.getOrderHistory();
        initList();

        setupListeners();
    }

    private void initList() {
        //orderHistory = managmentCart.getOrderHistory();

        if (orderHistory.isEmpty()) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.orderRecyclerView.setVisibility(View.GONE);
        } else {
            binding.emptyTxt.setVisibility(View.GONE);
            binding.orderRecyclerView.setVisibility(View.VISIBLE);
        }

        binding.orderRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        adapter = new OrderAdapter(orderHistory, this);
        binding.orderRecyclerView.setAdapter(adapter);

    }

    private void loadOrderHistory(String userName) {
        orderManager.getUserOrders(userName, new OnOrdersLoadedCallback() {
            @Override
            public void onOrdersLoaded(List<Order> orders) {
                orderHistory.clear();
                orderHistory.addAll(orders);  // Adiciona todos os pedidos diretamente
                adapter.notifyDataSetChanged();

                // Atualiza a visibilidade dos elementos
                if (orderHistory.isEmpty()) {
                    binding.emptyTxt.setVisibility(View.VISIBLE);
                    binding.orderRecyclerView.setVisibility(View.GONE);
                } else {
                    binding.emptyTxt.setVisibility(View.GONE);
                    binding.orderRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setupListeners() {

        // DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("orders");
        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("users").child(userName).child("orders");

        database.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Um novo pedido foi adicionado. Você pode adicionar este pedido à lista se necessário.
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                String orderId = dataSnapshot.getKey();
                String status = (String) dataSnapshot.child("status").getValue();

                // binding.status.setText(status);

                //String status = (String) dataSnapshot.child("status").getValue();
                if (status != null) {
                    updateOrderStatusInList(orderId, status);
                    managmentCart.saveOrderHistory(orderHistory);
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // Pedido removido (opcional: se necessário, você pode remover da lista)
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Pedido movido (opcional: se necessário, você pode atualizar a posição na lista)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(OrderHistoryActivity.this, "Erro ao carregar pedidos: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateOrderStatusInList(String orderId, String status) {

        for (Order order : orderHistory) {
            if (order.getIdPedido().equals(orderId)) {
                order.setStatus(status);
                adapter.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void onOrderCreated(@NonNull String orderId) {


    }

    @Override
    public void onOrderCreationFailed(@NonNull String error) {

    }

    @Override
    public void onOrderDeleted(@NonNull String orderId) {
        // Remover o pedido da lista local de histórico
        for (int i = 0; i < orderHistory.size(); i++) {
            if (orderHistory.get(i).getIdPedido().equals(orderId)) {
                orderHistory.remove(i);
                adapter.notifyDataSetChanged();
                break;
            }
        }

        // Verificar se a lista ficou vazia
        if (orderHistory.isEmpty()) {
            binding.emptyTxt.setVisibility(View.VISIBLE);
            binding.orderRecyclerView.setVisibility(View.GONE);
        }

        // Exibir uma mensagem de sucesso
        Toast.makeText(this, "Pedido deletado com sucesso!", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onOrderDeletionFailed(@NonNull String error) {
        Toast.makeText(this, "Erro ao deletar o pedido: " + error, Toast.LENGTH_SHORT).show();

    }

    public interface OnOrdersLoadedCallback {
        void onOrdersLoaded(List<Order> orders);
    }

//    private void deleteOrder(String orderId) {
//        // Recupera o nome do usuário (por exemplo, do UserManager)
//        String userName = UserManager.getInstance(this).getUserName();
//
//        // Chama o método deleteOrder do OrderManager
//        orderManager.deleteOrder(orderId, userName, this);
//    }


}

