package com.example.proje.fragments

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proje.util.DataModelSource
import com.example.proje.R
import com.example.proje.activity.SelectedItemActivity
import com.example.proje.adapter.DashBoardItemAdapter
import com.example.proje.adapter.OnAppItemClickListener
import com.example.proje.model.ApplicationModel


class DashBoardFragment : Fragment() , OnAppItemClickListener {

        private lateinit var recylerViewAdapter : DashBoardItemAdapter
        private lateinit var recyclerView: RecyclerView
        private lateinit var dataModelSource: DataModelSource

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var view :View =  inflater.inflate(R.layout.fragment_dash_board, container, false)


        //recyclerview
        recyclerView = view.findViewById(R.id.recyclerView)
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(view.context)
        recyclerView.layoutManager = layoutManager

        dataModelSource = DataModelSource.getInstance()
   //     dataModelSource.initList(justInstalledApps(),requireActivity().packageManager,requireActivity().getSharedPreferences("AllInstalledApps",0))

    //    recylerViewAdapter = DashBoardItemAdapter(getAllApps(),requireActivity().packageManager)
     //   recylerViewAdapter = DashBoardItemAdapter(dataModelSource.appsList,requireActivity().packageManager,this)
        recylerViewAdapter = DashBoardItemAdapter(dataModelSource.forDashBoard(),requireActivity().packageManager,this)
        recyclerView.adapter = recylerViewAdapter
        recyclerView.setHasFixedSize(true)


        return view
    }

    fun getAllApps() : ArrayList<PackageInfo>{
        return activity?.packageManager?.getInstalledPackages(0) as ArrayList<PackageInfo>
    }

    fun justInstalledApps():ArrayList<PackageInfo>{
        return (activity?.packageManager?.getInstalledPackages(0) as ArrayList<PackageInfo>).filter {
            !(((ApplicationInfo.FLAG_UPDATED_SYSTEM_APP or ApplicationInfo.FLAG_SYSTEM) and it.applicationInfo.flags) > 0)
        } as ArrayList<PackageInfo>
    }

    fun sortingList(liste:List<PackageInfo>){

    }

    fun determineAllApps(){
        var installedApps = activity?.packageManager?.getInstalledPackages(0)
        installedApps?.let {
            installedApps.forEach {
                println("paket ismi : ${it.packageName}")
                println("application info : ${it.applicationInfo}")
                if(((ApplicationInfo.FLAG_UPDATED_SYSTEM_APP or ApplicationInfo.FLAG_SYSTEM) and it.applicationInfo.flags) > 0){
                    println("system app")
                }else{
                    println("system app değil")
                }
            }
        }
    }

    override fun onAppItemClickListener(item: ApplicationModel, position: Int) {
        //Toast.makeText(context,item.name,Toast.LENGTH_SHORT).show()
        val intent = Intent(activity,SelectedItemActivity::class.java)
        intent.putExtra("item_name",item.name)
        intent.putExtra("item_application_info",item.packageInfo.applicationInfo)
        intent.putExtra("item_package_name",item.packageName)
        startActivity(intent)
    }


}