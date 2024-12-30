package com.example.project1732.Helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project1732.Activity.Principal;
import com.example.project1732.Domain.Foods;
import com.example.project1732.Domain.Order;
import com.example.project1732.Domain.OrderCallback;
import com.example.project1732.Domain.OrderManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.UUID;


public class ManagmentCart extends AppCompatActivity {
    private Context context;
    private TinyDB tinyDB;

    private UserManager userManager;

    public ManagmentCart(Context context) {
        this.context = context;
        this.tinyDB = new TinyDB(context);
        // Inicializa o UserManager usando o contexto passado
        this.userManager = new UserManager(context);
    }

    public void insertFood(Foods item) {
        String userName = userManager.getUserName();
        ArrayList<Foods> listpop = getListCart(); // Obtém a lista atual do carrinho
        boolean existAlready = false;
        int index = -1;

        // Verifica se o item já existe no carrinho
        for (int i = 0; i < listpop.size(); i++) {
            if (listpop.get(i).getTitle().equals(item.getTitle())) {
                existAlready = true;
                index = i;
                break;
            }
        }

        if (existAlready) {
            // Se o item já existir, atualiza a quantidade no carrinho local e no Firebase
            listpop.get(index).setNumberInCart(item.getNumberInCart());
            Toast.makeText(context, "Este item já está no carrinho", Toast.LENGTH_SHORT).show();

            // Atualiza a quantidade no Firebase também
            if (userName != null && !userName.isEmpty()) {
                updateItemInFirebase(listpop.get(index), userName); // Função para atualizar o item no Firebase
            } else {
                Log.e("ManagmentCart", "Erro: Nome do usuário não recuperado.");
            }

        } else {
            // Se o item não existir, adiciona ao carrinho e salva no Firebase
            String orderId = UUID.randomUUID().toString();
            item.setFirebaseOrderId(orderId);
            listpop.add(item);
            Toast.makeText(context, "Item adicionado ao carrinho", Toast.LENGTH_SHORT).show();

            // Somente salvar no Firebase se o item for novo no carrinho
            if (userName != null && !userName.isEmpty()) {
                saveItemToFirebase(item, userName); // Chama a função para salvar no Firebase
            } else {
                Log.e("ManagmentCart", "Erro: Nome do usuário não recuperado.");
            }
        }

        // Salva a lista atualizada no TinyDB (independente de ser novo ou já existente)
        tinyDB.putListObject("CartList", listpop);
    }

    private void updateItemInFirebase(Foods item, String userName) {
        // Referência ao nó do carrinho do usuário no Firebase
        DatabaseReference databaseUserCart = FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(userName)
                .child("cart")
                .child(item.getFirebaseOrderId()); // Usando o ID exclusivo do pedido

        // Atualiza a quantidade do item no Firebase
        databaseUserCart.child("numberInCart").setValue(item.getNumberInCart())
                .addOnSuccessListener(aVoid -> {
                    Log.d("ManagmentCart", "Quantidade do item atualizada no Firebase.");
                })
                .addOnFailureListener(e -> {
                    Log.e("ManagmentCart", "Erro ao atualizar o item no Firebase.", e);
                });
    }


    private void saveItemToFirebase(Foods item, String userName) {

        // Referência ao nó do carrinho do usuário no Firebase
        DatabaseReference databaseUserCart = FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(userName)
                .child("cart")
                .child(item.getFirebaseOrderId());

        // Salva o item no Firebase
        databaseUserCart.setValue(item)
                .addOnSuccessListener(aVoid -> {
                    // Sucesso ao salvar no Firebase
                    // Toast.makeText(context, "Item adicionado ao Firebase", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    // Erro ao salvar no Firebase
                    Toast.makeText(context, "Erro ao adicionar item ao Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void removeItemFromFirebase(String userN, @Nullable String orderId) {
        String userName = userManager.getUserName();
        DatabaseReference databaseUserCart = FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(userName)
                .child("cart");

        // Se o orderId for fornecido, remove apenas o item correspondente, caso contrário, remove o carrinho inteiro
        if (orderId != null && !orderId.isEmpty()) {
            databaseUserCart = databaseUserCart.child(orderId); // Remover item específico
        }

        databaseUserCart.removeValue()
                .addOnSuccessListener(aVoid -> {
                    // if (orderId != null && !orderId.isEmpty()) {
                    // Toast.makeText(context, "Item " + orderId + " removido do Firebase", Toast.LENGTH_SHORT).show();
                    // } else {
                    //  Toast.makeText(context, "Carrinho removido com sucesso.", Toast.LENGTH_SHORT).show();
                    // }
                })
                .addOnFailureListener(e -> {
                    if (orderId != null && !orderId.isEmpty()) {
                        Toast.makeText(context, "Erro ao remover item: " + orderId + " do Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Erro ao remover carrinho: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void fetchCartFromFirebase(String userName, final CartFetchListener listener) {
        // Referência ao nó do carrinho do usuário no Firebase
        DatabaseReference databaseUserCart = FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(userName)
                .child("cart");

        // Listener para obter os dados do carrinho
        databaseUserCart.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Foods> cartItems = new ArrayList<>();
                for (DataSnapshot itemSnapshot : dataSnapshot.getChildren()) {
                    // Converte os dados do Firebase para o objeto Foods
                    Foods foodItem = itemSnapshot.getValue(Foods.class);
                    if (foodItem != null) {
                        cartItems.add(foodItem);
                    }
                }

                // Retorna os itens do carrinho através do listener
                listener.onCartFetchSuccess(cartItems);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Retorna o erro através do listener
                listener.onCartFetchFailed(databaseError.toException());
            }
        });
    }


    public ArrayList<Foods> getListCart() {

        return tinyDB.getListObject("CartList");
    }

    //Calcula o valor total dos itens no carrinho, multiplicando o preço de cada item pela sua quantidade.
    public Double getTotalFee() {
        ArrayList<Foods> listItem = getListCart();
        double fee = 0;
        for (int i = 0; i < listItem.size(); i++) {
            fee = fee + (listItem.get(i).getPrice() * listItem.get(i).getNumberInCart());
        }
        return fee;
    }

    public void calculateCartFromFirebase(String userName, final CartTotalListener listener) {
        fetchCartFromFirebase(userName, new CartFetchListener() {
            @Override
            public void onCartFetchSuccess(ArrayList<Foods> cartItems) {
                double totalFee = 0.00;
                for (Foods item : cartItems) {
                    totalFee += item.getPrice() * item.getNumberInCart();
                }

                listener.onTotalCalculated(totalFee);
            }

            @Override
            public void onCartFetchFailed(Exception e) {
                Log.e("ManagmentCart", "Erro ao buscar carrinho do Firebase", e);
                listener.onTotalCalculationFailed(e);
            }
        });
    }


    public void minusNumberItem(ArrayList<Foods> listItem, int position, ChangeNumberItemsListener changeNumberItemsListener) {
        if (listItem.get(position).getNumberInCart() == 1) {
            // Recupera o orderId salvo no item
            String firebaseOrderId = listItem.get(position).getFirebaseOrderId();
//
            listItem.remove(position);
            // Verifica se a lista está vazia após remover o item
            if (listItem.isEmpty()) {
                Toast.makeText(context, "Seu carrinho está vazio", Toast.LENGTH_SHORT).show();
                // Adiciona um atraso de 3 segundos antes de voltar para a tela inicial
                new Handler().postDelayed(() -> {
                    // Inicia a tela inicial ou qualquer outra atividade desejada
                    Intent intent = new Intent(context, Principal.class); // Substitua MainActivity pela sua tela inicial
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpa a pilha de atividades
                    context.startActivity(intent);
                    // Finaliza a atividade atual
                    if (context instanceof Activity) {
                        ((Activity) context).finish();
                    }
                }, 1000); // 3000 milissegundos = 3 segundos
            }

            // Remove o item do Firebase usando o orderId
            removeItemFromFirebase(null, firebaseOrderId);

        } else {
            listItem.get(position).setNumberInCart(listItem.get(position).getNumberInCart() - 1);
            // Salva o item atualizado no Firebase
            saveItemToFirebase(listItem.get(position), userManager.getUserName());
        }
        tinyDB.putListObject("CartList", listItem);
        changeNumberItemsListener.change();
    }


    public void plusNumberItem(ArrayList<Foods> listItem, int position, ChangeNumberItemsListener changeNumberItemsListener) {
        listItem.get(position).setNumberInCart(listItem.get(position).getNumberInCart() + 1);
        // Salva o item atualizado no Firebase
        saveItemToFirebase(listItem.get(position), userManager.getUserName());
        tinyDB.putListObject("CartList", listItem);
        changeNumberItemsListener.change();
    }

    //Limpa o histórico de pedidos armazenado localmente no TinyDB
    public void clearOrderHistory() {
        // Limpa a lista de pedidos no TinyDB
        tinyDB.putListOrderObject("OrderHistory", new ArrayList<>());
    }

    public void placeOrder(String deliveryAddress, String paymentMethod, String cpf, String telefone, String userName, double taxaEntrega, String status, String observacao) {
        ArrayList<Foods> listpop = getListCart();
        double totalFee = getTotalFee();

        ArrayList<Map<String, Object>> foodsListMap = new ArrayList<>();
        for (Foods food : listpop) {
            foodsListMap.add(food.toMap());
        }

        Order newOrder = new Order(
                foodsListMap,
                totalFee,
                deliveryAddress,
                paymentMethod,
                cpf,
                telefone,
                new Date(),
                userName,
                taxaEntrega,
                status,
                null,//ID ainda não definido
                null,
                observacao
        );

        UserManager userManager = UserManager.getInstance(context);
        String userId = userManager.getUserId();

        if (userManager.isUserLoggedIn()) {
            OrderManager orderManager = new OrderManager();

            orderManager.createOrderForUser(
                    newOrder,
                    userId,           // Passa o userId
                    userName,        // Passa o userName
                    new OrderCallback() {
                        @Override
                        public void onOrderDeletionFailed(@NonNull String error) {

                        }

                        @Override
                        public void onOrderDeleted(@NonNull String orderId) {

                        }

                        @Override
                        public void onOrderCreated(String createdOrderId) {
                            updateOrderWithId(createdOrderId, newOrder, userId, userName);
                        }

                        @Override
                        public void onOrderCreationFailed(String error) {
                            Toast.makeText(context, "Falha ao criar pedido: " + error, Toast.LENGTH_LONG).show();
                        }
                    }
            );
        } else {
            userManager.showToastIfNotLoggedIn();
        }
    }

    private void updateOrderWithId(String orderId, Order originalOrder, String userId, String userName) {
        Order updatedOrder = new Order(
                originalOrder.getFoodsList(),
                originalOrder.getTotalFee(),
                originalOrder.getDeliveryAddress(),
                originalOrder.getPaymentMethod(),
                originalOrder.getCpf(),
                originalOrder.getTelefone(),
                originalOrder.getOrderDate(),
                originalOrder.getUserName(),
                originalOrder.getTaxaEntrega(),
                originalOrder.getStatus(),
                orderId,
                null,
                originalOrder.getObservacao()

        );

        ArrayList<Order> orders = tinyDB.getListObject("OrderHistory", Order.class);

        if (orders == null) {
            orders = new ArrayList<>();
        }
        orders.add(updatedOrder);
        tinyDB.putListObjectN("OrderHistory", orders);
        tinyDB.putListObject("CartList", new ArrayList<>());

        // Chama a função para remover o carrinho do Firebase
        removeItemFromFirebase(userName, null);

        DatabaseReference databaseOrders = FirebaseDatabase.getInstance().getReference().child("orders").child(orderId);

        databaseOrders.setValue(updatedOrder.toMap())
                .addOnSuccessListener(aVoid -> {
                    DatabaseReference databaseUserOrders = FirebaseDatabase.getInstance()
                            .getReference()
                            .child("users")
                            .child(userName) // Usa userName no lugar de userId
                            .child("orders")
                            .child(orderId);

                    databaseUserOrders.setValue(updatedOrder.toMap())
                            .addOnSuccessListener(aVoidUser -> {
                                // Sucesso ao salvar no nó do usuário
                                Toast.makeText(context, "Pedido realizado com sucesso!", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Erro ao salvar pedido no nó do usuário: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Erro ao atualizar pedido: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });


    }

    //Retorna o histórico de pedidos armazenado no TinyDB.
    public ArrayList<Order> getOrderHistory() {
        return tinyDB.getListObject("OrderHistory", Order.class);
    }

    // Salva o histórico de pedidos no TinyDB.
    public void saveOrderHistory(ArrayList<Order> orders) {
        tinyDB.putListObjectN("OrderHistory", orders);
    }

    // Método para limpar os dados locais (carrinho e histórico de pedidos)
    public void clearLocalData() {
        tinyDB.putListObject("CartList", new ArrayList<>());  // Limpa o carrinho
        tinyDB.putListObject("OrderHistory", new ArrayList<>());  // Limpa o histórico de pedidos
    }

}
