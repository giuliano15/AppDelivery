package com.example.project1732.Domain

import android.util.Log
import com.example.project1732.Activity.OrderHistoryActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Date
import java.util.HashMap


interface OrderCallback {
    fun onOrderCreated(orderId: String)
    fun onOrderCreationFailed(error: String)
    fun onOrderDeleted(orderId: String)
    fun onOrderDeletionFailed(error: String)
}


class OrderManager {
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun createOrderForUser(
        order: Order,
        userId: String,
        userName: String,
        callback: OrderCallback
    ) {
        val orderId = database.child("orders").push().key
        if (orderId != null) {
            order.idPedido = orderId
            val orderMap = order.toMap()

            database.child("orders").child(orderId).setValue(orderMap)
                .addOnSuccessListener {
                    saveOrderInUserNode(userName, orderId, orderMap, callback) // Usa userName aqui
                }
                .addOnFailureListener { e ->
                    callback.onOrderCreationFailed(e.message ?: "Erro desconhecido.")
                }
        } else {
            callback.onOrderCreationFailed("Não foi possível gerar um ID para o pedido.")
        }
    }

    private fun saveOrderInUserNode(
        userName: String,
        orderId: String,
        orderMap: Map<String, Any>,
        callback: OrderCallback
    ) {
        val userOrderRef = database.child("users").child(userName).child("orders").child(orderId)

        userOrderRef.setValue(orderMap)
            .addOnSuccessListener {
                callback.onOrderCreated(orderId)
            }
            .addOnFailureListener { e ->
                callback.onOrderCreationFailed(
                    e.message ?: "Erro ao salvar o pedido no nó do usuário."
                )
            }
    }


    fun getUserOrders(userName: String, callback: OrderHistoryActivity.OnOrdersLoadedCallback) {
        val userOrdersRef = database.child("users").child(userName).child("orders")

        userOrdersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = ArrayList<Order>() // Kotlin ArrayList
                for (orderSnapshot in snapshot.children) {
                    val data = orderSnapshot.value as? Map<String, Any>
                    val id = orderSnapshot.key ?: ""
                    val userId = data?.get("userId") as? String ?: ""
                    val timestamp = (data?.get("data") as? Number)?.toLong() ?: 0L
                    val totalPrice = (data?.get("subTotal") as? Number)?.toDouble() ?: 0.0
                    val taxaEntrega = (data?.get("taxa") as? Number)?.toDouble() ?: 0.0
                    val deliveryAddress = data?.get("endereço") as? String ?: ""
                    val paymentMethod = data?.get("forma de pagamento") as? String ?: ""
                    val cpf = data?.get("cpf") as? String ?: ""
                    val telefone = data?.get("telefone") as? String ?: ""

                    val userName = data?.get("nome") as? String ?: ""
                    val observacao = data?.get("observacao") as? String ?: "sem observação"

                    // Corrige a criação de items para garantir que está usando o tipo correto
                    val items =
                        ArrayList<Map<String, Any>>() // Kotlin ArrayList com Map<String, Any>
                    val foodsList = data?.get("foodsList") as? List<Map<String, Any>>
                    foodsList?.forEach { itemMap ->
                        val title = itemMap["Title"] as? String ?: ""
                        val quantity = (itemMap["numberInCart"] as? Number)?.toInt() ?: 0
                        val price = (itemMap["Price"] as? Number)?.toDouble() ?: 0.0

                        // Adiciona o item ao ArrayList com a conversão de tipo necessária
                        val javaMap = HashMap<String, Any>().apply {
                            put("Title", title)
                            put("numberInCart", quantity)
                            put("Price", price)
                        }
                        items.add(javaMap) // Adiciona o HashMap à lista
                    }

                    // Cria o objeto Order com os dados do Firebase
                    val order = Order(
                        items, // foodsList
                        totalPrice, // totalFee
                        deliveryAddress, // deliveryAddress
                        paymentMethod, // paymentMethod
                        cpf, // cpf
                        telefone,
                        Date(timestamp), // orderDate
                        userName, // userName
                        taxaEntrega, // taxaEntrega
                        data?.get("status") as? String ?: "", // status
                        id, // idPedido
                        timestamp,
                        observacao

                    )

                    orders.add(order)
                }
                callback.onOrdersLoaded(orders)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OrderManager", "Erro ao carregar pedidos do usuário: ${error.message}")
            }
        })
    }

    fun deleteOrder(orderId: String, userName: String, callback: OrderCallback) {
        // Remove o pedido do nó "orders"
        database.child("orders").child(orderId).removeValue()
            .addOnSuccessListener {
                // Remove o pedido do nó do usuário
                database.child("users").child(userName).child("orders").child(orderId).removeValue()
                    .addOnSuccessListener {
                        callback.onOrderDeleted(orderId)
                    }
                    .addOnFailureListener { e ->
                        callback.onOrderDeletionFailed(
                            e.message ?: "Erro ao remover o pedido do nó do usuário."
                        )
                    }
            }
            .addOnFailureListener { e ->
                callback.onOrderDeletionFailed(
                    e.message ?: "Erro ao remover o pedido do nó 'orders'."
                )
            }
    }

    fun updateOrderStatus(orderId: String, status: String) {
        database.child("orders").child(orderId).child("status").setValue(status)
            .addOnSuccessListener {
                println("Status do pedido atualizado com sucesso!")
            }
            .addOnFailureListener { e ->
                println("Erro ao atualizar status do pedido: ${e.message}")
            }
    }
}

