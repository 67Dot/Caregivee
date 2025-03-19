package com.example.caregivee

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlin.system.exitProcess

//Review Code For This Page [√√√√√]

class ActivityTimeToExitApp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Get The Exit Screen
            setContentView(R.layout.activity_time_to_exit_app)

        //Immediately Exit App
            finishAndRemoveTask()

        //Exit Process
            exitProcess(-1)
    }
}