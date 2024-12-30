package com.example.project1732.Activity

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Verifica se a mensagem contém dados ou notificação
        if (remoteMessage.notification != null) {
            // Chama o método para exibir a notificação
            sendNotification(remoteMessage.notification!!.title, remoteMessage.notification!!.body)
        } else if (remoteMessage.data.isNotEmpty()) {
            // Caso tenha dados, processar aqui
            val title = remoteMessage.data["title"]
            val body = remoteMessage.data["body"]
            sendNotification(title, body)
        }
    }

    private fun sendNotification(title: String?, messageBody: String?) {
        // Intent para abrir a atividade principal quando a notificação for clicada
        val intent = Intent(this, Principal::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Som padrão para a notificação
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Construtor da notificação
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(com.example.project1732.R.drawable.ic_baseline_location_on_24)  // Definir seu ícone
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        // Gerenciador de Notificação
        val notificationManager = NotificationManagerCompat.from(this)

        // Verifique se o canal já foi criado (Android O e superior)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        // Verificar e solicitar permissões no Android 13 e superior
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Solicitar permissões caso necessário (Android 13+)
            return
        }

        // Envia a notificação
        notificationManager.notify(0, notificationBuilder.build())
    }

    // Método para criar o canal de notificação (usado apenas uma vez)
    private fun createNotificationChannel(notificationManager: NotificationManagerCompat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notificações de Pedidos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para notificações de atualização de pedidos"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "Novo token FCM: $token")

        // Armazene o token no Firebase Firestore
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid
            val db = FirebaseFirestore.getInstance()

            // Atualize o token no Firestore
            val tokenMap = mapOf("deviceToken" to token)

            db.collection("Usuarios").document(uid).update(tokenMap)
                .addOnSuccessListener {
                    Log.d(TAG, "Token atualizado com sucesso no Firestore")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Erro ao atualizar o token", e)
                }
        } else {
            Log.w(TAG, "Usuário não autenticado. Não foi possível salvar o token.")
        }
    }

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "delivery_notifications"
    }
}

