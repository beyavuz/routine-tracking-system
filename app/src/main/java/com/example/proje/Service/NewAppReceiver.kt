package com.example.proje.Service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.proje.R
import com.example.proje.activity.LoginActivity

class NewAppReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("CALİSTİ ", "ÇALİSTİTİTTT")
        val packageName = intent?.data?.encodedSchemeSpecificPart
        val notificationChannel = NotificationChannel(
                "Channel5",
                "BroadcastRecevier notification",
                NotificationManager.IMPORTANCE_DEFAULT
        )
        val notificationManager = ContextCompat.getSystemService(context!!, NotificationManager::class.java)
        var pendingIntent =
                PendingIntent.getActivity(context, 0, Intent(context, LoginActivity::class.java), 0)
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(notificationChannel)
            val notification2: Notification = Notification.Builder(context, "Channel5")
                    .setContentTitle("Yeni uygulama yüklendi.")
                    .setContentText("${context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(packageName!!,0))}")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentIntent(pendingIntent)
                    //   .setTicker(getText(R.string.ticker_text))
                    .build()
            notification2.flags = notification2.flags or Notification.FLAG_AUTO_CANCEL
            notificationManager.notify(2, notification2)
        }
    }
}