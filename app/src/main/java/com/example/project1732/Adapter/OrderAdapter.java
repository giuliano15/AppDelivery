package com.example.project1732.Adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.project1732.Domain.Order;
import com.example.project1732.Helper.PdfUtils;
import com.example.project1732.Helper.Utils;
import com.example.project1732.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orderList;
    private Context context;

    public OrderAdapter(List<Order> orderList, Context context) {
        this.orderList = orderList != null ? orderList : new ArrayList<>();
        this.context = context;
    }

    // Método para atualizar a lista de pedidos
    public void setOrderList(List<Order> newOrderList) {
        this.orderList = newOrderList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    // Atualiza o status de um pedido específico
    public void updateOrderStatus(String orderId, String newStatus) {
        for (Order order : orderList) {
            if (order.getIdPedido().equals(orderId)) {
                order.setStatus(newStatus);
                notifyDataSetChanged();
                break;
            }
        }
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView orderItemsTextView;
        private TextView deliveryAddressTextView;
        private TextView paymentMethodTextView;
        private TextView cpfTextView;
        private TextView telefoneTextView;
        private TextView orderDateTextView;
        private TextView statusTextView;
        private TextView idPedidoTextView;

        private EditText edtOrderObservacao;
        private ImageView printButton;

        // Adicione um ImageView para o ícone de status
        private ImageView statusIcon;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderItemsTextView = itemView.findViewById(R.id.orderItemsTextView);
            deliveryAddressTextView = itemView.findViewById(R.id.deliveryAddressTextView);
            paymentMethodTextView = itemView.findViewById(R.id.paymentMethodTextView);
            cpfTextView = itemView.findViewById(R.id.cpfTextView);
            telefoneTextView = itemView.findViewById(R.id.telefoneTextView);
            orderDateTextView = itemView.findViewById(R.id.orderDateTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            idPedidoTextView = itemView.findViewById(R.id.idPedidoTextview);
            edtOrderObservacao = itemView.findViewById(R.id.edtOrderObservacao);
            printButton = itemView.findViewById(R.id.iconPrint);

            // Novo ícone de status
            statusIcon = itemView.findViewById(R.id.statusIcon);
        }

        public void bind(Order order) {
            if (order == null || order.getFoodsList() == null) {
                orderItemsTextView.setText("Detalhes do pedido não disponíveis");
                idPedidoTextView.setText("");
                deliveryAddressTextView.setText("");
                paymentMethodTextView.setText("");
                cpfTextView.setText("");
                telefoneTextView.setText("");
                orderDateTextView.setText("");
                statusTextView.setText("");
                return;
            }

            // Construir detalhes dos itens
            StringBuilder itemsDetails = new StringBuilder();
            for (Map<String, Object> itemMap : order.getFoodsList()) {
                int numberInCart = ((Number) itemMap.get("numberInCart")).intValue();
                String title = (String) itemMap.get("Title");
                double price = ((Number) itemMap.get("Price")).doubleValue();

                itemsDetails.append(numberInCart)
                        .append("x ")
                        .append(title)
                        .append(" - R$ ")
                        .append(String.format("%.2f", price))
                        .append("<br>"); // Usando <br> para quebra de linha no HTML
            }

            // Formatar a data do pedido para incluir o dia da semana
            String formattedDate = "";
            if (order.getOrderDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy HH:mm"); // Dia da semana por extenso
                formattedDate = dateFormat.format(order.getOrderDate());
            } else {
                formattedDate = "Data não disponível"; // Mensagem padrão quando a data é nula
            }

            // Formatar a data do pedido para incluir o dia da semana
            //SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy HH:mm"); // Dia da semana por extenso
            // String formattedDate = dateFormat.format(order.getOrderDate());

            // Usar a taxa de entrega passada para calcular o total
            double subtotal = order.getTotalFee();
            double total = subtotal + order.getTaxaEntrega();
            String obervacao = order.getObservacao();
            edtOrderObservacao.setText(obervacao);

            // Formatar o texto dos detalhes do pedido com HTML
            String orderDetailsText = String.format(
                    "<b>Pedido via delivery - %s</b><br>" +
                            "----------------------------<br>" +
                            "<b>Detalhes do Pedido</b><br>" +
                            "%s" +
                            "----------------------------<br>" +
                            "<b>Resumo dos valores</b><br>" +
                            "Subtotal: R$ %.2f<br>" +
                            "Taxa de entrega: R$ %.2f<br>" +
                            "<b>Total: R$ %.2f</b><br>" +
                            "----------------------------<br>" +
                            "<b>Nome do Usuário:</b> %s<br>" +
                            "<b>Endereço:</b> %s<br>" +
                            "<b>Pagamento:</b> %s<br>" +
                            "<b>CPF:</b> %s<br>" +
                            "<b>Telefone:</b> %s<br>" +
                            "<b>Pedido feito:</b> %s",
                    //order.getIdPedido(),
                    "Rango Comida Caseira",
                    itemsDetails.toString(),
                    subtotal,
                    order.getTaxaEntrega(),
                    total,
                    order.getUserName(),
                    order.formatAddress(order.getDeliveryAddress()),
                    order.getPaymentMethod(),
                    Utils.formatCpf(order.getCpf()), // Chamada para formatar CPF
                    Utils.formatPhone(order.getTelefone()), // Chamada para formatar telefone
                    formattedDate
            );

            // Definir o texto dos TextViews
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                orderItemsTextView.setText(Html.fromHtml(orderDetailsText, Html.FROM_HTML_MODE_LEGACY));
            } else {
                orderItemsTextView.setText(Html.fromHtml(orderDetailsText));
            }
            idPedidoTextView.setText("Id: " + order.getIdPedido());
            statusTextView.setText("Status: " + order.getStatus());
            deliveryAddressTextView.setVisibility(View.GONE);
            paymentMethodTextView.setVisibility(View.GONE);
            telefoneTextView.setVisibility(View.GONE);
            cpfTextView.setVisibility(View.GONE);
            orderDateTextView.setVisibility(View.GONE);


            // Definir o texto e o ícone do status com base no status do pedido
            switch (order.getStatus()) {
                case "Pedido confirmado...":
                    statusTextView.setText("Pedido confirmado...");
                    statusTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_primary_inactive)); // Cinza
                    statusIcon.setImageResource(R.drawable.aprovado); // Ícone de "Aguarde"
                    break;
                case "Aguarde...":
                    statusTextView.setText("Aguarde...");
                    statusTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.color_primary_inactive)); // Cinza
                    statusIcon.setImageResource(R.drawable.ic_aguarde_24); // Ícone de "Aguarde"
                    break;
                case "Seu pedido esta sendo preparado...":
                    statusTextView.setText("Pedido em preparação");
                    statusTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.orange)); // Laranja
                    statusIcon.setImageResource(R.drawable.ic_prepared_24); // Ícone de preparação
                    break;
                case "Seu pedido esta a caminho...":
                    statusTextView.setText("Pedido a caminho");
                    statusTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.blue)); // Azul
                    statusIcon.setImageResource(R.drawable.entregue); // Ícone de entrega
                    break;
                case "Seu pedido foi entregue...":
                    statusTextView.setText("Pedido entregue");
                    statusTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.green)); // Verde
                    statusIcon.setImageResource(R.drawable.ic_entregue); // Ícone de entrega concluída
                    break;
                default:
                    statusTextView.setText("Status desconhecido");
                    statusTextView.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.background_dafult)); // Cor padrão
                    statusIcon.setImageResource(R.drawable.ic_baseline_location_on_24); // Ícone padrão
                    break;
            }



            printButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Acessando métodos do singleton PdfUtils diretamente
                    File pdfFile = PdfUtils.INSTANCE.generatePdf(itemView.getContext(), order);

                    if (pdfFile != null) {
                        PdfUtils.INSTANCE.openPdf(itemView.getContext(), pdfFile);
                    } else {
                        Toast.makeText(itemView.getContext(), "Erro ao gerar PDF", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
}

