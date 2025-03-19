package com.example.caregivee

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

//Review Code For This Page [√√√√√]

class ActivityTelephoneContactsRecyclerView : Activity() {
    //Settings Variables
        private var mvSettings : ClassSettingsState = ClassSettingsState(ArrayList(listOf(ClassLineItem(), ClassLineItem(), ClassLineItem())), 15)
        private val mvClassSerializableHandling : ClassSerializableHandling = ClassSerializableHandling()

    //Permissions Checker
        private lateinit var mvClassPermissionsMode : ClassPermissionsMode

    //Strings 'n' Things
        private lateinit var mvEmergencyNumberNotCurrentlyAllowed : String

    //Countdown Variables
        private var mvCountdown = ClassCountdown()

    //Phone Stuff
        private val mvClassPhoneNumberLength = ClassPhoneNumberLength()

    //Basic Variables
        private var mvShortCodeOneTimeNotice = false
        private val mvCountdownFrom = 2
        private val mvRefresh = 15000L

    //Contact List
        private var mvContactList : MutableList<ClassLineItem> = arrayListOf()

    //Framelayout Overlaid "Message Notice"
    //(i.e. Local, Longer-Form Alternative to Toasts/Dialogs)
        private lateinit var mvMessageNotice : TextView

    //Framelayout Overlaid "Restart Activity" ("Click To Restart Page" View)
    //(i.e. Alternative To Dialogs To Prevent "Window Leak" Errors)
        private lateinit var mvRestartActivity : TextView
        private var mvSwitchingActivities = false

    //Which Contact Clicked?
        private var mvWhichContact = 0 //<-- Primitives Need A Default

    //OnCreate
        override fun onCreate(mvSavedInstanceState: Bundle?) {
            super.onCreate(mvSavedInstanceState)
            setContentView(R.layout.activity_telephone_contacts_recyclerview)

            //Get Previous State Just In Case
                if (mvSavedInstanceState != null) {
                    mvSettings = mvClassSerializableHandling.mmGetSerializable(mvSavedInstanceState,"mvSettings", mvSettings, ClassSettingsState::class.java)
                }

            //Permissions Checker
                mvClassPermissionsMode = ClassPermissionsMode(applicationContext)

            //Strings 'n' Things
                mvEmergencyNumberNotCurrentlyAllowed = getString(R.string.mtEmergencyNumberNotCurrentlyAllowed)

            //Get Settings From Previous Screen
                if (intent.extras != null) {
                    mvSettings = mvClassSerializableHandling.mmGetSerializable(this.intent,"mvSettings", mvSettings, ClassSettingsState::class.java)
                    mvWhichContact = this.intent.getIntExtra("mvWhichContact", 1)
                }

            //Framelayout Overlaid "Restart Activity" ("Click To Restart Page" View)
            //(i.e. Alternative To Dialogs To Prevent "Window Leak" Errors)
                mvRestartActivity = findViewById(R.id.mxRestartActivity)

            //Check To See If GPS Permissions Have Expired
            //(Note: Just By Calling This, We Can Keep Track In Shared Preferences If We Change Permissions Extra-app-ularly During This Activity, Which Is Helpful Since Changing Said Permissions Extra-app-ularly Will Likely Trigger An Activity Restart)
                mvClassPermissionsMode.mmGpsPermissionsExpired()

            //Framelayout Overlaid "Message Notice"
            //(i.e. Local, Longer-Form Alternative to Toasts/Dialogs)
                mvMessageNotice = findViewById(R.id.mxMessageNotice) //<-- Get The View

            //Let's Return To The Previous Screen If Important Permissions Were Abruptly Changed Extra-App-ularly
            //"No Contacts List Mode"
                if (mvClassPermissionsMode.mmNoContactsMode()) {
                    mmBackButtonClick()
                }
                else {
                    //Get Contact List
                        mvContactList = ClassContactsRetriever(applicationContext).mmContactsRetriever().also{it.sortBy{mvIt -> mvIt.mvName }}

                    //Get RecyclerView By ID
                        val mvRecyclerView = findViewById<RecyclerView>(R.id.mxRecyclerView) //<-- Get The View

                    //"LinearLayout is a view group that aligns all children in a single direction, vertically or horizontally." - Android
                    //(As Opposed To FrameLayout Where "Views" Get Overlaid — On Top Of Each Other Along The "View Z Axis" — And Stacked)
                    //The Following Creates A "Vertical Layout" Manager
                        mvRecyclerView.layoutManager = LinearLayoutManager(applicationContext)

                    //Populate A New ArrayList Of Line Items
                        val mvData = ArrayList<ClassLineItem>()
                        for (mvJ in 0 until mvContactList.size) {
                            mvData.add(mvContactList[mvJ])
                        }

                    //This Will Pass The ArrayList To Our Adapter
                    //Note: "An AdapterView is a view whose children are determined by an Adapter" (https://developer.android.com/reference/kotlin/android/widget/AdapterView)
                        val mvAdapter = ClassRecyclerViewAdapter(mvData, 1)

                    //Setting the Adapter In The RecyclerView
                        mvRecyclerView.adapter = mvAdapter

                    //RecyclerView "onClick"
                    //(Source: https://stackoverflow.com/questions/29424944/recyclerview-itemclicklistener-in-kotlin)
                        mvAdapter.mvOnItemClick = { mvLineItem ->
                            if (mvClassPhoneNumberLength.mmEmergencyNumber(mvLineItem.mvMobile)) {
                                //Don't Allow Any Contacts That Point To Emergency Numbers!
                                    Toast.makeText(applicationContext, mvEmergencyNumberNotCurrentlyAllowed, Toast.LENGTH_SHORT).show()
                            }
                            else if (!mvShortCodeOneTimeNotice && mvClassPhoneNumberLength.mmShortCode(mvLineItem.mvMobile)) {
                                //Post An Overlaid Warning If The Contact Contains A Possible "SMS Short Code"
                                    mvMessageNotice.visibility = View.VISIBLE
                                    mvShortCodeOneTimeNotice = true //<-- Let's Only Warn Once About This, Then Let Them Proceed
                            }
                            else {
                                //If Everything Looks On The Up And Up, Update The Relevant Contact In mvSettings And Return To The Previous Screen
                                    mvSettings.mvContacts[mvWhichContact] = mvLineItem
                                    mmBackButtonClick()
                            }

                        }
                    //Main "Countdown" Thread
                    //When We Reach 0, We Return To Settings!
                    //(Note: Purpose Is Just In Case Of A Misclick During A Situation Where Someone Might Need Assistance, It Would Be Better To Get Them Back To The Main App)
                        mvCountdown.mmCountdownClock(mvCountdownFrom, mvRefresh, ::mmBackButtonClick)
                }
            //Add onClickListeners (More Futureproof Than Using android:onClick In The XML Files: https://stackoverflow.com/a/44184111/16118981)
                findViewById<Button>(R.id.mxBackButton).setOnClickListener{ mmBackButtonClick() }
                mvMessageNotice.setOnClickListener{ mmDismissMessageNotice() }
                mvRestartActivity.setOnClickListener{ mmRestartActivity() }
        }
    //Save The State!
        public override fun onSaveInstanceState(mvSavedInstanceState: Bundle) {
            super.onSaveInstanceState(mvSavedInstanceState)
            mvSavedInstanceState.putSerializable("mvSettings", mvSettings)
        }
    //Return To Settings
        private fun mmBackButtonClick() {
            //Switch Activities
                mmSwitchActivities(ActivitySettings::class.java)
        }
    //Framelayout Overlaid "Message Notice"
    //Has It Been Clicked To Dismiss?
        private fun mmDismissMessageNotice() {
            mvMessageNotice.visibility = View.GONE
        }
    //Restart Activity
        private fun mmRestartActivity() {
            //Restart Activity Button Should Disappear onClick
                mvRestartActivity.visibility = View.GONE

            //Refresh Page
                mmSwitchActivities(ActivityTelephoneContactsRecyclerView::class.java)
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
                if (!mvSwitchingActivities)
                    mvRestartActivity.visibility = View.VISIBLE
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