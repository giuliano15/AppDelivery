//
//package com.example.project1732.Domain;
//package com.example.project1732.Domain;
package com.example.project1732.Domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Order implements Serializable {
    private ArrayList<Map<String, Object>> foodsList;
    private double totalFee;
    private String deliveryAddress;
    private String paymentMethod;
    private String cpf;

    private String telefone;
    private Date orderDate;
    private String userName;
    private double taxaEntrega;
    private String status;
    private String idPedido;
    private Long timestamp;

    private String observacao;

    // Construtor completo
    public Order(ArrayList<Map<String, Object>> foodsList, double totalFee, String deliveryAddress, String paymentMethod, String cpf, String telefone, Date orderDate, String userName, double taxaEntrega, String status, String idPedido, Long timestamp, String observacao) {
        this.foodsList = foodsList;
        this.totalFee = totalFee;
        this.deliveryAddress = deliveryAddress;
        this.paymentMethod = paymentMethod;
        this.cpf = cpf;
        this.telefone = telefone;
        this.orderDate = orderDate;
        this.userName = userName;
        this.taxaEntrega = taxaEntrega;
        this.status = status;
        this.idPedido = idPedido;
        this.timestamp = timestamp;
        this.observacao = observacao;
    }

    // Getters e Setters
    public ArrayList<Map<String, Object>> getFoodsList() {
        return foodsList;
    }

    public void setFoodsList(ArrayList<Map<String, Object>> foodsList) {
        this.foodsList = foodsList;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public double getTaxaEntrega() {
        return taxaEntrega;
    }

    public void setTaxaEntrega(double taxaEntrega) {
        this.taxaEntrega = taxaEntrega;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(String idPedido) {
        this.idPedido = idPedido;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    // Método para converter Order para Map<String, Object>
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("foodsList", foodsList);
        map.put("subTotal", totalFee);
        map.put("endereço", deliveryAddress);
        map.put("forma de pagamento", paymentMethod);
        map.put("cpf", cpf);
        map.put("telefone", telefone);
        map.put("data", orderDate != null ? orderDate.getTime() : null);
        map.put("nome", userName);
        map.put("taxa", taxaEntrega);
        map.put("status", status);
        map.put("idPedido", idPedido);
        map.put("observacao", observacao);
        return map;
    }


//    public String formatAddress(String fullAddress) {
//        // Divide a string em partes
//        String[] parts = fullAddress.split(",");
//
//        // Verifica se o endereço contém pelo menos 4 partes
//        if (parts.length >= 4) {
//            // Trim e captura as partes relevantes
//            String ruaNumero = parts[0].trim();  // Rua e Número
//            String bairro = parts[1].trim();      // Bairro
//            String cidade = parts[2].trim();      // Cidade
//            String estadoAbreviado = parts[3].trim(); // Estado abreviado
//
//            // Formata o endereço como desejado
//            return ruaNumero + ", " + bairro + ", " + cidade + " - " + estadoAbreviado;
//        } else {
//            // Caso o formato seja inesperado, retornar uma mensagem de erro
//            return "Endereço inválido"; // Você pode personalizar essa mensagem
//        }
//    }


    public String formatAddress(String fullAddress) {
        // Divida a string em partes
        String[] parts = fullAddress.split(","); // Divide a string por vírgulas

        if (parts.length >= 4) {
            // Obter as partes relevantes: Rua, Bairro, Número, Cidade e Estado abreviado
            String ruaNumero = parts[0].trim();  // Rua e Número
            String bairroCidade = parts[1].trim();  // Bairro e Cidade
            String estadoAbreviado = parts[2].trim().split(" ")[0]; // Estado abreviado (primeira parte antes do espaço)

            // Agora, formatar o endereço como desejado: "Rua, Bairro, Número, Cidade - Estado Abreviado"
            return ruaNumero + ", " + bairroCidade + " - " + estadoAbreviado;
        } else {
            // Caso o formato seja inesperado, retornar o endereço completo ou uma mensagem de erro
            return fullAddress; // Ou "Endereço inválido"
        }
    }


    public String formattedTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy HH:mm:ss", new Locale("pt", "BR"));
        return sdf.format(orderDate);
    }

}


