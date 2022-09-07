package com.example.proje.denemeler

/*
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.proje.R
import kotlinx.android.synthetic.main.row_layout.view.*

class CustomRecylerViewAdapter(private var appsList:ArrayList<PackageInfo>,
                               private var packageManager: PackageManager) : RecyclerView.Adapter<CustomRecylerViewAdapter.RowHolder>() {



    class RowHolder(itemView:View) : RecyclerView.ViewHolder(itemView){

        fun bind(packageInfo: PackageInfo,packageManager: PackageManager){
            itemView.row_text.text = packageInfo.applicationInfo.packageName
            itemView.row_image.setImageDrawable(packageInfo.applicationInfo.loadIcon(packageManager))
        }
    }

    //recycleview'da her satır için geçerli olan xml'i bağlıyoruz.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_layout_silinebilir,parent,false)
        return RowHolder(view)
    }

    //değer ile görünümdeki elemanları eşliyoruz.
    override fun onBindViewHolder(holder: RowHolder, position: Int) {
        holder.bind(appsList.get(position),packageManager)
    }

    //kaç tane eleman olucak , recyler view'da
    override fun getItemCount(): Int {
        return appsList.count()
    }


}

 */