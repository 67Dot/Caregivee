package com.example.caregivee

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

//Review Code For This Page [√√√√√]

class ActivitySettings : Activity() {
    //Settings Variables
        private var mvSettings = ClassSettingsState(ArrayList(listOf(ClassLineItem(), ClassLineItem(), ClassLineItem())), 15)
        private val mvClassSerializableHandling = ClassSerializableHandling()

    //Permissions Checker
        private lateinit var mvClassPermissionsMode : ClassPermissionsMode

    //File Manager
        private lateinit var mvClassFileManager : ClassFileManager
        private var mvJustStartedApp = false //<-- Note: Primitives Need A Default Value

    //Strings 'n' Things
        private lateinit var mvAllContactSlotsRecommendedAndCurrent : String
        private lateinit var mvAtLeastOneNumberNeeded : String
        private lateinit var mvNoContactsInPhone : String

    //String-Related Resources
        private var mvAllContactSlotsRecommendedNotice = 0 //<-- To Limit The Number Of Times We Show The Notice
        private val mvClassFromHtml = ClassFromHtml() //<-- Object For Formatting HTML Tags In Strings

    //Countdown Variables
        private var mvCountdown = ClassCountdown()

    //Sound
        private lateinit var mvClassSound : ClassSound

    //Phone Stuff
        private lateinit var mvClassContactsUpToDate : ClassContactsUpToDate

    //Basic Variables
        private var mvBackButtonOnPhoneClicked = false
        private val mvCountdownFrom = 2
        private val mvRefresh = 15000L

    //Contact List Buttons Container
        private lateinit var mvContactButtons : MutableList<Button>

    //Status Window
    //(i.e. The Window That Indicates Which Mode We're Currently In On The Bottom Of The "Settings" Screen)
        private lateinit var mvStatusWindow : TextView

    //FrameLayout Overlaid "Restart Activity" ("Click To Restart Page" View)
    //(i.e. Alternative To Dialogs To Prevent "Window Leak" Errors)
        private lateinit var mvRestartActivity : TextView
        private var mvSwitchingActivities = false

    //Which Contact Clicked?
        private var mvWhichContact = 0 //<-- Note: Primitives Need A Default Value

    //OnCreate
        override fun onCreate(mvSavedInstanceState: Bundle?) {
            super.onCreate(mvSavedInstanceState)
            setContentView(R.layout.activity_settings)

            //Give The System's Garbage Collector A Hint To Remove Unused Objects Here
                System.gc()

            //Get Previous State Just In Case
                if (mvSavedInstanceState != null) {
                    mvSettings = mvClassSerializableHandling.mmGetSerializable(mvSavedInstanceState,"mvSettings", mvSettings, ClassSettingsState::class.java)
                }

            //Permissions Checker
                mvClassPermissionsMode = ClassPermissionsMode(applicationContext)

            //File Manager
                mvClassFileManager = ClassFileManager(applicationContext)

            //Strings 'n' Things
                mvAllContactSlotsRecommendedAndCurrent = getString(R.string.mtAllContactSlotsRecommendedAndCurrent)
                mvAtLeastOneNumberNeeded = getString(R.string.mtAtLeastOneNumberNeeded)
                mvNoContactsInPhone = getString(R.string.mtNoContactsInPhone)

            //Sound
                mvClassSound = ClassSound(applicationContext)

            //Phone Stuff
                mvClassContactsUpToDate = ClassContactsUpToDate(applicationContext)

            //Get "mvSettings" From Previous Screen
            //Also Find Out If We Only JUST Started The App!
                if (intent.extras != null) {
                    mvSettings = mvClassSerializableHandling.mmGetSerializable(this.intent, "mvSettings", mvSettings, ClassSettingsState::class.java)
                    mvJustStartedApp = this.intent.getBooleanExtra("mvJustStartedApp", false)
                }

            //Load "mvSettings" From Disk (I.E. Read File)
            //But Only If We JUUUUUUUST Started The App
                if (mvJustStartedApp) mvSettings = mvClassFileManager.mmReadSerializable("mvSettings", mvSettings)

            //FrameLayout Overlaid "Restart Activity" ("Click To Restart Page" View)
            //(i.e. Alternative To "Dialogs" To Prevent "Window Leak" Errors)
                mvRestartActivity = findViewById(R.id.mxRestartActivity)

            //Status Window
            //(Note: Set The "Long Press" Listener For The "Status Window")
                mvStatusWindow = findViewById(R.id.mxStatusWindow)
                mvStatusWindow.setOnLongClickListener {
                    mmExtraAppularPermissions()
                    true//Return "true" Required (Source: https://stackoverflow.com/questions/49712663/how-to-properly-use-setonlongclicklistener-with-kotlin)
                }
            //Announce Important Permissions Information
                val mvAirplaneMode       = mvClassPermissionsMode.mmAirplaneMode()
                val mvSirenMode          = mvClassPermissionsMode.mmSirenMode()
                if (mvSirenMode)           mvClassSound.mmScheduleSound(R.raw.ma_siren_mode_on,    ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = true, mvImmediateCallFwd = false, 0).also{mvClassSound.mmScheduleSound(R.raw.ma_sms_not_being_sent, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = true, mvImmediateCallFwd = false, 0)}
                else if (mvAirplaneMode)   mvClassSound.mmScheduleSound(R.raw.ma_airplane_mode_on, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = true, mvImmediateCallFwd = false, 0).also{mvClassSound.mmScheduleSound(R.raw.ma_sms_not_being_sent, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = true, mvImmediateCallFwd = false, 0)} //<-- We Don't Care About "Airplane Mode" Being On UNLESS We're Actually Allowed To Send SMS Messages

            //Display The Current MODE In The "Status Window"
                val mvGPSStatus = if (mvClassPermissionsMode.mmGpsPermissionsExpired() == ClassEnum.GPSEXPIRED.mvInt) getString(R.string.mtGpsExpiredLongForm) else if (mvClassPermissionsMode.mmCoarseLocationMode()) getString(R.string.mtCoarseLocationMode) else ""
                val mvLongPress = getString(R.string.mtModeLongPress) //<-- Tell User They Can "Long Press" To Enter Phone Settings To Change Permissions If Desired
                var mvMode = ""
                if (mvSirenMode) {
                    //Are We In "Siren Mode"?
                        mvMode = getString(R.string.mtModeSiren, mvGPSStatus, mvLongPress)
                }
                else if (mvAirplaneMode)
                {
                    //Are We In "Airplane Mode"?
                        mvMode = getString(R.string.mtModeAirplane, mvGPSStatus, mvLongPress)
                }
                else if (mvClassPermissionsMode.mmAnonymousLocationMode()) {
                    //Are We In "Anonymous Location Mode"?
                        mvMode = getString(R.string.mtModeAnonLocationMode, mvGPSStatus, mvLongPress)
                }
                else if (mvClassPermissionsMode.mmEmergencySmsMode()) {
                    //Are We In "Emergency SMS Mode"?
                        mvMode = getString(R.string.mtModeEmergencySms, mvGPSStatus, mvLongPress)
                }
                mvStatusWindow.text = mvClassFromHtml.mmFromHtml(mvMode)

            //Text "Spinner" (Drop-Down Menu) For Selecting Desired "mvCountdownFrom" Value
                val mvCountdownFromOptions = resources.getStringArray(R.array.mtCountdownFromOptions) //<-- Get Array Of Values From "strings.xml" Representing The Different Permitted Intervals Between Check-Ins
                val mvSpinner = findViewById<Spinner>(R.id.mxCountdownFrom)
                mvSpinner.adapter = ArrayAdapter(applicationContext, R.layout.spinner_refresh_rate, mvCountdownFromOptions) //"ArrayAdapter" Gives An Array To An "AdapterView", Such As A "Spinner" — Note: "An AdapterView is a view whose children are determined by an Adapter" (Source: https://developer.android.com/reference/kotlin/android/widget/AdapterView)
                for (mvJ in mvCountdownFromOptions.indices) {
                    if (mvCountdownFromOptions[mvJ].toInt() == mvSettings.mvRefreshRate) mvSpinner.setSelection(mvJ) //<-- Set The Default Setting To The Value We Selected Last Time
                }
                mvSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(mvParent: AdapterView<*>, mvView: View?, mvPosition: Int, mvId: Long) { //<-- "Type?" Safety Review: This Built-In Method Returns An Error When You Try To Use A Non-Nullable "View" Instead Of "View?", Hence The "?" :)
                        //Set A New Refresh Rate If We Change Our Selection
                            mvSettings.mvRefreshRate = mvCountdownFromOptions[mvPosition].toInt()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        //Not Currently Needed, But This Override Is Required To Prevent An Error
                    }
                }

            //Populate The "Contacts" If Available
                mvContactButtons = ArrayList(listOf(findViewById(R.id.mxContact0), findViewById(R.id.mxContact1), findViewById(R.id.mxContact2)))
                val mvContactPrefix = getString(R.string.mtContactPrefix) //<-- Usually Something Like A Phone Emoji, Just For Visual Flair
                for (mvIt in 0 until mvSettings.mvContacts.size) {
                    //Populate The Three Contacts
                        if (mvSettings.mvContacts[mvIt].mvMobile != null) mvContactButtons[mvIt].text = "".let{"${mvContactPrefix}${mvSettings.mvContacts[mvIt].mvName} ${mvSettings.mvContacts[mvIt].mvMobile}"}
                }

            //Check For Unpopulated Or Outdated Contacts And Make Them Red
            //(Note: If We're In "Siren Mode", Let's Just Gray Out ALL The Contacts)
                mvClassContactsUpToDate.mmClassContactsUpToDate<Unit>(mvSettings, mvContactButtons, !mvClassPermissionsMode.mmNoContactsMode(), mvSirenMode, 0)

            //Main "Countdown" Thread
            //When We Reach 0, We Return To Main Caregivee Activity!
            //(Note: Purpose Is Just In Case Of A Misclick During A Situation Where Someone Might Need Assistance, It Would Be Better To Get Them Back To The Main SMS-Sending App)
                mvCountdown.mmCountdownClock(mvCountdownFrom, mvRefresh, ::mmBackButtonClick)

            //Add onClickListeners (More Futureproof Than Using android:onClick In The XML Files: https://stackoverflow.com/a/44184111/16118981)
                for (mvIt in 0 until mvSettings.mvContacts.size)
                    mvContactButtons[mvIt].setOnClickListener { mmContactNumberClick(mvIt) }
                findViewById<Button>(R.id.mxBackButton).setOnClickListener{ mmBackButtonClick() }
                mvStatusWindow.setOnClickListener{ mmBackButtonClick() }
                mvRestartActivity.setOnClickListener{ mmRestartActivity() }

        }

    //Save The State!
        public override fun onSaveInstanceState(mvSavedInstanceState: Bundle) {
            super.onSaveInstanceState(mvSavedInstanceState)
            mvSavedInstanceState.putSerializable("mvSettings", mvSettings)
        }

    //"Back" Button Clicked
        private fun mmBackButtonClick() {
            //Check For Unpopulated Or Outdated Contacts And Make Them Red
            //(Note: If We're In "Siren Mode", Let's Just Gray Out The Contacts)
                val mvSirenMode = mvClassPermissionsMode.mmSirenMode()
                val (mvContactsAreCurrent, mvPattern) = mvClassContactsUpToDate.mmClassContactsUpToDate<Pair<Boolean, String>>(mvSettings, mvContactButtons, !mvClassPermissionsMode.mmNoContactsMode(), mvSirenMode, 1)

            //Let's Make Having ALL THREE Contacts CURRENT/UP-TO-DATE Only A Recommendation
            //(Note: That Way, We Can Automatically Go Back To The Main Screen If We Accidentally Went Into Settings
            // ....  Or We Can Just Make It Easier To Choose 1 Contact)
            //See If Everything Looks Hunky-Dory, Then Let's Proceed :D
                if (!mvSirenMode && mvPattern == "000") {
                    //At Least One Number Needed! (Unless We're In "Siren Mode")
                        Toast.makeText(applicationContext, mvAtLeastOneNumberNeeded, Toast.LENGTH_SHORT).show()
                }
                else if (!mvSirenMode && mvAllContactSlotsRecommendedNotice < 1 && !mvContactsAreCurrent) {
                    //Recommend Filling All Contacts Slots, But Allow User To Override
                        Toast.makeText(applicationContext, mvAllContactSlotsRecommendedAndCurrent, Toast.LENGTH_SHORT).show()
                        mvAllContactSlotsRecommendedNotice++
                }
                else {
                    //If Some Of Our Slots Remain Unfilled, Let's Fill Them Out With Existing Slots In A Logical Pattern
                    //100, 010, 001 -> 111
                    //120, 102, 012 -> 121
                    //Where 1 is the first contact populated...
                    //And 2 is the second contact populated....
                    //And 0 represents unpopulated contacts (note: only UNPOPULATED, not OUTDATED... because it's better if we don't repopulate outdated slots... as they still miiiiiiight represent valid phone numbers).
                        if (arrayListOf("100", "010", "001").any{it == mvPattern}) {
                            //In Case Of These Patterns, Just Populate All Three Slots With The Same Contact
                                val mvNonNull1 = mvSettings.mvContacts[mvPattern.indexOf("1")]
                                mvSettings.mvContacts[0] = mvNonNull1.mmCopy()
                                mvSettings.mvContacts[1] = mvNonNull1.mmCopy()
                                mvSettings.mvContacts[2] = mvNonNull1.mmCopy()
                        }
                        else if (arrayListOf("120", "102", "012").any{it == mvPattern}) {
                            //In Case Of These Patterns, Just Alternate Between The Contacts
                                val mvNonNull1 = mvSettings.mvContacts[mvPattern.indexOf("1")]
                                val mvNonNull2 = mvSettings.mvContacts[mvPattern.indexOf("2")]
                                mvSettings.mvContacts[0] = mvNonNull1.mmCopy()
                                mvSettings.mvContacts[1] = mvNonNull2.mmCopy()
                                mvSettings.mvContacts[2] = mvNonNull1.mmCopy()
                        }
                    //Switch Activities
                        mmSwitchActivities(ActivityCaregivee::class.java)
                }
        }
    //"Contact #" Button Clicked? Go To The RecyclerView Activity :)
        private fun mmContactNumberClick(mvContactNumber : Int) {
            //Figure Out Where To Go And What To Do
                if (!mvClassPermissionsMode.mmSirenMode()) { //<-- Buttons Aren't Active In "Siren Mode"
                    //Which Button Was Selected?
                        mvWhichContact = mvContactNumber
                    //Check For Unpopulated Or Outdated Contacts And Make Them Red
                    //(Note: If We're In "Siren Mode", Let's Just Gray Out The Contacts)
                        val mvContactsListPermitted = !mvClassPermissionsMode.mmNoContactsMode()
                        val mvContactsCount         = mvClassContactsUpToDate.mmClassContactsUpToDate<Int>(mvSettings, mvContactButtons, mvContactsListPermitted, false, 2)
                        if (mvContactsListPermitted && mvContactsCount == 0) Toast.makeText(applicationContext, mvNoContactsInPhone, Toast.LENGTH_SHORT).show()
                    //Switch Activities
                        mmSwitchActivities(if (!mvContactsListPermitted || mvContactsCount == 0) ActivityTelephoneContactsManualInput::class.java else ActivityTelephoneContactsRecyclerView::class.java)
                }
        }
    //Redirect User To Phone Settings If They So Choose
    //(i.e. So They Can Change Permissions Extra-App-ularly If They Made An Error Choosing Permissions Earlier)
        private fun mmExtraAppularPermissions () {
            //Stop The Countdown
                mvCountdown.mmStop()
            //Continue...
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also{it.data = Uri.fromParts("package", packageName, null)})
        }
    //Restart Activity
        private fun mmRestartActivity() {
            //"Restart Activity" Button Should Disappear onClick
                mvRestartActivity.visibility = View.GONE

            //Refresh Page
                mmSwitchActivities(ActivitySettings::class.java)
        }
    //Switch Activities
        private fun mmSwitchActivities(mvClazz : Class<*>?) {
            //Switching Activities Flag
                mvSwitchingActivities = true
            //Stop The Countdown
                mvCountdown.mmStop()
            //Continue...
                startActivity(Intent(applicationContext, mvClazz).putExtra("mvSettings", mvSettings).putExtra("mvWhichContact", mvWhichContact).putExtra("mvBackButtonOnPhoneClicked", mvBackButtonOnPhoneClicked).also{if (mvClazz == ActivityCaregivee::class.java) it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK}) //<-- We Remove All Previous Tasks From The Back Stack, Since There's A Possibility That We Will Close The App From "ActivityCaregivee" (Either By Swiping Away The Secondary Caregivee Button When The Foreground Service IS On -OR- By Dismissing The Activity from The Recents Screen When The Foreground Service ISN'T On Due To Special Code We Added). As Such, It's Helpful If It Doesn't Try To Immediately Relaunch The App To An Activity Stored In The Back Stack.
        }
    //If The App Is Defocused
        override fun onPause() {
            super.onPause()

            //Save Settings To Disk (I.E. Create A File)
            //So If We Change Permissions Outside Of The File And The App Auto-Restarts The Activity...
            //... Without Displaying "Click to Refresh Page"...
            //... (Like When We Turn Off GPS Permissions Extra-App-ularly)...
            //... We Can Keep Our Changes
            //Note: This Also Saves When We Switch Activities:
                mvClassFileManager.mmSaveSerializable(mvSettings,"mvSettings")

            //Let's Prompt The User To Manually Refresh When They Come Back From What They Were Doing
                if (!mvSwitchingActivities) //<-- Check This Since The Activity Essentially Pauses When We Switch Activities
                    Handler(Looper.getMainLooper()).postDelayed({mvRestartActivity.visibility = View.VISIBLE},1000) //<-- Put This One On A Slight Delay So It Doesn't IMMEDIATELY Appear When We "Long Press" The Link In The Status Window To Alter Permissions Extra-App-ularly
                else
                    mvClassSound.mmStopAllPendingSound(true) //<-- Stop Any Pending Sounds
    }
    //On Back Button Pressed
        override fun onKeyDown(mvKeyCode: Int, mvEvent: KeyEvent?): Boolean {
            if (mvKeyCode == KeyEvent.KEYCODE_BACK) {
                //Let's Override All Back Button Presses To Activate Our Internal Back Button
                //Otherwise, Weird Cache-y Stuff Can Happen...
                    if (mvRestartActivity.visibility != View.VISIBLE) {
                        mvBackButtonOnPhoneClicked = true
                        mmBackButtonClick()
                    }
                    else {
                        mmRestartActivity() //<-- If We Pressed The Back Button Too Many Times From Changing Extra-App-ular Permissions, Let's First Refresh The Page Before Clicking The Internal Back Button
                    }
            }
            return super.onKeyDown(mvKeyCode, mvEvent)
        }
    //Let's Not Have The Countdown Automatically Send Us Back To The Previous Screen If We Aren't Actually Idle
        override fun onUserInteraction() {
            super.onUserInteraction()

            //Note That If "mvRefresh" Is Very Long, This Could Lead To Inconsistent Timeouts
            //E.G. If We Have An "mvCountdown" Of 2 But An "mvRefresh" Of 15000:
            //1) Idling Until 15001 Could Lead To "15000ms To Timeout" (Since 2 Wouldn't Have Decremented By This Point And Would Thus Remain Unchanged Below)
            //2) Idling Until 14999 Could Lead To "30000ms To Timeout" (Since 2 Would Have Decremented To 1, And Therefore Led To A Complete Reset)
                if (mvCountdown.mvCountdown < mvCountdownFrom)
                    mvCountdown.mvCountdown = mvCountdownFrom
        }
}