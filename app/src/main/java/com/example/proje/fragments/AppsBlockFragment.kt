package com.example.proje.fragments

import android.app.AlertDialog
import android.app.Service
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proje.util.DataModelSource
import com.example.proje.R
import com.example.proje.adapter.AppslockItemAdapter


class AppsBlockFragment : Fragment() {

    private lateinit var recylerViewAdapter : AppslockItemAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var dataModelSource: DataModelSource
    private lateinit var progressDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        var view :View =  inflater.inflate(R.layout.fragment_apps_block, container, false)


        //alert dialog
        /*
        val ad = AlertDialog.Builder(activity)
                .create()
        ad.setCancelable(false)
        ad.setTitle("BASLİK")
        ad.setMessage("MESAJ")
        ad.show()

         */


        //recyclerview
        recyclerView = view.findViewById(R.id.appsLockRecyclerView)
        val layoutManager : RecyclerView.LayoutManager = LinearLayoutManager(view.context)
        recyclerView.layoutManager = layoutManager

        /*
        dataModelSource = DataModelSource()
        if (arguments==null){
            dataModelSource.initList(justInstalledApps(), requireActivity().packageManager,requireActivity().getSharedPreferences("AllInstalledApps",0))
        }else{
            dataModelSource.initList(requireArguments().getStringArrayList("uygulamalar"), requireActivity().packageManager,requireActivity().getSharedPreferences("AllInstalledApps",0))
        }
         */
       // dataModelSource = requireArguments().getSerializable("uygulamalar") as DataModelSource
        dataModelSource = DataModelSource.getInstance()

        dataModelSource.appsList.forEach {
            println("applockta nasıl : ${it.name}")
        }


        //    recylerViewAdapter = DashBoardItemAdapter(getAllApps(),requireActivity().packageManager)
        recylerViewAdapter = AppslockItemAdapter(dataModelSource.forAppsLock(), requireActivity().packageManager , requireActivity().getSharedPreferences("apps_lock_status", Service.MODE_MULTI_PROCESS))
        recyclerView.adapter = recylerViewAdapter
        recyclerView.setHasFixedSize(true)

        return view
    }
    fun justInstalledApps():ArrayList<PackageInfo>{
        return (activity?.packageManager?.getInstalledPackages(0) as ArrayList<PackageInfo>).filter {
            !(((ApplicationInfo.FLAG_UPDATED_SYSTEM_APP or ApplicationInfo.FLAG_SYSTEM) and it.applicationInfo.flags) > 0)
        } as ArrayList<PackageInfo>
    }


}