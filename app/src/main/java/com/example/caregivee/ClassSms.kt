package com.example.caregivee

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager

//Review Code For This Page [√√√√√]

class ClassSms(val mvContext : Context) {
    //Dispatch An SMS And Check Delivery
        @Suppress("DEPRECATION") //<-- To Suppress The Deprecation Warning For SmsManager.getDefault(), Which Is Already Handled By A Conditional That Takes The Android API Into Account
        fun mmSendSms(mvSmsMessage : String, mvNumber : String, mvContactIndex : Int) : Boolean {
            //Note: Because The Following Section is wrapped in a try/catch...
            //... exceptions might be trickier to spot in the "Logcat" since they aren't highlighted red:
                try {
                    //Fetch The SMS Manager
                        val mvSmsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { //Originally This Was ">= Build.VERSION_CODES.M", But It Seems To Throw An Error Before Snowcone (Which Is When SmsManager.getDefault() Was Deprecated).
                                                            mvContext.getSystemService(SmsManager::class.java)
                                                        } else {
                                                            SmsManager.getDefault() //<-- This Was Specifically Deprecated From Snowcone Forward (Source: https://developer.android.com/reference/android/telephony/SmsManager.html#getDefault())
                                                        }

                    //Set These Broadcasters (Through PendingIntents Since These Broadcasts Are Extra-app-ular) Up So They Might Send A Broadcast Back When The SMS Is Either Sent Or Delivered (Note That It Doesn't Seem To Send The Delivered Broadcast Pre-Snowcone, In The Emulators At Least)
                        val mvForwardedIntent = PendingIntent.getBroadcast(mvContext, 100, Intent("mbSmsSent$mvContactIndex"), PendingIntent.FLAG_IMMUTABLE) //<-- PendingIntent Explained: https://developer.android.com/reference/android/app/PendingIntent
                        val mvDeliveredIntent = PendingIntent.getBroadcast(mvContext, 200, Intent("mbSmsDelivered" /* + "?!?!?!?!" <-- For Debugging Delivery Delays */), PendingIntent.FLAG_IMMUTABLE) //<-- PendingIntent Explained: https://developer.android.com/reference/android/app/PendingIntent

                    //Actually Send The SMS
                        mvContext.sendBroadcast(Intent("mbSmsAttempted")) //<-- So We Can Signal A Pseudo-Toast BEFORE We sendTextMessage() To Ensure The "SMS Attempted" (mvSmsAttempted) Message Shows Up Before An "SMS Failed" (mvSmsNotForwarded) Message Chronologically; However, This Means That — Should We Get An Exception In This "try/catch" — We'll Show BOTH mvSmsAttempted And mvSmsGotException In Succession
                        mvSmsManager.sendTextMessage(mvNumber.filter{it.isDigit()} /* <-- Remove Dashes And Parentheses And Stuff */, null, mvSmsMessage, mvForwardedIntent, mvDeliveredIntent)

                    //We At Least Succeeded In ATTEMPTING The SMS
                    //The Broadcasters (PendingIntents) Included With "sendTextMessage()" Should Tell Us (Triggering Psuedo-Toasts Elsewhere) If It Was Actually Dispatched And/Or Delivered
                        return true
                } catch (mvEx: Exception) {
                    //Attempt Failed
                        mvContext.sendBroadcast(Intent("mbSmsGotException$mvContactIndex")) //<-- Signal A Toast In BeginForegroundServiceRunnable
                        mvEx.printStackTrace()
                        return false
                }
        }
}