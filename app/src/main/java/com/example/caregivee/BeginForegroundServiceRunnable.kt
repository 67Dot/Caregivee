package com.example.caregivee

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlin.math.abs

//Review Code For This Page [√√√√√]

@SuppressLint("UnspecifiedRegisterReceiverFlag") //<-- For The Broadcast Receiver Registering Section
class BeginForegroundServiceRunnable (val mvSettings : ClassSettingsState, val mvContext : Context, private val mvLaunchedFromForegroundService : Boolean) {
	//Permissions Checker
		private val mvClassPermissionsMode = ClassPermissionsMode(mvContext)

	//Strings 'n' Things
        private var mvAssistance = mvContext.getString(R.string.mtAssistance)
		private var mvMap = mvContext.getString(R.string.mtMap)
		private var mvMinutesUntilCheckIn = mvContext.getString(R.string.mtMinutesUntilCheckIn)
		private var mvSmsAttempted = mvContext.getString(R.string.mtSmsAttempted)

	//Countdown Variables
		private lateinit var mvRunnable : Runnable //<-- Needs To Be In The Same Scope As mvHandler (Source: https://stackoverflow.com/questions/60761647/kotlin-how-to-pass-a-runnable-as-this-in-handler)
		private lateinit var mvRunnableRateLimiter : Runnable
		private lateinit var mvRunnableSmsDelayed : Runnable
		private var mvCountdown = 0 //<-- Begin At 0 So We Actually Have To "Check In" Right From When We Start The Activity
		private val mvHandler = Handler(Looper.getMainLooper())
        private var mvRepost = true

    //Sound
        private val mvClassSound = ClassSound(mvContext)
		private var mvRateLimitSoundFlag = false

    //Initialize GPS Stuff
        private lateinit var mvClassGpsGetter : ClassGPS
        private lateinit var mvCoordinates : ClassGPSCoordinates
		private var mvGpsOff = false
		private var mvZeroCoordinates : ClassGPSCoordinates

	//Initialize Signal Strength Stuff
		private val mvClassSignalStrength = ClassSignalStrength(mvContext, mvClassSound)

    //Initialize SMS Stuff
        private var mvClassSms = ClassSms(mvContext)

	//Shared Preferences
		private val mvClassSharedPreferences = ClassSharedPreferences(mvContext)

	//Premature App Ending:
	//Currently, The Issue With The App Ending Prematurely (At Least In The Emulator) Can Be Simulated By Increasing The "mvRefreshDebugSpeedFactor" Variable... E.G. When It's Set To A Default Value Of 16 (for a refresh rate of 1/16min), The Foreground Service Times Out Quicker (Like In ~10m); When It's Set To A Default Value Of 1 (for a refresh rate of 1min), The Foreground Service Times Out Much Slower In The Emulator (Like In ~2.5h)
		private val mvRefreshDebugSpeedFactor = if (mvClassSharedPreferences.mmGetSharedPreferencesInt("mvDebugFlag", 0) == 1) 8 else 1 //Default Value Is 1, While 8 or 16 Are Helpful Values For Debug

	//Initialize Basic Variables
		var mvActivitiesDismissed = false //Have We Removed All Activities From The Recents Menu And Are Now Just Running On The Foreground Process?
		private var mvContactsAlternationIndex = 0 //Index For The Local Contacts. This Value Tells Us Which Local Contact To Reference When Sending An SMS. We Usually Cycle Through All (Three) Of The Available Choices.
        private val mvNumberOfContacts = 3 //What Is The Maximum Number Of Local Contacts To Which We Can Send An SMS?
        private val mvRefresh = 60000/mvRefreshDebugSpeedFactor //Default Is One Minute Between Each Iteration For The Runnable. In Debug Mode, We Usually Make This Faster For Testing Purposes.
		private var mvSmsDelayed = 0 //Every Time An SMS Is Sent, This Value Increments. When An SMS Is Delivered, It Decrements. So If This Remains Above 0, Then At Least One SMS Was Delayed. (Note: Since Multiple Sent SMS Messages Can Be Delayed For Quite A While (Even 15 Minutes) It's Helpful For This To Be An Incrementing Value And Not Just A Boolean Flag.)
        private val mvSmsRepeatRate = 2 //If Set To 2, It Will Dispatch An SMS On EVERY OTHER Iteration Of The Runnable

	//Power Management (To Ensure The "Process" Doesn't End Early Due To Screen Being Turned Off, For Example)
		private val mvPowerManager = mvContext.getSystemService(Context.POWER_SERVICE) as PowerManager
		private var mvWindowFocused = true

	//Audio Volume
		private val mvAudioManager = mvContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

	//Device Plugged In?
		private val mvClassPluggedIn = ClassPluggedIn()

	//Toast & Pseudo-Toast Handler: Part 1
	//Handle Paeudo-Toasts And Stuff All In One Convenient Location With A Broadcast Listener So There's Little Conflict
	//Broadcast Receiver Listed In Manifest? [No] (Registered Programmatically In The init Block)
		private val mvBroadcastReceiver : BroadcastReceiver = object : BroadcastReceiver() {
			override fun onReceive(mvContext: Context, mvIntent: Intent) {
				if (mvIntent.action == "mbResetCountdown") {
					//Reset Coundown
						if (mvCountdown <= 0) { //<-- If We're Not In The Process Of Counting Down To "Check-In"
							mvCountdown = mvSettings.mvRefreshRate
							mvClassSound.mmStopAllPendingSound(false)
							mmChangePleaseCheckInButtonProperties(ClassEnum.BUTTONCHECKEDIN.mvInt, ClassEnum.COLORGREEN.mvInt, true)

							//Make Sure When We Check-In, We Don't Have To Wait For The Next Iteration And Instead Repost Immediately
								if (this@BeginForegroundServiceRunnable::mvRunnable.isInitialized) {
									mvHandler.removeCallbacks(mvRunnable)
									mvHandler.post(mvRunnable)
								}
						}
					//Update Pseudo-Toasts And Stuff (By Running A Non-SMS Iteration)
					//(One Possible Use-Case: If (For Example) The User Is Trying To Move The Phone To A Different Location For A Better Signal (When mvCountdown > 0), And Clicks The Screen To Try To Dismiss A "Low Signal" Alert, Maybe We Should Run A Non-SMS Iteration)
						mmIteration(false)
				}
				else if (mvIntent.action == "mbActivitiesDismissed") {
					//If The All Activities Were Removed From The Recents Screen And We're Running A True "Foreground Service", Let's Perhaps Take That Into Consideration Going Forward
						mvActivitiesDismissed = true
					//Update Immediately, If Need Be
					//(Note: In This Case, We Add Some Extra Info On How To Exit The App Once The Activities Are Removed From The Recents Screen... Specifically To The String Displayed In The Secondary Caregivee Button Located Within The Notification, Since Testing In Older Versions Of Android Showed That Trying To Swipe The Notification Away Might Not Always Work To Exit The App, As Our Code Would Otherwise Intend)
						mmUpdateNotification(if (mvCountdown > 0) ClassEnum.BUTTONCHECKEDIN.mvInt else ClassEnum.BUTTONCHECKIN.mvInt)
				}
				else if (mvIntent.action == "mbCloseApp") {
					//Code To Exit App
						onDestroy(false) //<-- Immediately Cancel All Toasts, Remove Any Pending Runanbles, Pending Sounds, And Whatnot To Mitigate Interference
						Toast.makeText(mvContext, mvContext.getString(R.string.mtAppExit), Toast.LENGTH_LONG).show() //<-- Standard Toasts Seem To Be Less Predictable When Running A Foreground Service, But Seeing As How Most Alerts Are Handled With Pseudo-Toasts In ActivityCaregivee, This One Should Theoretically Be More Likely To Show Up Since It's Sufficiently Rare (And It's One Of Our Few Known Visual Communication Options When The Activity Is Dismissed During A Foreground Service)
						mvClassSound.mmScheduleSound(R.raw.ma_app_offline, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKCLOSEAPP.mvInt, mvAddSoundToQueue = true, mvImmediateCallFwd = false, 0)
				}
				else if (mvIntent.action == "mbScreenOff" || mvIntent.action == Intent.ACTION_SCREEN_OFF) { //<-- Power Management (Realtime Power Button Detection WITHIN A Service) (Src: https://stackoverflow.com/a/9478013/16118981)
					//Power Management (Realtime Power Button Detection)
					//(Since This One Is SUPER High Priority, Let's Interrupt All The Others By Superseding Most Pending Sounds!)
						mvClassSound.mmSchedulePrioritySound(ClassEnum.PRIORITYSCREENOFF.mvInt)
				}
				else if (mvIntent.action == "mbSoundBarrage") {
					//For Special Debug Situations Where We Need To Test Out A Sudden Influx Of Lengthy Sounds
						mvClassSound.mmSchedulePrioritySound(ClassEnum.PRIORITYVOLUMELOW.mvInt)
						mvClassSound.mmSchedulePrioritySound(ClassEnum.PRIORITYSCREENOFF.mvInt)
						mvClassSound.mmSchedulePrioritySound(ClassEnum.PRIORITYWINDOWDEFOCUSED.mvInt)
						mvClassSound.mmSchedulePrioritySound(ClassEnum.PRIORITYUNPLUGGED.mvInt)
				}
				else if (mvIntent.action == "mbUnplugged" || mvIntent.action == Intent.ACTION_POWER_DISCONNECTED) {
					//Power Management (Realtime AC Power Detection)
					//(Since This One Is SUPER High Priority, Let's Interrupt All The Others By Superseding Most Pending Sounds!)
						mvClassSound.mmSchedulePrioritySound(ClassEnum.PRIORITYUNPLUGGED.mvInt)
				}
				else if (mvIntent.action == "mbUpdatePseudoToasts") {
					//Update Pseudo-Toasts And Stuff (If We're Just Coming Back From Defocusing, Since The User Might've Altered Permissions Extra-app-ularly)
						mvWindowFocused = true //<-- Make Sure To Add This Redundancy So Our mmIteration() Doesn't Trigger mmFocusDetection() SO Early That It Doesn't Gauge That We've Actually Already Refocused
						mmIteration(false)
				}
				else if (mvIntent.action == "mbVolumeLow") {
					//Power Management (Realtime Volume Button Detection)
					//(Since This One Is SUPER DUPER High Priority, Let's Interrupt All The Others By Superseding All Pending Sounds!)
						if (!mvRateLimitSoundFlag) {
							//Let's Try To Make Sure If Someone Is Decrementing The Volume, That It Plays ASAP And Then Waits A Bit Before The Sound Is Triggerable Again
								mvRateLimitSoundFlag = true
								mvHandler.postDelayed(mvRunnableRateLimiter, 5000)
								mvClassSound.mmSchedulePrioritySound(ClassEnum.PRIORITYVOLUMELOW.mvInt)

							//Send A Real Toast If Activities Were Dismissed
								if (mvActivitiesDismissed)
									Toast.makeText(mvContext, mvContext.getString(R.string.mtLowVolume), Toast.LENGTH_LONG).show() //<-- Standard Toasts Seem To Be Less Predictable When Running A Foreground Service, But Seeing As How Most Alerts Are Handled With Pseudo-Toasts In ActivityCaregivee, This One Should Theoretically Be More Likely To Show Up Since It's Sufficiently Rare (And It's One Of Our Few Known Visual Communication Options When The Activity Is Dismissed During A Foreground Service)
						}
				}
				else if (mvIntent.action == "mbWindowDefocused") {
					//Power Management (Realtime Recents Button Detection)
					//Note: This Also Seems To Be Triggered By Turning Off The Power Button In Red Velvet+
					//(Since This One Is SUPER High Priority, Let's Interrupt All The Others By Superseding Most Pending Sounds!)
						mvClassSound.mmSchedulePrioritySound(ClassEnum.PRIORITYWINDOWDEFOCUSED.mvInt)
						mvWindowFocused = false
				}
				else if (mvIntent.action == "mbWindowFocused") {
					//Power Management (Realtime Recents Button Detection)
					//Note: This Also Seems To Be Triggered By Turning Off The Power Button In Red Velvet+
						mvWindowFocused = true
				}
				else {
					//In This Section, We Play SMS-Related Sounds (Complete With Pseudo-Toasts That Launch When The Sounds Play)
					//(Note: Please Make Sure These Actions Are Also Registered With The Receiver)
						when (mvIntent.action) {
							"mbSmsAttempted"    -> mmPseudoToast(mvSmsAttempted, 2, mvAppend = false, mvDismiss = false, 0)
							"mbSmsDelivered"    -> if (resultCode == Activity.RESULT_OK) {
														mvClassSound.mmScheduleSound(R.raw.ma_sms_delivered, ClassEnum.CALLFWDSMSDELIVERED.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = true, mvImmediateCallFwd = false, 0)
														mvSmsDelayed = kotlin.math.max(mvSmsDelayed-1, 0)
												   }
												   else
														mvClassSound.mmScheduleSound(R.raw.ma_sms_delivery_failed, ClassEnum.CALLFWDSMSDELIVERYFAILED.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = true, mvImmediateCallFwd = false, 0)
							else 				-> for (mvI in 0 until mvNumberOfContacts) {
														if (mvIntent.action == "mbSmsSent$mvI")
															if (resultCode == Activity.RESULT_OK) {
																mvClassSound.mmScheduleSound(R.raw.ma_sms_sent_to_contact, ClassEnum.CALLFWDSMSSENTTOCONTACT.mvInt + mvI, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = true, mvImmediateCallFwd = false, 0).also{mvClassSound.mmScheduleSound(when(mvI) {0 -> R.raw.ma_number_one 1 -> R.raw.ma_number_two else -> R.raw.ma_number_three}, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = true, mvImmediateCallFwd = false, 0)}
																mvSmsDelayed++
																mvHandler.postDelayed(mvRunnableSmsDelayed, 15000)
															}
															else
																mvClassSound.mmScheduleSound(R.raw.ma_sms_failed_to_contact, ClassEnum.CALLFWDSMSFAILEDTOCONTACT.mvInt + mvI, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = true, mvImmediateCallFwd = false, 0).also{mvClassSound.mmScheduleSound(when(mvI) {0 -> R.raw.ma_number_one 1 -> R.raw.ma_number_two else -> R.raw.ma_number_three}, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = true, mvImmediateCallFwd = false, 0)}
														else if (mvIntent.action == "mbSmsGotException$mvI")
															mvClassSound.mmScheduleSound(R.raw.ma_sms_failed_to_contact,ClassEnum.CALLFWDSMSGOTEXCEPTIONTOCONTACT.mvInt + mvI, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = true, mvImmediateCallFwd = false, 0).also{mvClassSound.mmScheduleSound(when(mvI) {0 -> R.raw.ma_number_one 1 -> R.raw.ma_number_two else -> R.raw.ma_number_three}, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = true, mvImmediateCallFwd = false, 0)}
												   }
						}
				}
			}
		}
	//Toast & Pseudo-Toast Handler: Part 2
	//(Note: Since Toast Fonts Can Be Small, Toast Duration Difficult To Control, And Toast Consistency Unpredictable When The App Is Defocused During Foreground Processes... We Use A TextView-Based Pseudo-Toast Instead)
		fun mmPseudoToast(mvToastMessage : String, mvWhichBlock : Int, mvAppend : Boolean, mvDismiss : Boolean, mvTimeAddend : Int) {
			//Send Broadcast
				mvContext.sendBroadcast(Intent("mbPseudoToast").putExtra("mvToastMessage", mvToastMessage).putExtra("mvWhichBlock", mvWhichBlock).putExtra("mvAppend", mvAppend).putExtra("mvDismiss", mvDismiss).putExtra("mvTimeAddend", mvTimeAddend))
		}

	//Init
        init {
			//Do One Right From The Get-Go So We Can Grab An Initial Volume Value
				mmVolumeDetection()
			//Register The Broadcast Receiver
				val mvIntentFilter = IntentFilter().also{
														 it.addAction("mbActivitiesDismissed")
														 it.addAction("mbCloseApp")
														 it.addAction("mbResetCountdown")
														 it.addAction("mbScreenOff")
														 it.addAction("mbSmsAttempted")
														 it.addAction("mbSmsDelivered")
														 it.addAction("mbSoundBarrage")
														 it.addAction("mbUnplugged")
														 it.addAction("mbUpdatePseudoToasts")
														 it.addAction("mbVolumeLow")
														 it.addAction("mbWindowDefocused")
														 it.addAction("mbWindowFocused")
														 it.addAction(Intent.ACTION_POWER_DISCONNECTED)
														 it.addAction(Intent.ACTION_SCREEN_OFF)
														 for (mvI in 0 until mvNumberOfContacts) it.addAction("mbSmsGotException$mvI")
														 for (mvI in 0 until mvNumberOfContacts) it.addAction("mbSmsSent$mvI")}
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
					mvContext.registerReceiver(mvBroadcastReceiver, mvIntentFilter, Context.RECEIVER_EXPORTED)
				else
					mvContext.registerReceiver(mvBroadcastReceiver, mvIntentFilter)

			//Main "Countdown" Thread
				mvRepost = true //<-- This Is Changed To false When onDestroy() Is Called
				mvRunnable = Runnable { mmIteration(true) }

			//Rate Limiter Runnable
				mvRunnableRateLimiter = Runnable {mvHandler.removeCallbacks(mvRunnableRateLimiter)
					                              mvRateLimitSoundFlag = false}

			//SMS Delayed Runnable
				mvRunnableSmsDelayed   = Runnable {if (mvSmsDelayed > 0) mvClassSound.mmScheduleSound(R.raw.ma_sms_delayed, ClassEnum.CALLFWDSMSDELAYED.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = true, mvImmediateCallFwd = false, 0)}

			//GPS Stuff
				mvZeroCoordinates = ClassGPSCoordinates(mvContext).also{it.mmUpdate("0", "0")}

			//Start the Countdown
				mvHandler.post(mvRunnable)
        }
	//Actual Iteration
		fun mmIteration(mvSendSms : Boolean) {
			//Contact Nullification Testing (Debug Stuff)
				// /* ?!?!?!?! */ if (mvCountdown <= -2) mvSettings.mvContacts = ArrayList<ClassLineItem>(Arrays.asList(ClassLineItem(), ClassLineItem(), ClassLineItem())) //<-- This Line Nullifies All The Contact's Info To Test The Following Lines Of Code, But It Does So After A Delay To Simulate An Error Where Contacts Mysteriously Disappear Some Time After The App Has Started
				// /* ?!?!?!?! */ mvSettings.mvContacts[0] = ClassLineItem() <-- Selective Contact Nullification
				// /* ?!?!?!?! */ mvSettings.mvContacts = if (abs(mvCountdown) % 6 < 3) ArrayList<ClassLineItem>(Arrays.asList(ClassLineItem(), ClassLineItem(), ClassLineItem())) else ArrayList<ClassLineItem>(Arrays.asList(ClassLineItem().also{it.mvMobile = "4154154154"}.also{it.mvName = "myName"}, ClassLineItem(), ClassLineItem())) //Alternate Between Nullification and Non-Nullification

			//Let's Start Our PsuedoToasts Afresh!
				mmPseudoToast("", 0, mvAppend = false, mvDismiss = false, 0)
				mmPseudoToast("", 1, mvAppend = false, mvDismiss = false, 0) //<-- Especially Helpful For Erasing The "No Contacts" PseudoToast In Case We End Up Recovering Our Local Contacts
				mmPseudoToast("", 2, mvAppend = false, mvDismiss = false, 0) //<-- Especially Helpful Since On Android Emulators BEFORE Snowcone, As It Doesn't Show A Delivery "Status Update" For Some Reason, Which Leaves The "Block 2" Pseudo-Toast Lingering (Since It Normally Dismisses On The DELIVERY "Status Update" Specifically).

			//Countdown
				val mvSirenMode = mvClassPermissionsMode.mmSirenMode()
				val mvAirplaneMode = mvClassPermissionsMode.mmAirplaneMode()
				val mvPseudoSirenMode = !mvSirenMode && mvSettings.mvContacts.filter {it.mvMobile == null}.size == mvSettings.mvContacts.size //<-- Are All The Contacts Nullfied Even If We Have The Proper Proper Permissions (i.e. We're Not In "Siren Mode" But Everything's null)

			//In "Anonymous Location Mode" and "Siren Mode", We Don't Really Need GPS
				mvGpsOff = mvClassPermissionsMode.mmAnonymousLocationMode() || mvClassPermissionsMode.mmSirenMode()
				if (!mvGpsOff && !this@BeginForegroundServiceRunnable::mvClassGpsGetter.isInitialized) {
					//GPS Stuff
						mvClassGpsGetter = ClassGPS(mvContext.getSystemService(Service.LOCATION_SERVICE) as LocationManager, mvSmsRepeatRate, mvContext)
				}

			//Check If The Notification Is Still Active (Source: https://stackoverflow.com/a/39315744/16118981)
			//In Some Older Versions Of Android (Pre-Tiramisu), Turning Off Notifications Extra-app-ularly Would Keep The Foreground Process Going, But Would Still Remove The Notification.
			//As Such, In The Condition Where Notifications Are Not Permitted During One Iteration, But Suddenly Permitted On The Next, Let's Try To Relaunch The Notification
			//The Delays Due To Waiting For Each Iteration Instead Of Using A (Potentially Memory-Intensive) Custom Listener With A Runnable Are Not Really A Big Deal... Since This Is An Unlikely Situation Anyway AND Messages For Assistance Would Still Theoretically Be Sent Via SMS In The Interim.
				if (mvClassPermissionsMode.mmForegroundServiceMode()) {
					var mvNotificationNotPresent = true
					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) //<-- In Lollipop, We Need Special Handling. So This Section Gets Skipped And It ALWAYS Gives The Appearance That mvNotificationNotPresent Is true (And Therefore, Tries To Update Accordingly At Each And Every Iteration)
						for (mvNotification in (mvContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).activeNotifications)
							if (mvNotification.id == 1) mvNotificationNotPresent = false
					if (mvNotificationNotPresent && mvRepost)
						mmUpdateNotification(if (mvCountdown > 0) ClassEnum.BUTTONCHECKEDIN.mvInt else ClassEnum.BUTTONCHECKIN.mvInt)
				}

			//This Section CURRENTLY Contains All The "Block 1" PseudoToast Warnings
			//Note: We Set mvImmediateCallFwd To true Because, Unlike Sent/Delivery "Status Warnings", These Are Ongoing "States Of Interest" And It Would Be Better To Make Sure The PseudoToasts Are Visible Rather Than To Wait For The Sound's Turn In The Queue
				val mvVerbal = mvCountdown > 0 && mvCountdown % 5 == 0 || mvCountdown <= 0 && mvCountdown % 2 == 0 //<-- Please Don't Be Annoying And Only Verbally Inform User Once In A While If It's "Siren Mode" Or "Airplane Mode" XD XD XD
				if (mvSirenMode)
					mvClassSound.mmScheduleSound(R.raw.ma_siren_mode_on, ClassEnum.CALLFWDSIRENMODE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = mvSendSms && mvVerbal, mvImmediateCallFwd = true, 0).also{mvClassSound.mmScheduleSound(R.raw.ma_sms_not_being_sent, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = mvSendSms && mvVerbal, mvImmediateCallFwd = false, 0)}
				else if (mvPseudoSirenMode)
					mvClassSound.mmScheduleSound(R.raw.ma_no_contacts, ClassEnum.CALLFWDNOCONTACTS.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = mvSendSms, mvImmediateCallFwd = true, 0).also{mvClassSound.mmScheduleSound(R.raw.ma_please_go_to_settings, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = mvSendSms, mvImmediateCallFwd = false, 0)} //<-- If The Contacts Have Suddenly Disappeared And It's Not "Siren Mode", Let's Warn The User Something's Off)
				else if (mvAirplaneMode) //<-- We Don't Care About "Airplane Mode" Being On UNLESS We're Actually Allowed To Send SMS Messages
					mvClassSound.mmScheduleSound(R.raw.ma_airplane_mode_on, ClassEnum.CALLFWDAIRPLANEMODE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = mvSendSms && mvVerbal, mvImmediateCallFwd = true, 0).also{mvClassSound.mmScheduleSound(R.raw.ma_sms_not_being_sent, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = mvSendSms && mvVerbal, mvImmediateCallFwd = false, 0)}
				else if (mvClassPermissionsMode.mmFineLocationMode()) { //<-- "Fine Location Mode" Permissions Required For Testing Signal Strength
					try {
						mvClassSignalStrength.mmGetSignalStrength(mvSendSms, mvVerbal) //<-- Inform User About Signal Strength (IFF We Can Send SMS Messages)
					} catch (mvEx: Exception) {
						mvEx.printStackTrace()
					}
				}
			//Get GPS Location
				val mvGpsPermissionsExpired = mvClassPermissionsMode.mmGpsPermissionsExpired() //<-- Please Keep This Out Of The Following "if (mvGpsOff) {}" Conditional, So It Can Properly Set The "Shared Preferences" Flag Even When GPS Permissions Are On (Logging This Data All The Time Is Helpful For Keeping Track Of Whether GPS Permissions Have Expired)
				mvCoordinates = if (mvGpsOff) {
									if (mvGpsPermissionsExpired == ClassEnum.GPSEXPIRED.mvInt) {
										//GPS Permissions Expired! (Inform User That One-Time GPS Permissions Have Likely Expired)
											if (!mvSirenMode)
												mvClassSound.mmScheduleSound(R.raw.ma_gps_permissions_expired, ClassEnum.CALLFWDGPSEXPIRED.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = mvSendSms && mvVerbal, mvImmediateCallFwd = true, 0).also{mvClassSound.mmScheduleSound(R.raw.ma_please_go_to_settings, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = mvSendSms && mvVerbal, mvImmediateCallFwd = false, 0)}
									}
									mvZeroCoordinates
								}
								else {
									mvClassGpsGetter.mvGPSLocation
								}

			//To Test The Expiration Of One-Time Location Permissions (For Red Velvet Cake+, Which Is When One-Time Permissions Seemed To Be Introduced):
			//1) Uncomment the following line(s) of code.
			//2) Uninstall and rebuild app.
			//3) During the main "Please Check In" part of the app: defocus, wait for the "process" to stop, and then refocus.
			//(Note: This Code May Trigger The Expiration Inconsistently Pre-Tiramisu, Although It Might Just Be A Matter Of Waiting A Sufficient Amount Of Time Before Refocusing The App (Source: https://www.reddit.com/r/androiddev/comments/jwldcs/comment/gcszmx5/?utm_source=share&utm_medium=web3x&utm_name=web3xcss&utm_term=1&utm_content=share_button)
				//if (mvCountdown < 0 && mvCountdown % 3 == 0) exitProcess(-1)

			//Color Changes
				val mvRedThreshold = -mvSmsRepeatRate * (if (mvSirenMode || mvPseudoSirenMode || mvAirplaneMode) 1 else mvNumberOfContacts) //<-- Cycle Through, Sending An SMS To Each Contact Once, Then Turn Red (Unless We're In "Siren Mode" Or A Similar Mode, In Which Case... Let's Alert Anyone Within Earshot Earlier Since No SMS' Are Being Sent)
				if (mvCountdown == 0) {
					//Yellow Light
						mmChangePleaseCheckInButtonProperties(ClassEnum.BUTTONCHECKIN.mvInt, ClassEnum.COLORYELLOW.mvInt, true)
					//Sound
						mvClassSound.mmScheduleSound(R.raw.ma_please_check_in, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = mvSendSms, mvImmediateCallFwd = false, 0)
				}
				else if (mvCountdown in mvRedThreshold until 0) {
					//Orange Light
					//Cycle Through Sending An SMS To All Contacts Once With Orange Light On, Then Change To Red
						mmChangePleaseCheckInButtonProperties(ClassEnum.BUTTONCHECKIN.mvInt, ClassEnum.COLORORANGE.mvInt, false)
					//Sound
						mvClassSound.mmScheduleSound(R.raw.ma_please_check_in, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = mvSendSms, mvImmediateCallFwd = false, 0)
				}
				else if (mvCountdown < mvRedThreshold) {
					//Red Light
						mmChangePleaseCheckInButtonProperties(ClassEnum.BUTTONCHECKIN.mvInt, ClassEnum.COLORRED.mvInt, false)
					//Sound
						mvClassSound.mmScheduleSound(R.raw.ma_help_i_need_help, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = mvSendSms, mvImmediateCallFwd = false, 0)
				}

			//Where Are We At Countdown-wise?
				if (mvCountdown > 0) {
					//Reset "ActivityCaregivee" If Foreground Service Permissions Have Been Changed Extra-app-ularly...
					//... And We're NOT Currently In A Position Where We're Waiting For The User To Check In (I.E. Our "mvCountdown" Variable Is Still Greater Than 0)
					//tldr; So It Only AUTO-RESETS After Extra-app-ular Permissions Changes IFF The Screen Is Green :)
						if (mvLaunchedFromForegroundService && !mvClassPermissionsMode.mmForegroundServiceMode() || !mvLaunchedFromForegroundService && mvClassPermissionsMode.mmForegroundServiceMode())
							mvContext.sendBroadcast(Intent("mbRestartActivity"))
					//Send A Pseudo-Toast Every Once In A While To Update User On Countdown Progress
						if (mvCountdown % 5 == 0 && mvSendSms) mmPseudoToast(String.format(mvMinutesUntilCheckIn, mvCountdown), 2, mvAppend = false, true, 1000) //<-- Skip On Non-SMS Iterations, So (For Example) We Don't End Up Showing The Same "15 Minutes Until Check-In" Twice In A Row If We End Up Refocusing At JUUUUUUST The Right Time
				} else if (mvSendSms && mvCountdown < 0 && !mvAirplaneMode && !mvSirenMode && !mvPseudoSirenMode && (abs(mvCountdown) - 1) % mvSmsRepeatRate == 0) {
					//SMS
						var mvNullFlood = 0
						while (mvSettings.mvContacts[mvContactsAlternationIndex].mvMobile == null) {
							//If Only SOME Of Our Contacts Have Been Nullified...
							//Then Alternate Through Contacts (To See If ANY Are Still Valid)
								mvContactsAlternationIndex = (mvContactsAlternationIndex + 1) % 3
								mvNullFlood++
								if (mvNullFlood >= 3) break
						}
						if (mvSettings.mvContacts[mvContactsAlternationIndex].mvMobile != null) {
							//Make An SMS
								//val mvSms = ".".repeat(1000) //<-- For testing failed "sending" of an SMS. You can also alternatively cause the SMS sender to completely throw an error right from the get-go (on an SMS "attempt") by turning on airplane mode in the emulator (essentially testing what happens if the SMS can't even be "attempted"), but you also need to make sure to remove "!mvAirplaneMode" from this "else if" block).
								val mvSms = mvAssistance + if (mvGpsOff) "" else "\n${mvCoordinates.mvLatLon}\n$mvMap ${mvCoordinates.mvLatLonLink}" //<-- Populate The Actual SMS Message
								mvClassSms.mmSendSms(mvSms, mvSettings.mvContacts[mvContactsAlternationIndex].mvMobile!!, mvContactsAlternationIndex) //"!!" Safety Review: Because Of The Surrounding "!= null" Conditional, This Should Always Be Non-Null

							//Alternate Through Contacts
								mvContactsAlternationIndex = (mvContactsAlternationIndex + 1) % 3
						}
				}

			//Power Management (NON-Realtime Volume Detection, This Subroutine Runs Only Once Per Iteration)
				mmVolumeDetection() //<-- Helpful For If We Lower The Volume, Turn Off The Screen, Max Out The Volume, And Turn Back On The Screen (So It Will Dismiss The Priority-Pseudo-Toast On A Non-SMS Iteration)

			//Power Management (NON-Realtime Power Button Detection, This Subroutine Runs Only Once Per Iteration)
				mmPowerDetection()

			//Power Management (NON-Realtime Focus Detection, This Subroutine Runs Only Once Per Iteration)
				mmFocusDetection()

			//Power Management (NON-Realtime AC Power Detection, This Subroutine Runs Only Once Per Iteration)
				mmPlugDetection()

			//If We're Just Coming Back From Defocusing The App, mmIteration() Is Triggered But "mvSendSms" Is Set To false... Because We're ONLY Looking To Update Pseudo-Toasts And Stuff... So We Shouldn't Refresh Or Decrement Below
				if (mvSendSms) {
					//Post The Code Another Time After Our mvRefresh Period (Currently 1 Minute... Or Perhaps Less Depending On If We're Debugging)
						if (mvRepost) mvHandler.postDelayed(mvRunnable, mvRefresh.toLong())

					//Decrement The Countdown Clock
						mvCountdown--
				}
		}
	//Change Button UI
        fun mmChangePleaseCheckInButtonProperties(mvCaregiveeStringCode : Int, mvCaregiveeColourCode : Int, mvNotificationUpdate : Boolean) {
			//Communicate With The Activity To Make Any Desired Alterations To The Button UI (Source: https://www.tutorialspoint.com/how-to-update-ui-from-intent-service-in-android)
				mvContext.sendBroadcast(Intent("mbChangeButtonProperties").putExtra("mvCaregiveeStringCode", mvCaregiveeStringCode).putExtra("mvCaregiveeColourCode", mvCaregiveeColourCode))
			//Communicate With The Foreground Service To Update The Notification
			//(Note: We Can Use "startForegroundService()" To Communicate With An Already-Running Foreground Service, See mmUpdateNotification() (Source: https://stackoverflow.com/questions/43736714/how-to-pass-data-from-activity-to-running-service#comment74519790_43737298)
				if (mvLaunchedFromForegroundService) {
					//Update The Foreground Service's Notification String (When Needed)
					//... Which We Use As A "Secondary Caregivee Button"
						if (mvNotificationUpdate) mmUpdateNotification(mvCaregiveeStringCode)
					//Update Shared Preferences With The Button Change Information...
					//... Since (Otherwise) If We Don't Swipe The App Out Of The Recents Menu...
					//... But Still Use The Notification-Based "Secondary Caregivee Button"...
					//... It Won't Update The UI (Because It's Paused)...
					//... And Will Show An Outdated Display If We Return To The Activity.
					//Storing This Information Allows Us To Show Updated Information
					//... Upon Returning To THe Activity.
						mvClassSharedPreferences.mmSetSharedPreferencesInt("mvCaregiveeStringCode", mvCaregiveeStringCode)
						mvClassSharedPreferences.mmSetSharedPreferencesInt("mvCaregiveeColourCode", mvCaregiveeColourCode)
				}
        }
	//Power Management (Realtime Recents Button Detection)
	//Note: This Also Seems To Be Triggered By Turning Off The Power Button In Red Velvet+
		private fun mmFocusDetection() {
			if (!mvWindowFocused)
				mvContext.sendBroadcast(Intent("mbWindowDefocused")) //<-- So We Can Play A Sound On Each Iteration If We're Still Defocused, "mbWindowFocused" Handled Elsewhere In ActivityCaregivee (Since That's The Activity We Would Be Refocusing To)
		}
	//Power Management (Realtime AC Power Detection)
		private fun mmPlugDetection() {
			if (!mvClassPluggedIn.mvPluggedIn(mvContext))
				mvContext.sendBroadcast(Intent("mbUnplugged"))
		}
	//Power Management (Realtime Power Button Detection)
		private fun mmPowerDetection() {
			if (!mvPowerManager.isInteractive)
				mvContext.sendBroadcast(Intent("mbScreenOff"))
		}
	//Update The Notification
		private fun mmUpdateNotification(mvCaregiveeStringCode : Int) {
			//Update The Foreground Service's Notification String (When Needed)
			//... Which We Use As A "Secondary Caregivee Button"
				ContextCompat.startForegroundService(mvContext, Intent(mvContext, BeginForegroundService::class.java).putExtra("mvNotificationUpdate", when (mvCaregiveeStringCode) {
																																								ClassEnum.BUTTONCHECKIN.mvInt   -> String.format(mvContext.getString(R.string.mtPleaseCheckInNotification), if (!mvActivitiesDismissed) "" else String.format(mvContext.getString(R.string.mtHowToExitApp)))
																																								ClassEnum.BUTTONCHECKEDIN.mvInt -> String.format(mvContext.getString(R.string.mtCheckedInNotification),           if (!mvActivitiesDismissed) "" else String.format(mvContext.getString(R.string.mtHowToExitApp)))
																																								else                            -> mvContext.getString(R.string.mtPleaseRestart)
																																							}))
		}
	//Power Management (Realtime Volume Button Detection)
		fun mmVolumeDetection() {
			//What Was The Previous Volume And What Is The Current?
			//Also, What's The Max?
				val mvVolPrv = mvClassSharedPreferences.mmGetSharedPreferencesInt("mvVolume", 0)
				val mvVolCur = mvAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
				val mvVolMax = mvAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
			//Play A Sound If The Volume Is Being Decreased At All, So The Volume Isn't Too Low When We Try To Alert The User
				if (mvVolCur < mvVolPrv) {
					//Send Broadcast
						mvContext.sendBroadcast(Intent("mbVolumeLow"))
						mvClassSharedPreferences.mmSetSharedPreferencesInt("mvVolumeLow", 1)
				}
				else if (mvVolCur > mvVolPrv) {
					//Send Broadcast
						mvContext.sendBroadcast(Intent("mbVolumeOk"))
						mvClassSharedPreferences.mmSetSharedPreferencesInt("mvVolumeLow", 0)
				}
				else if (mvVolCur == mvVolMax) { //<-- Note That This Checks If mvVolCur == mvVolMax, Not If mvVolCur == mvVolPrv... Because We Don't Want To Send The OK Signal (To Dismiss The Visual Warning) Until We're Either a) TRULY Increasing Again -Or- b) If Volume Is Indeed Maxed Out And We Need To Clear Out A Previously Cached "Volume Too Low!" Priority-Pseudo-Toast Still Lingering From (... Say...) Before We Turned Off The Screen, Maxed The Volume, And Turned Back On The Screen
					//Send Broadcast
						mvContext.sendBroadcast(Intent("mbVolumeOk"))
						mvClassSharedPreferences.mmSetSharedPreferencesInt("mvVolumeLow", 0)
				}
			//Save The Current Volume To The Shared Preferences
				mvClassSharedPreferences.mmSetSharedPreferencesInt("mvVolume", mvVolCur)
		}
	//Some Stuff To Do When We Want To Treat The Runnable As Removed
        fun onDestroy(mvUnregisterReceiver : Boolean) {
			//Prevent Any Pending Runnables From Reposting
				mvRepost = false
			//Unregister
				if (mvUnregisterReceiver) mvContext.unregisterReceiver(mvBroadcastReceiver)
			//Let's Remove Any Callbacks Before We Proceed
				mvHandler.removeCallbacks(mvRunnable)
			//Stop Any Pending Sounds
			    mvClassSound.mmStopAllPendingSound(true)
			//Remove Location Listener
			    if (this@BeginForegroundServiceRunnable::mvClassGpsGetter.isInitialized) {
					(mvContext.getSystemService(Service.LOCATION_SERVICE) as LocationManager).removeUpdates(mvClassGpsGetter) //Source: https://stackoverflow.com/a/12458604/16118981
				}
        }
}