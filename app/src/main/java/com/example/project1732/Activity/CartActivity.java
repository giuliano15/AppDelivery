package com.example.project1732.Activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.project1732.Adapter.CartAdapter;
import com.example.project1732.Domain.Foods;
import com.example.project1732.Domain.OrderCallback;
import com.example.project1732.Helper.CartFetchListener;
import com.example.project1732.Helper.CartTotalListener;
import com.example.project1732.Helper.ChangeNumberItemsListener;
import com.example.project1732.Helper.CustomLocationManager;
import com.example.project1732.Helper.ManagmentCart;
import com.example.project1732.Helper.TaxaDelivery;
import com.example.project1732.Helper.UserManager;
import com.example.project1732.databinding.ActivityCartBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import eightbitlab.com.blurview.RenderScriptBlur;

public class CartActivity extends BaseActivity implements OrderCallback {
    private ActivityCartBinding binding;
    private CartAdapter adapter;
    ;
    private ManagmentCart managmentCart;
    private double tax;

    private CustomLocationManager customLocationManager;

    private UserManager userManager;
    private String userName;

    private double taxaEntrega;

    private double total;

    private String status;

    private String idPedido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCartBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        customLocationManager = new CustomLocationManager(this, null);

        // Inicializa o UserManager
        userManager = UserManager.getInstance(this);
        // Usa o UserManager para obter e exibir o nome do usuário
        userName = userManager.getUserName();

        managmentCart = new ManagmentCart(this);

        taxaEntrega = TaxaDelivery.getInstance().getGlobalPrice();


        setVariable();
        calculateCart();
        initList();
        setBlurEffect();
        updateItemCount();
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

    private void initList() {
        // Mostra um indicador de carregamento enquanto busca os dados
        // binding.progressBar.setVisibility(View.VISIBLE);
        managmentCart.fetchCartFromFirebase(userName, new CartFetchListener() {
            @Override
            public void onCartFetchSuccess(ArrayList<Foods> cartItems) {
                // binding.progressBar.setVisibility(View.GONE); // Oculta o indicador de carregamento
                if (cartItems.isEmpty()) {
                    binding.emptyTxt.setVisibility(View.VISIBLE);
                    binding.scrollview.setVisibility(View.GONE);

                } else {
                    binding.emptyTxt.setVisibility(View.GONE);
                    binding.scrollview.setVisibility(View.VISIBLE);
                }

                // Método que você deve implementar no adapter

                //updateCart();
                //updateItemCount(cartItems);

                // Calcula o total do carrinho
                //  total = calculateTotal(cartItems);

                // Configura o RecyclerView com os itens do carrinho obtidos do Firebase
                binding.cartView.setLayoutManager(new LinearLayoutManager(CartActivity.this, LinearLayoutManager.VERTICAL, false));
//                adapter = new CartAdapter(cartItems, CartActivity.this,
//                        CartActivity.this::calculateCart, // Atualiza o total do carrinho
//                        CartActivity.this::updateItemCount // Atualiza a contagem de itens
//                );
                adapter = new CartAdapter(cartItems, CartActivity.this,
                        new ChangeNumberItemsListener() {
                            @Override
                            public void change() {
                                // Recalcula o total sempre que o callback for chamado
                                calculateCart(); // Aqui chamamos a função calculateCart()
                            }
                        },
                        CartActivity.this::updateItemCount // Atualiza a contagem de itens
                );
                binding.cartView.setAdapter(adapter);

                // Calcula o total inicial do carrinho
                calculateCart();

            }

            @Override
            public void onCartFetchFailed(Exception e) {
                // binding.progressBar.setVisibility(View.GONE); // Oculta o indicador de carregamento
                // Mostra uma mensagem de erro se falhar ao buscar os itens do carrinho
                Toast.makeText(CartActivity.this, "Erro ao buscar itens do carrinho: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });

    }

    private void updateCart() {
        managmentCart.fetchCartFromFirebase(userName, new CartFetchListener() {
            @Override
            public void onCartFetchSuccess(ArrayList<Foods> cartItems) {
                adapter.updateCartItems(cartItems); // Chamada correta
                updateItemCount(cartItems);
                calculateCart(); // Recalcula o total
            }

            @Override
            public void onCartFetchFailed(Exception e) {
                // Lidar com erro
            }
        });
    }

    private void updateItemCount(ArrayList<Foods> cartItems) {
        int itemCount = cartItems.size();
        if (itemCount > 3) {
            // Atualiza o texto e torna o TextView visível
            binding.qtdItens.setText(String.valueOf(itemCount));
            binding.qtdItens.setVisibility(View.VISIBLE);
            binding.txtItens.setVisibility(View.VISIBLE);
        } else {
            // Oculta o TextView se a quantidade for 3 ou menos
            binding.qtdItens.setVisibility(View.GONE);
            binding.txtItens.setVisibility(View.GONE);
        }
    }

    private void updateItemCount() {
        int itemCount = managmentCart.getListCart().size();
        if (itemCount > 3) {
            // Atualiza o texto e torna o TextView visível
            binding.qtdItens.setText(String.valueOf(itemCount));
            binding.qtdItens.setVisibility(View.VISIBLE);
            binding.txtItens.setVisibility(View.VISIBLE);
        } else {
            // Oculta o TextView se a quantidade for 3 ou menos
            binding.qtdItens.setVisibility(View.GONE);
            binding.txtItens.setVisibility(View.GONE);
        }
    }

    private void setVariable() {
        binding.backBtn.setOnClickListener(v -> finish());

        binding.btnCupon.setOnClickListener(v -> {
            Toast.makeText(this, "Este cupon não é válido", Toast.LENGTH_SHORT).show();
            binding.edtCupon.setText("");
        });

        binding.btnPlaceOrder.setOnClickListener(v -> {
            String username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

            // Referência ao nó "formulario" do usuário no Firebase
            DatabaseReference userFormRef = FirebaseDatabase.getInstance().getReference("users").child(username).child("formulario");

            // Recupera o endereço do Firebase
            userFormRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Recupera o endereço do Firebase
                        String deliveryAddress = dataSnapshot.child("endereco").getValue(String.class);
                        String telefone = dataSnapshot.child("telefone").getValue(String.class);
                        String cpf = dataSnapshot.child("cpf").getValue(String.class);

                        if (deliveryAddress != null) {
                            // Cria uma instância do BottomSheet
                            ConfirmarPedidoBottomSheet bottomSheet = new ConfirmarPedidoBottomSheet();

                            // Cria um Bundle para passar o valor do total, taxa de entrega e endereço
                            Bundle bundle = new Bundle();
                            bundle.putDouble("TOTAL_VALUE", total);
                            bundle.putDouble("TAXA_ENTREGA", taxaEntrega);  // Passa a taxa de entrega
                            bundle.putString("DELIVERY_ADDRESS", deliveryAddress);// Passa o endereço
                            bundle.putString("TELEFONE", telefone);
                            bundle.putString("USER_NAME", userName);
                            bundle.putString("CPF", cpf);

                            // Define os argumentos do BottomSheet
                            bottomSheet.setArguments(bundle);

                            // Exibe o BottomSheet
                            bottomSheet.show(((AppCompatActivity) v.getContext()).getSupportFragmentManager(), bottomSheet.getTag());
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Lidar com o erro ao acessar o Firebase
                    Log.e("FirebaseError", "Erro ao acessar o Firebase: " + databaseError.getMessage());
                }
            });
        });

    }

    private void calculateCart() {

        String userName = userManager.getUserName();
        managmentCart.calculateCartFromFirebase(userName, new CartTotalListener() {
            @Override
            public void onTotalCalculated(double totalFee) {
                // Aqui você pode fazer o cálculo de impostos e entrega, e atualizar as views
                double percentTax = 0.00;
                double delivery = taxaEntrega;

                BigDecimal totalFeeBig = new BigDecimal(totalFee).setScale(2, RoundingMode.DOWN);
                BigDecimal percentTaxBig = new BigDecimal(percentTax);
                BigDecimal tax = totalFeeBig.multiply(percentTaxBig).setScale(2, RoundingMode.DOWN);
                BigDecimal deliveryFeeBig = new BigDecimal(delivery);
                BigDecimal totals = totalFeeBig.add(tax).add(deliveryFeeBig).setScale(2, RoundingMode.DOWN);

                // Atualiza os textos
                binding.totalFeeTxt.setText("R$ " + totalFeeBig.toString());
                binding.taxTxt.setText("R$ " + tax.toString());
                binding.deliveryTxt.setText("R$ " + deliveryFeeBig.toString());
                binding.totalTxt.setText("R$ " + totals.toString());

                if (adapter != null) {
                    // Obtém o total recalculado
                    total = adapter.calculateCartTotal();

                    // Atualiza a visualização com o novo total
                    //updateTotalView(total);
                }
            }

            @Override
            public void onTotalCalculationFailed(Exception e) {

            }
        });
    }

//    private void calculateCart() {
//        double percentTax = 0.00;
//        double delivery = taxaEntrega;
//
//        // Obter o total do carrinho como BigDecimal
//        BigDecimal totalFee = new BigDecimal(managmentCart.getTotalFee());
//
//        // Calcular o imposto
//        BigDecimal percentTaxBig = new BigDecimal(percentTax);
//        BigDecimal tax = totalFee.multiply(percentTaxBig).setScale(2, RoundingMode.DOWN);
//
//        // Calcular o total
//        BigDecimal deliveryFeeBig = new BigDecimal(delivery);
//        BigDecimal totals = totalFee.add(tax).add(deliveryFeeBig).setScale(2, RoundingMode.DOWN);
//
//        // Calcular o total dos itens
//        BigDecimal itemTotal = totalFee.setScale(2, RoundingMode.DOWN);
//
//        // Configurar o texto com os valores formatados
//        binding.totalFeeTxt.setText("R$ " + itemTotal.toString());
//        binding.taxTxt.setText("R$ " + tax.toString());
//        binding.deliveryTxt.setText("R$ " + deliveryFeeBig.toString());
//        binding.totalTxt.setText("R$ " + totals.toString());
//
//        if (adapter != null) {
//            // Obtém o total recalculado
//            total = adapter.calculateCartTotal();
//
//            // Atualiza a visualização com o novo total
//            //updateTotalView(total);
//        }
//
//    }

    @Override
    public void onOrderCreated(@NonNull String orderId) {
        this.idPedido = orderId;

    }

    @Override
    public void onOrderCreationFailed(@NonNull String error) {
        // Trata o erro de criação do pedido
        Toast.makeText(this, "Erro ao criar pedido: " + error, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onOrderDeleted(@NonNull String orderId) {
        this.idPedido = orderId;
    }

    @Override
    public void onOrderDeletionFailed(@NonNull String error) {
        Toast.makeText(this, "Erro ao deletar pedido: " + error, Toast.LENGTH_SHORT).show();

    }

    private void updateTotalView(double newTotal) {
        //total = newTotal;
    }
}