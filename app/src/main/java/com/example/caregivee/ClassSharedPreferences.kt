package com.example.caregivee

import android.app.Activity
import android.content.Context

//Review Code For This Page [√√√√√]

class ClassSharedPreferences (val mvContext : Context) {
    fun mmGetSharedPreferencesInt(mvKey : String, mvDefault : Int) : Int {
        //Fetch Any Flags (Like mvGpsPermissionsExpiredFlag) From Shared Preferences
            return mvContext.getSharedPreferences("mvSharedFlags", Activity.MODE_PRIVATE).getInt(mvKey, mvDefault)
    }
    fun mmSetSharedPreferencesInt(mvKey : String, mvValue : Int) {
        //Save Any Flags (Like mvGpsPermissionsExpiredFlag) To Shared Preferences So They Don't Get Reset Upon Extra-App-ular Permissions Changes
        //E.G. The Application's "Process" Ends And Restarts If Location Permissions Are Turned Off Extra-App-ularly, Meaning Any Pending Flags Could Lose State!
        //(Note: Using Singleton-based or Application()-based global variables is insufficient, because they also appear to lose state if the process is restarted.)
            mvContext.getSharedPreferences("mvSharedFlags", Activity.MODE_PRIVATE).edit().putInt(mvKey, mvValue).apply()
    }
}