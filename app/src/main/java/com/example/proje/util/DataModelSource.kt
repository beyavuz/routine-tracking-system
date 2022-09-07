package com.example.proje.util

import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import com.example.proje.model.ApplicationModel
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime


class DataModelSource private constructor(){

    lateinit var smallInfoList : MutableList<AppUsageInfo>

    companion object{
        private var instance : DataModelSource? = null
        fun getInstance():DataModelSource{
            if (instance == null){
                instance = DataModelSource()
            }
            return instance as DataModelSource
        }
    }

    var appsList: ArrayList<ApplicationModel> = ArrayList<ApplicationModel>()
    var context: Context? = null

    //lateinit var sharedPreferences: SharedPreferences

    fun initList(liste: List<PackageInfo>, packageManager: PackageManager, sharedPreferences: SharedPreferences, applicationContext: Context){
        Log.d("LISTE BASLATMA", "LİSTE BAŞLATILDI. boyut ${appsList.size}")
        setDataModelContext(applicationContext)
//        var kullanimListesi = initiliazeDataUsageForToday()
        var kullanimListesi = initiliazeDataUsageForToday2()
        getRealUsageStat()

        liste?.let {
            liste.forEach {
                var kullanimListesiFilterListe = kullanimListesi!!.filter{ eleman -> eleman.packageName == it.packageName}

             //   var kullanimListesiFilterListe = kullanimListesi!!.get(it.packageName)
                Log.d("EKLEME", "Ekleniyor : ${packageManager.getApplicationLabel(it.applicationInfo)}  paket ismi ${it.packageName}") // burada sıkıntı yok.
                if(it.packageName == "com.example.proje"){
                    //Projemi bu listeye ekleme
                }
                var filtreli_liste = smallInfoList.filter { eleman -> eleman.packageName == it.packageName }
                appsList.add(
                        ApplicationModel(
                                it.applicationInfo.uid,
                                packageManager.getApplicationLabel(it.applicationInfo).toString(),
                                it.packageName,
                                it.firstInstallTime,
                                it.lastUpdateTime,
                              //  if (kullanimListesiFilterListe.size == 0) 0 else kullanimListesiFilterListe[0].totalTimeInForeground,
                                ////     if (kullanimListesiFilterListe != null) kullanimListesiFilterListe.totalTimeInForeground else 0,
                                if(filtreli_liste.size==0) 0 else filtreli_liste[0].timeInForeground,
                                it,
                                sharedPreferences.getBoolean(packageManager.getApplicationLabel(it.applicationInfo).toString() + "_withlock", false),
                                sharedPreferences.getBoolean(packageManager.getApplicationLabel(it.applicationInfo).toString() + "_withUsage", false))
                )
            }
            appsList.removeIf { eleman-> eleman.name == "Proje" || eleman.name == "proje" }
            appsList.sortByDescending { it.kullanimSuresi }
            Log.d("YAZDIRMA", " İCİNDE")
            appsList.forEach {
                Log.d("Yazdırılıyor", "${it.name}")
            }
        }
    }

    private fun setDataModelContext(applicationContext: Context){
        this.context = applicationContext
    }

    fun forAppsLock():ArrayList<ApplicationModel>{
        appsList.sortBy { it.name }
        Log.d("YAZDIRMA", " FOR APPS LOCK")
        appsList.forEach {
            Log.d("Yazdırılıyor", "${it.name}")
        }
        return appsList
    }

    fun forDashBoard():ArrayList<ApplicationModel>{
        // bunların kullanımı doğru değil. referansıda yolluyoruz , listenin bir kopyasını yollamalıyız.
        appsList.sortByDescending { it.kullanimSuresi }
        return appsList
    }

    fun deleteList(){
        this.appsList.clear()
    }


    /**
     * summary kısmındaki drop menu için gönderilcek stringler
     */
    fun forSummary():ArrayList<String>{
        var liste =  appsList.map {
            it.name
        } as ArrayList
        return liste
    }

    fun initiliazeDataUsageForToday(): MutableMap<String, UsageStats>? {
        var usageStatManager : UsageStatsManager = context!!.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

      //  usageStatManager.c

        var startTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        var endTime = ZonedDateTime.now().toInstant().toEpochMilli()

        println("süreler => $startTime  ,   ${LocalDate.now().atStartOfDay(ZoneId.of("Turkey")).toInstant()}")

       // var kullanimListesi = usageStatManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,startTime,endTime)

        var kullanimListesi = usageStatManager.queryAndAggregateUsageStats(0, System.currentTimeMillis())

        kullanimListesi.forEach { key, value ->
            println("paket ismi : ${key} ---- kullanım süresi   ${value}")
        }

        return kullanimListesi
    }

    fun initiliazeDataUsageForToday2(): MutableList<UsageStats>? {
        var usageStatManager : UsageStatsManager = context!!.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        //  usageStatManager.c

        var startTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        var endTime = ZonedDateTime.now().toInstant().toEpochMilli()

        println("süreler => $startTime  ,   ${LocalDate.now().atStartOfDay(ZoneId.of("Turkey")).toInstant()}")

        var kullanimListesi = usageStatManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, endTime - 1200000, endTime)

        return kullanimListesi
    }

    fun getRealUsageStat(){

            var currentEvent  : UsageEvents.Event
            var  allEvents : MutableList<UsageEvents.Event> = ArrayList()
            var map : HashMap<String, AppUsageInfo> = HashMap<String, AppUsageInfo> ()

            var currTime = System.currentTimeMillis();
            var startTime = currTime - 1000*3600*3; //querying past three hours
            var gunBaslangic = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()


        var usageStatManager : UsageStatsManager = context!!.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

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
            smallInfoList = ArrayList(map.values)

        smallInfoList.forEach {
            println("BAK BURAYA : app name : ${it.packageName}   foreground : ${it.timeInForeground}")
        }

    }

}
class AppUsageInfo internal constructor(var packageName: String) {
    var appIcon: Drawable? = null
    var appName: String? = null
    var timeInForeground: Long = 0
    var launchCount = 0
}
