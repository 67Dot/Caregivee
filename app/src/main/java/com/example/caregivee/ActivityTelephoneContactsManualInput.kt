package com.example.caregivee

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

//Review Code For This Page [√√√√√]

//"No Contacts List Mode" (If Certain Permissions Aren't Set)
class ActivityTelephoneContactsManualInput : Activity() {
    //Settings Variables
        private var mvSettings : ClassSettingsState = ClassSettingsState(ArrayList(listOf(ClassLineItem(), ClassLineItem(), ClassLineItem())), 15)
        private val mvClassSerializableHandling : ClassSerializableHandling = ClassSerializableHandling()

    //Permissions Checker
        private lateinit var mvClassPermissionsMode : ClassPermissionsMode

    //File Manager
        private lateinit var mvClassFileManager : ClassFileManager

    //Strings 'n' Things
        private val mvClassFromHtml = ClassFromHtml()
        private var mvShortCodeNotice = "" //<-- Note: Primitives Need A Default Value

    //Countdown Variables
        private var mvCountdown : ClassCountdown = ClassCountdown()

    //Phone Stuff
        private val mvClassPhoneNumberLength = ClassPhoneNumberLength()

    //Basic Variables
        private var mvShortCodeOneTimeNotice = false
        private val mvCountdownFrom = 2
        private val mvRefresh = 15000L

    //"Views"
        private lateinit var mvManualName   : EditText
        private lateinit var mvManualMobile : EditText
        private lateinit var mvStatusWindow : TextView

    //Framelayout Overlaid "Restart Activity" ("Click To Restart Page" View)
    //(i.e. Alternative To Dialogs To Prevent "Window Leak" Errors)
        private lateinit var mvRestartActivity : TextView
        private var mvSwitchingActivities = false

    //Which Contact Clicked?
        private var mvWhichContact = 0 //<-- Note: Primitives Need A Default Value

    //OnCreate
        override fun onCreate(mvSavedInstanceState: Bundle?) {
            super.onCreate(mvSavedInstanceState)
            setContentView(R.layout.activity_telephone_contacts_manual_input)

            //Get Previous State Just In Case
                if (mvSavedInstanceState != null) {
                    mvSettings = mvClassSerializableHandling.mmGetSerializable(mvSavedInstanceState,"mvSettings", mvSettings, ClassSettingsState::class.java)
                }

            //Permissions Checker
                mvClassPermissionsMode = ClassPermissionsMode(applicationContext)

            //File Manager
                mvClassFileManager = ClassFileManager(applicationContext)

            //Strings 'n' Things
                mvShortCodeNotice = getString(R.string.mtShortCodeNotice)

            //Get Settings From Previous Screen
                if (intent.extras != null) {
                    mvSettings = mvClassSerializableHandling.mmGetSerializable(this.intent, "mvSettings", mvSettings, ClassSettingsState::class.java)
                    mvWhichContact = this.intent.getIntExtra("mvWhichContact", mvWhichContact)
                }

            //Framelayout Overlaid "Restart Activity" ("Click To Restart Page" View)
            //(i.e. Alternative To Dialogs To Prevent "Window Leak" Errors)
                mvRestartActivity = findViewById(R.id.mxRestartActivity)

            //Check To See If GPS Permissions Have Expired
            //(Note: Just By Calling This, We Can Keep Track In Shared Preferences If We Change Permissions Extra-app-ularly During This Activity, Which Is Helpful Since Changing Said Permissions Extra-app-ularly Will Likely Trigger An Activity Restart)
                mvClassPermissionsMode.mmGpsPermissionsExpired()

            //Initialize "Views"
                mvManualName   = findViewById(R.id.mxManualName)
                mvManualMobile = findViewById(R.id.mxManualMobile)
                mvStatusWindow = findViewById(R.id.mxStatusWindow)

            //Populate "Views" With Information From Previous Screen (Initially)
                mvManualName.setText(mvSettings.mvContacts[mvWhichContact].mvName     ?: "") //"?:" Safety Review: Should Be OK As This Is Just For Displaying null As ""
                mvManualMobile.setText(mvSettings.mvContacts[mvWhichContact].mvMobile ?: "") //"?:" Safety Review: Should Be OK As This Is Just For Displaying null As ""

            //Main "Countdown" Thread
            //When We Reach 0, We Return To Settings!
            //(Note: Purpose Is Just In Case Of A Misclick During A Situation Where Someone Might Need Assistance, It Would Be Better To Get Them Back To The Main SMS-Sending App)
                mvCountdown.mmCountdownClock(mvCountdownFrom, mvRefresh, ::mmBackButtonClick)

            //Add onClickListeners (More Futureproof Than Using android:onClick In The XML Files: https://stackoverflow.com/a/44184111/16118981)
                findViewById<Button>(R.id.mxBackButton).setOnClickListener{ mmBackButtonClick() }
                mvStatusWindow.setOnClickListener{ mmBackButtonClick() }
                mvRestartActivity.setOnClickListener{ mmRestartActivity() }
        }

    //"Back" Button Clicked
        private fun mmBackButtonClick() {
            //Get The User Input Data
                mvSettings.mvContacts[mvWhichContact].mvName   = mvManualName.text.toString()
                mvSettings.mvContacts[mvWhichContact].mvMobile = mvManualMobile.text.toString()

            //Parse The Phone Numbers (E.G. Remove Dashes And Parentheses)
                val mvMobileParsed = mvManualMobile.text.toString().filter{it.isDigit()}

            //Check To See If Everything Looks Valid
                var mvAllow = true
                var mvNotice = ""
                if (mvSettings.mvContacts[mvWhichContact].mvMobile == "" && mvSettings.mvContacts[mvWhichContact].mvName != "") { //<-- If there's a name but no number, that's not allowed. However, allow it to go back if we clear all fields, and just treat it as a contact removed.
                    mvNotice = getString(R.string.mtAtLeastOneNumberNeeded)
                    mvAllow = false
                }
                else if (mvClassPhoneNumberLength.mmEmergencyNumber(mvMobileParsed)) {
                    mvNotice = getString(R.string.mtEmergencyNumberNotCurrentlyAllowed)
                    mvAllow = false
                }
                else if (!mvShortCodeOneTimeNotice && mvClassPhoneNumberLength.mmShortCode(mvMobileParsed)){
                    mvNotice = getString(R.string.mtNumbersWrongLength)
                    mvStatusWindow.text = mvClassFromHtml.mmFromHtml(String.format(mvShortCodeNotice, mvMobileParsed))
                    mvShortCodeOneTimeNotice = true //<-- Let's Only Warn Once About This, Then Let Them Proceed
                    mvAllow = false
                }

            //Convert All "" Fields Into NULL As "ClassSettingsState" Considers That An Empty Field
                if (mvSettings.mvContacts[mvWhichContact].mvMobile == "") mvSettings.mvContacts[mvWhichContact].mvMobile = null

            //Continue...
                if (mvAllow) {
                    //Save Settings To Disk (I.E. Create A File)
                    //(NOTE: Helpful To Also Do This Here Just In Case The User Accidentally Closes The App Before They've Entered All Three Contacts)
                        mvClassFileManager.mmSaveSerializable(mvSettings,"mvSettings")
                    //Switch Activities
                        mmSwitchActivities(ActivitySettings::class.java)
                }
                else {
                    //Actually Post One Of Our Notices From Before, If Applicable
                        Toast.makeText(applicationContext, mvNotice, Toast.LENGTH_SHORT).show()
                }
        }

    //Save The State!
        public override fun onSaveInstanceState(mvSavedInstanceState: Bundle) {
            super.onSaveInstanceState(mvSavedInstanceState)
            mvSavedInstanceState.putSerializable("mvSettings", mvSettings)
        }

    //Restart Activity
        private fun mmRestartActivity() {
            //Restart Activity Button Should Disappear onClick
                mvRestartActivity.visibility = View.GONE

            //Refresh Page
                mmSwitchActivities(ActivityTelephoneContactsManualInput::class.java)
        }
    //Switch Activities
        private fun mmSwitchActivities(mvClazz : Class<*>?) {
            //Switching Activities Flag
                mvSwitchingActivities = true
            //Stop The Countdown
                mvCountdown.mmStop()
            //Continue...
                startActivity(Intent(applicationContext, mvClazz).putExtra("mvSettings", mvSettings))
        }
    //If The App Is Defocused
        override fun onPause() {
            super.onPause()

            //Pause Countdown
                mvCountdown.mmPause()

            //Let's Prompt The User To Manually Refresh When They Come Back From What They Were Doing
                if (!mvSwitchingActivities) mvRestartActivity.visibility = View.VISIBLE
        }
    //If The App Is Refocused
        override fun onResume() {
            super.onResume()

            //Unpause Countdown
                mvCountdown.mmUnpause()
        }
    //On Back Button Pressed
        override fun onKeyDown(mvKeyCode: Int, mvEvent: KeyEvent?): Boolean {
            if (mvKeyCode == KeyEvent.KEYCODE_BACK) {
                //Let's Override All Back Button Presses To Activate Our Internal Back Button
                //Otherwise, Weird Cache-y Stuff Can Happen...
                    mmBackButtonClick()
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