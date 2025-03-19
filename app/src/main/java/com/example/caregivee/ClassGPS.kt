package com.example.caregivee

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

//Review Code For This Page [√√√√√]

//Location Listener (Source: https://www.tutorialspoint.com/how-to-get-the-current-gps-location-programmatically-on-android-using-kotlin)
    @SuppressLint("MissingPermission") //<-- Because We Handle Permission Checks Elsewhere, This Prevents A *Check Permissions Explicitly* Error Below When We Call "requestLocationUpdates()"
    class ClassGPS(mvLocationManager : LocationManager, mvSmsRefreshRate : Int, mvContext : Context) : LocationListener {
        //GPS Stuff
            var mvGPSLocation: ClassGPSCoordinates = ClassGPSCoordinates(mvContext) //The Object Into Which We Insert Our GPS Coordinates

        //GPS SAMPLING RATE
        //Always Have A GPS Coordinate Ready For The Next SMS Dispatch...
        //... By Making Sure We Sample Location Data At TWICE The Rate We Dispatch SMS Messages).
        //===================================================================================
        //NOTE: This Shouldn't Provide Particularly Outdated Data Because It's NOT Reliant On mvSettings.mvRefreshRate...
        //..... Which Can Be A VERY Long Interval — But Instead On "mvSmsRefreshRate", Which Is Usually Only About 2 Minutes.
        //..... mvMinTime Halves That Value To About 1 Minute, Meaning...
        //..... Even A Quite Independent Caregivee Driving at 100kph (60mph) Would Only Ever Theoretically Mean Checking...
        //..... Somewhere In The Range Of <2km (1mi) Of Road From Their Last Known Location.
            private val mvMinTime = (mvSmsRefreshRate.toFloat()/2f).toLong()*60000

        //Request From The System The Ability To Check GPS Location Upon Initialization
            init {
                mvLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, mvMinTime, 5f, this)
            }
        //Event Listener That Updates The Local GPS Data When The User's ACTUAL GPS Location Changes
            override fun onLocationChanged(mvLocation : Location) {
                mvGPSLocation.mmUpdate(mvLocation.latitude.toString(), mvLocation.longitude.toString())
            }

        //Needed For Proper Functionality BEFORE "Red Velvet Cake", I.E. Before API 30 (Source: https://stackoverflow.com/questions/64638260/android-locationlistener-abstractmethoderror-on-onstatuschanged-and-onproviderd/)
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
    }