package com.example.proje.Service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v4.media.session.PlaybackStateCompat
import android.telephony.ServiceState

class MyBootService:BroadcastReceiver() {
    private lateinit var sharedPreferences: SharedPreferences
    override fun onReceive(context: Context?, intent: Intent?) {
        sharedPreferences =
                context!!.getSharedPreferences("app_is_running", Context.MODE_PRIVATE)
        var deger = sharedPreferences.getBoolean("isServiceRunning", false)
        if (!deger) {
            sharedPreferences.edit().putBoolean("isServiceRunning", true).apply()
            Intent(context, LockService::class.java).also {
                context.startForegroundService(it)
            }.putExtra("yol","ikinci")
        }
        sharedPreferences.edit().putBoolean("isServiceRunning", true).apply()
        Intent(context, LockService::class.java).also {
            context.startForegroundService(it)
        }.putExtra("yol","ikinci")
    }


}