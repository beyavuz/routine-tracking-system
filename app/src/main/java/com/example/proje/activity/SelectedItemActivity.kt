package com.example.proje.activity

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.proje.R
import com.example.proje.util.LoadingScreenInSelectedItem
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import kotlinx.android.synthetic.main.activity_selected_item.*
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList


class SelectedItemActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selected_item)

        val myIntent = intent

        var decreaseButton = findViewById<Button>(R.id.button2)
        var increaseButton = findViewById<Button>(R.id.button_lock_acti)
        var uygulaButton = findViewById<Button>(R.id.button3)
        var seekBarForApplication = findViewById<SeekBar>(R.id.seekBar2)
        var sureKilit = findViewById<TextView>(R.id.sure_kilit)
        var switch = findViewById<Switch>(R.id.switch1)

        initiliazeChartWithDefaultValues(myIntent.getStringExtra("item_package_name")!!)


        seekBarForApplication.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                //println("deger ${seekBarForApplication.progress}")
                var pair = calculateTime(seekBarForApplication.progress)
                sureKilit.text = "Sure :  ${pair.first} saat   ${pair.second} dakika"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                //bunlarla işim yok
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                //bunlarla işim yok
            }

        })

        decreaseButton.setOnClickListener { 
            seekBarForApplication.progress -=1
            println("azaltma tusu...")
            var pair = calculateTime(seekBarForApplication.progress)
            sureKilit.text = "Sure :  ${pair.first} saat   ${pair.second} dakika"
        }

        increaseButton.setOnClickListener {
            seekBarForApplication.progress +=1
            var pair = calculateTime(seekBarForApplication.progress)
            sureKilit.text = "Sure : ${pair.first} saat   ${pair.second} dakika"
        }


        


        if (myIntent.extras != null){
            imageView4.setImageDrawable(
                myIntent.getParcelableExtra<ApplicationInfo>("item_application_info")
                    ?.loadIcon(packageManager)
            )
            textView3.text = myIntent.getStringExtra("item_name")
            sureKilit.text = "Sure : ${getSureFromSharedPreferences(myIntent.getStringExtra("item_name"))}"
            seekBarForApplication.progress = getSharedPreferences("apps_lock_timer",MODE_PRIVATE).getInt(myIntent.getStringExtra("item_name"),0)
            switch.isChecked = getSharedPreferences("apps_lock_persist",MODE_PRIVATE).getBoolean(myIntent.getStringExtra("item_name"),false)
        }

        uygulaButton.setOnClickListener {
            println("uygula tuşu")

            var loadingDialog = LoadingScreenInSelectedItem(this)
            loadingDialog.startLoadingScreen()

            editSharedPreferences(myIntent.getStringExtra("item_name")!!,seekBarForApplication.progress)

            if (switch1.isChecked){
                lockIsPersist(myIntent.getStringExtra("item_name")!!,true)
            }else{
                lockIsPersist(myIntent.getStringExtra("item_name")!!,false)
            }

            Handler().postDelayed(object : Runnable{
                override fun run() {
                    loadingDialog.dismissDialog()
                    finish()
                    Toast.makeText(applicationContext,"Ayarlamala tamamlandı.",Toast.LENGTH_LONG).show()
                }
            },3000)

     //       finish()
        }
        
        
    }

    private fun calculateTime(minutes: Int):Pair<Int, Int>{
        return Pair(minutes / 60, minutes % 60)
    }

    private fun initiliazeChartWithDefaultValues(packageName:String){
        var barchart : BarChart = findViewById<BarChart>(R.id.barChart)
        barchart.setPinchZoom(false)
        barchart.setDrawBarShadow(false)
        barchart.description.isEnabled=false
        barchart.setDrawGridBackground(false)

        var gunler = getLastFiveDays()

        var xAxis = barchart.xAxis
        xAxis.setCenterAxisLabels(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity =  1f
        xAxis.textColor = Color.BLACK
        xAxis.textSize = 9f
        xAxis.axisLineColor = Color.WHITE
        xAxis.axisMinimum = -0.5f
        xAxis.valueFormatter = IndexAxisValueFormatter(gunler)


        var leftAxis : YAxis = barchart.axisLeft

        leftAxis.textColor = Color.BLACK
        leftAxis.textSize = 9f
        leftAxis.axisLineColor = Color.WHITE
        leftAxis.setDrawGridLines(true)
        leftAxis.granularity = 2f  //2f
        leftAxis.setLabelCount(5,true)
        leftAxis.axisMinimum = 0f
        leftAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)

        barchart.axisRight.isEnabled = false
        barchart.legend.isEnabled = false


        var usagestatsforlast5days = usageStatLastFiveDays(packageName)
        var barEntries = ArrayList<BarEntry>()
        barEntries.add(BarEntry(0f,usagestatsforlast5days.get(4).toFloat()/60000))
        barEntries.add(BarEntry(1f,usagestatsforlast5days.get(3).toFloat()/60000))
        barEntries.add(BarEntry(2f,usagestatsforlast5days.get(2).toFloat()/60000))
        barEntries.add(BarEntry(3f,usagestatsforlast5days.get(1).toFloat()/60000))
        barEntries.add(BarEntry(4f,usagestatsforlast5days.get(0).toFloat()/60000))


        var barDataSet = BarDataSet(barEntries,"label_istedi")
        barDataSet.color = Color.BLUE

        barDataSet.isHighlightEnabled = false

        barDataSet.setDrawValues(false)

        var ibardataset = ArrayList<IBarDataSet>()
        ibardataset.add(barDataSet)

        var barData = BarData(ibardataset)

        var barspace = 0f
        var barwidth = 0.5f

        barData.barWidth = barwidth

        xAxis.mAxisMaximum= gunler.size - 0.5f

        barchart.data = barData
        barchart.setScaleEnabled(false)
        barchart.setVisibleXRangeMaximum(6f)
        barchart.invalidate()

    }

    private fun getLastFiveDays():ArrayList<String>{
        var today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
        var daysOfWeek = arrayListOf<String>("Pazar","Pazartesi","Salı","Çarşamba","Perşembe","Cuma","Cumartesi")
        var tempList = ArrayList<String>()
        var dayIndex = daysOfWeek.indexOf(getDayName(Calendar.getInstance().get(Calendar.DAY_OF_WEEK)))
        repeat(5){
            if (dayIndex<0) dayIndex = daysOfWeek.size - 1
            tempList.add(daysOfWeek.get(dayIndex))
            dayIndex--
        }
        return tempList.reversed() as ArrayList<String>
    }


    private fun getDayName(no:Int):String{
        var gun = when(no){
            1 ->  "Pazar"
            2 -> "Pazartesi"
            3 -> "Salı"
            4 -> "Çarşamba"
            5 -> "Perşembe"
            6 -> "Cuma"
            7 -> "Cumartesi"
            else -> "Gun yok"
        }
        return  gun
    }

    private fun editSharedPreferences(appName:String,value:Int){
        var sharedPreferences = getSharedPreferences("apps_lock_timer", MODE_PRIVATE)
        sharedPreferences.edit().putInt(appName,value).commit()
    }

    private fun getSureFromSharedPreferences(isim:String?):String{
        var deger = getSharedPreferences("apps_lock_timer",MODE_PRIVATE).getInt(isim!!,0)
        return when(deger){
            0 -> "Henüz ayarlama yok."
            else -> "${deger/60} saat   ${deger%60} dakika"
        }
    }

    private fun lockIsPersist(isim:String,boolean: Boolean){
        var shared = getSharedPreferences("apps_lock_persist",MODE_PRIVATE)
        var editorInmethod = shared.edit()
        if(boolean){
            if(!shared.getBoolean(isim!!,false)){
                editorInmethod.putBoolean(isim,true).commit()
            }
        }else{
            if(shared.getBoolean(isim,false)){
                editorInmethod.putBoolean(isim,false).commit()
            }
        }
    }

    private fun usageStatLastFiveDays(appPackageName:String):ArrayList<Long>{
        var usageStatManager : UsageStatsManager = this!!.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        var count = 1
        var dondurulcekLongListesi = ArrayList<Long>()
        repeat(5){
            val beginCal = Calendar.getInstance()
            beginCal.add(Calendar.DATE, --count)
            var startOfDay = beginCal.toInstant().atZone(ZoneId.of("Turkey")).toLocalDate().atStartOfDay(ZoneId.of("Turkey")).toInstant().toEpochMilli()
            var kullanimListesi = usageStatManager.queryAndAggregateUsageStats(startOfDay,startOfDay+(24 * 60 * 60 * 1000))

          //  kullanimListesi.get(appPackageName)?.let { it1 -> dondurulcekLongListesi.add(it1.totalTimeInForeground) }

            if(kullanimListesi.get(appPackageName) == null){
                Log.d("VERİ GETİRME","$appPackageName     ----   başlama $startOfDay  bitis ${startOfDay+(24 * 60 * 60 * 1000)}")
                dondurulcekLongListesi.add(0)
            }else if(kullanimListesi.get(appPackageName)!!.totalTimeInForeground ==0L){
                Log.d("VERİ GETİRME","$appPackageName     ----   başlama $startOfDay  bitis ${startOfDay+(24 * 60 * 60 * 1000)}")
                dondurulcekLongListesi.add(0)
            }else{
                Log.d("VERİ GETİRME","$appPackageName     ----   başlama $startOfDay bitis ${startOfDay+(24 * 60 * 60 * 1000)}  ve süresi ${kullanimListesi.get(appPackageName)!!.totalTimeInForeground}")
                dondurulcekLongListesi.add(kullanimListesi.get(appPackageName)!!.totalTimeInForeground)
            }

            /*
            if (kullanimListesi.size == 0 ||  kullanimListesi == null  || (kullanimListesi.filter { eleman -> eleman.packageName == appPackageName }) == null
                    || (kullanimListesi.filter { eleman -> eleman.packageName == appPackageName }).size == 0 ){
                dondurulcekLongListesi.add(0.toLong())
                println("gun : $startOfDay    ---   kullanim : 0")
            }else{
                dondurulcekLongListesi.add((kullanimListesi.filter { eleman -> eleman.packageName == appPackageName }[0].totalTimeInForeground))
                println("gun : $startOfDay    ---   kullanim : ${kullanimListesi.filter { eleman -> eleman.packageName == appPackageName }[0].totalTimeInForeground}")
            }

             */
        }
        dondurulcekLongListesi.forEach {
            Log.d("DONDURULCEK_LISTESI : ","$it")
        }
        return dondurulcekLongListesi
    }
}


