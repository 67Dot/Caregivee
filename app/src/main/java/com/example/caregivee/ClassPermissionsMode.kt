package com.example.caregivee

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat

//Review Code For This Page [√√√√√]

//This Section Checks Which Permissions The User Granted The App So We Can Throttle Down Functionality Based On User Preferences (Source: https://stackoverflow.com/questions/33407250/checkselfpermission-method-is-not-working-in-targetsdkversion-22)
//NOTE: If I Understand Correctly, Before Android 6, The Only Way An App Could Even Work At All Is If *ALL* Permissions Were Granted At Installtime. So, We're Probably A-OK To Return A Simple Boolean Representing Full Permissions When The API Is Pre-Android 6.0 (Source: https://stackoverflow.com/a/34084609/16118981)
    class ClassPermissionsMode (val mvContext : Context) {
        //Shared Preferences
            private var mvClassSharedPreferences = ClassSharedPreferences(mvContext)

        //Check The Current Target SDK Version
        //Note: Check The build.gradle.kts File For This Info:
        //......We TARGET api 34 (Upside-Down Cake), yet our MINIMUM api is 21 (Lollipop).
            private fun mmTargetSdkVersion() : Int {
                return try {
                           //Fetch The Minimum API That This App Is Targeting
                               mvContext.packageManager.getPackageInfo(mvContext.packageName, 0).applicationInfo.targetSdkVersion
                       } catch (mvEx: PackageManager.NameNotFoundException) {
                           //If We Run Into An Error By This Point, mvTargetSdkVersion Should Take On The Default Value Of 0
                               mvEx.printStackTrace()
                               0
                       }
            }

        //No Contacts Mode
            fun mmNoContactsMode() : Boolean {
                //Are We In "No Contacts List Mode"?
                //(I.E. Access To The Phone's "Contacts List" Not Granted)
                    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mmTargetSdkVersion() >= Build.VERSION_CODES.M)
                                mmTargetSdkVersion().run{mvContext.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED}
                           else
                                false
            }
        //Anonymous Location Mode
            fun mmAnonymousLocationMode() : Boolean {
                //Are We In "Anonymous Location Mode"?
                //(I.E. GPS Permissions Not Granted)
                    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mmTargetSdkVersion() >= Build.VERSION_CODES.M)
                                mmTargetSdkVersion().run{mvContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && mvContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED}
                           else
                                false
            }
        //Siren Mode
            fun mmSirenMode() : Boolean {
                //Are We In "Siren Mode"?
                //(I.E. SMS Permissions Not Granted)
                    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mmTargetSdkVersion() >= Build.VERSION_CODES.M)
                                mmTargetSdkVersion().run{mvContext.checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT == 26 && mvContext.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED}
                           else
                                false
            }
        //Airplane Mode
            fun mmAirplaneMode(): Boolean {
                //Is "Airplane Mode" on in the phone? (Source: https://stackoverflow.com/questions/4319212/how-can-one-detect-airplane-mode-on-android)
                    return Settings.System.getInt(mvContext.contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0
            }
        //Foreground Service Mode
            fun mmForegroundServiceMode() : Boolean {
                //Are We In "Foreground Service Mode"? (I.E. Is "Notification Posting" Granted So We Can Use A "Foreground Process" That Runs Even When The App Is Closed?)
                //(NOTE: In SDK 34+ ("Upside-Down Cake"), Requiring FOREGROUND_SERVICE_LOCATION In The Manifest (As We Do) Also Requires Either ACCESS_COARSE_LOCATION Or ACCESS_FINE_LOCATION To Be true...
                //...... Therefore, "Anonymous Location Mode" Will NOT Theoretically Be Compatible With The Foreground Service If The User Decides Not To Allow GPS Location Permissions: (Source: https://stackoverflow.com/a/77642329/16118981)
                    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mmTargetSdkVersion() >= Build.VERSION_CODES.M)
                                NotificationManagerCompat.from(mvContext).areNotificationsEnabled() && (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) !mmAnonymousLocationMode() else true)
                           else
                                true
            }
        //Coarse Location Mode
            fun mmCoarseLocationMode() : Boolean {
                //Are We In "Coarse Location Mode"?
                //(I.E. GPS Permissions Only Allowed For Approximate Location Via App Settings)
                    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mmTargetSdkVersion() >= Build.VERSION_CODES.M)
                                mmTargetSdkVersion().run{mvContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && mvContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED}
                           else
                                false
            }
        //Fine Location Mode
            fun mmFineLocationMode() : Boolean {
                //Are We In "Fine Location Mode"?
                    return !mmAnonymousLocationMode() && !mmCoarseLocationMode()
            }
        //Emergency SMS Mode
            fun mmEmergencySmsMode() : Boolean  {
                //Are We In "Emergency SMS Mode"?
                //(I.E. Both GPS Permissions And SMS Permissions Granted)
                    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && mmTargetSdkVersion() >= Build.VERSION_CODES.M)
                                mmTargetSdkVersion().run{(mvContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || mvContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) && mvContext.checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED}
                           else
                                true
            }
        //Have GPS Permissions Expired?
        //(Note: We Use Shared Preferences For The GPS Permissions Flag Because If We Turn Off GPS Location Permissions Extra-app-ularly, The App's "Process" Has A Tendency To Restart. So, Our GPS Permissions Flag Loses State And We Get Inaccurate Information.)
            fun mmGpsPermissionsExpired() : Int {
                //Get Flag (Default To "GPSNEVERSTARTED", The State That Represents The User Having Never Granted GPS Location Permissions In The First Place)
                    val mvGpsPermissionsExpiredFlag = mvClassSharedPreferences.mmGetSharedPreferencesInt("mvGpsPermissionsExpiredFlag", ClassEnum.GPSNEVERSTARTED.mvInt)
                //Have Our GPS Permissions Expired?
                //I.E. They WERE Active, But Now They're Not?
                    val mvNewValue = if (mvGpsPermissionsExpiredFlag == ClassEnum.GPSACTIVE.mvInt && mmAnonymousLocationMode())
                                        ClassEnum.GPSEXPIRED.mvInt
                                     else if (mvGpsPermissionsExpiredFlag != ClassEnum.GPSACTIVE.mvInt && !mmAnonymousLocationMode())
                                        ClassEnum.GPSACTIVE.mvInt
                                     else
                                        mvGpsPermissionsExpiredFlag
                //Save Updated Flag As Shared Preferences
                    if (mvGpsPermissionsExpiredFlag != mvNewValue)
                        mvClassSharedPreferences.mmSetSharedPreferencesInt("mvGpsPermissionsExpiredFlag", mvNewValue)
                //Return Value
                    return mvNewValue
            }
    }
