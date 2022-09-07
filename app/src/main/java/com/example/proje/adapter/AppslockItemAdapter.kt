package com.example.proje.adapter

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.proje.R
import com.example.proje.model.ApplicationModel
import kotlinx.android.synthetic.main.apps_lock_item.view.*
import kotlinx.android.synthetic.main.dashboard_item.view.*

class AppslockItemAdapter(private var appsList:ArrayList<ApplicationModel>, private var packageManager: PackageManager,private var sharedPreferences: SharedPreferences)
    : RecyclerView.Adapter<AppslockItemAdapter.ExampleViewHolder>(){
    // (private var appsList:ArrayList<PackageInfo>,private var packageManager: PackageManager)



    class ExampleViewHolder(view: View): RecyclerView.ViewHolder(view){

        private lateinit var app_status : ImageView



        fun bind(apps: ApplicationModel, packageManager: PackageManager,sharedPreferences: SharedPreferences){
            //itemView.text_view.text = packageManager.getApplicationLabel(apps.packageInfo.applicationInfo).toString()
            itemView.appName_apps_lock.text = apps.name
            itemView.appIcon_apps_lock.setImageDrawable(apps.packageInfo.applicationInfo.loadIcon(packageManager))
        //    app_status = R.id.imageView3 as ImageView
         //   var degr = sharedPreferences.getBoolean(apps.name+"_withlock",false)
            if (apps.status){
                itemView.imageView3.setImageResource(R.drawable.lock_active)
            }else{
                itemView.imageView3.setImageResource(R.drawable.lock_disable)
            }

            itemView.imageView3.setOnClickListener {


                if(apps.status){
                    apps.status = !apps.status
                    sharedPreferences.edit().putBoolean(apps.name+"_withlock",false).commit()
                    itemView.imageView3.setImageResource(R.drawable.lock_disable)
                }else{
                    apps.status = !apps.status
                    sharedPreferences.edit().putBoolean(apps.name+"_withlock",true).commit()
                    itemView.imageView3.setImageResource(R.drawable.lock_active)
                }
/*
                var degr = sharedPreferences.getBoolean(apps.name+"_withlock",false)
                if(degr){
                    apps.status = !apps.status
                    sharedPreferences.edit().putBoolean(apps.name+"_withlock",false).commit()
                    itemView.imageView3.setImageResource(R.drawable.lock_disable)
                }else{
                    apps.status = !apps.status
                    sharedPreferences.edit().putBoolean(apps.name+"_withlock",true).commit()
                    itemView.imageView3.setImageResource(R.drawable.lock_active)
                }

 */
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.apps_lock_item,parent,false)
        return ExampleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {
        holder.bind(appsList.get(position),packageManager,sharedPreferences)


    }

    override fun getItemCount(): Int {

        return appsList.size
    }



}