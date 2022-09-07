package com.example.proje.adapter

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proje.model.ApplicationModel
import com.example.proje.R
import kotlinx.android.synthetic.main.dashboard_item.view.*

class DashBoardItemAdapter(private var appsList:ArrayList<ApplicationModel>, private var packageManager: PackageManager,var clickListener: OnAppItemClickListener?) : RecyclerView.Adapter<DashBoardItemAdapter.ExampleViewHolder>(){
   // (private var appsList:ArrayList<PackageInfo>,private var packageManager: PackageManager)


    class ExampleViewHolder(view: View): RecyclerView.ViewHolder(view){


        fun bind(apps: ApplicationModel, packageManager: PackageManager){
            itemView.text_view.text = packageManager.getApplicationLabel(apps.packageInfo.applicationInfo).toString()
            itemView.image_view.setImageDrawable(apps.packageInfo.applicationInfo.loadIcon(packageManager))
       /*
            var rastgeleSayi = (0..1440).random()
            itemView.progressBar.progress = rastgeleSayi
            var saat = rastgeleSayi / 60
            var dakika = rastgeleSayi % 60
        */
            itemView.progressBar.progress = apps.kullanimSuresi.div(1000).div(60).toInt()
            var saat = (apps.kullanimSuresi.div(1000).div(60).toInt())/60
            var dakika = (apps.kullanimSuresi.div(1000).div(60).toInt())% 60
            itemView.text_view_sure.text = "Sure:"
            itemView.text_view_sure.text =   "${itemView.text_view_sure.text} $saat saat  $dakika dakika"
        }


        fun baslatma(item:ApplicationModel,action:OnAppItemClickListener){
            itemView.setOnClickListener {
                action.onAppItemClickListener(item,adapterPosition)
            }
        }



    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.dashboard_item,parent,false)
        return ExampleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {
        holder.bind(appsList.get(position),packageManager)
        holder.baslatma(appsList.get(position),clickListener as OnAppItemClickListener)
/*
        holder.itemView.setOnClickListener {

        }
 */

    }

    override fun getItemCount(): Int {

        return appsList.size
    }
}

interface OnAppItemClickListener{
    fun onAppItemClickListener(item:ApplicationModel,position: Int)
}