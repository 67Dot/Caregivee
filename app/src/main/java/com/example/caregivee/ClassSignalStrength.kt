@file:Suppress("DEPRECATION") //<-- Because Otherwise Some Deprecation Errors Appear, Despite The Fact That We Have "Responsive Design" Based On The Android API Below
package com.example.caregivee

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.CellInfo
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager

//Review Code For This Page [√√√√√]

@SuppressLint("MissingPermission") //<-- We Handle Permissions Checking Elsewhere
class ClassSignalStrength (private val mvContext : Context, private val mvClassSound : ClassSound) {
    //Signal Stuff
        private val mvTelephonyManager = mvContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        private var mvTechnology = ""
        private var mvBigNumberAsReferenceValue = 1000000
        private var mvSignalStrengthDbm = 0

    //Init
        init {
            //Get Signal Strength Data (Pre-Snowcone)
            //Source: https://stackoverflow.com/questions/72852032/network-signal-in-android-with-kotlin
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                    mvTelephonyManager.listen(object : PhoneStateListener() {
                        @Deprecated("Deprecated in Java")
                        override fun onSignalStrengthsChanged(mvSignalStrength: SignalStrength) {
                            super.onSignalStrengthsChanged(mvSignalStrength)
                            mvTechnology = if (mvSignalStrength.isGsm) "GSM" else "CDMA"
                            mvSignalStrengthDbm = if (mvTechnology == "GSM") {
                                2 * mvSignalStrength.gsmSignalStrength - 113 //Convert ASU To dBm
                            } else {
                                mvSignalStrength.cdmaDbm //Should Already Be Converted To dBm
                            }
                        }
                    }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
                }
    }
    fun mmGetSignalStrength(mvSendSms : Boolean, mvVerbal : Boolean) {
        //Get Signal Strength
        //(Source 1: https://stackoverflow.com/a/77285142/16118981)
        //(Source 2: https://stackoverflow.com/questions/61075598/what-is-proper-usage-of-requestcellinfoupdate/63370975#63370975)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                //Get Signal Strength Data (Snowcone+)
                //Note: The Following Requests An Up-To-Date Rundown Of The Cell Info
                    mvTelephonyManager.requestCellInfoUpdate(mvContext.mainExecutor, object : TelephonyManager.CellInfoCallback() {
                        override fun onCellInfo(mvCellInfoList: MutableList<CellInfo>) {
                            mmParseSignalStrength(mvCellInfoList, mvSendSms, mvVerbal)
                        }
                    })
            }
            else {
                //Alert User To The Status
                //(Explanation: Pre-Snowcone, The Listener Declared In The init Block Should Update This Info Automatically)
                    mmShowToUser(mvSendSms, mvVerbal)
            }
    }
    private fun mmParseSignalStrength(mvCellInfoList : MutableList<CellInfo>, mvSendSms : Boolean, mvVerbal : Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            //Find The Minimum Signal Strength Value Of All Returned Instances
                mvSignalStrengthDbm = mvBigNumberAsReferenceValue //<-- When Notifications are off AND the Phone's Screen is off, the signal strength is NOT acquired and it shows the default 1000000 strength for Snowcone+ (i.e. the "No Signal Information" state). It is unclear whether this permutation affects pre-Snowcone, as that's based on a listener (although there are hints that pre-Snowcone, it worked fine: https://stackoverflow.com/q/76807249/16118981). Anyway, this is not really much of a concern now that we try to alert the user to keep the screen on.
                for (mvCellInfo in mvCellInfoList) {
                    if (mvCellInfo.cellSignalStrength.dbm < mvSignalStrengthDbm) {
                        mvSignalStrengthDbm = mvCellInfo.cellSignalStrength.dbm
                        mvTechnology = mvCellInfo.javaClass.kotlin.toString()
                    }
                }
            //Alert User As To The Status
                mmShowToUser(mvSendSms, mvVerbal)
        }
    }
    private fun mmShowToUser(mvSendSms : Boolean, mvVerbal : Boolean) {
        //Tell User Explicitly If We Detect A Weak Signal
            val mvDebugAddend = 0 //<-- For Manually Testing "Low Signal" PseudoToasts If The Emulator Otherwise Shows Sufficient Signal Strength
            val mvSignalThresholdForSound = -133 //<-- This Was Tested In Dad's Room On A Samsung A12 Phone, And It Still Sent In As Low Signal Conditions As -133dB
            if (mvSignalStrengthDbm < mvSignalThresholdForSound+mvDebugAddend) mvClassSound.mmScheduleSound(R.raw.ma_low_signal, ClassEnum.CALLFWDLOWSIGNAL.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = mvSendSms && mvVerbal, mvImmediateCallFwd = true, mvSignalStrengthDbm).also{mvClassSound.mmScheduleSound(R.raw.ma_please_move_the_phone_to_a_different_location, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = mvSendSms && mvVerbal, mvImmediateCallFwd = false, 0)}
        //How's The Signal? (Source: https://www.surecallboosters.ca/post/cell-phone-signal-strength-everything-you-need-to-know#:~:text=%2D80%20to%20%2D89%20dBm%20is,lower%20is%20nearly%20no%20signal.)
            val mvSignalQuality= if (mvSignalStrengthDbm < -120) {
                "Very Low"
            } else if (mvSignalStrengthDbm <= -100) {
                "Low"
            } else if (mvSignalStrengthDbm <= -90) {
                "Normal"
            } else if (mvSignalStrengthDbm < 0) {
                "Above Average"
            } else {
                "No Signal Information" //<-- This Condition Can Be Simulated By Turning On "Airplane Mode" In The Emulator, But Only For Snowcone+ (Pre-Snowcone, Emulators Do Seem To Change To Signal Strength 0 IFF The Emulator Is Cold Booted In Airplane Mode. Incidentally, A dBm Of 0 Is Likewise Just About The Theoretical Asymptote Of Signal Strength, Hence Why We Consider Anything 0 Or Higher To Be The "No Information" State: https://www.reddit.com/r/HomeNetworking/comments/17kq3nz/the_highest_possible_rssi_for_lte/)
            }
            println("Signal: $mvSignalStrengthDbm ($mvSignalQuality) [$mvTechnology]")
    }
}