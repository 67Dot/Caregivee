package com.example.caregivee

import android.content.Intent
import android.os.Build
import android.os.Bundle
import java.io.Serializable

//Review Code For This Page [√√√√√]

class ClassSerializableHandling {
    //Custom Alternative To getSerializable() To Fix A Deprecated Call...
    //We Use This In Situations Like (For Example) Where We Want To Fetch Intent "Extras"...
    //(Source: https://stackoverflow.com/questions/72571804/getserializableextra-and-getparcelableextra-are-deprecated-what-is-the-alternat)
        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        fun <T : Serializable> mmGetSerializable(mvBundle: Bundle, mvName: String, mvSettings: ClassSettingsState, mvClazz: Class<T>): T
        {
            //For Save States
                val mvReturn : T? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        mvBundle.getSerializable(mvName, mvClazz)
                                    } else {
                                        mvBundle.getSerializable(mvName) as T? //<-- "?:" Safety Review: Added because we received the following error on Android Oreo: "null cannot be cast to non-null type T of com.example.caregivee.ClassSerializableHandling.mmGetSerializable", Should Be OK Since We Return mvSettings If Otherwise null In The Below Return
                                    }
            //Return
                return mvReturn ?: (mvSettings as T) //<-- "?:" Safety Review: Should Be OK As We Default To The Previous "mvSettings" If The Return Value Is null
        }
        @Suppress("UNCHECKED_CAST", "DEPRECATION")
        fun <T : Serializable> mmGetSerializable(mvIntent: Intent, mvName: String, mvSettings: ClassSettingsState, mvClazz: Class<T>): T
        {
            //For Intent "Extras"
                val mvReturn : T? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        mvIntent.getSerializableExtra(mvName, mvClazz)
                                    } else {
                                        mvIntent.getSerializableExtra(mvName) as T? //<-- "?:" Safety Review: Added because we received the following error on Android Oreo: "null cannot be cast to non-null type T of com.example.caregivee.ClassSerializableHandling.mmGetSerializable", Should Be OK Since We Return mvSettings If Otherwise null In The Below Return
                }
            //Return
                return mvReturn ?: (mvSettings as T) //<-- "?:" Safety Review: Should Be OK As We Default To The Previous "mvSettings" If The Return Value Is null
        }
}