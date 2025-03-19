package com.example.caregivee

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import kotlin.system.exitProcess

//Review Code For This Page [√√√√√]

//Note: Only Instantiate Once Per Activity And Call "mmScheduleSound()" Or "mmSchedulePrioritySound()" Whenever We Want To Play A Sound File
class ClassSound (val mvContext : Context) {
    //Create A Special MutableList For The Highest Priority Sounds (These Should Take Priority In Descending Order Of Index, I.E. Superseding Any Subsequent Sound In The List)
        private var mvPrioritySoundBoard: MutableList<ClassMediaPlayer> = arrayListOf(ClassMediaPlayer(ClassEnum.PRIORITYVOLUMELOW.mvInt, false, R.raw.ma_volume_low, mvContext),
                                                                                      ClassMediaPlayer(ClassEnum.PRIORITYSCREENOFF.mvInt, false, R.raw.ma_please_turn_screen_back_on, mvContext),
                                                                                      ClassMediaPlayer(ClassEnum.PRIORITYWINDOWDEFOCUSED.mvInt, false, R.raw.ma_please_return_to_app, mvContext),
                                                                                      ClassMediaPlayer(ClassEnum.PRIORITYUNPLUGGED.mvInt, false, R.raw.ma_please_plug_in, mvContext))
        private var mvPrioritymostSound = mvPrioritySoundBoard.size
    //Create A MutableList (To Act As A Queue) Slated To Comprise The References To Non-Priority Sounds
        private var mvSoundBuffer : MutableList<ClassSoundBuffer> = arrayListOf()
        private var mvSoundCurrent : ClassMediaPlayer = ClassMediaPlayer(0, false, R.raw.ma_null, mvContext) //<-- Holds The Actual Currently Playing Sound, Not Just A Reference
        private var mvClosingApp = false
    //Shared Preferences
        private val mvClassSharedPreferences = ClassSharedPreferences(mvContext)
        private val mvSendExtra = mvClassSharedPreferences.mmGetSharedPreferencesInt("mvDebugFlag", 0) == 2
    //Schedule The Desired Priority Sound To Play, Then Pause All Current Non-Priority (And Priority Sounds Of Lower Priority) If Need Be...
    //... Play Priority Sounds In Order Of Priority, Not Playing One Of Equal Or Lower Priority When One Is Already Playing, And Going Back To Playing Standard Sounds If There Are No More Scheduled Priority Sounds
        fun mmSchedulePrioritySound(mvIndex : Int?) { //<-- "Type?" Safety Review: This Allows Us To Reuse The Same Method For Automatically Calling The Next Sound — That Is In The Queue — Upon onCompletion() (By Inserting A null Value)
            //Is The App Closing (mvClosingApp == true)? If So, Ignore This Section...
                if (!mvClosingApp) {
                    //Let's Schedule A Priority Sound
                        mvIndex?.also{mvPrioritySoundBoard[it].mvScheduled = true} //<-- "?." Safety Review: This Allows Us To Skip Scheduling A New Sound If We're Just Playing The Next One In Rotation After onCompletion() (When mvIndex Has A null Value)
                    //Pause Any Currently Playing NON-Priority Sounds
                        if (mvSoundCurrent.mmIsPlaying()) {
                            mvSoundCurrent.mmPause()
                            mvSoundCurrent.mmReset() //This resets the MediaPlayer to its uninitialized state. This seems to fix the glitch where audio (specifically non-priority audio) eventually stops playing when Android Studio is minimized in API 32 and Tiramisu. Note: The MediaPlayer needs to be re-.create()'d after each .reset().
                        }
                    //Find The First Priority Sound In The Queue That Is Scheduled
                    //Update This Value Each Time This Method Is Called To Better Ensure Synchronicity
                        mvPrioritymostSound = mvPrioritySoundBoard.firstOrNull { /* ... grab the first where the following is true: */ it.mvScheduled }?.mvInt ?: mvPrioritySoundBoard.size //?. and ?: Safety Review: Simply Return The Size Of The Priority Queue If No Sounds Are Scheduled

                    //Now Let's Focus On The Priority Sound Queue
                        for (mvSound in mvPrioritySoundBoard) {
                            //Have We Reached Our Scheduled Sound With The Most Priority?
                            //If So, Rewind (If Need Be) And Play The Sound!
                                if (mvSound.mvScheduled && mvPrioritymostSound == mvSound.mvInt && !mvSound.mmIsPlaying()) {
                                    mvSound.mmRewind()
                                    mvSound.mmStart()
                                    mvSound.mvMediaPlayer.setOnCompletionListener {
                                        mvSound.mvScheduled = false
                                        mmSchedulePrioritySound(null) //<-- Move To The Next Priority Sound
                                    }
                                }
                            //Pause Any Currently Playing Priority Sounds If They Aren't Of Sufficient Priority Compared To Our Scheduled Sound With The Most Priority
                                else if (mvSound.mvInt > mvPrioritymostSound && mvSound.mmIsPlaying()) {
                                    mvSound.mmPause() //<-- Do Not mmRewind() Immediately After Pausing, Instead, mmRewind() Just Before We Start Playing The Sound Again (Since In Marshmallow, It Seems To mmRewind() And Overlap With Any Interrupting Higher Priority Sounds BEFORE It Successfully mmPause()'s The Lower Priority Sound)
                                }
                            //If There Are No Remaining Priority Sounds, Let's Start Playing Any NON-PRIORITY Sounds Again
                                else if (mvSound.mvInt >= mvPrioritySoundBoard.size - 1) {
                                    mmScheduleSound(0, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = false, mvImmediateCallFwd = false, 0)
                                }
                        }
            }
        }
    //Add Our New Sound To Queue, Then Play The First Sound In The Queue If It Exists And It's Not Playing, "Remove" It From The Queue onCompletion, Then Repeat The Process To Move Onto The Next One In The Queue (Without Adding A New Sound On Subsequent Recursive Calls)
        fun mmScheduleSound(mvSound : Int, mvCallFwdCode : Int, mvCallBackCode : Int, mvAddSoundToQueue : Boolean, mvImmediateCallFwd : Boolean, mvExtra : Int) {
            //Schedule A New Sound At The End Of The Queue
                if (mvAddSoundToQueue) {
                    mvSoundBuffer.add(ClassSoundBuffer(mvSound, if (mvImmediateCallFwd) ClassEnum.CALLFWDNONE.mvInt else mvCallFwdCode, mvCallBackCode, false))
                    if (mvImmediateCallFwd) mmCallForward(mvCallFwdCode, mvExtra)
                }
                else
                    mmCallForward(mvCallFwdCode, mvExtra) //<-- Sometimes, We Just Want To Skip The Sound Altogether And Go Right To The CallForward Function (Be It "Immediate CallForward" Or Otherwise), Like When We Do A Non-SMS Iteration In BeginForegroundServiceRunnable (I.E. mvSendSms = false).
            //Is The App Closing (mvClosingApp == true)? If So, Ignore This Section...
                if (!mvClosingApp) {
                    //Get The Next Sound In The Rotation
                        val mvFirstSound = mvSoundBuffer.getOrNull(0)
                    //If There Indeed Was A Next Sound In Rotation...
                        if (mvFirstSound != null) {
                            //Are Any Non-Priority Sounds Currently Playing -OR- Are Any Priority Sounds Scheduled? If So, Ignore This Section...
                                if (!mvSoundCurrent.mmIsPlaying() && !mvPrioritySoundBoard.any{it.mvScheduled}) {
                                        mvSoundCurrent.mmReplaceSound(mvFirstSound.mvSoundRef)
                                        if (!mvFirstSound.mvCallFwdAlreadyIssued) mmCallForward(mvFirstSound.mvCallFwdRef, mvExtra).also{mvFirstSound.mvCallFwdAlreadyIssued = true}
                                        mvSoundCurrent.mmRewind()
                                        mvSoundCurrent.mmStart()
                                        mvSoundCurrent.mvMediaPlayer.setOnCompletionListener{mvSoundBuffer.removeFirstOrNull()?.also{mvSoundCurrent.mmRelease(); mmCallback(mvFirstSound.mvCallbackRef)}} //"?." Safety Check: In Any Situation Where We Return null, It Theoretically Shouldn't Continue To mmCallback() And Should (Therefore) Avoid A Subsequent Recursive Call (Since The Queue Would Theoretically Be Empty)
                                }
                        }
                }
        }
    //Pause All Pending Sounds, Release Allotted Resources, And "Clear" From The Queue
        fun mmStopAllPendingSound(mvBoth : Boolean) {
            mvSoundCurrent.also{it.mmPause(); it.mmRelease(); mvSoundBuffer.clear()}
            if (mvBoth) mvPrioritySoundBoard.also{for (mvSound in it) {mvSound.also{mvIt -> mvIt.mmPause() ; mvIt.mmRelease()}}}.clear()
        }

    ////////////////////////////////
    // CallForward/Callback Stuff //
    ////////////////////////////////

    //CallForward/Callback Variables
        private val mvNumberOfContacts = 3
        private var mvAppend = false
        private var mvDismiss = false
        private var mvWhichBlock = 0
        private var mvWhichContact = 0

    //CallForward/Callback Strings
        private var mvPseudoToastMessage = ""
        private var mvAirplaneMode              = mvContext.getString(R.string.mtAirplaneModePseudoToast)
        private var mvGpsExpired                = mvContext.getString(R.string.mtGpsExpired)
        private var mvLowSignal                 = mvContext.getString(R.string.mtLowSignal)
        private var mvNoContactsInApp           = mvContext.getString(R.string.mtNoContactsInApp)
        private var mvSirenMode                 = mvContext.getString(R.string.mtSirenModePseudoToast)
        private var mvSmsDelayed                = mvContext.getString(R.string.mtSmsDelayed)
        private var mvSmsDelivered              = mvContext.getString(R.string.mtSmsDelivered)
        private var mvSmsForwardedToContact     = mvContext.getString(R.string.mtSmsForwardedToContact)
        private var mvSmsGotExceptionToContact  = mvContext.getString(R.string.mtSmsGotExceptionToContact)
        private var mvSmsNotDelivered           = mvContext.getString(R.string.mtSmsNotDelivered)
        private var mvSmsNotForwardedToContact  = mvContext.getString(R.string.mtSmsNotForwardedToContact)

    //Special Callforwards
        private fun mmCallForward(mvCallFwdCode : Int, mvExtra : Int) {
            if (mvCallFwdCode != ClassEnum.CALLFWDNONE.mvInt) {
                //Default Values
                    mvPseudoToastMessage = ""
                    mvAppend             = true  //<-- Do We Append To An Existing Pseudo-Toast Block, Or Overwrite?
                    mvDismiss            = false //<-- Does The Pseudo-Toast Automatically Disappear?
                    mvWhichBlock         =  2    //<-- Bottommost Pseudo-Toast (I.E. Number 2) Block Is Our Default
                    mvWhichContact       = -1    //<-- This Variable Is So We Can Forward Which Contact (Was Contacted) To The Broadcast Receiver

                //Fetch The Appropriate Pseudo-Toast Message
                    when(mvCallFwdCode) {
                        ClassEnum.CALLFWDAIRPLANEMODE.mvInt      -> mvPseudoToastMessage = mvAirplaneMode.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDGPSEXPIRED.mvInt        -> mvPseudoToastMessage = mvGpsExpired.also{mvAppend = false}.also{mvWhichBlock = 0}
                        ClassEnum.CALLFWDLOWSIGNAL.mvInt         -> mvPseudoToastMessage = String.format(mvLowSignal, if (mvSendExtra) " $mvExtra dB" else "").also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDNOCONTACTS.mvInt        -> mvPseudoToastMessage = mvNoContactsInApp.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDSIRENMODE.mvInt         -> mvPseudoToastMessage = mvSirenMode.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDSMSDELAYED.mvInt        -> mvPseudoToastMessage = mvSmsDelayed.also{mvAppend = true}.also{mvDismiss = false}
                        ClassEnum.CALLFWDSMSDELIVERED.mvInt      -> mvPseudoToastMessage = mvSmsDelivered.also{mvDismiss = true}
                        ClassEnum.CALLFWDSMSDELIVERYFAILED.mvInt -> mvPseudoToastMessage = mvSmsNotDelivered.also{mvDismiss = true}
                        else                                     -> for (mvI in 0 until mvNumberOfContacts) {
                                                                        if (mvCallFwdCode == ClassEnum.CALLFWDSMSSENTTOCONTACT.mvInt + mvI)
                                                                            mvPseudoToastMessage = mvSmsForwardedToContact.also{mvWhichContact = mvI}
                                                                        if (mvCallFwdCode == ClassEnum.CALLFWDSMSFAILEDTOCONTACT.mvInt + mvI)
                                                                            mvPseudoToastMessage = mvSmsNotForwardedToContact.also{mvWhichContact = mvI}
                                                                        if (mvCallFwdCode == ClassEnum.CALLFWDSMSGOTEXCEPTIONTOCONTACT.mvInt + mvI)
                                                                            mvPseudoToastMessage = mvSmsGotExceptionToContact.also{mvAppend = false; mvDismiss = true; mvWhichContact = mvI}
                        }
                    }

                //Signal The Pseudo-Toaster (Located In ActivityCaregivee)
                    if (mvPseudoToastMessage != "") mvContext.sendBroadcast(Intent("mbPseudoToast").putExtra("mvToastMessage", mvPseudoToastMessage).putExtra("mvAppend", mvAppend).putExtra("mvDismiss", mvDismiss).putExtra("mvWhichBlock", mvWhichBlock).putExtra("mvWhichContact", mvWhichContact).putExtra("mvTimeAddend", 0))
            }
        }
    //Special Callbacks
        private fun mmCallback(mvCallbackCode : Int) {
            //Our Only Current Callback Is If We "Close App" From "Secondary Caregivee Button" (I.E. Notification Swipe) During A "True" Foreground Process
                when (mvCallbackCode) {
                    ClassEnum.CALLBACKCLOSEAPP.mvInt -> {mmStopAllPendingSound(true)
                                                         mvClosingApp = true
                                                         Handler(Looper.getMainLooper()).postDelayed({ exitProcess(-1) },3500)} //<-- Delay Exiting Until After The Toast — Which Is Handled In BeginForegroundServiceRunnable — Disappears (Source: https://stackoverflow.com/a/7607614/16118981)
                    else                             ->  mmScheduleSound(0, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvAddSoundToQueue = false, mvImmediateCallFwd = false, 0) //<-- Under Normal Circumstances, We Want To Just Move Onto The Next Sound In The Queue
                }
        }

}