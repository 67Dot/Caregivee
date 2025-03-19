package com.example.caregivee

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

//Review Code For This Page [√√√√√]

//Broadcast Receiver Listed In Manifest? [Yes] (Registered In Manifest... This One May Need To Be Registered In The Manifest As It's Its Own Separate Class File)
class BeginForegroundServiceTransmissionReceiver : BroadcastReceiver() {
    //Permissions
        private lateinit var mvClassPermissionsMode : ClassPermissionsMode

    //Broadcast Receiver
        override fun onReceive(mvContext: Context, mvIntent: Intent) {
            //Permissions
                if (!this::mvClassPermissionsMode.isInitialized) mvClassPermissionsMode = ClassPermissionsMode(mvContext)
            //What Was The Associated Action With This Intent?
                val mvAction = mvIntent.action
            //Clicked Or Cancelled?
                if (mvAction == "mbNotificationClicked") {
                    //Treat The Notification Button As A Secondary Caregivee Button
                        if (mvClassPermissionsMode.mmForegroundServiceMode()) mvContext.sendBroadcast(Intent("mbResetCountdown"))
                } else if (mvAction == "mbNotificationCancelled") {
                    //Did The User Swipe The Notification To The Side Or Click It? (I.E. Did They Dismiss It?)
                    //If So, Let's Assume They Want To Exit The App
                    //(Note: This Will Only Fully Exit IF The Activity Was Also Swiped Up And Dismissed In The Task Manager And We Were Running A "True" Foreground Process; Otherwise, It Just Stops The Process And Resets Upon Refocus)
                        if (mvClassPermissionsMode.mmForegroundServiceMode()) mvContext.sendBroadcast(Intent("mbCloseApp"))
                }
        }

}