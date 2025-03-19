package com.example.caregivee

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.NotificationCompat

//Review Code For This Page [√√√√√]

/*
    Information About Foreground Services:
    =====================================
    Source 1 (BEST): https://www.here.com/docs/bundle/sdk-for-android-navigate-developer-guide/page/topics/get-locations-enable-background-updates.html
                    ^ Also helpful for describing the purpose of overridden methods that haven't been implemented (like onBind).
    Source 2: https://www.geeksforgeeks.org/foreground-service-in-android/
    Source 3: https://medium.com/@dugguRK/kotlin-music-foreground-service-play-on-android-4b57b10fe583

    FAQ:
        Why is the foreground service notification delayed by ~10s in the emulator?
            "UX delay for foreground service notifications
            To provide a streamlined experience for short-running foreground services,
            devices that run Android 12 or higher can delay the display of
            foreground service notifications by 10 seconds,
            with a few exceptions.
            This change gives short-lived tasks a chance to
            complete before their notifications appear."
            Source: https://developer.android.com/about/versions/12/behavior-changes-all#foreground-service-notification-delay
        Why does the foreground service end up getting dismissed by the device sometimes?
            Explanation: https://dontkillmyapp.com/
 */

class BeginForegroundService: Service() {
    //Settings Variables
        private var mvSettings = ClassSettingsState(ArrayList(listOf(ClassLineItem(), ClassLineItem(), ClassLineItem())), 15)

    //Strings 'n' Things
        private var mvChannelId = "com.example.caregivee.notification.CHANNEL_ID_FOREGROUND"
        private var mvBody  = "" //<-- Note: Primitives Need A Default Value
        private var mvTitle = "" //<-- Note: Primitives Need A Default Value
        private var mvError = "" //<-- Note: Primitives Need A Default Value

    //Class Containing Main Caregivee Runnable
        private lateinit var mvBeginForegroundServiceRunnable : BeginForegroundServiceRunnable

    //Notification Builder
        private lateinit var mvNotificationBuilder : NotificationCompat.Builder

    //Power Management (Realtime Volume Detection WITHIN A Service)
        private val mvVolumeObserver: ContentObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(mvSelfChange: Boolean) {
                super.onChange(mvSelfChange)
                mvBeginForegroundServiceRunnable.mmVolumeDetection()
            }
        }

    //Init
        override fun onCreate() {
            super.onCreate()

            //Power Management (Realtime Volume Detection WITHIN A Service)
            //=============================================================
            //Let's Register A ContentObserver, Which Is Like A BroadcastReceiver!
            //This Allows The Background Service To Immediately Respond To Changes In The Device's Volume
            //Source 1: https://stackoverflow.com/questions/7297242/any-way-to-detect-volume-key-presses-or-volume-changes-with-android-service
            //Source 2: https://stackoverflow.com/questions/17192253/how-to-register-contentobserver-for-media-volume-change
            //Source 3: https://stackoverflow.com/questions/32139947/settings-system-volume-settings-removed-in-android-api-23
            //Source 4: https://android.googlesource.com/platform/frameworks/base/+/android-8.0.0_r4/core/java/android/provider/Settings.java
                contentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, mvVolumeObserver)
        }

    //On Service Start
        override fun onStartCommand(mvIntentParam: Intent, mvFlags: Int, mvStartId: Int): Int {
            //Strings 'n' Things
                mvBody  = applicationContext.getString(R.string.mtNotificationBody)
                mvTitle = applicationContext.getString(R.string.mtNotificationTitle)
                mvError = applicationContext.getString(R.string.mtPleaseRestart)
            //Is Some Part Of The App Just Trying To Send Flags Or Other Data To An Already-Running Foreground Service?
                if (mvIntentParam.hasExtra("mvNotificationUpdate")) {
                    //Update The Notification Text
                        if (this@BeginForegroundService::mvBeginForegroundServiceRunnable.isInitialized) {
                             mvNotificationBuilder.setContentText(mvIntentParam.getStringExtra("mvNotificationUpdate") ?: mvError) //"?:" Safety Check: It Should Just Show mvError If This Value Is null
                             (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(1, mvNotificationBuilder.build()) //<-- Lock In Our Above Change
                        }
                }
            //Or Are We Starting The Service From Scratch?
                else {
                    //Create Notification Channel
                        mvCreateNotificationChannel()

                    //Create A Notification
                        mvNotificationBuilder = NotificationCompat.Builder(applicationContext, mvChannelId)
                                                                  .setAutoCancel(false)
                                                                  .setContentIntent(mmGetClickIntent()) //<-- What if the notification is clicked?
                                                                  .setDeleteIntent(mmGetDeleteIntent()) //<-- What if the notification is cancelled?
                                                                  .setContentText(mvBody)
                                                                  .setContentTitle(mvTitle)
                                                                  .setSmallIcon(R.drawable.caregivee_notification_png) //<-- Don't Use An .SVG, Some Android Versions (Like Snowcone) Work Better With PNG Notification Icons: https://stackoverflow.com/questions/25317659/how-to-fix-android-app-remoteserviceexception-bad-notification-posted-from-pac
                    //Start The Foreground Service
                        startForeground(1, mvNotificationBuilder.build())

                    ////////////////////////////////////////////////
                    /////// Here We Begin Our Service Proper ///////
                    ////////////////////////////////////////////////

                    //Begin
                        mvBeginForegroundServiceRunnable = BeginForegroundServiceRunnable(ClassSerializableHandling().mmGetSerializable(mvIntentParam, "mvSettings", mvSettings, ClassSettingsState::class.java), applicationContext, true)
                        Toast.makeText(applicationContext, applicationContext.getString(R.string.mtForegroundServiceStarted), Toast.LENGTH_SHORT).show()
                }

            //Return Value
                return START_NOT_STICKY //Or you can use START_STICKY (but upon personal experimenting, that seems to be error-prone): https://stackoverflow.com/a/9441795/16118981

        }
    //If The Notification Is Tapped (I.E. Clicked)
        private fun mmGetClickIntent() : PendingIntent? {
            //Send A Broadcast When The "Notification" Is Clicked (I.E. To "BeginForegroundServiceTransmissionReceiver")
            //"Retrieve a PendingIntent that will perform a broadcast" <-- Android's Documentation on "PendingIntent.getBroadcast"
                return PendingIntent.getBroadcast(applicationContext, 0, Intent(applicationContext, BeginForegroundServiceTransmissionReceiver::class.java).setAction("mbNotificationClicked"),PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }
    //If The Notification Is Swiped To The Side (I.E. Cancelled)
        private fun mmGetDeleteIntent() : PendingIntent? {
            //Send A Broadcast When The "Notification" Is Dismissed (I.E. To "BeginForegroundServiceTransmissionReceiver")
            //"Retrieve a PendingIntent that will perform a broadcast" <-- Android's Documentation on "PendingIntent.getBroadcast"
                return PendingIntent.getBroadcast(applicationContext, 0, Intent(applicationContext, BeginForegroundServiceTransmissionReceiver::class.java).setAction("mbNotificationCancelled"),PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

    //Has The Service Been Removed?
        override fun onDestroy() {
            //Let's Clean Up Some Stuff Over In The Cargivee Runnable Class
                if (this@BeginForegroundService::mvBeginForegroundServiceRunnable.isInitialized) {
                    //Cleanup On Aisle "BeginForegroundServiceRunnable"
                        mvBeginForegroundServiceRunnable.onDestroy(true)
                    //Indicate That The Service Has Ceased
                        Toast.makeText(applicationContext, applicationContext.getString(R.string.mtForegroundServiceStopped), Toast.LENGTH_SHORT).show()
                }

            //Power Management (Realtime Volume Detection WITHIN A Service)
                contentResolver.unregisterContentObserver(mvVolumeObserver)

            //Call Superclass onDestroy() Method
                super.onDestroy()
        }

    //onBind Method
        override fun onBind(mvIntent: Intent?): IBinder? {
            return null
        }

    //Create Notification Channel
        private fun mvCreateNotificationChannel() {
            //Create The "Notification" Chanel
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(mvChannelId, applicationContext.getString(R.string.mtNotificationChannelName), NotificationManager.IMPORTANCE_DEFAULT))
        }
}