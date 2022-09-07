package com.example.proje.Service

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.IBinder
import android.os.Process
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.proje.R
import com.example.proje.activity.DashboardActivity
import com.example.proje.activity.LockActivity
import com.example.proje.util.AppUsageInfo
import java.lang.NullPointerException
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.ArrayList

class LockService : Service() {

    //private lateinit var handler: Handler

    private lateinit var activityManager: ActivityManager

    private var sortedMap : TreeMap<Long, UsageStats> = TreeMap()

    var appCounter = 0
    var appFlag = false
    var currentAppName = ""

    private lateinit var sharedPreferences: SharedPreferences

    private var allLockedApps = ArrayList<String>()

    //bir sade object oluşturuyorum, bu objeye Runnable özellğini ekliyorum , thread değişkeni ile object'ye erişebiliyorum.
    private val birinciYol_thread = object : Runnable{
        //tekrarlı olarak çalışacak kod burada yazılıyor.
        override fun run() {
            while(true){
                Thread.sleep(5000)


                var usageEvent: UsageEvents.Event? = null

                val mUsageStatsManager = getSystemService(Service.USAGE_STATS_SERVICE) as UsageStatsManager
                val time = System.currentTimeMillis()

                val usageEvents = mUsageStatsManager.queryEvents(time - 1000 * 3600, time)
                val event = UsageEvents.Event()
                while (usageEvents.hasNextEvent()) {
                    usageEvents.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                        usageEvent = event
                    }
                }
                if (usageEvent != null) {
                    println(usageEvent.className)
                }


            }
        }
    }

    private val ikinciyol_Thread = object : Runnable{
        //tekrarlı olarak çalışacak kod burada yazılıyor.
        override fun run() {
            var topPackageName :String = ""
            var appName : String = ""
            var packageManager = applicationContext.packageManager
            var baska_birsey : Any? = null
            while (true){
                Thread.sleep(1000)

                //uygulama adını ve package ismini çekmek için
                val mUsageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
                val time = System.currentTimeMillis()
                // We get usage stats for the last 10 seconds
                val stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time)
                // Sort the stats by the last time used
                if (stats != null) {
                    val mySortedMap: SortedMap<Long, UsageStats> = TreeMap()
                    for (usageStats in stats) {
                        mySortedMap[usageStats.lastTimeUsed] = usageStats
                    }
                    if (!mySortedMap.isEmpty()) {
                        baska_birsey = mySortedMap[mySortedMap.lastKey()]!!
                        topPackageName = mySortedMap[mySortedMap.lastKey()]!!.packageName
                        appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(topPackageName, 0)) as String

                    }
                }

                //activity'sini çekmek için
                var usageEvent: UsageEvents.Event? = null
                val usageEvents = mUsageStatsManager.queryEvents(time - 1000 * 3600, time)
                val event = UsageEvents.Event()
                while (usageEvents.hasNextEvent()) {
                    usageEvents.getNextEvent(event)
                    if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                        usageEvent = event
                    }
                }
                if (usageEvent != null) {
                    println(usageEvent.className)
                }

     //           println("package name : $topPackageName  ,    appName : $appName  ,    activity: ${usageEvent?.className} ")

          //      var usageStatsManager:UsageStatsManager? = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

                allLockedApps = getAllLockedApp()
                println("Tum kilitli uygulamalar : $allLockedApps")
                if(((appName in allLockedApps) || getUsageStatFromStartOfTheDayToNow(topPackageName))){

                    appFlag = true

                    var intent = Intent(applicationContext, LockActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    intent.addCategory(Intent.CATEGORY_HOME)
                    intent.putExtra("packageName","$topPackageName")

                    //           ActivityCompat.finishAffinity()
//                    activityManager.killBackgroundProcesses(topPackageName)

                    //    activityManager.mo
                    //   activityManager.restartPackage(topPackageName)

                    startActivity(intent)
                }else{

                }
           //     usageStatsManager = null

                /*
                if(appName.equals("Spotify")){
                    var intent = Intent(applicationContext, LockActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    intent.addCategory(Intent.CATEGORY_HOME)
                    intent.putExtra("packageName","$topPackageName")

                    //           ActivityCompat.finishAffinity()
//                    activityManager.killBackgroundProcesses(topPackageName)

                    //    activityManager.mo
                    //   activityManager.restartPackage(topPackageName)

                    startActivity(intent)
                }
                 */

            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {

        //önce recevier'ı kaydet
        var receiver = NewAppReceiver()
        registerReceiver(receiver, IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        })

        println("on create thread :${Thread.currentThread().name} ")
        println("on create proces ${Process.myPid()}")
        sharedPreferences = this.getSharedPreferences("app_is_running", 0)
        sharedPreferences.edit().putBoolean("isServiceRunning", true).commit()   //.apply()
        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        super.onCreate()
    }



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        var deger = intent?.getStringExtra("yol")
        if(deger.equals("birinci")){
            println("birinci yol thread başladı.")
            Thread(birinciYol_thread).start()
        }else{
            println("ikinci yol thread başladı.")
            Thread(ikinciyol_Thread).start()
        }

        createNotificationChannel()
        val intent1 = Intent(this, DashboardActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent1, 0)
        val notification = NotificationCompat.Builder(this, "Channel2")
            .setContentTitle("Servis Uygulaması")
            .setContentText("Service uygulamam çalışıyor.")
            //.setSmallIcon()
            .setContentIntent(pendingIntent).build()

        val notification2: Notification = Notification.Builder(this, "Channel2")
            .setContentTitle("Dinleme calisiyor.")
            .setContentText("Kilitleme Servisi calisiyor.")
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            //   .setTicker(getText(R.string.ticker_text))
            .build()


        startForeground(1, notification2)

        // return super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }


    fun createNotificationChannel(){

        val notificationChannel = NotificationChannel("Channel2", "Foreground notification", NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    override fun onDestroy() {
        println("destroy ediliyor.")
        stopForeground(true)
        stopSelf()
        super.onDestroy()

    }

    fun getAllLockedApp():ArrayList<String>{
        var getAllAppsWithLock = ArrayList<String>()
        sharedPreferences = this.getSharedPreferences("apps_lock_status", MODE_MULTI_PROCESS)
        sharedPreferences.all.forEach { t, any ->
            println("key : $t    value:$any")
            if (any as Boolean){

                if(t.contains("_withlock") || t.contains("_withUsage")){    //withusage'a gerek kalmayabilir.
                    getAllAppsWithLock.add(t.substringBefore("_"))
                }
  //              println("uygulama kilitli : $t")
            }
        }
        return getAllAppsWithLock
    }


    /**
     * usage ' a göre sınırlanmış uygulamalar için.
     */
    fun getUsageStatFromStartOfTheDayToNow(packageName:String):Boolean{
        var usageStatManager : UsageStatsManager? = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        var startTime = LocalDate.now().atStartOfDay(ZoneId.of("Turkey")).toInstant().toEpochMilli()
        var endTime = ZonedDateTime.now().toInstant().toEpochMilli()

       // var kullanimListesi = usageStatManager!!.queryUsageStats(UsageStatsManager.INTERVAL_BEST,startTime,endTime)
         var kullanimListesi = usageStatManager!!.queryAndAggregateUsageStats(startTime,endTime)
        var toplamSuree : Int
        var toplamSuree2 : Int
        try {
            toplamSuree = kullanimListesi.get(packageName)!!.totalTimeInForeground.div(1000).div(60).toInt()
            var filtreli_liste = realUsageStat().filter { eleman -> eleman.packageName == packageName }
            toplamSuree2 = if(filtreli_liste.size==0) 0 else filtreli_liste[0].timeInForeground.div(1000).div(60).toInt()
        }catch (exception:NullPointerException){
            toplamSuree = 0
            toplamSuree2 = 0
        }




      //  var toplamSure = kullanimListesi.filter { eleman -> eleman.packageName == packageName }[0].totalTimeInForeground / (1000*60)

        var shared = getSharedPreferences("apps_lock_timer", MODE_MULTI_PROCESS)
        var appName = packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName,0)).toString()

        if (currentAppName != appName){
            currentAppName = appName
            appCounter = 0
        }else{
            appCounter++
        }

        if (!shared.contains(appName)) return false
        var kilitSuresi = shared.getInt(appName,0)
        if(kilitSuresi == 0) return false
        Log.d("KARŞİLA","nesne:$usageStatManager   TOPLAM SURE: $toplamSuree    $kilitSuresi    $appName   counter:$appCounter  yenisuree2: $toplamSuree2")
        println("toplamsuree = $toplamSuree yenisuree2toplam: $toplamSuree2 appcounter=$appCounter    veriler : toplamSuree+appCounter ${(toplamSuree+appCounter)/60}       kilitSuresi: ${kilitSuresi}")
       // return (toplamSuree)+(appCounter/60) >= kilitSuresi
        return (toplamSuree2)+(appCounter/60) >= kilitSuresi
    }

    fun realUsageStat():ArrayList<AppUsageInfo>{

        var smallInfoList : MutableList<AppUsageInfo>


        var currentEvent  : UsageEvents.Event
            var  allEvents : MutableList<UsageEvents.Event> = ArrayList()
            var map : HashMap<String, AppUsageInfo> = HashMap<String, AppUsageInfo> ()

            var currTime = System.currentTimeMillis();
            var startTime = currTime - 1000*3600*3; //querying past three hours
            var gunBaslangic = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()


            var usageStatManager : UsageStatsManager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

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

            
            return smallInfoList
        }


}