package com.example.proje.Service

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.util.Log
import android.widget.Toast
import com.example.proje.model.DatabaseEntry
import com.example.proje.util.AppUsageInfo
import com.example.proje.util.DataBaseHelper
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.ArrayList

class MyAlarmReceiver:BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context,"Alarm Manager çalışıyor...",Toast.LENGTH_LONG).show()
        var db = DataBaseHelper(context!!,"uygulamalar")
        var usageStatManager : UsageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        var startTime = LocalDate.now().atStartOfDay(ZoneId.of("Turkey")).toInstant().toEpochMilli()
        var endTime = ZonedDateTime.now().toInstant().toEpochMilli()

        var kullanimListesi = usageStatManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,startTime,endTime)
        var allInstalledApps = justInstalledApps(context).map {
            it.packageName
        }
        //verileri database kaydetme
        println("Kaydetme başlıyor.......")
        var asilKullanimListesi = realKullanimListesi(context)
        asilKullanimListesi.forEach {
            println("GELENLERİİ : ${it.packageName}" )
            Log.d("Error:" , "GELENLERİİ : ${it.packageName}" )
            if (it.packageName in allInstalledApps){  //sadece yüklenen uygulamalar
                println("VERİTABANINA EKLENENLERİİ : ${it.packageName}")
                Log.d("Error:" , "VERİTABANINA EKLENENLERİİ : ${it.packageName}")
                db.addEntry(
                    DatabaseEntry(
                        it.packageName,
                        context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(it.packageName,0)).toString(),
                        SimpleDateFormat("d-M-yyyy").format(Date()),
                        it.timeInForeground
                    )
                )
            }
        }
        /*
        kullanimListesi.forEach {
            println("GELENLERİİ : ${it.packageName}" )
            Log.d("Error:" , "GELENLERİİ : ${it.packageName}" )
            if (it.packageName in allInstalledApps){  //sadece yüklenen uygulamalar
                println("VERİTABANINA EKLENENLERİİ : ${it.packageName}")
                Log.d("Error:" , "VERİTABANINA EKLENENLERİİ : ${it.packageName}")
                db.addEntry(
                        DatabaseEntry(
                                it.packageName,
                                context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(it.packageName,0)).toString(),
                                SimpleDateFormat("d-M-yyyy").format(Date()),
                                it.totalTimeInForeground
                        )
                )
            }
        }
         */
        cleanUsageLimitData(context)
    }

    private fun justInstalledApps(context : Context?):ArrayList<PackageInfo> {
        var appName = mutableListOf("YouTube","LinkedIn","Facebook","Google Play Movies & TV","Chrome","Gmail") as ArrayList
        var uygListe = (context!!.packageManager.getInstalledPackages(0) as ArrayList<PackageInfo>).filter {
            !((((ApplicationInfo.FLAG_UPDATED_SYSTEM_APP or ApplicationInfo.FLAG_SYSTEM) and it.applicationInfo.flags) > 0) and (context!!.packageManager.getApplicationLabel(it.applicationInfo) !in appName))
        } as ArrayList<PackageInfo>
        uygListe.removeIf { eleman -> eleman.packageName == "com.example.proje" }
        return uygListe
    }


    private fun cleanUsageLimitData(context:Context){
        var sharedPreferencesInMethod = context.getSharedPreferences("apps_lock_persist",Context.MODE_PRIVATE)
        var persist_liste = ArrayList<String>()
        justInstalledApps(context).forEach {
            var isim = context.packageManager.getApplicationLabel(it.applicationInfo).toString()
           if(!sharedPreferencesInMethod.getBoolean(isim,false)){
               persist_liste.add(isim)
               Log.d("Persisit olanlar: ", "$it")
           }
        }
        var shared_other = context.getSharedPreferences("apps_lock_timer",Context.MODE_PRIVATE)
        var editor_shared = shared_other.edit()
        persist_liste.forEach {
            Log.d("Sıfırlannalar :" , "$it")
            editor_shared.putInt(it,0)
        }
        editor_shared.commit()
    }

    private fun realKullanimListesi(context:Context):MutableList<AppUsageInfo>{
        var kullanimListesi : MutableList<AppUsageInfo>


        var currentEvent  : UsageEvents.Event
        var  allEvents : MutableList<UsageEvents.Event> = ArrayList()
        var map : HashMap<String, AppUsageInfo> = HashMap<String, AppUsageInfo> ()

        var currTime = System.currentTimeMillis();
        var startTime = currTime - 1000*3600*3; //querying past three hours
        var gunBaslangic = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()


        var usageStatManager : UsageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        var usageEvents : UsageEvents = usageStatManager.queryEvents(gunBaslangic, currTime)

        while (usageEvents.hasNextEvent()) {
            currentEvent =  UsageEvents.Event()
            usageEvents.getNextEvent(currentEvent)
            if (currentEvent.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                currentEvent.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                allEvents.add(currentEvent)

                var key : String = currentEvent.getPackageName()
                if (map.get(key)==null)
                    map.put(key, AppUsageInfo(key))
            }
        }

        for ( i in 0..allEvents.size-2 step 1){
            var E0 : UsageEvents.Event =allEvents.get(i)
            var E1 : UsageEvents.Event =allEvents.get(i + 1)

            if (!E0.getPackageName().equals(E1.getPackageName()) && E1.getEventType()==1){
                map.get(E1.getPackageName())!!.launchCount++
            }

            if (E0.getEventType()==1 && E1.getEventType()==2
                && E0.getClassName().equals(E1.getClassName())){
                var diff : Long = E1.getTimeStamp()-E0.getTimeStamp()
                //    phoneUsageToday+=diff; //gloabl Long var for total usagetime in the timerange
                map.get(E0.getPackageName())!!.timeInForeground+= diff
            }
        }
        kullanimListesi = ArrayList(map.values)
        return kullanimListesi
    }

}