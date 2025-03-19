package com.example.caregivee

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.TextView

//Review Code For This Page [√√√√√]

class ActivityBehestImportant : Activity() {
    //Countdown Variables
        private var mvCountdown = ClassCountdown()

    //Sound
        private lateinit var mvClassSound : ClassSound

    //Basic Variables
        private val mvCountdownFrom = 8
        private var mvBehestState0 = false
        private var mvBehestState1 = false
        private var mvBehestState2 = false

    //Initialize "View" Resources
        private lateinit var mvSalientWarning1 : TextView
        private lateinit var mvSalientWarning2 : TextView

    //Device Plugged In?
        private val mvClassPluggedIn = ClassPluggedIn()

    //OnCreate
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_behest_important)

            //Sound
                mvClassSound = ClassSound(applicationContext)

            //Main "Countdown" Thread (When We Reach *0*, We Jump To "ActivityBehestRecyclerView")
            //Purpose Is To Treat EVEN THE SECOND Activity Like An (Informative) Splash Screen (Though Clickable)
                mvCountdown.mmCountdownClock<Int, Long>(mvCountdownFrom, 1000, ::mmScreenClick)

            //Get "Views"
                mvSalientWarning1 = findViewById(R.id.mxSalientWarning1)
                mvSalientWarning2 = findViewById(R.id.mxSalientWarning2)

            //Add onClickListeners (More Futureproof Than Using android:onClick In The XML Files: https://stackoverflow.com/a/44184111/16118981)
                mvSalientWarning1.setOnClickListener { mmScreenClick() }
                mvSalientWarning2.setOnClickListener { mmScreenClick() }
        }
    //Save The State!
        public override fun onSaveInstanceState(savedInstanceState: Bundle) {
            super.onSaveInstanceState(savedInstanceState)
        }
    //Screen Click
        private fun mmScreenClick() {
            //Switch Activities
                mmSwitchActivities(ActivityBehestRecyclerView::class.java)
        }
    //Switch Activities
        private fun mmSwitchActivities(mvClazz : Class<*>?) {
            if (!mvBehestState0 && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                //Older Version?
                    mvSalientWarning1.text = getString(R.string.mtSalientSuggestionAboutVersions1)
                    mvSalientWarning2.text = getString(R.string.mtSalientSuggestionAboutVersions2)
                    mvCountdown.mvCountdown = mvCountdownFrom
                    mvBehestState0 = true
            }
            else if (!mvBehestState1 && !mvClassPluggedIn.mvPluggedIn(applicationContext)) {
                //Power Management (Realtime AC Power Detection)
                    mvSalientWarning1.text = getString(R.string.mtSalientSuggestionAboutPlugs1)
                    mvSalientWarning2.text = ""
                    mvCountdown.mvCountdown = 1000000
                    mvBehestState1 = true
                    mvClassSound.mmSchedulePrioritySound(ClassEnum.PRIORITYUNPLUGGED.mvInt)
            }
            else if (!mvBehestState2 && !mvClassPluggedIn.mvPluggedIn(applicationContext)) {
                //Power Management (Realtime AC Power Detection)
                    mvSalientWarning1.text = getString(R.string.mtSalientSuggestionAboutPlugs1)
                    mvSalientWarning2.text = getString(R.string.mtSalientSuggestionAboutPlugs2)
                    mvCountdown.mvCountdown = 1000000
                    mvBehestState2 = true
            }
            else {
                //Stop The Countdown
                    mvCountdown.mmStop()
                //Switch Activities
                    startActivity(Intent(applicationContext, mvClazz))
            }
        }
}