package com.example.proje.model

import android.content.pm.PackageInfo
import java.io.Serializable

data class ApplicationModel(
        var uid:Int,
        var name:String,
        var packageName:String,
        var firstInstallTime : Long,
        var lastUpdatedTime : Long,
        var kullanimSuresi:Long,
        var packageInfo: PackageInfo,
        var status:Boolean,   //STATUS_WITH_LOCK ,  IN LOCK_FRAGMENT
        var statusWithUsage: Boolean
) :Serializable{
}