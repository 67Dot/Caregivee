package com.example.caregivee

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

//Review Code For This Page [√√√√√]

/*
   To-Do List When Ready To Publish:
   ================================
   1) Turn off automatic debug mode (if it's been activated in ActivityBehestRecyclerView like so: mvClassSharedPreferences.mmSetSharedPreferencesInt("mvDebugFlag", 0)
   2) Update Caregivee "mtVersion" number in strings.xml.
   3) Publish:
         Build > "Generate Signed Bundle / APK"
         Keystore "Desktop/mk_key_store.jks" with legacy password "Appl......".
*/

class ActivityBegin : Activity() {
    //Countdown Variables
        private var mvCountdown = ClassCountdown()

    //OnCreate
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_begin)

            //If We're Completely Restarting The App, Let's Remove The Foreground Service Just In Case
                stopService(Intent(applicationContext, BeginForegroundService::class.java))

            //Main "Countdown" Thread (When We Reach *0*, We Jump To "ActivityBehestImportant")
            //Purpose Is To Treat The First Activity Like A Splash Screen (Though Clickable)
                mvCountdown.mmCountdownClock<Int, Long>(2, 2000, ::mmScreenClick)

            //Show Version Number
                Toast.makeText(applicationContext, getString(R.string.mtVersion), Toast.LENGTH_SHORT).show()

            //Add onClickListeners (More Futureproof Than Using android:onClick In The XML Files: https://stackoverflow.com/a/44184111/16118981)
                findViewById<Button>(R.id.mxCaregiveeButton).setOnClickListener { mmScreenClick() }
        }
    //Save The State!
        public override fun onSaveInstanceState(savedInstanceState: Bundle) {
            super.onSaveInstanceState(savedInstanceState)
        }
    //Screen Click
        private fun mmScreenClick() {
            //Switch Activities
                mmSwitchActivities(ActivityBehestImportant::class.java)
        }
    //Switch Activities
        private fun mmSwitchActivities(mvClazz : Class<*>?) {
            //Stop The Countdown
                mvCountdown.mmStop()
            //Switch Activities
                startActivity(Intent(applicationContext, mvClazz))
        }
}