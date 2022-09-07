package com.example.proje.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.proje.R
import com.example.proje.Service.LockService
import com.example.proje.Service.MyAlarmReceiver
import com.example.proje.fragments.AppsBlockFragment
import com.example.proje.fragments.DashBoardFragment
import com.example.proje.fragments.SummaryFragment
import com.example.proje.util.DataBaseHelper
import com.example.proje.util.DataModelSource
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.coroutines.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import kotlin.collections.ArrayList

class DashboardActivity : AppCompatActivity() {

    private lateinit var sharedPreferences : SharedPreferences

    private lateinit var dataModelSource: DataModelSource


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dashboard)
        supportFragmentManager.beginTransaction().add(R.id.fragmentTutucu, DashBoardFragment()).commit()


/*
        //sharedPreference ayarlama
        sharedPreferences = getSharedPreferences("AllInstalledApps",Context.MODE_PRIVATE)
        if(sharedPreferences.getBoolean("isItFirstTime",true)){
            Log.d("Shared","Evet ilk giriş")
           val editor = sharedPreferences.edit()
           editor.putBoolean("isItFirstTime",false)
           justInstalledApps().forEach {
               var isim = packageManager.getApplicationLabel(it.applicationInfo).toString()
               var nameWithLock :String = isim +"_withlock"
               var nameWithUsage :String = isim +"_withUsage"
               editor.putBoolean(nameWithLock,false)
               editor.putBoolean(nameWithUsage,false)
           }
           editor.commit()
            Intent(this, LockService::class.java).also {
                startForegroundService(it)
            }.putExtra("yol","ikinci")
        }
 */

        //CREATE DATABASE -> AMA BUNU ASLINDA ALARM MANAGER YAPSA DAHA İYİ OLUR.
        createDatabase()

        //sharedPreference  app_is_running dosyasına ekleyeceğim asıl değeri
        sharedPreferences = getSharedPreferences("app_is_running",Context.MODE_PRIVATE)
        if(!sharedPreferences.getBoolean("appIsRunning",false)){
            Log.d("Shared","Evet ilk giriş")
            val editor = sharedPreferences.edit()
            editor.putBoolean("appIsRunning",true)
            editor.commit()
            startAllAppsLockTimer()
            startAllAppsLockStatus()
            startAllAppsIsLockPersist()
            startAlarmManager()
            Intent(this, LockService::class.java).also {
                startForegroundService(it)
            }.putExtra("yol","ikinci")
        }



        //datasource'u başlatalım.
       // dataModelSource = DataModelSource()
        dataModelSource = DataModelSource.getInstance()

        dataModelSource.initList(justInstalledApps(), packageManager,getSharedPreferences("apps_lock_status",0),this)
        justInstalledApps().forEach {
            println("yollanan  : ${packageManager.getApplicationLabel(it.applicationInfo).toString()}")
        }

        //bottom navigate'ında herhangi bir iteme tıklanıldığında çalışan listener
        bottomNavigationView.setOnNavigationItemSelectedListener {
            //parametre olarak it:MenuItem geldi , bu bottomnavigation'da tıklanılan , item'i temsil ediyor.
            //biz buradan menu üzerindeki itemlerin id'lerinden hangisine tıklanılıdğını öğrencez.
            if (it.itemId == R.id.dashboard_action_item){
                //dashboard fragment çalışsın.
                beginFragmentTransaction(DashBoardFragment(),1)
            }else if(it.itemId == R.id.app_block_item){
                //AppsBlockFragment çalışsın
                beginFragmentTransaction(AppsBlockFragment(),2)
            }else if(it.itemId == R.id.summary_item){
                //Summary fragment çalışsın.
                beginFragmentTransaction(SummaryFragment(),3)
            }
            true
        }
        loadUsageData()
    }

    override fun onDestroy() {
        super.onDestroy()
        this.dataModelSource.deleteList()
    }

    /**
     * uygulamayı alta attı , başka uygulamalara girdi , tekrar bu uygulamayı çalıştırdı.
     * Uygulamaların kullanım oranlarının güncellenmesi için , uygulamaların datasource'tan tekrar başlatılması lazım.
     * uygulamayı alta indirip tekrar başlattığımızda onResume metodu çalışır.
     * Uygulama alta indiğinde yani onStop olduğunda singleton datasource objesini yok etmeliyim , resume olduğunda tekrar oluşturmalıyım.
     */
    override fun onResume() {
        super.onResume()
        Log.d("RESUME " , "ON RESUME OLDU.")
        this.dataModelSource.deleteList()
        this.dataModelSource.initList(justInstalledApps(), packageManager,getSharedPreferences("apps_lock_status",0),this)
        this.dataModelSource.appsList.sortByDescending { it.kullanimSuresi }
        beginFragmentTransaction(DashBoardFragment(),1)
    }


    private fun beginFragmentTransaction(fragment:Fragment,number:Int){

        if (number == 3){
            var bundle = Bundle()
            bundle.putInt("summary",3)
            fragment.arguments = bundle
        }else if(number==2){
            var bundle = Bundle()
        //    bundle.putSerializable("uygulamalar",dataModelSource)
            fragment.arguments = bundle
        }

        supportFragmentManager.beginTransaction().replace(R.id.fragmentTutucu,fragment).commit()
    }

    fun getAllInstalledApps():ArrayList<String>{
        var appName = mutableListOf("YouTube","LinkedIn","Facebook","Google Play Movies & TV","Chrome","Gmail") as ArrayList
        var allApp = ArrayList<String>()

        var installedApps = this?.packageManager?.getInstalledPackages(0)
        installedApps?.let {
            installedApps.forEach {
        //        println("paket ismi : ${it.packageName}")
        //        println("application info : ${it.applicationInfo}")
                if(((ApplicationInfo.FLAG_UPDATED_SYSTEM_APP or ApplicationInfo.FLAG_SYSTEM) and it.applicationInfo.flags) > 0){
                    println("app ismi: ${packageManager.getApplicationLabel(it.applicationInfo)}  : sistem uygulamasi")
                    if(packageManager.getApplicationLabel(it.applicationInfo) in appName){
                        allApp.add(packageManager.getApplicationLabel(it.applicationInfo) as String)
                    }
                }else{
                    println("app ismi: ${packageManager.getApplicationLabel(it.applicationInfo)}  :  sistem uygulaması degil")
                    allApp.add(packageManager.getApplicationLabel(it.applicationInfo) as String)
                }
            }
        }
        return allApp
    }

    private fun justInstalledApps():ArrayList<PackageInfo> {
        var appName = mutableListOf("YouTube","LinkedIn","Facebook","Google Play Movies & TV","Chrome","Gmail") as ArrayList
        var uygListe = (this?.packageManager?.getInstalledPackages(0) as ArrayList<PackageInfo>).filter {
            !((((ApplicationInfo.FLAG_UPDATED_SYSTEM_APP or ApplicationInfo.FLAG_SYSTEM) and it.applicationInfo.flags) > 0) and (packageManager.getApplicationLabel(it.applicationInfo) !in appName))
        } as ArrayList<PackageInfo>
        uygListe.removeIf { eleman -> eleman.packageName == "com.example.proje" }
        return uygListe
    }

    private fun loadUsageData(){
        var usageStatManager : UsageStatsManager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        var startTime = LocalDate.now().atStartOfDay(ZoneId.of("Turkey")).toInstant().toEpochMilli()
        var endTime = ZonedDateTime.now().toInstant().toEpochMilli()

       // var kullanimListesi = usageStatManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,startTime,endTime)

        var kullanimListesi = usageStatManager.queryAndAggregateUsageStats(startTime,endTime)

        kullanimListesi.forEach { key , value ->
            println("${key} ----   ${value.totalTimeInForeground}")
        }

    }

    /**
     * apps_lock_timer  dosyası oluşturuldu ve gerekli değerler dosyaya eklendi.
     */
    private fun startAllAppsLockTimer(){
        var sharedPreferencesInMethod = getSharedPreferences("apps_lock_timer",Context.MODE_PRIVATE)
        var editorInMethod = sharedPreferencesInMethod.edit()
        justInstalledApps().forEach {
            var isim = packageManager.getApplicationLabel(it.applicationInfo).toString()
            var nameWithLock :String = isim
            editorInMethod.putInt(nameWithLock,0)
        }
        editorInMethod.commit()
    }

    /**
     * apps_lock_status dosyası oluşturuldu ve gerekli değerler eklendi.
     */
    private fun startAllAppsLockStatus(){
        var sharedPreferencesInMethod = getSharedPreferences("apps_lock_status",Context.MODE_PRIVATE)
        var editorInMethod = sharedPreferencesInMethod.edit()
        justInstalledApps().forEach {
            var isim = packageManager.getApplicationLabel(it.applicationInfo).toString()
            var nameWithLock :String = isim +"_withlock"
            var nameWithUsage :String = isim +"_withUsage"
            editorInMethod.putBoolean(nameWithLock,false)
            editorInMethod.putBoolean(nameWithUsage,false)
        }
        editorInMethod.commit()
    }

    private fun startAllAppsIsLockPersist(){
        var sharedPreferencesInMethod = getSharedPreferences("apps_lock_persist",Context.MODE_PRIVATE)
        var editorInMethod = sharedPreferencesInMethod.edit()
        justInstalledApps().forEach {
            var isim = packageManager.getApplicationLabel(it.applicationInfo).toString()
            editorInMethod.putBoolean(isim,false)
        }
        editorInMethod.commit()
    }

    /**
     * saat 23.59.59 oldumu alarm recevier çalışacak.
     */
    private fun startAlarmManager(){
        var calendar = Calendar.getInstance()
        var alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        calendar.set(Calendar.HOUR_OF_DAY,23) //  23
        calendar.set(Calendar.MINUTE,59)     // 59
        calendar.set(Calendar.SECOND,0)     // 0
        Log.d("SAATT","${calendar.time}    mili: ${calendar.timeInMillis}")
        var intent = Intent(this, MyAlarmReceiver::class.java)
        intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        var pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
 //bunu kullanma       //alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis,pendingIntent)
        //alarmManager.setRepeating(AlarmManager.RTC,calendar.timeInMillis, AlarmManager.INTERVAL_DAY,pendingIntent)
        alarmManager.setExact(AlarmManager.RTC,calendar.timeInMillis,pendingIntent)
    }

    private fun createDatabase(){
        var db = DataBaseHelper(this,"uygulamalar")
        db.readableDatabase
    }

}