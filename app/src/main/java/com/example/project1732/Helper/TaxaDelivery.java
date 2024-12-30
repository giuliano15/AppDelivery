package com.example.project1732.Helper;

public class TaxaDelivery {
    private static TaxaDelivery instance;
    private double globalPrice = 0.0;

    // Construtor privado para evitar a criação de instâncias adicionais

    public TaxaDelivery() {
        // Inicialização
    }

    // Método para obter a instância única da classe
    public static synchronized TaxaDelivery getInstance() {
        if (instance == null) {
            instance = new TaxaDelivery();
        }
        return instance;
    }

    // Getter e Setter para globalPrice
    public double getGlobalPrice() {
        return globalPrice;
    }

    public void setGlobalPrice(double globalPrice) {
        this.globalPrice = globalPrice;
    }
}
