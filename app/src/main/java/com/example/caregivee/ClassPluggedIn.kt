package com.example.caregivee

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

//Review Code For This Page [√√√√√]

class ClassPluggedIn {
    //Power Management (Realtime AC Power Detection)
    //Is The Device Plugged In?
    //Note: In The "Quince Tart" Emulator, You Have To "Cold Boot" Entirely To Get The Emulator To Detect Any Changes To "Plugged In" Status
        fun mvPluggedIn(mvContext: Context): Boolean {
            val mvPlugged = mvContext.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) //"?." And "null" Safety Review: Since The Next Line Merely Checks An "Int?" Against Certain Values, We Should Probably Be OK In The null Case. Also, Probably NO Need To Unregister This Receiver As The Receiver Parameter Is (Contrary To Normal Use Of .registerReceiver()) null.
            return mvPlugged == BatteryManager.BATTERY_PLUGGED_AC || mvPlugged == BatteryManager.BATTERY_PLUGGED_USB || mvPlugged == BatteryManager.BATTERY_PLUGGED_WIRELESS
        }
}