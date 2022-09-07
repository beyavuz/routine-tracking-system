package com.example.proje.util

import android.app.Activity
import android.app.AlertDialog
import android.text.Layout
import com.example.proje.R

class LoadingScreenInSelectedItem(activity: Activity) {

    private var myActivity : Activity = activity
    private lateinit var alertDialog: AlertDialog

    fun startLoadingScreen(){
        var alertDialogBuilder = AlertDialog.Builder(myActivity)

        var inflater = myActivity.layoutInflater
        alertDialogBuilder.setView(R.layout.loading_screen_selected_item)
        alertDialogBuilder.setCancelable(false)


        this.alertDialog = alertDialogBuilder.create()
        this.alertDialog.show()
    }

    fun dismissDialog(){
        this.alertDialog.dismiss()
    }


}