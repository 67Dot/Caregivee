package com.example.caregivee

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.Float.min
import java.lang.ref.WeakReference

//Review Code For This Page [√√√√√]

class ActivityBehestRecyclerView : ComponentActivity() {
    //Permissions Launcher
        private val mvPermissionsList : ArrayList<String> = arrayListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS).also{if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) it.addAll(listOf(Manifest.permission.POST_NOTIFICATIONS))}.also{if (Build.VERSION.SDK_INT == 26) it.addAll(listOf(Manifest.permission.READ_PHONE_STATE))} //We Ask For "Read Phone State" Permissions For API 26 Because That's The Only API That Seems To Require That Permission For Sending SMS Messages
        private lateinit var mvPermissionsLauncher : ActivityResultLauncher<Array<String>>
        private lateinit var mvWeakReference : WeakReference<ComponentActivity>

    //Strings 'n' Things
        private lateinit var mvSuggestions : Array<String>
        private lateinit var mvSuggestionColors : Array<String>

    //Sound
        private lateinit var mvClassSound : ClassSound

    //Contact List
        private var mvWarningList : MutableList<ClassLineItem> = arrayListOf()

    //Staggered Animated Fade-In Variables
        private val mvHandler = Handler(Looper.getMainLooper())
        private lateinit var mvRunnable : Runnable
        private var mvAlpha = 0f
        private var mvSize = 0
        private lateinit var mvRecyclerView : RecyclerView

    //Shared Preferences
        private lateinit var mvClassSharedPreferences : ClassSharedPreferences

    //Basic Variables
        private var mvPasscode = ""

    //OnCreate
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_behest_recyclerview)

            //Sound
                mvClassSound = ClassSound(applicationContext)

            //Shared Preferences
                mvClassSharedPreferences = ClassSharedPreferences(applicationContext)
                mvClassSharedPreferences.mmSetSharedPreferencesInt("mvDebugFlag", 0) //?!?!?!?! <-- Reset The Debug Flag Every Time We Launch The App Anew (Note: You Can Turn On "Automatic Debug Mode", Where It's On ALL The Time, By Simply Setting The Default Value To 1)

            //Prepare To Request Permissions
                mvWeakReference = WeakReference<ComponentActivity>(this) //<-- Use A Weak Reference To Prevent Memory Leaks: https://stackoverflow.com/a/23333620/16118981

            //Get The List Of "Warnings" From The Strings XML File
                mvSuggestions      = resources.getStringArray(R.array.mtSuggestions)
                mvSuggestionColors = resources.getStringArray(R.array.mtSuggestionColors)
                with (mvWarningList) {
                    for (mvIndex in mvSuggestions.indices) {
                        add(ClassLineItem())
                        last().mvImage     = R.drawable.baseline_warning_amber_24
                        last().mvId        = (mvIndex + 1).toLong()
                        last().mvName      = mvSuggestions[mvIndex]
                        last().mvColorImg  = Color.parseColor(mvSuggestionColors[mvIndex])
                        last().mvColorText = Color.parseColor(mvSuggestionColors[mvIndex])
                    }
                    sortBy {it.mvId}
                    first().mvColorImg                = Color.parseColor("#000000")  //Make The First Entry's Image (Essentially) Disappear For Aesthetic Reasons
                    first().mvImage                   = R.drawable.caregivee_block_vector     //Override First Image — In Conjunction With The Above Color Change — To Make It Disappear (Note: Setting The Image Itself To "android.R.color.transparent" Might Also Work, But It Could Have An Issue On VERY Early Android Versions) (https://stackoverflow.com/a/8243184/16118981)
                    get(mvWarningList.size-2).mvImage = R.drawable.baseline_door_front_24     //Override Penultimate Image
                    last().mvImage                    = R.drawable.baseline_lightbulb_24      //Override Last Image
                }

            //Get RecyclerView By ID And Give It An onScrolled Listener
            //https://stackoverflow.com/a/63720115/16118981
                mvRecyclerView = findViewById<RecyclerView>(R.id.mxBehestRecyclerView).also {
                    it.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        private var mvIsScrolledDown = false
                        override fun onScrolled(mvRecyclerView: RecyclerView, mvDx: Int, mvDy: Int) {
                            super.onScrolled(mvRecyclerView, mvDx, mvDy)

                            //Have We Scrolled Down?
                                mvIsScrolledDown = mvDy < 0

                            //When We Scroll The RecyclerView, Let's Make Sure None Of The...
                            //... Fade-In Animations Have Reset Themselves.
                            //... By Calling mmAnimateAlpha()
                            //... Even After The Runnable Is Complete:
                                mmAnimateAlpha()
                        }
                    })
                }

            //"LinearLayout is a view group that aligns all children in a single direction, vertically or horizontally." - Android
            //(As Opposed To FrameLayout Where "Views" Get Overlaid — On Top Of Each Other Along The "View Z Axis" — And Stacked)
            //The Following Creates A "Vertical Layout" Manager
                mvRecyclerView.layoutManager = LinearLayoutManager(applicationContext)

            //This Will Pass The ArrayList To Our Adapter
            //Note: "An AdapterView is a view whose children are determined by an Adapter" (https://developer.android.com/reference/kotlin/android/widget/AdapterView)
                val mvAdapter = ClassRecyclerViewAdapter(mvWarningList, 0)

            //Setting the Adapter In The RecyclerView.
            //Give The Last ViewHolder An onClick Listener...
            //... So When We Click It, We Can Continue To The Next Activity...
            //(https://stackoverflow.com/questions/29424944/recyclerview-itemclicklistener-in-kotlin)
                mvRecyclerView.adapter = mvAdapter.also{it.mvOnItemClick = {mvLineItem ->
                                                                                //If We Click Certain Suggestions In A Certain Order, We Will Launch Debug Mode
                                                                                    mvPasscode = (if (mvPasscode.length >= 7) mvPasscode.drop(1) else mvPasscode) + mvLineItem.mvId.toString()
                                                                                    println(mvPasscode)
                                                                                    if (mvPasscode == "5554444") mvClassSharedPreferences.mmSetSharedPreferencesInt("mvDebugFlag", 1).also{mvClassSound.mmScheduleSound(R.raw.ma_number_one, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = true, mvImmediateCallFwd = false, 0)} //<-- In This Debug Mode, We Get Faster SMS-Sending Iterations (It Also Allows Us To Immediately Defocus The App And Dismiss From "Recents" When We Press The "Back" Button in ActivitySettings)
                                                                                    if (mvPasscode == "3332222") mvClassSharedPreferences.mmSetSharedPreferencesInt("mvDebugFlag", 2).also{mvClassSound.mmScheduleSound(R.raw.ma_number_two, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = true, mvImmediateCallFwd = false, 0)} //<-- In This Debug Mode, We Can See Signal Strength For Device Testing.
                                                                                //The Last Two Lines Are Essentially Like Buttons, Either To Exit The App Or To Continue To The Next Activity
                                                                                    when (mvLineItem.mvId) {
                                                                                        mvWarningList.size.toLong()-2 -> mvLineItem.mvName = getString(R.string.mtLegalInfo).also{mvAdapter.notifyItemChanged(mvWarningList.size-3)}
                                                                                        mvWarningList.size.toLong()-1 -> mmExitAppClick()
                                                                                        mvWarningList.size.toLong()   -> mmStartAppClick()
                                                                                    }
                                                                            }}

            //Staggered Fade In
                mvSize = mvAdapter.mvList.size
                mvRunnable = Runnable { mmAnimateAlpha() }
                mvHandler.post(mvRunnable)

            //Register Permissions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    mvPermissionsLauncher = (mvWeakReference.get() as ComponentActivity).registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { mmSwitchActivities(ActivitySettings::class.java) }
        }
    //Save The State!
        public override fun onSaveInstanceState(outState: Bundle) {
            super.onSaveInstanceState(outState)
        }
    //Fade In
        private fun mmAnimateAlpha() {
            //Animate The Alpha
                for (mvI in 0..mvSize) mvRecyclerView.findViewHolderForAdapterPosition(mvI)?.itemView?.findViewById<TextView>(R.id.mxTextView).also{ it?.alpha = min(mvAlpha - (mvI.toFloat() / mvSize.toFloat()), 1f) } //"?." Null Safety Check: If The ViewHolder Doesn't Exist Because It Hasn't Yet Been Scrolled To, It's Not A Big Deal... Because We Have An onScrolled Listener Elsewhere That Should Call This Function Later For When It Ostensibly IS In Sight
                mvAlpha += 1f / 24f //<-- Gradually Increase The Overall Alpha Value, It's Staggered For Different ViewHolders Using Subtraction Above
            //Once All Of The Line Items Have Animated To Maximum Alpha, Let's Stop Reposting The Runnable (To Prevent Any Of The Alpha Values From Resetting Upon Scrolling, We Have An onScrolled Listener That Should Call mmAnimateAlpha() Anew)
                if (mvAlpha < 2f) mvHandler.postDelayed(mvRunnable, 41)
        }
    //Check Permissions
        private fun mmStartAppClick() {
            //Runtime Permissions Check
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    mvPermissionsLauncher.launch(mvPermissionsList.toTypedArray())
                else mmSwitchActivities(ActivitySettings::class.java) //<-- Before Android 6.0, We Ostensibly Wouldn't Even Be Able To Use The App Without All Permissions Being Granted, Hence We Just Proceed As Usual
        }
    //Exit App
        private fun mmExitAppClick() {
            //Go To The App-Exiting Activity (Note: The Special Flags Are To Ensure It Doesn't Try To Automatically Reopen The App To A Previous Activity In The Back Stack)
                startActivity(Intent(applicationContext, ActivityTimeToExitApp::class.java).also{it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK}) //Adapted From Two Sources: https://stackoverflow.com/a/24205399/16118981 And https://stackoverflow.com/a/74835742/16118981
        }
    //Switch Activities
        private fun mmSwitchActivities(mvClazz : Class<*>?) {
            //Switch Activities
                startActivity(Intent(applicationContext, mvClazz).putExtra("mvJustStartedApp", true))
        }
}