package com.example.caregivee

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat

//Review Code For This Page [√√√√√]

class ActivityCaregivee : Activity()  {
    //Note: Adapted from a stopwatch app here: https://www.geeksforgeeks.org/how-to-create-a-stopwatch-app-using-android-studio/

    //Settings Variables
        private var mvSettings = ClassSettingsState(ArrayList(listOf(ClassLineItem(), ClassLineItem(), ClassLineItem())), 15)
        private val mvClassSerializableHandling = ClassSerializableHandling()

    //Permissions Checker
        private lateinit var mvClassPermissionsMode : ClassPermissionsMode

    //Strings 'n' Things
        private var mvNoName = ""

    //String-Related Resources
        private val mvClassFromHtml = ClassFromHtml() //<-- Object For Formatting HTML Tags In Strings

    //Initialize View Resources
        private lateinit var mvPleaseCheckInButton : Button
        private lateinit var mvLowVolumeTextView   : TextView
        private lateinit var mvPseudoToastView0    : TextView
        private lateinit var mvPseudoToastView1    : TextView
        private lateinit var mvPseudoToastView2    : TextView

    //Initialize BeginForegroundServiceRunnable
        private lateinit var mvBeginForegroundServiceRunnable : BeginForegroundServiceRunnable

    //Shared Preferences
        private lateinit var mvClassSharedPreferences : ClassSharedPreferences

    //Basic Variables
        private var mvBackButtonOnPhoneClicked = false
        private var mvDebugForegroundServiceAutoStart = false
        private var mvResumeCount = 0
        private var mvTrueWindowFocusChanged = false

    //Handlers & Runnables
        private val mvHandler = Handler(Looper.getMainLooper())
        private lateinit var mvRunnable0 : Runnable //<-- Needs To Be In The Same Scope As mvHandler: https://stackoverflow.com/questions/60761647/kotlin-how-to-pass-a-runnable-as-this-in-handler
        private lateinit var mvRunnable1 : Runnable //<-- Needs To Be In The Same Scope As mvHandler: https://stackoverflow.com/questions/60761647/kotlin-how-to-pass-a-runnable-as-this-in-handler
        private lateinit var mvRunnable2 : Runnable //<-- Needs To Be In The Same Scope As mvHandler: https://stackoverflow.com/questions/60761647/kotlin-how-to-pass-a-runnable-as-this-in-handler

    //onCreate
        override fun onCreate(mvSavedInstanceState: Bundle?) {
            super.onCreate(mvSavedInstanceState)
            setContentView(R.layout.activity_caregivee) //<-- Show activity_main.xml stuff. Note that any findViewById()'s need to go after this line of code: https://stackoverflow.com/questions/32062310/attempt-to-invoke-virtual-method-android-view-view-android-view-window-findview

            //Get Previous State Just In Case
                if (mvSavedInstanceState != null) {
                    mvSettings = mvClassSerializableHandling.mmGetSerializable(mvSavedInstanceState,"mvSettings", mvSettings, ClassSettingsState::class.java)
                }

            //Permissions Checker
                mvClassPermissionsMode = ClassPermissionsMode(applicationContext)

            //Strings 'n' Things
                mvNoName = applicationContext.getString(R.string.mtNoName)

            //Get Settings From Previous Screen
                if (intent.extras != null) {
                    mvSettings = mvClassSerializableHandling.mmGetSerializable(this.intent, "mvSettings", mvSettings, ClassSettingsState::class.java)
                    mvBackButtonOnPhoneClicked = intent.getBooleanExtra("mvBackButtonOnPhoneClicked", false)
                }

            //Get "Views"
                mvLowVolumeTextView    = findViewById(R.id.mxLowVolume)
                mvPleaseCheckInButton  = findViewById(R.id.mxPleaseCheckInButton)
                mvPseudoToastView0     = findViewById(R.id.mxPseudoToastBlock0)
                mvPseudoToastView1     = findViewById(R.id.mxPseudoToastBlock1)
                mvPseudoToastView2     = findViewById(R.id.mxPseudoToastBlock2)

            //Shared Preferences
                mvClassSharedPreferences = ClassSharedPreferences(applicationContext)

            //Debug Mode?
                mvDebugForegroundServiceAutoStart = mvClassSharedPreferences.mmGetSharedPreferencesInt("mvDebugFlag", 0) == 1
                /* ?!?!?!?!
                    //The Following Will Send An "SMS Delivered" Notification For Testing Purposes Upon "Long Press"
                        mvPleaseCheckInButton.setOnLongClickListener {
                            applicationContext.sendBroadcast(Intent("mbSmsDelivered"))
                            true //Return "true" Required (Source: https://stackoverflow.com/questions/49712663/how-to-properly-use-setonlongclicklistener-with-kotlin)
                        }
                    //For Special Debug Situations Where We Need To Test Out A Sudden Influx Of Lengthy Sounds
                        (findViewById(R.id.mxSettingsButton) as Button).setOnLongClickListener {
                            applicationContext.sendBroadcast(Intent("mbSoundBarrage"))
                            true //Return "true" Required (Source: https://stackoverflow.com/questions/49712663/how-to-properly-use-setonlongclicklistener-with-kotlin)
                        }
                */

            //Handlers & Runnables
                mvRunnable0 = Runnable {mvPseudoToastView0.visibility = View.INVISIBLE}
                mvRunnable1 = Runnable {mvPseudoToastView1.visibility = View.INVISIBLE}
                mvRunnable2 = Runnable {mvPseudoToastView2.visibility = View.INVISIBLE}

            //Make Sure We ALWAYS Start With The Default Button Display (Because onResume() Might Otherwise Pull From Shared Preferences And Display The Previous Button String/Color From Last Time)
                mmChangePleaseCheckInButtonProperties(ClassEnum.BUTTONCHECKIN.mvInt, ClassEnum.COLORYELLOW.mvInt)

            //Stop Any Running Services JUUUUUUUST In Case ActivityCaregivee Restarted
            //... And Notification Permissions Have Since Changed
                mmStopServiceOrAlternative()

            //Start Foreground Service
                mmStartForegroundServiceOrAlternative()

            //Add onClickListeners (More Futureproof Than Using android:onClick In The XML Files: https://stackoverflow.com/a/44184111/16118981)
                findViewById<Button>(R.id.mxSettingsButton).setOnClickListener { mmSettingsClick() }
                findViewById<Button>(R.id.mxPleaseCheckInButton).setOnClickListener { mmPleaseCheckInClick() }
         }

    //Save The State!
        public override fun onSaveInstanceState(mvSavedInstanceState: Bundle) {
            super.onSaveInstanceState(mvSavedInstanceState)
            mvSavedInstanceState.putSerializable("mvSettings", mvSettings)
        }

    //Start Foreground Service (And Send mvSettings To Said Service)
        private fun mmStartForegroundServiceOrAlternative() {
            //Only Start The Foreground Service (Containing BeginForegroundServiceRunnable) If We Have Permission To Post Notifications
            //Otherwise, Just Start The Runnable It Would Have Otherwise Commenced From Said Service: "mvBeginForegroundServiceRunnable"
                if (mvClassPermissionsMode.mmForegroundServiceMode())
                    ContextCompat.startForegroundService(applicationContext, Intent(applicationContext, BeginForegroundService::class.java).putExtra("mvSettings", mvSettings))
                else
                    mvBeginForegroundServiceRunnable = BeginForegroundServiceRunnable(mvSettings, applicationContext, false)
            //For Debug Purposes:
            //We Can Make It So The Phone's Physical Back Button Can Be Used To Start The Main ActivityCaregivee From ActivitySettings, And Likewise Immediately Trigger The Activity To Close To Begin A True Foreground Process
                if (mvDebugForegroundServiceAutoStart && mvBackButtonOnPhoneClicked) finishAndRemoveTask()
        }
    //Stop The Foreground Service
        private fun mmStopServiceOrAlternative() {
            //Stop The Foreground Service With An Intent
                stopService(Intent(applicationContext, BeginForegroundService::class.java))
            //Or If The Foreground Service Was Never Started, Begin The Process Of Shutting Down The Runnable
                if (this@ActivityCaregivee::mvBeginForegroundServiceRunnable.isInitialized) mvBeginForegroundServiceRunnable.onDestroy(true)
        }
    //"Settings" Button Clicked
        private fun mmSettingsClick() {
            //Proceed To Settings
                mmSwitchActivities(ActivitySettings::class.java)
        }
    //Restart Activity
        private fun mmRestartActivity() {
            //Proceed To Caregivee Again
                mmSwitchActivities(ActivityCaregivee::class.java)
        }
    //Switch Activities
        private fun mmSwitchActivities(mvClazz : Class<*>?)  {
            //So Switching Activities Doesn't Get Mistaken As A Defocus By onWindowFocusChange()
                mvTrueWindowFocusChanged = false
            //Stop The Countdown
                mmStopServiceOrAlternative()
            //Continue...
                startActivity(Intent(applicationContext, mvClazz).putExtra("mvSettings", mvSettings).putExtra("mvRedirectFromActivityCaregivee", true))
        }
    //"Please Check In..." Button Clicked
        private fun mmPleaseCheckInClick() {
            //Conditionals
                if (!this@ActivityCaregivee::mvBeginForegroundServiceRunnable.isInitialized && !mvClassPermissionsMode.mmForegroundServiceMode() || this@ActivityCaregivee::mvBeginForegroundServiceRunnable.isInitialized && mvClassPermissionsMode.mmForegroundServiceMode()) {
                    //If Permissions Have Changed (On A Sudden)!
                    //I.E We Started A Foreground Service But Have Since Lost Permission, Or We Didn't Start A Foreground Service And Have Since Gained Permission
                        //Let's Just Restart The Activity In Realtime :)
                        //(Instead Of Waiting For Next Iteration Of mvBeginForegroundServiceRunnable)
                            mmRestartActivity()
                }
                else {
                    //Reset Countdown
                        applicationContext.sendBroadcast(Intent("mbResetCountdown"))
                }
        }

    //Adjust "Views" From Foreground Service By Receiving Broadcasts From Said Service In THIS Activity
    //Broadcast Receiver Listed In Manifest? [No] (Registered Programmatically In The onStart() Block)
        private val mvBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(mvContext: Context, mvIntent: Intent) {
                if (mvIntent.action == "mbChangeButtonProperties") {
                    //Acquire "Extras" From Broadcast To Adjust The Button's Color And Text
                        mmChangePleaseCheckInButtonProperties(mvIntent.getIntExtra("mvCaregiveeStringCode", ClassEnum.BUTTONCHECKIN.mvInt), mvIntent.getIntExtra("mvCaregiveeColourCode", ClassEnum.COLORYELLOW.mvInt))
                }
                if (mvIntent.action == "mbVolumeLow") {
                    //Power Management (Realtime Volume Detection)
                    //Inform User The Volume Is Too Low!
                        mvLowVolumeTextView.visibility = View.VISIBLE
                }
                if (mvIntent.action == "mbVolumeOk") {
                    //Power Management (Realtime Volume Detection)
                        mvLowVolumeTextView.visibility = View.GONE
                }
                if (mvIntent.action == "mbPseudoToast") {
                    //Pseudo-Toast
                        mmPseudoToast(mvIntent.getStringExtra("mvToastMessage") ?: "", mvIntent.getIntExtra("mvWhichBlock", 2), mvIntent.getBooleanExtra("mvAppend", true), mvIntent.getIntExtra("mvWhichContact", -1), mvIntent.getBooleanExtra("mvDismiss", false), mvIntent.getIntExtra("mvTimeAddend", 0)) //<-- "?:" Safety Review: Should Be OK As We Theoretically Default To Not Making Any Noticeable Changes To The Pseudo-Toast. A Blank Pseudo-Toast Message Should THEORETICALLY Lead To The Pseudo-Toast Being Rendered Invisible, The "Reset Condition".
                }
                if (mvIntent.action == "mbRestartActivity") {
                    //Restart Activity
                        mmRestartActivity()
                }
            }
        }
    //Adjust The UI
        fun mmChangePleaseCheckInButtonProperties(mvCaregiveeStringCode : Int, mvCaregiveeColourCode : Int) {
            //Adjust The Button's Display String
                mvPleaseCheckInButton.text = when (mvCaregiveeStringCode)  {
                                                 ClassEnum.BUTTONCHECKIN.mvInt   -> getString(R.string.mtPleaseCheckIn)
                                                 ClassEnum.BUTTONCHECKEDIN.mvInt -> getString(R.string.mtCheckedIn)
                                                 else                            -> getString(R.string.mtPleaseRestart)
                                             }
            //... And Color...
                mvPleaseCheckInButton.setBackgroundColor(mvCaregiveeColourCode)
        }
    //Pseudo-Toaster (Launches A Pseudo-Toast)
    //(Note: Since Toast Fonts Can Be Small, Toast Duration Difficult To Control, And Toast Consistency Unpredictable When The App Is Defocused During Foreground Processes...
    ///We Use A Pseudo-Toast Instead)
        fun mmPseudoToast(mvToastMessage : String, mvWhichBlock : Int, mvAppend : Boolean, mvWhichContact : Int, mvDismiss : Boolean, mvTimeAddend : Int) {
            //Format Pseudo-Toast Message
                val mvToastMessageFormatted = if (mvWhichContact < 0) mvToastMessage else String.format(mvToastMessage, mvSettings.mvContacts[mvWhichContact].mvName.let {if (it == null || it == "") String.format(mvNoName, mvWhichContact+1) else it })
            //Which Pseudo-Toast Block Do We Want To Use? Top, Middle, Or Bottom?
            //Top: Currently Reserved For GPS Expiry Warnings
            //Mid: Currently Reserved For General SMS Capability Warnings (I.E. Airplane Mode, Siren Mode, Low Signal, or if all local contacts have been nullified for some reason... stuff that shouldn't interfere with each other)
            //Low: Currently Reserved For Countdown Reminders AND Status On Specific Attempts To Send An SMS
                when (mvWhichBlock) {
                    0    -> mvPseudoToastView0.also{mvHandler.removeCallbacks(mvRunnable0) /* <-- Delay Any Previously Pending Pseudo-Toast Dismissals Since We Have A New Incoming Pseudo-Toast */}.also{if (mvDismiss) mvHandler.postDelayed(mvRunnable0, 3500+mvTimeAddend.toLong())}
                    1    -> mvPseudoToastView1.also{mvHandler.removeCallbacks(mvRunnable1) /* <-- Delay Any Previously Pending Pseudo-Toast Dismissals Since We Have A New Incoming Pseudo-Toast */}.also{if (mvDismiss) mvHandler.postDelayed(mvRunnable1, 2500+mvTimeAddend.toLong())}
                    else -> mvPseudoToastView2.also{mvHandler.removeCallbacks(mvRunnable2) /* <-- Delay Any Previously Pending Pseudo-Toast Dismissals Since We Have A New Incoming Pseudo-Toast */}.also{if (mvDismiss) mvHandler.postDelayed(mvRunnable2, 1500+mvTimeAddend.toLong())}
                }.also{it.visibility = if (mvToastMessageFormatted != "") View.VISIBLE else View.INVISIBLE /* <-- Reset Condition */}.also{if (!mvAppend) it.text = mvClassFromHtml.mmFromHtml(mvToastMessageFormatted) else it.append("\n" + mvClassFromHtml.mmFromHtml(mvToastMessageFormatted))}
        }
    //If We've Been Using The Notification-Based "Secondary Caregivee Button"
    //(And We Didn't Dismiss The App From The Recents Menu)
    //Let's Make Sure We Update The UI If We Reopen The Activity From Said Recents Menu:
        override fun onResume() {
            super.onResume()

            //True Resume? (I.E. Is "mvResumeCount" Greater Than Or Equal To 1?)
            //Since onResume() Is Triggered When We First Enter The Activity
            //Increment A Flag (Right From The Get-Go) So We Know If We TRULY Navigated Away From The App And Back Again On Subsequent onResume()'s
                if (mvResumeCount >= 1 || intent.getBooleanExtra("mvRedirectFromActivityCaregivee", false)) { //<-- If We Simply Did An mmRestartActivity(), Then We Can Still Consider That A "True Resume" To Update The Activity's UI Accordingly
                    //Power Management (Realtime Volume Button Detection)
                    //Let's Say We Turn Off The Screen (Or Defocus), Turn Down The Volume, And Then Turn The Screen Back On.
                    //In Theory, The Following SHOULD Ensure That We Show The "Volume Too Low!" Overlay When We Return, By Querying The "Shared Preferences".
                    //I.E. Let's Update The "Volume Too Low!" Overlay Based On Any Changes We Made While The Screen Was Off Or While We Defocused.
                        if (mvClassSharedPreferences.mmGetSharedPreferencesInt("mvVolumeLow", 0) == 1)
                            applicationContext.sendBroadcast(Intent("mbVolumeLow"))
                        else
                            applicationContext.sendBroadcast(Intent("mbVolumeOk"))
                    //Update UI (Note: These Values Are Set In BeginForegroundServiceRunnable)
                        mmChangePleaseCheckInButtonProperties(mvClassSharedPreferences.mmGetSharedPreferencesInt("mvCaregiveeStringCode", ClassEnum.BUTTONRESTARTAPP.mvInt), mvClassSharedPreferences.mmGetSharedPreferencesInt("mvCaregiveeColourCode", ClassEnum.COLORYELLOW.mvInt))
                    //Update PseudoToasts (So They Don't Show Legacy Values From Before The Defocusing)
                        mvPseudoToastView0.also{it.text = ""
                                                it.visibility = View.INVISIBLE}
                        mvPseudoToastView1.also{it.text = ""
                                                it.visibility = View.INVISIBLE}
                        mvPseudoToastView2.also{it.text = ""
                                                it.visibility = View.INVISIBLE}
                        applicationContext.sendBroadcast(Intent("mbUpdatePseudoToasts"))
                        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P && mvResumeCount >= 2) {
                            //In "Pistachio Ice Cream", If We Turn Off The Screen And Turn It Back On Twice, The UI Freezes In Both The "Google APIs" And "Google Play" Emulators. Let's Reset The Activity Under Such Circumstances.
                            //Note: No Currently Known Way To Refresh The UI Or Check If The Screen Is Frozen:
                            /////// 1) Tried Forcing A Layout Refresh (https://stackoverflow.com/questions/5991968/how-to-force-an-entire-layout-view-refresh), To No Avail.
                            /////// 2) Tried Altering A TextView Via setText(), And Then Checking To See If It Didn't Change, To No Avail.
                                 applicationContext.sendBroadcast(Intent("mbRestartActivity"))
                        }
                }
                mvResumeCount++
        }
    //If The Activity Is Dismissed
        override fun onDestroy() {
            super.onDestroy()

                if (this@ActivityCaregivee::mvBeginForegroundServiceRunnable.isInitialized) {
                    //If The Foreground Service Was Never Started, Make Sure That Dismissing The Activity From The "Recents" Screen Will Close The App (As There Are Times When It Seems To Unintuitively Linger)
                        applicationContext.sendBroadcast(Intent("mbCloseApp"))
                }
                else {
                    //If The Foreground Service WAS Started, It's Probably A Wise Idea To Inform The User Via The Notification How To Close The App (Since All Activities Have Been Dismissed And We Normally Close The App From The UI)
                        applicationContext.sendBroadcast(Intent("mbActivitiesDismissed"))
                }

        }
    //Register Our Broadcast Receiver
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        override fun onStart() {
            super.onStart()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                //Register Broadcast Receiver For Newer APIs
                    registerReceiver(mvBroadcastReceiver, IntentFilter().also{it.addAction("mbChangeButtonProperties")
                                                                              it.addAction("mbVolumeLow")
                                                                              it.addAction("mbVolumeOk")
                                                                              it.addAction("mbPseudoToast")
                                                                              it.addAction("mbRestartActivity")}, Context.RECEIVER_EXPORTED)
            }
            else {
                //Register Broadcast Receiver For Older APIs
                    registerReceiver(mvBroadcastReceiver, IntentFilter().also{it.addAction("mbChangeButtonProperties")
                                                                              it.addAction("mbVolumeLow")
                                                                              it.addAction("mbVolumeOk")
                                                                              it.addAction("mbPseudoToast")
                                                                              it.addAction("mbRestartActivity")})
            }
        }
    //Unregister Our Broadcast Receiver
        override fun onStop() {
            super.onStop()

            //Unregister
                unregisterReceiver(mvBroadcastReceiver)
        }
    //On Back Button Pressed
        override fun onKeyDown(mvKeyCode: Int, mvEvent: KeyEvent?): Boolean {
            if (mvKeyCode == KeyEvent.KEYCODE_BACK) {
                //Let's Override All Back Button Presses To Activate Our Internal Back Button
                //Otherwise, Weird Cache-y Stuff Can Happen...
                    mmSwitchActivities(ActivitySettings::class.java)
            }
            return super.onKeyDown(mvKeyCode, mvEvent)
        }
    //Was The Window Defocused? (E.G. Was The Phone's "Recents Button" Pushed?)
    //Note: This Also Seems To Be Triggered By Turning Off The Power Button In Red Velvet+
        override fun onWindowFocusChanged(mvFocused: Boolean) {
            super.onWindowFocusChanged(mvFocused)
            if (!mvTrueWindowFocusChanged) {
                //Since onWindowFocusChanged() Is Triggered When Enter/Leave The Activity
                //Set A Flag (Right From The Get-Go) So We Know If We TRULY Defocused And Refocused On Subsequent onWindowFocusChanged()'s
                //(Note: We Reset It To false In mmSwitchActivities() So It Doesn't Trigger When We Switch Activities)
                    mvTrueWindowFocusChanged = true
            }
            else if (!mvFocused) {
                //Power Management (Realtime Recents Button Detection)
                //Note: This Also Seems To Be Triggered By Turning Off The Power Button In Red Velvet+
                    //Has The Window Been Defocused?
                        applicationContext.sendBroadcast(Intent("mbWindowDefocused"))
            }

            if (mvFocused) {
                //Power Management (Realtime Recents Button Detection)
                //Note: This Also Seems To Be Triggered By Turning Off The Power Button In Red Velvet+
                    //Is The Window In Focus?
                        applicationContext.sendBroadcast(Intent("mbWindowFocused"))
            }
         }
}