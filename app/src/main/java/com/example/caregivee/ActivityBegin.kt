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
   1) Turn off automatic debug mode (if it's been activated in ActivityBehestRecyclerView) like so: mvClassSharedPreferences.mmSetSharedPreferencesInt("mvDebugFlag", 0)
   2) Update Caregivee "mtVersion" number in strings.xml.
   3) Is it a BETA version? Set "mvBeta" to true in this activity.
   4) Publish:
         Build > "Generate Signed Bundle / APK"
         Keystore "Desktop/mk_key_store.jks" with legacy password "Appl......".
*/

class ActivityBegin : Activity() {
    //String-Related Resources
        private val mvClassFromHtml = ClassFromHtml() //<-- Object For Formatting HTML Tags In Strings

    //Countdown Variables
        private var mvCountdown = ClassCountdown()

    //Initialize View Resources
        private lateinit var mvCaregiveeButton : Button

    //Basic Variables
        private val mvBeta = true

    //OnCreate
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_begin)

            //If We're Completely Restarting The App, Let's Remove The Foreground Service Just In Case
                stopService(Intent(applicationContext, BeginForegroundService::class.java))

            //Get "Views"
                mvCaregiveeButton = findViewById(R.id.mxCaregiveeButton)
                mvCaregiveeButton.text = mvClassFromHtml.mmFromHtml(getString(R.string.mtAppNameWithEmojis, if (mvBeta) getString(R.string.mtBetaVersion) else ""))

            //Main "Countdown" Thread (When We Reach *0*, We Jump To "ActivityBehestImportant")
            //Purpose Is To Treat The First Activity Like A Splash Screen (Though Clickable)
                mvCountdown.mmCountdownClock<Int, Long>(2, 2000, ::mmScreenClick)

            //Show Version Number
                Toast.makeText(applicationContext, getString(R.string.mtVersion), Toast.LENGTH_SHORT).show()

            //Add onClickListeners (More Futureproof Than Using android:onClick In The XML Files: https://stackoverflow.com/a/44184111/16118981)
                mvCaregiveeButton.setOnClickListener { mmScreenClick() }
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