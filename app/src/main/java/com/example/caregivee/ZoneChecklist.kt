package com.example.caregivee

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import kotlin.system.exitProcess

class Itinerary {
    /* Itinerary:
   Unicode Symbols:
      1/2 symbol: ½
   TIPS: Maybe only have buttons on top half of SETTINGS screen to prevent stuff from spilling offscreen.
   TIPS: ...
   [√] Settings "Add Contacts"   (have the screen be scrollable?)
   [√] Settings "Change Refresh Rate" (have the screen be scrollable?)
   [√] Warning if you try to choose 911, 112, etc. as a contact.
   [√] Have "Contacts" and "Change Refresh Rate" Save To Disk
       No extra permissions required for intra-app read/write.
   [√] Make sure the timeout in Settings won't time out if a contact is outdated.
          Actually, this already does this!
          Since it uses the same method as just clicking the back button.
   ===
   Since Refactoring...
   [√] Calls are ending prematurely now since the refactoring, not sure why.
       * It may be because I fixed the text message sending so it didn't just go to 555-555-5555.
          That being the case, it might be running into an issue if it was sent too soon afterwards.
          Doesn't seem to be the case.
       * Might be because I declined signing up for that RCS chat.
       * Might just need to reset emulator or caches.
         This appeared to work, cleared out the VCS caches.
   [.] Between Countdown -4 and -5, We Get: This Error: 2023-11-23 18:36:26.519  4983-4983  FrameTracker            com.example.caregivee                E  force finish cuj, time out: J<IME_INSETS_ANIMATION::1@0@com.example.caregivee>
       Recurs sporadically, doesn't seem to affect anything, and there's not much documentation online about it.
   ===
   [√] Settings "Get GPS Location" (use a separate test app to test first)
   [√] Send Text to Primary Contact (ONLY if mobile available)
   ===
   [√] Make Phonecall to Primary Contact
   [√] Modes without permissions:
          [√] Text-Only Mode (No Call Permissions)
                 Simply ignore calling.
          [√] No GPS Mode (No Location Permissions)
                 Simply ignore sending GPS data.
          [√] Manual Input Mode (No Contacts Permissions)
                 Create a New Activity for text input, or maybe there's a view/dialogue for that.
          [√] Call-Only Mode (No Text Permissions)
                 Make it so mvStartCallingAfter is set to 0.
                 Ignore text sending.
          [√] Siren Mode (No Permissions Chosen Whatsoever, The App Just Hollers For Assistance)
                 Ignore everything and just blare a siren.
   [√] Change color to orange/red regardless of if we have text/call permissions,
       especially so it knows when to start sirening.
   [√] Don't even bother with the contacts if we're in siren mode.
       Gray them out, gray out the text, and turn off the checks.
   [√] In Manual Input Mode,
       the Contacts should show red before inputting them.
   [√] Remove any non-digits from phone number before going from Custom Number addition to Settings
       Number-based input fields won't allow it.
   [√] Timeout the RecyclerView.
       Change Timeout To Global Object (i.e. maybe move the countdown to its own class?)
       Regarding Above, Is Passing A Function's Reference Through A Method Parameter-Safe?
       Though reflection may cause issues (like when trying to access hidden APIs), this one seems like it would be pretty stable.
   [√] Change name of app to "Caregivee: Emergency Text Sender" Per Below...
>> [.] ... Hang up when about to make next text. (May be unnecessary, it seems to place a concurrent call and put it on hold.)
       Can you hang up on an unexpected incoming call too?
       Note: Hanging up a call or trying to merge calls may rely on hidden APIs, and may therefore have unreliable compatibility with different phones.
             It may be that even sending multiple calls and hanging them up based either on time or phone states might create more confusion in situations where someone might require assistance.
             Therefore, it may be beneficial to move this app to a purely text-message-based alert system.
                That way, the caregiver can call the caregivee to check if they're OK being less concerned about the caregivee sending a call to them or someone else simultaneously.
                HOWEVER, this does preclude landlines, for which we should probably check to make sure it's a MOBILE and not a HOME phone.
                   If MOBILE, send text.
                   If also EMAIL, send email.
             Maybe adding email capabilities might also help.
>> [.] If location permissions are set to one-time only, and either a phone call is made or we defocus the app...
       ... the permissions reset after a short time and when we return to the app, it can crash.
       The best solution here might be to use try{}catch(){} and if permissions are denied, simply use the last GPS data.
          Tried this, but thus far, I haven't been able to keep the program from stopping on defocus.
          Is this simply an issue for any program that requires this permission?
          Is this simply an issue for any program that requires this permission?
          Is this simply an issue for any program that requires this permission?
          If so, and we do remove call functionality and steer this only towards texting, this shouldn't be a huge issue (at least on a dedicated phone).
       Conclusion from testing: It appears that you merely need to ask for FINE/COARSE LOCATION permissions in order to trigger Android's idle timeout for permissions.
                                No currently known solution.
       Warn user via Dialogs that it would be best to avoid one-time permissions before we ask for that permission
   [.] Permissions for app running in background to override defocus timeout? ACCESS_BACKGROUND_LOCATION
       Nothing currently found.
   [√] Move permissions to its own object
>>>[√] Reference the permissions object every time we need permissions somewhere (for if permissions have been changed)?
       One way we could do this is by:
          1) Placing runnable contents in mmRunnableContents, and then each time the handler refreshes, we call for a permissions check.
          2) Other activities are just a matter of doing it onCreate, although...
          3) Also beginning of Back button on Settings too. (we shouldn't need to reinitialize the ClassPermissions object).
          4) If we're in Siren Mode and permissions change in the background to allow calling/texting, perhaps have it immediately snap us back to the Settings Menu so we can choose contacts.
       >>>It might be best to have it triggered by button presses and runnable repeats. Although, we may also need the top of onCreates() for defocusing/refocusing.<<<
       Maybe check permission only (don't launch requester) using checkSelfPermission.
       Then simply adjust settings.
   [√] For some reason, the Spinner is causing issues with the above!
       If we allow Contacts, then extra-app-ularly remove that permission, then go back into the app, it crashes on the Spinner stuff (but only AFTER onCreate completes its thing).
          Exception: java.lang.NullPointerException: Parameter specified as non-null is null: method com.example.caregivee.ActivitySettings$onCreate$4.onItemSelected, parameter view
          Solution:  Change from View to View? in the Spinner's "onItemSelect" method.
   [√] Certain screens should probably immediately redirect if there was a permissons change.
          [√] RecyclerView
   [√] Dialog box describing which mode(s) we're in.
       And where to go to fix it.
       Three-way dialogue box might work out well: (https://www.digitalocean.com/community/tutorials/android-alert-dialog-using-kotlin)
          Just pass strings and functions by reference in the parameters of an abstract dialogue object.
       Use the change-your-mind permissions dialogue.
       The modes as the end user would see them:
          HIERARCHY (Going Down, Each Top Mode Supersedes Each Lower Mode In Precedence, And Only The Highest Relevant Member Of The Hierarchy Is Shown To The End User):
          =========
             Siren Mode !4,!5
                Call-Only Mode !4
                   Anonymous Location Mode !1,!2
                      Full Caregivee Mode 1,2,4,5
   [√] Throw an exception if the phone has no contacts?
       Or, just revert to Manual Input Mode (see above).
          Settings: Just to a permissions check at the beginning of
          RecyclerView: Redirect to Manual Input for RecyclerView
   [√] Use a scrollview for some of your notices (like "Please ___! and Please ___!"): (https://stackoverflow.com/questions/1748977/making-textview-scrollable-on-android)
   [√] Warn user via Dialogs that it would be best to avoid one-time permissions for location, as that permission can timeout when navigating away from app.
       Did so on the "PLEASE ___" notifications page (aka ActivityBehest)
   [√] What should be the flood limit between texts?
       Appears to be 30 texts in 30 minutes for modern devices.
       Perhaps limit to 1 every 2 minutes to be safe.
   [√] Text-Call Pattern:
       Text -> Contact 1
       2m
       Text -> Contact 2
       2m
       Text -> Contact 3
       2m
       Text -> Contact 1
       2m
       Text -> Contact 2
       2m
       Text -> Contact 3
       2m
       Text -> Contact 1
       2m
        CALL! -> Contact 1
       2m
       Text -> Contact 2
       2m
        CALL! -> Contact 2
       2m
       Text -> Contact 1
       2m
        CALL! -> Contact 3
       Repeat pattern from when we started calling.
   [√] Add some stochastic variation to the refresh intervals.
       Use Thread.sleep(ms)? (https://stackoverflow.com/questions/45213706/kotlin-wait-function)
       CURRENT THING IS:
           This causes a delay in the button turning yellow (maybe try moving it to top of runnable, and only trigger on subsequents).
           This might be causing other weird delays.
           Maybe look into this.
   [√] Currently red/gray is reversed for when contact lists are available!
   [.] ^ Send texts first to the three contacts, then try calling instead if BroadcastReceiver shows no calls pending.
       May be unnecessary to use BroadcastReceiver.
>>>[.] For if we get defocused and the process is ended/restarted... have it savestate every other countdown?
       Or, maybe just comment out all savestate queries and instead back up current relevant savestate information to disk and then have onCreate redirect to Settings Activity upon refresh (where it can take some time and then return to Caregivee).
       The "App Keeps Closing" dialogue might be interesting to figure out how to fix in this situation.
          Current Theory: It seems to run into a problem as soon as it loads the ClassGPS stuff.
                          It seems to run into exceptions due to lack of location permissions, can't resume the activity, and our focus ends up getting shunted to a different activity.
                          The solution is probably to put the permissions stuff in its own class and then run a check in the onCreate()s.
                          It is also possible we could use this technique (https://stackoverflow.com/questions/6682792/how-do-i-detect-if-the-user-has-left-my-app)
                          ... to determine when the app has been defocused.
                          Then, we could optimize so we only run a check if we were logged out.
                          OR... we could onCreate always update our referring activity.
                             It seems to save this from last time. If so, and referring activity = this activity, then we could check.
                          OR....... Always send an Extra when you start a new activity and check if getIntent().getExtras() == null
                             If it does, we weren't referred.
                          ^ Both face challenges, as it does restore the INTENT EXTRAS, so it doesn't return null.
                            Also, it does pull those INTENT EXTRAS, so we get the previous settings state where we the referring activity is ACTIVITYSETTINGS, and if we try to fetch it before that, it's just the initialized null values.
                               Possible Solutions:
                                  Move the savestate recovery to the first thing we do onCreate.
                                  Check to see if referring = this activity.
                                  Change the referring activity in settings to current.
                                  ^ This way, if the state is saved, we can fetch the referring activity and determine what to do.
                               Possible Solutions:
                                  Also, to avoid the issue of having to reaffirm, we can just ignore all the GPS subroutines if we can determine we've returned from an ended process due to defocusing.
   [.] Have it truly check to see if the text/call went through.
       Not sure if my current methods are adequate (especially since it shows successfully texted when the URL is too long and it doesn't send... or at least, ostensibly so)?
       [√] Appeared to be catching exceptions correctly so far for that SMS I tried to send one to an email.
       [.] Call might need to have the exception checking changed to what it recommended on the stackoverflow article.
           ActivityNotFoundException e
           Although that's theoretically a subclass of Exception e, so Exception e is probably OK.
           Maybe include both, with subclass before class: https://stackoverflow.com/questions/38442108/will-a-subclass-catch-block-catch-a-checked-parent-exception
   [√] Try to send text messages to HOME-only contacts regardless of whether they're mobile or not, just in case.
   [√] Update contact list to include Email.
       Note: There don't appear to be any currently-known ways to induce SMS redirect of messages intended for email recipients.
             Instead, all known methods to send email seem to require an email client.
       Perhaps we should, therefore, exclude any contacts that are email-only.

   [√] Turn making texts and calls into its own class.
   ===
   [√] Alternate (altern8) Through Contacts For Each Missed Check-In
   [√] Speed Up Check-Ins After One Missed Check-In (maybe twice speed)
   [√] Have it recheck on Back button press whether the contacts are outdated as an extra safety precaution (reload/recheck)?
       Turn the check into a method in the ContactsReceiver.
   ===
   [√] Add Audio "Please Check In!"
   [√] Add Audio "Assistance needed!!!"
   ===
   [√] "Please fully charge or plug in phone."
       "Please test app, first!"
       "Please exit app when not in use!"
       "Please use a dedicated phone that doesn't normally receive calls/messages!"
       "Please use in conjunction with other devices, like a life alert system. Do not rely on this app alone."
       "Please don't use an SMS shortcode as a contact! Those require an extra confirmation!"
   [.] Short numbers (5 digit numbers like text 12345... rates may apply) might lead to a '"Might lead to charges!" confirmation box.
       "SMS shortcode"
       Most SMS shortcodes worldwide are less than 10 digits, perhaps with a major exception being sometimes in Italy: https://android.googlesource.com/platform/frameworks/base/+/master/core/res/res/xml/sms_short_codes.xml
   ===
   [√] Negative and positive latlon
   [√] In all Log.d's, change "Tag"'s to line#'s.
   [.] Research if use of !! is safe?
       Currently it looks like it just throws an exception if it is null.
   [√] MakeLogo (BG Color: 1DAAAA)
   [√] "Refresh Debug Factor" and "Refresh Rate" for ClassCountdown too?
   [√] Extra-appular change to permissions while in one activity (which usually re-triggers onCreate), then we BACK BUTTON to another... what happpens?
       Test for back button press and prompt the refresh dialogue, or like... refresh... or like... just go to Settings?
       Maybe just have it refresh the page :)
          IF the page refreshes on refocus, pressing back seems to initiate onCreate anew!
          HOWEVER, sometimes it appears a defocused page (even outside of the "Long press here to chang permissions") button, can indeed return to the app without refreshing, and the back button will show outdated information.
          Perhaps we should, no matter what... prompt that "Refresh Page" DIALOGUE whenever we defocus from the app (after a bit of waiting via a runnable).
             However, it might be better to not do so if the main Caregivee App is running.
       Note: The solution here using Application.registerActivityLifecycleCallbacks() (https://stackoverflow.com/questions/6682792/how-do-i-detect-if-the-user-has-left-my-app)
             ... is on an app-wide basis.
             As such, we should be able to just use onPause() to calculate if the app has been defocused on a local Activity-wide basis, and then just start the delayed "Refresh Screen" dialogue.
                [.] It seems to detect well, but it also triggers on INTRA-app back-button presses.
                    Just have it consult Settings to see what the current Activity should be. If it's different, then don't bother refreshing?
                    Or, maybe refreshing would be helpful considering there might be outdated information?
                [.] Figure out how to calculate whether a dialogue is running, so there aren't conflicts with other potential dialogues.
                    First, maybe test to see if it's triggering onPause multiple times O.O
                       Possible fixes for detecting when the Dialog isShowing()
                          Make it so there's only one runnable that plays when onPause is clicked, and if that runnable is already playing, don't launch a new one?
                          Initialize the dialog as a static, then check equality of current dialogue against parameters.
                          Use onResume instead?
                      [√] Initialize the dialog as a static (someone said though not to keep it as a static), then check isShowing()
                             https://stackoverflow.com/questions/51882807/alertdialog-isshowing-always-returns-false
   [√] In case ALL the Contacts are invalid, have Activity TelephoneContacts have a back button at the top or maybe bottom (or as just one of the list)
   [√] If local Contacts are empty and we're in Caregivee Activity for some reason...
          ... please redirect to Settings Activity.
   [√] Just post a dialogue (not toast) about "Number might be too short! Possible SMS shortcode. Please review to make sure this is a valid number for your region and resubmit."
       Possible clash with Refresh Page dialogue. Maybe do a different activity instead?
       [√] ActivityTelephoneContactsRecyclerView
           Update ClassPhoneNumberLength so it can handle number-too-short checks as well as emergency number checks.
              Instead of a dialogue here, maybe switch to a different activity.
              A Custom Toast? https://stackoverflow.com/questions/11288475/custom-toast-on-android-a-simple-example)
                 Custom Toasts are deprecated. Look into snackbars or a toast-like thing (that only appears when the app is on https://stackoverflow.com/questions/63312296/method-toast-setview-is-deprecated).
           >>>Maybe launch a Framelayout TextView ovwerlay that appears for a few seconds.
       [√] ActivityTelephoneContactsManualInput
   [√] Actual values for permissions enums as indices in the mvSettings.mvPermissions[*] ArrayList?
   [√] User might expect that we cycle through mobile and home numbers.
       Maybe cycle through them for phone calls, but not for texts?
          If that's the case, and we're planning on removing call functionality, maybe remove Home Numbers.
          Or if we don't remove, since subdominant numbers aren't checked in the contacts for Emergency Numbers, maybe do an extra check on the Caregivee screen.
   [√] Call Functionality Sunset:
          [√] Grandpa Gojira shows "null" next to his line item.
          [√] If you manually turn off and on location permissions, it still shows anonymous location mode.
          [√] In Siren mode, if we have no contacts and go back to Caregivee, it pushes us back to Settings.
   [.] Practical Testing (Results from testing on actual phone):
       [√] +000.000000000000000 turns into a link for some reason (replace + with 0)
           The negative valued one "-00.000..." isn't showing the leadingmost 0.
       [√] Make a toast that says GPS permissions expired Restart app?
           Build this into the permissions checker?
           Maybe use Frame Layout to overlay a fade-out warning.
       [√] Since GPS permissions are completely reliant on texting permissions, maybe avoid the "GPS Expired" warning if texting is not permitted?
       [√] Text not sent and call not sent should probably NOT display EXCEPTION dialog in the toast, but instead something like "Call Not Sent" or "Text Not Sent"
       [√] Cellar with ostensibly low signal: "Text sent!" But the text's bg is grayed out and there's an exclamation point with something like a "Couldn't send text." with options for resending etc.
           Implement a broadcast receiver and test accordingly:
              Simulate no signal with airplane mode? (untested)
              Send a text that's too long? (this works)
           Note, if all else fails, a significant lag in actually starting the GPS sniffer might indicate that a text has, or will, fail.
              Maybe change "Text sent!" to "Text attempted!"
       [√] * As one of our notices on Behest, tell users that GPS is only an approximation.
       [√] * Tell in Behest to make sure adequate signal.
   [√] "+" Still isn't being replaced by "0" in MachCall, check if this is the case in Caregivee too?
   [√] Simplify code? Remove also, let, run, and when where applicable.
   [.] Switch over from toasts to a FrameLayout overlay View for all the SMS-related toasts.
       Maybe not, perhaps it's better to keep it subtle.
   [√] If in ActivitySettings, you minimize and then swipe it away:
          android.view.WindowLeaked: Activity com.example.caregivee.ActivitySettings has leaked window DecorView@682aea5[ActivitySettings] that was originally added here
       This may be a result of a pending RefreshPage dialogue that is in progress due to onPause when we X out of the app.
       This may be very low priority.
       Dismiss any "Dialog" prompts when onDestroy is called.
   [.] Timeout in Manual Input can be inconsistent due to the long intervals between the Runnable's iterations and the way we detect user interaction to extend it.
       This is probably a very low priority concern.
   [√] Check code/comments for readability and understandability.
   [√] Check null [√], nulls [√], ?: [√], ?., and !! [√] stuff?
   [√] GPS Permissions Expired
       Have Derpy say so?
       It currently overlaps with "Please check in".
       Figure out how to delay one over the other?
   [√] Is there a better way to check GPS permissions expiry?
       Like by checking if it's locally true but globally null or something?
   [√] What happens if mvReferral == ClassEnum.ACTIVITYCAREGIVEE and we go back to settings?
       Maybe use a GPS Expired Flag and explain in the Status Window?
   [.] Is using the nyquist rate for collecting GPS data (i.e. collecting it halfway between each iteration) really wise, because it might be extremely outdated for a caregivee who is travelling.
       Maybe change to every 30s instead?
            Maybe have an mvNyquist variable that we can set to true if we want to do it the current way.
       It's reliant on mvTextRefreshRate, not mvSettings.mvRefreshRate (which can be quite long)
       So we're probably OK.
   [√] In permissions, look at this: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
       There doesn't seem to be a valid "else" block for this one. Please consider looking into.
          https://stackoverflow.com/a/34084609/16118981
          https://developer.android.com/training/permissions/requesting
       Before that version, does the user only grant permissions at installtime?
          If so, maybe just default to mvSettings.mvPermissions?
          Or is there a different way to find out which ones were selected at installtime, if this is the case?
       From my understanding, the only way the app even works prior to Android 6 is if all permissions are granted...
          ... and they're immutable for the lifetime of the app.
          So, we're probably A-OK :)
   [√] Still some audio overlap, perhaps due to razor-thin asynchronous calls. Probably lopri, but maybe think about it.
       One possible solution:
          Audio Buffer Empty?
              Add to audio buffer, remove any callbacks on a runnable and post immediately.
          Audio Buffer NOT Empty?
              Add to audio buffer.
          ======================================================
              Runnable (First Entry Exists in Buffer):
                 Play the first entry.
                 Remove first entry from audio buffer.
                 Have the runnable repost after the duration of current.
                 When the runnable reposts, repeat the above lines.
              Runnable (First Entry DOESN'T Exist in Buffer):
                 Repost after a period of 400ms just in case there's an asynchronous call pending and raise a flag.
                 If it's still not populated on the second pass, don't post another one.
       Current Solution:
           class ClassSound { //<-- Only Instantiate Once Per Activity And Call "mmPlayAudioSynchronously()" Whenever We Want To Play An Audio File
                //Create A Collection Comprising Each Sound We Currently Have In The Queue
                    private var mvAudioBuffer: MutableList<MediaPlayer> = arrayListOf()

                //Countdown Variables
                    val mvHandler = Handler(Looper.getMainLooper())
                    private lateinit var mvAudioRunnable : Runnable

                //Initialize
                    init
                    {
                        mvAudioRunnable = object : Runnable {
                            override fun run() {
                                //If The Audio Buffer Is Empty, Let's Ignore This Posting Of The Runnable
                                    if (!mvAudioBuffer.isEmpty()) {
                                        //Play Sound
                                            mvAudioBuffer[0].start()
                                        //Upon Completion Of Sound...
                                            mvAudioBuffer[0].setOnCompletionListener {
                                                //Release Resources
                                                    mvAudioBuffer[0].release()
                                                //Find And Remove The Sound From The Buffer
                                                    mvAudioBuffer.removeFirst()
                                                //Keep Reposting If We Have More Sound(s) In The Queue
                                                    if (!mvAudioBuffer.isEmpty()) mvHandler.post(mvAudioRunnable)
                                        }
                                    }
                            }
                        }
                    }

                //Play Our Sound One At A Time (Reference To Audio File Stored In The "mvAudio" Parameter As "R.raw.___")
                    fun mmPlayAudioSynchronously(mvActivity: Activity, mvAudio: Int) {
                        //Add Our New Sound To Queue
                            mvAudioBuffer.add(MediaPlayer.create(mvActivity.applicationContext, mvAudio))
                        //Post The Runnable Manually If We Just Added To An EMPTY Queue
                        //Otherwise, The Runnable Just Keeps Reposting Itself If The Queue Has Pending Sounds
                            if (mvAudioBuffer.size == 1) mvHandler.post(mvAudioRunnable)
                    }
                //Stop All Pending Audio
                    fun mmStopAllPendingAudio()
                    {
                        for (mvSound in mvAudioBuffer) mvSound.stop() //<-- Stop Any Currently Playing Sounds
                        mvAudioBuffer.clear() //<-- Remove All Pending Sounds From The Queue
                        mvHandler.removeCallbacks(mvAudioRunnable) //<-- Remove Any Pending Runnable Postings
                    }
            }
       NOTE On The Current Solution:
       ============================
          The two things we need to consider with regard to issues with asynchronous calls:
             Accidentally posting two simultaneous runnables.
                (Unlikely, since due to the "control flow" it should always show a queue size of 2 when the second sound is added, which wouldn't allow a new posting of the runnable.).
             Accidentally removing a runnable when there's a pending sound being added.
                (Unlikely, since due to the "control flow" it should always show a queue size of 1 if a new sound is added juuuuuuust after the runnable decides not to repost, which would — in turn — cause a new posting of the runnable.)
   [√] What if suddenly we're unexpectedly NOT in siren mode but all of the contacts are null?
          This is already handled with mvAboutToSwitch, which sends us back to Settings.
       It might be more helpful to switch on PsuedoSiren Mode, and then just post a warning:
       Theoretical Solution:
          NO CONTACTS SET, PLEASE GO TO SETTINGS (derpy voice).
   [√] What happens if we nullify the intent extras before they can be fetched via ClassSerializableHandling?
       Will it throw an exception?
          It will keep saying "No contacts, please go to settings!"
   [.] Fully terminate app if onPause() handler runs for a full minute?
       Send a toast that says "App Shutting Down" along with a voice?
          Is this a good idea? What if someone accidentally touches the home button?
   [√] No runnable needed for Audio Playback?
       Pseudocode:
          mmRegisterAudio
             Sound -> Queue
             if (queue.size == 1) mmPlayAudio
          mmPlayAudio
             Queue[0].removeOrNull?.start.also(it.onComplete {it.release AND mmPlayAudio})
                (Note, preventing double posting of "start" might be difficult if we remove from buffer BEFORE it's finished)
   [√] Have audio for "GPS permissions expired" do it every caregivee cycle?
       Just have ClassPermissions raise the flag, and then the main caregivee app play the sound if the flag is raised.
   [√] What if you just leave it on in the background while doing other things? The app?
       How to keep it going? Possibly here: https://groups.google.com/g/phonegap/c/SKy5_Mt9wc8?pli=1
       What we may need is a FOREGROUND SERVICE.
          A foreground service keeps an OS Notification in the foreground while the app does its relevant stuff in the background.
          This is probably how LOUD ALARM CLOCK works :D
             In-Depth Example: https://medium.com/@dugguRK/kotlin-music-foreground-service-play-on-android-4b57b10fe583
             Simpler, But More Outdated Example: https://www.geeksforgeeks.org/foreground-service-in-android/
          This may add too much complexity to Caregivee.
             If so, maybe have a dedicated splash between Begin and Behest (BeGreat)
                In this one, use a simple dancing phone animation and explain in fade-in text that
                   when the app is in use, the app should ALWAYS be maximized.
                   The phone should be a dedicated phone that doesn't receive many calls.
          [√] If we do a foreground service... for SMS sending in Caregivee, we may need to do something like android:foregroundServiceType="location|mediaPlayback|specialUse" in the manifest.
                https://developer.android.com/about/versions/14/changes/fgs-types-required
                https://developer.android.com/about/versions/14/changes/fgs-types-required
          [√] Note: Send mvSettings and stuff through the intent extras if using foreground service.
          [√] POST_NOTIFICAIONS, what if the user doesn't select it?
              We may need to figure out an alternative (like having the service stuff run in an app).
              According to this link: https://developer.android.com/develop/ui/views/notifications/notification-permission
              We may not even need to ask for this permission.
                 In practice we appear to indeed need to do so.
                 [√] Make it so POST_NOTIFICATIONS is only included post tiramisu.
                 [√] Figure out how to make it work without POST_NOTIFICATIONS permission approval.
                     Place all the code in its own object and then either run it from a service or not depending on the android build version.
          [√] Click-to-green isn't currently working.
          [√] Setting Context.RECEIVER_EXPORTED when registering seems to be necessary for communication to the "Views" from the Foreground Service
              Maybe try updating this in the MANIFEST too for both Caregivee and Labyrinthine?
              [.] Note, I think I answered a question on STACKOVERFLOW where I had it not exported, might want to change that.
                  https://stackoverflow.com/questions/7465849/how-to-use-notification-deleteintent/14723652#14723652
              [.] But why the "Exported receiver does not require permission" error in the manifest?
                  https://stackoverflow.com/questions/16112470/android-exported-receiver-does-not-require-permission-on-receivers-meant-to
                  You can (theoretically) add something like android:permission="android.permission.POST_NOTIFICATIONS" to specify which permissions any external apps need to interact with our app (since they can interact when we export).
          [.] It seems to timeout after ~10m or so, even with foreground process going.
              It might be because it's doing WAAAAAAY too much in debug mode (i.e. sending requests too frequently).
              Perhaps slow down the refresh rate and try again?
              Maybe if it's at the normal slow speed, the emulator won't try to end the process prematurely.
                 It lasted ~2.5h on the normal slowspeed in emulator.
          [√] Clicking on Settings isn't properly dismissing the foreground process.
          [√] Side Note: When Android says that notification permissions aren't required for a foreground service to run,
              yet a notification MUST be posted...
              ... it may be only from the perspective of what they allow on the Play Store...
              ... because in practice it seems like we can do this on the emulator.
   [√] Foreground SEervice "Notification" currently shows placeholder %1$s in string.
   [√] Add "... Please go to settings" at the end of the "GPS Permissions Expired" audio?
   [√] Create an SVG vector Caregivee icon for the Notifications.
   [√] When Caregivee main resets, make sure it sends an Intent that cancels any outstanding services.
       The current method of communication requires using startForegroundService() to update things.
       This might not be the most ideal method if the user turns off notifications, right?
       Unless we can just run stopForegroundService?
   [√] If the program tries to add extra multiline spaces to expressions (like when it turns a single line Toast into a multiline with a new line for each parameter):
       Replace ([\(|,])(\r\n[^a-z]+?)([a-z]) with \1\3
       Replace (\r\n)([^a-z]+?)(\)) with \3
       Replace (^[^\/]+?)([a-z]|\}|\{|\)|\() with \1    \2
   [.] Have Caregivee's main section just keep checking on each runnable posting what our current permissions are?
       If there's a change, have the phone adjust accordingly (e.g. make it so we can initialize the GPS location listener within the runnable, and make it so we can warn when the local contacts are empty but we're not in siren mode.)
          The only issue with testing this is that the activity always seems to restart when we defocus from Caregivee activity.
          The solution here might be "need to set flag "alwaysRetainTaskState" to "true" for main Activity in your AndroidManifest.xml." https://groups.google.com/g/phonegap/c/SKy5_Mt9wc8?pli=1
             I think you can only do this with the main activity.
                It may be that the other activities inherit this.
                   Currently attempted:
                      If we turn location setting off to on, it doesn't reset the activity.
                      If we turn location setting on to off, it does reset the activity.
                Or, you might need to make Caregivee the main activity.
                   (Also, test to see if it keeps going indefinitely when onPause?)
            [.] The refresh only seems to occur (EVERY TIME) when we turn off GPS Location Permissions mid-Caregivee extra-appularly, not when we switch it on.
                   Probably a low priority, if at all... especially since the refresh basically solves the issue anyway.
   [.] Notification specifics can be adjusted extra-appularly.
       Maybe test these out to see how they work?
          Granular changes to the Notification Channel permissions don't seem to prevent the Foreground Service from working.
          Granular changes to Notification Dots really only refers to that little pink or orange dot on the top corner of the app's icon (on some devices).
   [√] TEST "Coarse Location" (changeable in the App Permissions Settings)?
       Are there situations where we ONLY check Fine Location? (I don't think so...)
         [√] Should we consider placing the GPS permissions checker in every activity so we can have the GPS permissions flag expire anywhere?
  [√] What of the app not turning on the Foreground Service automatically when permissions are changed extra-app-ularly?
      Well... we don't have it prompt to refresh the activity, do we?... when we turn on Location settings extra-app-ularly which would otherwise allow Foreground Services.
      Maybe instead of showing a dialog prompt for refresh, we just have it refresh automatically.
  [√] Status Window:
          [.] It still says "with notifications" in the permissions window even if the more granular Foreround Service Channel sub-permissions (in the Notification Permissions header) are specifically turned off in the Phone Settings
          [√] Maybe remove the one for contact list access/notifications, but retain the one for coarse location.
          [√] Rename Full Caregivee Mode to "Emergency Texter Mode"
  [.] The question of early timeouts with the Foreground Service.
         Early timeouts seem to be proportional to how many resources our foreground service uses.
            On Emulator:
               We timeout in about ~10 min when we cycle through iterations at 1/16 of a minute each.
               We timeout in about ~120 min when we cycle through iterations at 1 minute each.
            On Phone:
               Labyrinthine Jelly Beans seems to timeout roughly when the phone gets charged.
               It may last longer when it's not dismissed from "task manager" (i.e. hitting that vertical triple bar).
               It when it does go offline, if Dad swipes back to the pane that contains Labyrinthine Jelly Beans on the homescreen, it actually seems to cause the Notification to regenerate.
                  Might be time sensitive.
         Perhaps we could try:
            [√] Removing the timing variation on the thread (which halts service for a bit).
            [.] Simplifying the code.
                [.] Reducing the repeat rate.
  [.] "Pause App Activity If Unused" is a setting in the phone's app permissions.
        ============================
          "Remove permissions, delete temporary files, and stop notifications."
       See if that can be adjusted from within the app through USER prompt?
          This is called "App hibernation": https://developer.android.com/topic/performance/app-hibernation
             "If your app targets Android 11 (API level 30) or higher, and the user doesn't interact with your app for a few months,
              the system places your app in a hibernation state. The system optimizes for storage space instead of performance,
              and the system protects user data. This system behavior is similar to what occurs when the user manually
              force-stops your app from system settings."

              Possible exemption:
                 Work profile apps
                    Any app that a user installs on a work profile. Note that if the same app also resides on a personal profile, only the work profile app is exempt.
                    (Impractical)
              There is a way to request the user make the adjustment on the page: https://developer.android.com/topic/performance/app-hibernation#api-code-example
                 However, from the "//" comments, it appears to only allow you to warn the user and ask them to make the adjustment themselves.
       This seems to only be an issue after MONTHS of disuse.
          This app likely won't be used longer than a day at a time (after all, people need to sleep).
   [√] Tell users that they should only use this app for several hour stretches, no longer.
       Maybe summarize salient points on a specific Activity:
          ⏰ Please use only for a few hours at most.
          🔬 Please test thoroughly before actual use.
   [√] Then, maybe make the top of the normal warnings page say "PLEASE READ ALL"
   [√] Remove settings.permissions and just check permissions in realtime?
       [√] (1) There are situations where we currently adjust the values locally for the sake of ease.
                  E.G. As with manually switching READ_CONTACTS permissions to false if there are "no contacts" in the phone even if the permissions are otherwise true,
                  so it forces manual contacts input.
                  These probably just need to be reprogrammed accordingly.
               Search for "?!?!?!?!?!?!?!?!" (as that's how these situations are currently labeled).
       [√] (2) Another similar situation appears to be when we raise flags (and play audio) based on changes to permissions (e.g. GPS permissions expiring).
                  Hm....
                  Maybe just make the flags as variables in mvSettings instead, and call a function where desired to raise flags accordingly (e.g. for the status window and for each caregivee iteration).
                  (a) Instead of comparing the current permission to a previous (e.g. if GPS permissions WERE true but now they're false so we raise a flag), see if the current permission is false and the previous FLAG (not permission) is also false.
                      Use that instead
                  (b) Just make a permissions checker in ClassPermissionsMode, and have each mmAreWeIn___Mode enter different permissions into the parameters of said permissions checker.
                  ===
                  One issue that seems to be occuring is that when we start with no GPS permissions.
                     Then manually turn them on...
                     Then manually turn them off again.
                     The last one seems to gather a legacy version of mvSettings from a savestate...
                     ... thereby rendering the flag outdated and resetting to ClassEnum.GPSNEVERSTARTED.
                  Maybe just move the initial GPSNEVERSTARTED assignment out of the conditional that checks if the previous activity was BEHESTRECYCLERVIEW...
                     ... then check if mmAnonymousLocation && previous activity == BEHESTRECYCLERVIEW,
                     ... and do else's and else if's from there.
                  Maybe use shared preferences for this flag, or a superglobal variable.
                     Maybe use a singleton: https://stackoverflow.com/questions/9613055/declaring-a-global-variable-to-use-in-all-activities
                                            https://www.baeldung.com/kotlin/singleton-classes
                     Name the singleton ClassFlags() and use it for potential flags, since it really doesn't need to be saved to disk?
                        Here's where there's an issue:
                           Whenever it auto-refreshes upon removal of location permissions, it seems to reinstantiate the object even though it shouldn't!
                              Tried this with static (companion object) and non-static variables.
                           It doesn't seem to be drawing from a savestate...
                              ... because if I have the constructor assign a default value of ClassEnum.GPSNEVERSTARTED in the SingletonFlags class...
                              ... but set the variable Singletonlags.mvGpsPermissionsExpired to ClassEnum.GPSTEST in ClassBegin...
                              ... the process where we begin with permissions off, turn them on, and shut them off again doesn't revert to ClassEnum.GPSTEST, but instead ClassEnum.GPSNEVERSTARTED (as though it gets reinitialized).
                           What's weird about this though is that it does still seem to retain contact info from mvSettings that was previously saved.
                              Maybe we could try saving the current value onInstanceSave?
                              SInce it seems to remember the last value we selected for the interval, maybe if we adjust a View to hold this value accordingly, perhaps it will be remembered? But then... what of Caregivee Main?
                     Maybe don't use singletons: https://programmerr47.medium.com/singletons-in-android-63ddf972a7e7
                        Doesn't seem to fix it yet.
                     Or maybe just extend Application() to create a global variable:
                        https://stackoverflow.com/questions/52844343/kotlin-set-value-of-global-variable
                        Doesn't seem to fix it yet.
                  ======================
                  [√√√√√√√] Explanation!
                     Whenever we turn off Location permissions from its "on" state, it ends and restarts the process.
                        So the use of singletons or Application() level variables won't do, because their state will collapse upon the process ending.
                        Instead:
                           Maybe just write to disk whenever it CHANGES, and then pull from disk if referrer is same (or possibly it would be "NONE" if the process is being manually restarted).
                           Then maybe move some of the less essential mvSettings flags to the singleton, because the singleton is really cool =D
                        Note: Seems to work great!
                  [√] Please extend this from Settings also to the main Caregivee activity :)
       [√] (3) Bad notification error preventing the foreground service from starting.
                  This is a weird one. It seemed to only start once I switched over, but then I noticed another recent version did this too!
                  So, it might be that there was some kind of Permissions thing that didn't kick in until we uninstalled/reinstalled.
                     Backup 393 is working.
                     Backup 424 is not.
                  Compare all files (including XML files) to see what might've happened?
                  (1) Looks like I re-declared the "val" for mvChannelId between initialization and assignment, should've been var:
                      private val mvChannelId = ""
                              ===
                      val mvChannelId = applicationContext.getString(R.string.mtChannelId)
                      ===
                  (2) BUT... it also seems to be caused by trying to use a string resource for the channel ID.
  [√] Windows leaked error in ActivitySettings. Maybe try to make sure they dialogs get properly dismissed?
       It seems to, when we're in AnonymousLocation mode (that is, when Foreground Services are also off)
           show an ActivitySettings window leaked error even when we dismiss the app from ActivityCaregivee
           Actually, it always seems to show a leaked error for a dialog that was created in ANOTHER activity.
              Which makes sense, since we only remove the one for our current activity.
              Try removing callbacks from the ClassDialogue handler/runnable.
                 Maybe use a companion object so it always refers to the same one?
       This may be because I'm passing a STRONG reference to the Activity into the dialog handling class.
           Maybe try doing a weak reference: https://stackoverflow.com/a/23333620/16118981
                                             https://stackoverflow.com/questions/23332267/leaked-window-error-with-progress-dialog-even-after-calling-dismiss/23333620#23333620
           "My guess is that you're leaking the activity into the currentUser object, when you pass the CruiseDetailRollCallActivity into currentUser.saveInBackground(new SaveCallback().... The SaveCallback class that you just created now has a strong reference to the Activity. It will never be garbage collected, even though you exit the method. You should use a WeakReference and then it can be garbage collected.

            WeakReference<CruiseDetailRollCallActivity> weakRef = new WeakReference<CruiseDetailRollCallActivity>(CruiseDetailRollCallActivity.this)
            Then, pass the weakRef in to the ProgressDialog constructor:

            progressDialog = ProgressDialog.show(weakRef.get(), "", "Loading...", true);
            Whenever you're passing around the Context in Android, check to see if you need a WeakReference so it can be garbage collected. It's easy to leak the entire application."
       AHA!
          It's running onPause, and therefore launching a refresh dialog... WHEN we press the back button!
             If we simply switch a flag on when we're about to change activities:
                It does prevent a window leak if we go to Settings, immediately to Caregivee, and then defocus.
                However... if we first go to Settings, then LONG PRESS to go to Phone Settings, then go back to Settings, onward to Caregivee, and then defocus... it does lead to an error.
                   Maybe try triggering the flag when even one refresh has been interacted with?
                   I think we just need to go in and make it so we .dismiss() on mmRefreshYes(), that way... we can properly dismiss that instance of the dialog before the page refreshes.
                      Attempted. May have reduced the number of concurrent errors to only 1 (if there were more than 1 before).
                                 There may still be another insteance out there somewhere.
                                    Turns out we were also posting a dialog in the method that brought us into phone settings.
                                    So, coupled with the one that also is posted when we onPause(), our efforts were being doubled.
                                       Maybe try to prevent multiple simultaneous calls?
                                       Perhaps turn dialog pausing on, then off upon dismissal?
          There were three locations altogether that needed an mmRemoveDialog() adjustment:
             First, onPause and mmExtraAppularPermissions both were triggering mmRefreshYes() redundantly upon Extra-app-ular adjustments.
                Removed it for the latter.
             Two, when we press the Back button in the app, onPause is activated that woudl trigger mmRefreshYes().
                So we added a variable that temporarily paused the dialog class when we switch activities.
             Three, whenever we call mmRefreshYes, we probably need to call mmRemoveDialog(), just like whenever else we switch activities.
    [.] Error "android.view.WindowManager$BadTokenException: Unable to add window -- token android.os.BinderProxy@78b90bc is not valid; is your activity running?"

        Odd error, not sure how to reproduce.
           Seems to be dialog-based
              https://stackoverflow.com/questions/9529504/unable-to-add-window-token-android-os-binderproxy-is-not-valid-is-your-activ
        [√] Have we considered... maybe... just moving away from the built-in dialog system?
               Perhaps override the builder and instead make our own using FrameLayouts :)
               Or just have it autorefresh the page instead!
                  The dialogs really aren't necessary.
   [.] Fix possible memory leaks by using weak instead of strong references?
       Maybe try doing a weak reference: https://stackoverflow.com/a/23333620/16118981
           "My guess is that you're leaking the activity into the currentUser object, when you pass the CruiseDetailRollCallActivity into currentUser.saveInBackground(new SaveCallback().... The SaveCallback class that you just created now has a strong reference to the Activity. It will never be garbage collected, even though you exit the method. You should use a WeakReference and then it can be garbage collected.

            WeakReference<CruiseDetailRollCallActivity> weakRef = new WeakReference<CruiseDetailRollCallActivity>(CruiseDetailRollCallActivity.this)
            Then, pass the weakRef in to the ProgressDialog constructor:

            progressDialog = ProgressDialog.show(weakRef.get(), "", "Loading...", true);
            Whenever you're passing around the Context in Android, check to see if you need a WeakReference so it can be garbage collected. It's easy to leak the entire application."
       !!!!!!! [.] Just make mvThis : Activity = WeakReference<Activity>(this).get()!! and replace every this accordingly?
       ===
       Instead, all the Activity context function arguments have been removed,
       and we're currently trying to only pass applicationContext per this post:
            https://stackoverflow.com/a/57742074/16118981
            "You are free to pass a Context to a class that its not attached to an Activity
             in any way you like. Passing it through the class's constructor is
             a good practice (dependency injection), but only in the case where your class
             needs a Context to fully function correctly.
             If you need a Context to use only in a specific method,
             might as well pass it as an argument to the method.

             The most important thing you have to be aware here is that an Activity
             has a finite life cycle. If you keep a reference to an Activity Context
             in a class that will outlive that Activity, you will create a memory leak:
             the garbage collector will not be able to destroy that Activity
             because there's a reference to it somewhere.
             This is why people usually prefer to handle an Application Context instead,
             which will always outlive any class you can create."
   [.] Remove certain unnecessary lines of code:
       FROM CLASSGPS
       =============
        private val mvLocationPermissionCode = 2 //<-- Each Code Is USER-Defined And Is Used To Differentiate Between Different Permissions Requests: (https://stackoverflow.com/a/54041627/16118981)

        //We Handle Our Permissions Checks Elsewhere, So This Conditional And Its Contents Are Likely Superfluous
        //However, Its Presence Does Prevent An Error With The Subsequent Line Of Code within mmGetLocation()...
        //... So, It's Probably Better To Keep It Active Instead Of Suppressing The Compiler Warning
                //Get Location
                   fun mmGetLocation() {
                            if ((ContextCompat.checkSelfPermission(mvApplicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                                ActivityCompat.requestPermissions(mvActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), mvLocationPermissionCode)
                            }
                   }
        //onRequestPermissionsResult() Is Not An Overridden Function In LocationListener, Therefore It Must Be An Override Of A Function In AppCompatActivity()
        //We Handle Permissions Requests Elsewhere, So We're Probably OK To Ignore This Section
                //Strings 'n' Things
                    private val mvPermissionsGranted : String = mvActivity.getString(R.string.mtPermissionsGranted)
                    private val mvPermissionsDenied : String = mvActivity.getString(R.string.mtPermissionsDenied)

                //Permissions
                    fun onRequestPermissionsResult(mvRequestCode: Int, mvPermissions: Array<out String>, mvGrantResults: IntArray) {
                        if (mvRequestCode == mvLocationPermissionCode) {
                            if (mvGrantResults.isNotEmpty() && mvGrantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                Toast.makeText(mvApplicationContext, mvPermissionsGranted, Toast.LENGTH_SHORT).show()
                            }
                            else {
                                Toast.makeText(mvApplicationContext, mvPermissionsDenied, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
   [√] Test Synchronous audio:
                Handler(Looper.getMainLooper()).post(object : Runnable {  override fun run() {
                    mvClassAudio.mmPlayAudioSynchronously(R.raw.ma_please_check_in)
                    mvClassAudio.mmPlayAudioSynchronously(R.raw.ma_help_i_need_help)
                    mvClassAudio.mmPlayAudioSynchronously(R.raw.ma_gps_permissions_expired_please_go_to_settings)
                }})
                Handler(Looper.getMainLooper()).post(object : Runnable {  override fun run() {
                    mvClassAudio.mmPlayAudioSynchronously(R.raw.ma_help_i_need_help)
                    mvClassAudio.mmPlayAudioSynchronously(R.raw.ma_please_check_in)
                    mvClassAudio.mmPlayAudioSynchronously(R.raw.ma_gps_permissions_expired_please_go_to_settings)
                }})
                Handler(Looper.getMainLooper()).post(object : Runnable {  override fun run() {
                    mvClassAudio.mmPlayAudioSynchronously(R.raw.ma_gps_permissions_expired_please_go_to_settings)
                    mvClassAudio.mmPlayAudioSynchronously(R.raw.ma_help_i_need_help)
                    mvClassAudio.mmPlayAudioSynchronously(R.raw.ma_please_check_in)
                }})
   [√] ClassPermissions -> mmRegisterPermissions(mvActivity : ComponentActivity)
          Figure out an alternative to passing the Activity, or pass a weak reference through the parameter)
   [√] mmPleaseCheckInClick in ClassCaregivee should also check if !mmForegroundServiceMode() within the !this@ActivityCaregivee::mvBeginForegroundServiceRunnable.isInitialized conditional.
          Then, just have it restart the activity if so.
   [√] Listening for scrolling a RecyclerView:
            //Get RecyclerView By ID And Give It An onScrolled Listener
            //https://stackoverflow.com/a/63720115/16118981
                mvRecyclerView = findViewById<RecyclerView>(R.id.mxBehestRecyclerView).also {
                    it.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        private var mvIsScrolledDown = false
                        override fun onScrollStateChanged(mvRecyclerView: RecyclerView, mvNewState: Int) {
                            super.onScrollStateChanged(mvRecyclerView, mvNewState)
                            if (mvNewState == RecyclerView.SCROLL_STATE_SETTLING && mvIsScrolledDown) {
                                Log.d(Throwable().stackTrace[0].lineNumber.toString(), "Scroll state changed!") //<-- This Line Is Here To Prevent An "'if' has empty body" Error
                            }
                        }
                        override fun onScrolled(mvRecyclerView: RecyclerView, mvDx: Int, mvDy: Int) {
                            super.onScrolled(mvRecyclerView, mvDx, mvDy)
                            //Have We Scrolled Down?
                                mvIsScrolledDown = mvDy < 0

                            //When We Scroll The RecyclerView, Let's Make Sure None Of The...
                            //... Fade-In Animations Have Reset Themselves:
                                mmAnimateAlpha()
                        }
                    })
                }
   [√] Move warnings to String Array for the Warning List?
   [√] Change "Audio" -> "Sound" and "audio" -> "sound "
   [√] Change mvAnonymousName to simply display the phone number instead?
   [√] Warn with AUDIO if SIREN mode?
   [√] Change all broadcast actions to mbBroadCastAction (so like "notification_clicked" and "notification_cancelled" become "mbNotificationClicked" and "mbNotificationCancelled")
   [√] OK, so if we try to click on the notification, it's not reopening the app.
       It's entirely possible that now that we've made it possible to garbage collect Actvities by using weak references and applicationContext references,
       that it can't easily open up or communicate with the Activity anymore.
       That being the case, the question is can we even reset the app from the background?
       Can we open an activity from the background?
          Loud Alarm Clock seems to be able to do it.
          Maybe try this, but there MIGHT be an issue with memory leaks: https://stackoverflow.com/questions/57843074/how-to-return-to-app-on-notification-click
             Perhaps a WeakReference would do it?
          Maybe try this, open a new app: https://stackoverflow.com/questions/3872063/how-to-launch-an-activity-from-another-application-in-android
             Like So:
                if (mvAction == "notification_clicked") {
                    //Did The User Click On The Notification?
                        mvContext.getPackageManager().getLaunchIntentForPackage("com.example.caregivee")?.also{mvContext.startActivity(it)}  //"?." Null Safety Check: We Shouldn't Be Able To Start The App Anew Unless The Intent Is Non-null... Though Probably Not A Huge Deal If It IS null Because User Can Just Try Clicking Again Or Swiping Away
                        exitProcess(-1)
                }
             Doesn't seem to work as currently attempted (even without exitProcess(-1)
          Maybe try this approach: https://stackoverflow.com/a/46071075/16118981
               mvContext.getPackageManager().getLaunchIntentForPackage(mvContext.getPackageName()).also{it?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)}.also{mvContext.startActivity(it)}
               Doesn't seem to work as currently attempted.
          Maybe try this approach: https://stackoverflow.com/a/56526043/16118981
               mvContext.startActivity(Intent(mvContext, ActivityBegin::class.java))
               System.exit(0)
          Maybe try this approach: https://stackoverflow.com/q/23398827/16118981
               mvContext.startActivity(mvContext.packageManager.getLaunchIntentForPackage(mvContext.packageName))
          Maybe try this approach: https://stackoverflow.com/a/23399020/16118981
               val mvNewIntent = Intent(mvContext, ActivityBegin::class.java)
               mvNewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
               mvNewIntent.action = Intent.ACTION_MAIN
               mvNewIntent.addCategory(Intent.CATEGORY_LAUNCHER)
               mvContext.startActivity(mvNewIntent)
          The reason all of these seem to not be working is likely due to recent restrictions: https://stackoverflow.com/a/60568011/16118981
       NOTE: We should probably figure out something to do with this, as clicking the notification would essentially be the extra-app-ular equaivalent of clicking the Please Check In button.
                Try WeakReference method?
                Maybe instead, just have it say "App shutting down" in a toast (with DERPY AUDIO), then run a runnable that exits the process.
                Because the toast appears behind the notification window, the toast might be insufficient.
                Maybe have it change the notification first to say "App shutting down", then proceed to AUDIO via runnable.
                   Doing so works strangely, as it basically dismisses the notification screen and then you have to re-swipe down.
                   What if we (instead) have clicking the notification simply act as a check in.
                   That could work very well!
                      Just have it update the update the button whenever we click on the notification?
                      Just have it update the notification text whenever we update the button?
                         Notification: "Please check in"
                         Notification: ":)"
               [√] The notification now works like an alternate caregivee button.
               [√] If you press the button and then go back to the app (assuming you didn't swipe it out of the Recents menu),
                   the button turns out not to have been altered (probably because the broadcast receiver in the activity likely doesn't work onpause).
                   Maybe have the button check the countdown value onResume and adjust accordingly :)
                   Maybe just force the runnable to go through to the next iteration onResume to update verything.
               [√]    Use Shared Preferences instead?
   [√ ] Derpy: "App dismissed."
   [.] Toast: "Closing app."
       Haven't been able to get the toast to consistently appear.
       Might not be running it on the UI thread: https://stackoverflow.com/a/7803293/16118981
       Or it might be interfering with all the other toasts.
          Maybe make it so all caregivee toasts will override to "Closing app" if a flag is set?
       Maybe try putting all your SMS broadcast receivers and combine them into one broadcast receiver Runnable-side.
          Then, you can keep reusing the same broadcast receiver,
          you can save the toasts to a list so they can be cancelled en masse like in that one stackoverflow answer...
          then you can cancel all of them before you send the "closing app" toast.
          [√] Set it up so the broadcast receiver for SMS toats is in BeginForegroundServiceRunanble
          [.] Make it so it sends the contact name as an extra.
              For some reason, it doesn't seem to be updating our info each time and is only ever showing the first contact.
              The solution is perhaps to send a different Action each time (one for each Contact) that is instead properly parsed.
          [√] Set up a one-stop shop for toasts (with toasts stored in a list) in the Caregivee runnable, and cancel all pending toasts when it's time to close the app.
              This is still running into an issue, where it's not actually cancelling the toasts, and any interfering toasts prevent the Closing App toast from appearing.
              [_] Try testing to see if toast cancellation is actually working.
              [_] Maybe try having the callback of each toast simply be showing the next toast?
              [_] Maybe just try having it add closing app as the callback for all pending toasts?
              [_] Maybe try turning all pending toast messages into "Closing App" and then setting the callback?
              [.] ALSO, maybe only keep a certain number of toasts (like 8) in the queue).
              [√] MAYBE MAYBE MAYBE Move to something similar to ClassSound, where we use a queue (stop using a max queue size) and wait before each new toast is posted.
                  Because it turns out this isn't just an issue with the Closing App toast, it seems all the toasts are a bit off when the activity is closed in the Recents and the foreground service is on.
              [√√√√√√√] If the ClosingApp toast doesn't run, maybe just have a Failsafe Derpy for Closing App.
   [√] Move all text status Toasts to simply a status window in the "Please Check In" text in the Please Check In button? (i.e. Pseudo-Toast)
       That way, the messages better sync with the audio, and since it's pretty inconsistent with toasts in Foreground Mode, it's probably not a huge deal.
      [√] Derpy: Airplane mode is on, or maybe texts not being sent.
      [√] Have Derpy also say "Siren Mode" if "Airplane Mode" isn't on, yet "Siren Mode" is.
      [√] ^ Have this also happen on Settings Page.
   [√] Pseudo-Toast cleanup:
         [√] Move caregivee runnable Toasts to pseudo-toasts.
         [√]    Perhaps the non-standard warnings like No Contacts, and GPS Permissions expired should take advantage of the mxPseudoToast0 block instead of the standard mxPseudoToast2 block.
         [√] Make it so pseudo toasts stack on top of each other with a maximum number of newlines, like a queue.
         [√] Make it so pseudo toasts disappear after a few seconds (preferably with a built-in fade effect, or something stylish like change all the characters to asteriks and remove).
         [√] Make it so pseudo toasts are broadcast for consideration PRECISELY when the audio plays in the queue.
                I.E. A CallForward function as opposed to a CallBack function.
         [√] Make it so for the stackable pseudo-toasts, that it only dismisses them when the delivery notification arrives.
         [√] Turn all the other PseudoToasts into Callforwards for better alignment.
         [√] Should airplane mode and siren mode likewise trigger PseudoToasts?
             If so, aim them for the third block, since there would be no SMS being sent anyway.
         [√] Check the app offline stuff to make sure it's working properly.
             Maybe now a toast will work since there's no theoretical conflict :)
   [.] Make it so if the Activity is destroy()'d AAAAAAAND the caregivee runnable is going BUUUUUUUT it's not a foregroudn process AND we're not switching activities (whew)... that we exit out of the process.
   [.] If we send an SMS to the first two contacts, then check in... then we go through the whole thing again and start sending SMS to contacts again, it begins by sending a alert to the THIRD contact.
       Might actually be a sound idea, or at least fairly low priority.
   [.] If you try to exit out of the app via the Secondary Caregivee Button (the Notification-based one)...
       ... but the Activity wasn't dismissed from the Recents menu, it will remain in the Recents menu (though with the process closed).0
       Perhaps try setting a SharedPreference and then onResume if that SharedPreference is true, close (or maybe better RESET) the Activity?
          If so, maybe consider trying to post about this discover here, as someone did mention somewhere about the Recents menu lingering thing:
             https://stackoverflow.com/questions/51831820/how-to-close-android-application-in-kotlin
       Probably fairly low priority.
   [.] In GPS Permissions Expired mode, for some reason, it doesn't always seem to end the process if we just swipe out of recents.
   [.] Bring Pseudo-Toasts to other activities?
       Probably unnecessary.
   [.] Are the high-order functions causing a memory leak?
       After commenting them out, it appears this is not the case.
   [√]Less confusing wording:
            //Check To See If GPS Permissions Have Expired
            //(Note: So We Can Keep Track In Shared Preferences If We Change Permissions Extra-app-ularly During This Activity, Which Should Trigger An Activity Restart)
   [.] Do we need to recreate the object each time?
       mvCoordinates = ClassGPSCoordinates(mvContext)
       mvCoordinates.mmUpdate("0", "0")
       CONCLUSION: Probably trivial.
   [.] Change over to "compound drawables" for recycler views: https://stackoverflow.com/questions/8318765/how-do-i-use-a-compound-drawable-instead-of-a-linearlayout-that-contains-an-imag
       CONCLUSION Probably trivial.
   [√] Make it so all onClicks in XML's are transformed into onClickListeners
   [.] Create a transparent vector and use that instead of a transparent color for empty list items.
       It wouldn't allow a transparent vector, so I changed over to a block-shaped vector that I programmatically color-filtered black.
   [√] Code for ActivityCaregivee not yet reviewed.
   [√] Clarify which broadcasts receivers do and do not need to be listed in the manifest.
   [√] Maybe make GPS permission sexpired less commonly heard during checkin?
       if (mvCountdown % 5 == 1 || mvCountdown <= 0) //<-- Alert More Often If It's Time To Check In
   [.] Does the null-case properly show up?
       mvNotificationBuilder.setContentTitle(mvIntentParam.getStringExtra("mvNotificationUpdate") ?: mvError) //"?:" Safety Check: It Should Just Show An Exception If This Value Is null, Similar To How It Works In mmChangePleaseCheckInButtonProperties() In BeginForegroundServiceRunnable
   [.] Make sure this line in onPause() in ActivitySettings: mvClassFileManager.mmSaveSerializable(mvSettings,"mvSettings")
       is working OK in practice.
   [√] Just in case one of them has been nullified, "while loop" through till there's one that isn't:
       if (mvSettings.mvContacts[mvContactsAlternationText].mvMobile != null) {
   [√] Test out all the Status Window combinations for mvSettings?
   [.] Have GPS Permissions Expired audio play after all others in Settings? Might be better to just have it wait until app starts, and rely on red warning text in settings' status window.
   [√] Check the new pseudo-toasts under text fail conditions.
   [√] Check pseudotoasts under defocus conditions?
       1) All known pseudotoasts:
             Block 0
                5 minutes till check-in.
                    //Automatically and quickly dismisses (after full overwrite of block), and unlikely running into any major conflicts.
                GPS Expired w/ Dismiss (used during before check-in time)
                    //Automatically and quickly dismisses (after full overwrite of block), and unlikely running into any major conflicts.
                !!!!!!! GPS Expired w/o Dismiss!
                    //If you show GPS Expired! in pseudotoast block 0, then defocus and change the permissions manually and come back, GPS Expired! will still show.
                    //Or, just erase the text data in block 0 and wait for the next iteration to inform user.
                    //OR, in this case... maybe put the "GPS Expired!" tester in its own function and call that function onResume().
             Block 1
                 No contacts!
                    //Unlikely that doing anything outside the app would change anything, as any global contacts changed in the phone during the main Caregivee activity wouldn't theoretically change any local contacts. Plus, it occupies its own block and is EXTREMELY rare to begin with.
                    //It will probably adjust itself on subsequent iterations.
                    //However, if the loss or gain in contacts for whatever reason does occur while we're defocused, it doesn't immediately reflect the change.
                    //Theoretically solvable by simply erasing any pending text data in block 1 onResume().
                    //OR, in this case... maybe put the "No Contacts" tester in its own function and call that function onResume() (but depending on timing, this may interfere with the next iteration, perhaps running a new iteration would be better... but then there's a potential flood SMS concern).
             Block 2
                 !!!!!!! SMS status.
                    //If you defocus on a previous pseudotoast like "Text Sent to Contact 1!\nText Delivered!", then wait a while until you hear "Text Sent to Contact 2!" and refocus... it will add the subsequent "Text Delivered!" to the PREVIOUS "Text Sent to Contact 1!\nText Delivered" block.
                    //If you defocus after "Text Sent to Contact 1!", don't wait for "Text Delivered!" before navigating away, and then navigate back... it will have never posted "Text Delivered!" and therefore won't dismiss.
                    //As such, we may need to erase any pending text data in the third psuedotoast block immediately onResume(). Probably not worth even waiting for a slight pause, but doing so immediately.
                 !!!!!!! Airplane Mode on.
                    //It will remain on if you go and turn it off.
                    //Theoretically solvable by simply erasing any pending text data in block 2 onResume().
                    //OR, in this case... maybe put the "Siren Mode" tester in its own function and call that function onResume() (but depending on timing, this may interfere with the next iteration, perhaps running a new iteration would be better... but then there's a potential flood SMS concern).
                 !!!!!!! Siren mode on!
                    //If you defocus to turn OFF SMS permissions, it will just automatically reset the activity.
                    //If you defocus to turn ON  SMS permissions, it will show "Siren mode on!" until the texts are sent.
                    //Theoretically solvable by simply erasing any pending text data in block 2 onResume().
                    //OR, in this case... maybe put the "Siren Mode" tester in its own function and call that function onResume() (but depending on timing, this may interfere with the next iteration, perhaps running a new iteration would be better... but then there's a potential flood SMS concern).
             Omnibus Solution(s):
                First, erase all three blocks onResume().
                Then make it so we do a non-texting iteration onResume(), so we can adjust all three blocks immediately sans sending a new text message.
                   BUT! Only if we're far enough away from the next iteration? (doesn't seem to be necessary to have this time buffer in practice)
                   A non-texting iteration would check Airplane Mode, Siren Mode, No Contacts, GPS Permissions, and NOT repost.
   [√] Go through and double check to see if any of the resource files or XMLs are no longer necessary.
   [√] In the gradle build file, it looks like we may be able to update some of our dependencies (maybe just save a copy of the ones we're currently using.
   [√] Recheck all code class-by-class for any weirdnesses. [√√√]
   [√] Check for memory leaks?
       Possible with GPS listener that continues even when we click back to settings?
          https://developer.android.com/studio/profile/memory-profiler
       [_] Is this.intent causing memory leaks.
       [.] Get rid of all high order function references (")e.g. ::mmFunction argument passes)
           I THINK this is only now an issue with the Countdown clocks.
           At present, changed the main Countdown clock method to "inline"... unsure if conclusive results.
   [√] What is the behavior if someone were to press the side power button, or if they were to let the screen fall asleep??
       So long as it's plugged in and set to a sleep time of greater than 15m, should be theoretically OK even still.
          MachCall still works when the power button is pressed off.
             Would the process expire due to one-time permissions in Caregivee?
             Does it just seem to rely on the memory timeout that seems to affect debug mode (with its rapid-iterating/rapid-SMSing functionality that may be deemed a battery life or memory concern)?
   [.] Current pseudo-toast update oddity:
       Defocused and went to permissions.
       Turned location off (or it was already off?) and turned SMS on.
       When I came back, ti said Siren Mode on.
       Then it appended Text Sent to Contact _
       Then it showed GPS Permissions Expired. (possibly due to synchronous audio and CALLFWD callforward delay)
          Why did it show Siren Mode on?
            This is difficult to replicate, but here's the theory:
               There was already a chain of synchronous audio going at the time of onResume.
               It had to go through the chain until it got to the right callforward that effectively renewed all data.
               [.] This is likely a minor and uncommon concern, as it's just mildly confusing if someone comes back to the app.
       [√] Possible Solution: Have the non-texting iteration supersede all the others?
           Have it send a priority signal — essentially by removing all the callbacks and callforwards — by clearing the sound queue on a nontexting iteration?
           [.] Prolly not extremely important.
   [√] Retest Notification permissions stuff now that we use it as the Secondary Caregivee Button.
       If you're defocused and turn off notification settings, it will stop the process and erase the notification.
          [.] This is an unlikely scenario.
       If you try to turn it back on, it won't if you need to check in, and waits for you to click to check in.
       It will automatically refresh if it's not check-in time.
          [√] Make sense if someone needs assistance.
   [√] Maybe add a "please only change permissions when in Settings" warning.
   [√] Exit App clickable textview added to Behest Recycler View suggestions list:
       "exit app via dedicated activity technique"
       ===========================================
       https://stackoverflow.com/a/78438221/16118981
       Here's a technique that should remove it from the Recents screen (tested on numerous emulators past API 21):
       First, we create a new Activity that specifically handles exiting the app:
           package com.example.yourAppNameHere

           import android.os.Bundle
           import androidx.appcompat.app.AppCompatActivity

           class ActivityTimeToExitApp : AppCompatActivity()
           {
               override fun onCreate(savedInstanceState: Bundle?) {
                   super.onCreate(savedInstanceState)

                   //Get The "Exit Screen" Layout
                       setContentView(R.layout.activity_time_to_exit_app)

                   //Immediately Exit App
                       finishAndRemoveTask()
               }
           }
        Second, when we want to exit the app, we simply switch to this activity, while simultaneously setting some flags to clear any pending tasks from the back stack (source):
           startActivity(Intent(applicationContext, ActivityTimeToExitApp::class.java).also{it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK})
        Please comment if there are any best practices concerns with this technique.
1  [√] Extensive testing on earlier versions of Android?
      [√] (Note: Also, maybe basic testing on the IN-BETWEEN versions: APIs 22, 25, 26, 32.)
              32: [√] Basic functionality
              26: [√] Text Failed! "Neither user 10077 nor current process has android.permission.READ_PHONE_STATE."
                      https://stackoverflow.com/a/38782876/16118981
                      ^ Added an special case for runtime permissions checking to Android API 26.
                      Check to make sure the one-time exception methodology works OK for UTSRQPONML and in-between APIs 32/25/22
              25: [√] Basic functionality.
              22: [.] Sends text, but there's no audio (which equates to no callbacks)
           Recommended, based on all the current "Build.VERSION_CODE" exceptions:
              [√] COMMON: Earlier Android Common Issues
                  [.] No delivery confirmation (nor text not forwarded error in some cases).
                         Possible quirk of the emulator.
                         Also, might just be the fact that we use the now-deprecated SmsManager.getDefault() before Snowcone, which is the version where the problem seems to go away.
                  [√] No swiping away the notification (on Lollipop, clicking even exits the app!).
                         [_] Solution 1: Add another suggestion that if the app won't close, try restarting? (or just add it to the new activity)
                         [_] Solution 2: Triple click Notification will exit app?
                                         Just have a runnable zero out a variable that is otherwise incremented with each click?
                         [√] Solution 3: Update the notification when we exit the activity to give suggestions on how to close the app.
                                         I.E. Update notification to explain how to exit app.
                                         Note: In Lollipop and Marshmallow, it truncates the "-it.)" in "Running…\n(Swipe or reopen app to exit.)" (R.string.mtHowToExitApp)
                  [√] Possible Overall Solution: Add an extra activity via responsive design for earlier versions that says "Some texting confirmation and other features might not show up in your version of android. Please use a phone with Android [INSERT NO HERE]+"
                  ===
              [√] COMMON: Later Android Issue:
                  [√] Callforward delays when defocusing and focusing with regard to updated pseudotoast alerts.
                      Possible Solution 1: Maybe add "Screen Updates Pending" to the middle block, and another audio after everything that dismisses it.
                      Possible Solution 2: Have the non-SMS-sending iteration just adjust pseudotoasts without any audio.
              ===============================
              [.] Lollipop
                  Things tested: 1, 2, 3 (<-- N/A for Lollipop and Earlier), 4, 5, 6, 7
                  [√] Oddly, when the sound glitches on this emulator, it doesn't play any audio.
                      If the sound glitches on Lollipop, try clicking the hamburger in the Device Manager and "Wipe Data" ing it (or doing a "Cold Boot").
                      And when the sound doesn't play (due to the glitch), it won't show the appropriate text forwarded pseudotoast. So it's like the media player actually isn't even running to be able to show any Callforwards.
                  [.] No delivery notification.
                      Note: This could just be a quirk of the specific emulator.
                            Or, as it doesn't show the text not forwarded error, maybe it's not showing delivery/forwarding failed.
                      Test by sending to another emulator?
                            Tried sending it to 5554, which was to myself.
                            Text received, but no delivery notification was sent.
                      May need to do time-specific searches for delivery notifications (e.g. Google searches for dates when Android 6.0 was around).
                  [.] Secondary Button won't swipe away.
                  [.] Will not show a text not forwarded error (like if the text length is too long).
                      Is it possible delivery is not going through on this emulator, and the failstate is why there's an issue with delivery display?
                  [.] Secondary Button disappears when clicked! No "App Offline" procedure, it just disappears.
                      This only happens if the activity is removed from the Recents menu.
                      If you click to check in from the Secondary Button, and then remove the app from Recents, then clicking it again dismisses it.
                  [.] Some emojis (like those in the Start and Settings button) are not available and show a placholder unicode.
                      [√] Probably trivial.
              [.] Marshmallow
                  Things tested: 1, 2, 3, 4, 5, 6, 7
                  [√] "Spinner" View text should have higher contrast.
                      Might require explicit markup.
                  [.] No delivery notification.
                      Note: This could just be a quirk of the specific emulator.
                            Or, as it doesn't show the text not forwarded error, maybe it's not showing delivery/forwarding failed.
                      Test by sending to another emulator?
                  [.] Secondary Button won't swipe away.
                      Even if explicitly .setOngoing(false)
                      Note: This could just be a quirk of the specific emulator.
                  [.] Will not show a text not forwarded error (like if the text length is too long).
                      Is it possible delivery is not going through on this emulator, and the failstate is why there's an issue with delivery display?
                  [.] Some emojis (like those in the Start and Settings button) are not available and show a placholder unicode.
                      [√] Probably trivial.
              [.] Nougat
                  Things tested:  1, 2, 3, 4, 5, 6, 7
                  [.] If you reopen to end the background process...
                      and immediately swipe out of recents...
                      it will ONCE try to restart the app just after where we left off (at the Behest screen).
                      [√] Probably trivial.
                  [.] No delivery notification.
                      Note: This could just be a quirk of the specific emulator.
                            Or, as it doesn't show the text not forwarded error, maybe it's not showing delivery/forwarding failed.
                      Test by sending to another emulator?
                  [.] Secondary Button won't swipe away.
                      Even if explicitly .setOngoing(false)
                      Note: This could just be a quirk of the specific emulator.
                  [.] Will not show a text not forwarded error (like if the text length is too long).
                      Is it possible delivery is not going through on this emulator, and the failstate is why there's an issue with delivery display?
              [.] Oreo
                  Things tested: 1, 2, 3, 4, 5, 6, 7
                  [√] System.getDefault() might be necessary in the texter for later versions than just Marshmallow, otherwise, errors might result.
                      Since it's deprecated in Snowcone, maybe just use System.getDefault() prior to Snowcone.
                  [.] No delivery notification.
                      Note: This could just be a quirk of the specific emulator.
                            Or, as it doesn't show the text not forwarded error, maybe it's not showing delivery/forwarding failed.
                      Test by sending to another emulator?
                  [.] Secondary Button won't swipe away.
                      Even if explicitly .setOngoing(false)
                      Note: This could just be a quirk of the specific emulator.
                  [.] Will not show a text not forwarded error (like if the text length is too long).
                      Is it possible delivery is not going through on this emulator, and the failstate is why there's an issue with delivery display?
              [.] Pistachio Ice Cream
                  Things tested: 1, 2, 3, 4, 5, 6, 7
                  [.] No delivery notification.
                      Note: This could just be a quirk of the specific emulator.
                            Or, as it doesn't show the text not forwarded error, maybe it's not showing delivery/forwarding failed.
                      Test by sending to another emulator?
                  [.] Secondary Button won't swipe away.
              [.] Quince Tart 1, 2, 3, 4, 5, 6, 7
                  Things tested:
                  [.] No delivery notification.
                      Note: This could just be a quirk of the specific emulator.
                            Or, as it doesn't show the text not forwarded error, maybe it's not showing delivery/forwarding failed.
                      Test by sending to another emulator?
                  [.] Secondary Button won't swipe away.
              [.] Red Velvet
                  Things tested: 1, 2, 3, 4, 5, 6, 7
                  [√] Emulator flickers:
                      C:\Users\vegas\.android\avd\Red_Velvet_Cake.avd\config.ini
                      Find the hw.gpu.mode = auto and change it to the hw.gpu.mode = guest and save the changes.
                      Restart the emulator.
                  [.] No delivery notification.
                      Note: This could just be a quirk of the specific emulator.
                            Or, as it doesn't show the text not forwarded error, maybe it's not showing delivery/forwarding failed.
                      Test by sending to another emulator?
                  [.] Secondary Button won't swipe away.
                  [.] If you swipe out of recents too quickly after starting main app, it's as though you swiped away the Notification and it goes to the "App offline!" audio (see the mbCloseApp broadcast in onDestroy() in ActivityCaregivee for a possible explanation).
                      [√] Probably trivial, especially since the user is (most likely) made aware of this verbally.
              [.] Snowcone
                  Things tested: 1, 2, 3, 4, 5, 6, 7
                  [√] Notification icon needs to be PNG, not SVG.
                  [.] Secondary Button won't swipe away.
                  [.] If you swipe out of recents too quickly after starting main app, it's as though you swiped away the Notification and it goes to the "App offline!" audio (see the mbCloseApp broadcast in onDestroy() in ActivityCaregivee for a possible explanation).
                      [√] Probably trivial, especially since the user is (most likely) made aware of this verbally.
              [.] Tiramisu
                  Things tested: 1, 2, 3, 4, 5, 6, 7
                  [.] Slightly longer gap between synchronous sounds on Tiramisu.
                      Half gap at the beginning of the "one" "two" and "three" files.
                      [√] Probably trivial.
                  [.] No notification sound when the notification changes.
                      Trivial.
                  [√] Anonymous location mode doesn't turn off foreground service capabilities.
                      Expected behavior.
                  [.] It turns red and keeps saying Please Check In for about (3 rounds?) before it says "HELP! I NEED HELP!" in Siren Mode.
                               ===
                      This might be a misobservation, or something caused by defocusing... because it's actually similar to upside-down cake functionality.
                      That is, that it turns red, says one more please check in, then goes on to HELP!
                      [√] Is it possible the longer audio gaps are just causing a backlog in the sound queue?
                          Prolly half-right, please see below:
                      ========================================
                      Here's what's (probably) going on:
                         Under normal circumstances, it sends a text on every other iteration.
                         On Siren Mode, it's easy to count EVERY iteration as somethign that would otherwise be a texting iteration, so it gives a false impression.
                         Nevertheless, there does appear to be one extra rogue Please Check In at the beginning of the RedThreshold that shouldn't otherwise be there.
                            This is caused by:
                               A backlog in the audio queue (likely lengthened by the unusual gap between synchronized sounds in the Tiramisu emulator).
                      [√] We may want to decrease our Red Threshold dramatically in Siren Mode.
              [√] Upside-Down Cake
                  Things tested: 1, 2, 3, 4, 5, 6, 7
                  [.] It stays orange and keeps saying Please Check In for about (3 rounds?) before it says "HELP! I NEED HELP!" in Siren Mode.
                               ======
              Things To Test:
                 1) All Activities
                    (Then add contacts to phone and try that activity.)
                .2) Secondary Caregivee Button.
                    (Also try to quickly swipe out of recents just when we start main Caregivee Activity, to see if it shunts to App Offline like it does in Red Velvet and Snowcone.)
                .3) Reasonable number of permissions permutations.
                .4) Airplane Mode.
                .5) Text not forwarded error.
                .6) Let it countdown fully?
                .7) Test to see if when you reopen the app it stops a foreground process.
                .8) Test to see if clearing Block 2 pseudo-toast at the start of every iteration (not just every other) is a decent idea.
                .9) Low signal.
                .A) Legal disclaimer.
                .B) Test if we get alerted if one-time permission have expired.
                .C) Test with notifications off. (Currently Only A Requirement To Ask For Notfications For Foreground Processes Via "Launch Runtime Permissions" Request For Tiramisu+, But We Can Still Manually Turn Off Permissions And Test Pre-Tiramisu)
                .D) Maybe test the specific things where we have an API check in the code.
                    Note: Most things have already been tested, but there are two that might require attention:
                       [√] Foreground Service: Is the Foreground Service working without Fine Location settings off? As we have that turned off for Upside Down Cake+ but not pre-Upside Down Cake.
                       [√] Serializable Handling: Are all local contacts correctly saving for next time?
                       Versions Tested: TSRQPONML
                .E) Test the pseudo-toasts when we defocus, alter permissions, and refocus?
                       Versions Tested: TSRQPONML
                .F) Test to see if checking in clears out Block 2?
                       Versions Tested: TSRQPONML
                       [.] Please note, that if you check in when it says Text Attempted, it will clear it out, skip over Text Sent to Contact X, and then show Text Deliered after a time. (at least, this is common)
                           Likely a trivial concern.
                .G) Test with screen off?
                    Standard AND Foreground Process
                    Including testing how low signal works for both?
                     How to test:
                        Launch ActivityCaregivee
                            (do this for standard, background, and standard with notfications off)
                            Immediately turn off screen.
                            Low signal alert (check logcat).
                            Whether it says it sends a text (listen for Derpy).
                            Check if GPS info was sent in the text.
                            Tested on: UTSRQPONML
                               Note: With Q, a little trouble with the red block still showing up for app termination, but this might just be an idiosyncrasy of the emulator.
                               Note: With N, things seemed to slow down and get somewhat inconsistent, but this was ostensibly fixed on a cold boot of the emulator (there was another emulator where that happened too, not sure which... but it was probably a temporary fluke like this one AS upon retesting, it didn't recur).
                            Results:
                                When Notifications are Off AND Screen Is Off, the signal strength is NOT acquired and it shows the default 100000 strength for Snowcone+.
                                   It is unclear whether the screen off affects pre-Snowcone, as that's based on a listener (although there are hints that pre-Snowcone, it works fine: https://stackoverflow.com/q/76807249/16118981).
                .H) What happens if you adjust permissions during full Foreground Mode?
                    Turn off location.
                    Restart and turn off sms.
                    Restart and turn off notifications.
                      Turn back on location/sms.
                    Restart and turn off notifications.
                    Restart and turn on notifications, turn off loc/sms permissions before foreground service, then turn them back on during the foreground process.
                    Progress:
                       UT (Turning off Loc, Sms, and Notif. in real time turns off foreground process)
                       SRQPONM (Turning off Loc and Sms turn off foreground process, but turning off Notif. only turns off the notification itself, not the foreground process)
                       L (Permissions not granularly adjustable on Lollipop after install, turning off Notif. only turns off the notification itself, not the foreground process)
                          Notes: when loc/sms are off, they all seem to turn back on and start sending texts when reactivated in realtime (though U+ can't even launch a foreground service without location permissions, so this was only tested with sms permissions off to start for U). Also, sometimes there is a delay in gathering GPS data when we reactivate permissions in realtime, meaning the first SMS might be dispatched with empty location data.
                .I) Rotate screen.
                    P+ (No rotation, possible quirk of emulator).
                    O (Rotation, but then it shunted to App Offline for some reason, with multiple concurrent audio of "App Offline" playing... not sure why... or if it was just a weird fluke)
                      (See "App Offline error" elsewhere on this page for details)
                      Changed Over To screenOrientation="Vertical", tested on ONML (note: never tested without screenOrientation on emulators N,M,L)
                 J) Possible Permutation (S-L): Turning notifications back on when already off.
                    SR______ (doesn't resend the notification, maybe it should?)
                    [.] Maybe we should resend the notification if notification permissions are off and then suddenly back on again for the sake of older versions.
                        This would theoretically require a listener, which might be memory intensive.
                        No known built-in listener, so this would probably need to be done with a runnable.
                        [.] Instead, we could just resend the notification on the next iteration if notifications were previously off, then suddenly back on again.
                            Then click button to test.
                            (So far tested in SRQPONM, doesn't work on L)
                               (however, user would need to turn off notifications before one iteration, and turn back on after another iteration for this to work)
                            Nevertheless, this is a fairly trivial issue since a weird permutation that makes the program not be able to stop would still theoretically mean help messages would be sent out.
                        [_] Maybe resend the notification when countdown < 0 regardless (every time we go through three contacts based on a modulo operation), just in case the notification otherwise disappeared?
                        [√] Or, maybe test to see if the notification is still active? If we're in Foreground Service Mode and it isn't, just repost on the next iteration.
                               Potential solution to check for active notification (Probably Marshmallow+): https://stackoverflow.com/questions/12654820/is-it-possible-to-check-if-a-notification-is-visible-or-canceled
                        [√] The above should be tested on all versions, but also (please) make sure swiping away the notification doesn't reactivate it!
       [√] App Offline error.
              In Android O (Oreo), it appears that if you go to the Settings Page, rotate the screen, then start the app, it very soon shunts over to the "App Offline" subroutine.
                 This could be because, for some reason, it doesn't a) initialize the Foreground Service and b) somehow calls the onDestroy() for ActivityCaregivee (which shunts us over to App Offline by the current code).
              This has been theoretically solved by simply preventing the user from being able to rotate the screen via screenOrientation in the manifest.
       [√] Note that if we use the Android Upside-Down-Cake (Google Play) version instead of Upside-Down-Cake (Google API Version)
           ... that any button rendered offscreen in a ScrollView will take between ~3-5 clicks to respond.
           May need to change over to a RecyclerView.
       [√] Also, have it parse the HTML?
       [√] The above RecyclerView should probably be animated like before.
       [.] Make it so each symbol is different and relevant to the warning at hand?
   [√] Update Stackoverflow about Texter to Build Codes >= ">= Build.VERSION_CODES.S"
       Update the Sent and Delivery intents to use requestCode's 100 & 200 respectively for Oreo and other earlier versions, so delivery notification will go through?
          The second one didn't appear to make a difference, but still might be worth altering in accordance with this: https://stackoverflow.com/questions/3875354/android-sms-message-delivery-report-intent
   [√] The issue of if we defocus, change permissions, and then refocus... when all the CallForwards are waiting in the queue and we have an unintuitive pseudo-toast updating control flow.
       [√] Make it so when you click on the check-in button it erases all audio.
       [.] Should it also clear out block 2, or would that be an issue in airplane mode and siren mode?
       [√] Maybe do no-audio pseudotoasts on an SMS-less iteration (textless iteration).
           This one works the best so far, and uses a parameter to shunt our mmScheduleSound to skip over the sound and just go to the pseudo-toast.
   [√] Note that if you don't get a Text Delivered notification, like when were testing in Oreo, that the Pseudo-Toast might linger.
       Probably not a huge deal, except when we check in.
       So maybe just run a textless iteration, sans "Please Check In"...
       and erase the bottommost Pseudo-Toast when we check in.
       OR (and maybe also) erase the bottommost pseudo-toast at the START of EVERY iteration (not just the every-other text iteration), just as a sort of "clean up" thing.
   [√] Not sure if Runnable was [√√√]'d (triple checked).
   [√] Maybe remove all backtasks when we're about to switchto the main caregivee activity via intent flags?
       Since there's the possibility that it will close when removed from the recents menu.
   [√] Clearer wording? --> "//<-- Please Keep This Out Of The Following "if (mvGpsOff) {}" Conditional, So It Can Properly Set The "Shared Preferences" Flag Even When GPS Permissions Are On"
   [√] The mvNullFlood section only checks for if Mobile field is null, but earlier, we check if all fields are null.
       Maybe switch both to just checking if the mobile field is null, since that's the most important one?
   [√] "if (mvCountdown <= 0) //<-- Please Don't Be Annoying And Only Inform User During Check-In XD XD XD"
       Perhaps in the above case, we should also alert about siren/airplane mode every 5 minutes or so, similar to what we do with GPS expiry?
   [√] Add legal disclaimer.
   [√] Make it so when we press back, if "Refresh Page" is displayed, do that!
       That way, we don't click it too many times in a row and end up back in the app.
   [√] GPS Permissions Expired is overridden in the Counting-down greenscreen section by "15 minutes until check-in" in the same pseudo-toast.
        Maybe move 15 minutes to check-in to block 2.
           That would interfere with siren mode on.
        Maybe move siren mode/airplane mode to block 1, since they shouldn't interfere with "low signal" or "no contacts".
   [√] There's a slight gap between when the Low Signal warning is dismissed via the PseudoToast refresh at the top of BeginForegroundServiceRunnable and when it appears anew (I think this is due to the delay on the onRequestUpdate() callback).
   [√] We're currently running into a slight delay on GPS Permissions expired pseudotoast reappearing.
       After the PseudoToast block is cleared out, it has to wait for any other sounds in the queue before it does its callforward.
       Maybe certain things need a PRIORITY CALLFWD that doesn't wait for the sound to start:
          GPS Permissions Expired
          Low Signal
          No Contacts
          Airplane Mode
          Siren Mode
       Meanwhile, for all sent/delivery stuff for a specific text message, maybe that would all be more intuitive if it waits for the sound to play.
   [√] "Low Signal" warning.
       [.] Ask on StackOverflow if there's a way to check signal strength when the screen is off?
           Already asked: https://stackoverflow.com/questions/76807249/is-there-a-way-to-get-the-telephonycallback-signalstrengthslistener-to-work-when/78550681
       [√] Permissions check.
       [_] 1) Test for if the emulator can simulate CDMA vs. GSM.
           2) Test with latency: https://stackoverflow.com/questions/6236340/how-to-limit-speed-of-internet-connection-on-android-emulator
             .1) To test for low signal on an emulator, it is suggested to also turn off Wi-Fi in the emulator, but the signal strength seems to bounce back for some reason: https://www.forasoft.com/blog/article/simulate-slow-network-connection-57
             .2) Trying to set a Preferred Network (Internet & Network > SIM) in the phone Settings doesn't seem to have any effect.
             .3) One way you can do a test is to turn on Airplane Mode, but make sure we check signal strength anyway. It comes out to 1000000 signal strength, which should probably be designated "No Signal Information".
                 This is so far the only way that produces a result different than default.
                 [√] ^ Test Airplane Mode's effect on all emulators?
                     So far tested on: UTS (No effect: RQPONM) (note that L shows a strength of 0 IFF cold booted in airplane mode, and according to the following link that would probably be about the theoretical max under normal conditions, so maybe it should be anything greater than or equal to 0 should be a "No Signal Information" condition: https://www.reddit.com/r/HomeNetworking/comments/17kq3nz/the_highest_possible_rssi_for_lte/)
                 [√] Does cold booting in airplane mode also work on RQPONM too? Yup!
             .4) Could try launching from CMD: https://developer.android.com/studio/run/emulator-commandline
                    Open terminal in Android Studio.
                    cd C:\Users\vegas\AppData\Local\Android\Sdk\emulator\emulator.exe (You can find this by running the emulator like normal and going to task manager to see its location)
                    .\emulator -avd Upside-Down_Cake -netdelay none -netspeed full (You can find the AVD id by clicking the pencil icon for editing AVD and going to Advanced Settings, the AVD id should be near the top)
                       (Note: You can use this link to reference values for -netdelay and -netspeed: https://stackoverflow.com/questions/7026251/simulate-low-network-connectivity-for-android, remember you can even use milliseconds for the -netdelay value: https://stackoverflow.com/questions/30409121/how-to-simulate-network-delay-on-android-emulator)
                       (Note: We can cold boot by adding argument -no-snapshot-load : https://stackoverflow.com/questions/66362274/how-to-cold-boot-emulator-using-command-linecmd)
                    Learned a new skill here! Nevertheless, this did not cause any adjustments to the signal strength values returned and displayed in the logcat (tested in Upside-Down Cake and Red Velvet Cake).
             .5) For now, this may be a trivial concern.
                 Tried some (not all) methods on this page: https://www.forasoft.com/blog/article/simulate-slow-network-connection-57
                 We can already see that signal does adjust when toggling airplane mode, when the screen is off and there's no Foreground Process, and it displays a different "technology" for different emulators.
                    This may be sufficient information for the time present.
   [.] Have the app get progressively more panicky with the audio alerts?
       Maybe not, as the annoyance of hearing the same sound over and over might be sufficient to gather attention, and changing it up might lead to a false sense of normalcy.
   [.] Add Suggestion for User: "Please don't turn off screen, and please make sure "Sleep" or "Screen timeout" is set to the longest value possible in phone settings."
       Make sure we keep the screen on: https://stackoverflow.com/a/12906069/16118981
          (Use FLAG_KEEP_SCREEN_ON?: https://stackoverflow.com/a/12906069/16118981)
          Place under setContentView:
            //Please Make Sure That We Don't Get A Screen Timeout On This Activity!
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
          NOTE: This could be a strain on battery life, and might not be worth the advantage of getting "Low Signal" info.
                If we do use this, There could be a warning that the phone should be plugged in?
                If we don't, maybe we could use a WakeLock to turn the screen back on, if possible?
                We could try to keep the screen on, but set the dimness of the app low after a period of inactivity (essentially simulating a timeout): https://stackoverflow.com/a/9703871/16118981
                   So we can still get "Low Signal" information when Notifications are off (i.e. Foreground Service isn't running) and the Screen is off.
       Maybe ignore for the present.
  [.] RELEASE AND CLEAR ALL PRIORITY SOUNDS ON APP EXIT... OR MAYBE EVEN ACTIVITY CHANGE!
       [√] ^ Implemented by adding mvBoth parameter to stopAllSounds, but may not need to if we just frequently release any sounds in the priority list.
       [_] If the app needs to be restarted manually, it likely won't release them.
           We may need to start releasing these audio clips each time.
           ^ Probably impractical, maybe just let the garbage collector take care of this.
  [√] Current challenge:
         If you turn off the screen, let it go through "turn screen back on" and "return to app", then turn screen back on, it will repeat "return to app"
  [.] Please plug the device in plays on the onResume iteration.
         Prevent it from doing so?
      Note: Probably not a big deal.
   [√] If you turn down volume, then turn off screen, then max volume, then turn on screen... you need to reduce volume and then increase to dismiss the "Volume Too Low" overlay".
       I think this was fixed by doing a non-SMS iteration upon turning screen back on.
   [√] Show signal strength in a special alternate debug mode?
   [√] Should (Low Signal) be rate limited (along with others) at varying rate limitations when countdown is > 0 or when < 0?
   [√] Maybe we should make it so the Low Volume alert saves to shared preferences so when we reduce it when the screen is off or the app is defocused, it shows upon refocus.
          Basically have the overlay show based on shared preferences, and clear out shared preferences whenver we increase volume again.

          Note: The following might not work because onResume() might trigger before the runnable,
                making it so it would flicker on then off.
                Maybe make it so only onResume can zero out the shared preference instead of having mvVolCur > mvVolPrv and mvVolCur == mvVolMax do it.
                Don't think this is an issue.
          How to:
          Where:
                //////////RUNNABLE//////////
          		//Play A Sound If The Volume Is Being Decreased At All, So The Volume Isn't Too Low When We Try To Alert The User
                    if (mvVolCur < mvVolPrv)
                        //Send Broadcast
                            mvContext.sendBroadcast(Intent("mbVolumeLow"))
                            mvClassSharedPreferences.mmSetSharedPreferencesInt("mvVolumeLow", 1)
                    else if (mvVolCur > mvVolPrv)
                        //Send Broadcast
                            mvContext.sendBroadcast(Intent("mbVolumeOk"))
                            mvClassSharedPreferences.mmSetSharedPreferencesInt("mvVolumeLow", 0)
                    else if (mvVolCur == mvVolMax) //<-- Note That This Checks If mvVolCur == mvVolMax, Not If mvVolCur == mvVolPrv... Because We Don't Want To Send The OK Signal (To Dismiss The Visual Warning) Until We're Either a) TRULY Increasing Again -Or- b) If Volume Is Indeed Maxed Out And We Need To Clear Out A Previously Cached "Volume Too Low!" Priority-Pseudo-Toast Still Lingering From (... Say...) Before We Turned Off The Screen, Maxed The Volume, And Turned Back On The Screen
                        //Send Broadcast
                            mvContext.sendBroadcast(Intent("mbVolumeOk"))
                            mvClassSharedPreferences.mmSetSharedPreferencesInt("mvVolumeLow", 0)

               //////////CLASSCAREGIVEE//////////
               override fun onResume() {
                    super.onResume()

                    //Power Management (Realtime Volume Button Detection)
                    //Let's Say We Turn Off The Screen (Or Defocus), Turn Down The Volume, And Then Turn The Screen Back On.
                    //In Theory, The Following SHOULD Ensure That We Show The "Volume Too Low!" Overlay When We Return, By Querying The "Shared Preferences":
                        if (mvClassSharedPreferences.mmGetSharedPreferencesInt("mvVolumeLow", 0) == 1) {
                            mvContext.sendBroadcast(Intent("mbVolumeLow"))
                        }

                  a [*] Turn down volume. Turn up volume.
                  b [*] Turn down volume, turn screen off, turn back on, turn screen off, turn up volume, turn back on.
                  c [*] Turn screen off, turn down volume, turn screen back on, turn screen off, turn up volume, turn screen back on.
                  d [*] "" But with defocusing.
                  e [*] "" But with defocusing.

               Test:
                  34 √
                  33 √
                  32 √
                  31 √
                  30 √
                  29 √
                  28 √
                  27 √ (Volume Too Low! overlay seemd to have gotten stuck in the visible state on at one point, not sure if this was just a temporary bug).
                  26 √
                  25 √
                  24 √
                  23 √
                  22 √
                  21 √

                  MAJOR ISSUES:
                    27) [√]
                       ISSUE! In Pistachio Ice Cream, when we turn off the screen, turn it back on, a nd turn it off again, and turn it back on EVERYTHING seems to lose focus. We can't click the check in button, and volume adjustments won't dismiss the low volume warning. We can press the Settings Button though.
                            Possible Solution: In Pistachio only, just have it refresh the activity?
                                                Or, try to see if it's actually communicating with the activity's View objects? If not, refresh ActivityCaregivee?
                                                Further: Same thing happens in Google Play "Launch Version". This may be a Pistacchio issue that was never corrected.
   [√] Test in all versions?
       (For debug purposes, reduce the length of a full cycle to something like 20s)
       Methodology:
          Uninstall/Reinstall
          "Unplug" the phone.
          Open app.
          Immediately turn off screen.
          Immediately turn down volume.
          Wait a full cycle.
          Turn the screen back on.
          Wait a full cycle.
          Plug the phone back in.
          Wait a full cycle.
          Unplug phone.
          Immediately exit.

          Phase II:
          34 √
          33 √
          32 √
          31 √
          30 √
          29 √
          28 √
          27 √
          26 √
          25 √
          24 √
          23 √
          22 √
          21 √
          Add a new debug code so you can see signal strength?

          [√] 34«
          [√] 33« <--        Tiramisu runs into a similar issue as API 32!
          [√] 32 <--         Didn't send text the first time I tried this on the first text-sending iteration? Not sure why.
                             The only contacts in phone were the Test 1 contact.
                             (THis happened at least twice).
                             Possible Trick to Replication (still spotty): Wait long into the first "plug device in" you here just when you start the main ActivityCaregivee, before you start to do the "power off"/"volume down" regimen (like on the word "or" in "or the device might turn off").
                                                                           Because it takes SOOOOOO long to go through all the sounds in the queue (especially since the interval between sounds is so long), it may continue playing these sounds past the normal time when we send our normal text, and this might be messing with it.
                                                                              If we continue waiting for another cycle, we hear the text-attempted spiel at this point, even though it should have been on the previous cycle.
                                                                                 Is it possible that it's getting rewound again by the other priority sounds in the queue or something?
                                                                             Or is it even decrementing the countdown?
                                                                                 This will likely require some println("?!?!?!?! A")'s to get to the bottom of this!
                             Note 1: Very difficult to replicate with current techniques.
                                        Possible we already fixed this some time along the way. ¯\_(ツ)_/¯
                                        Maybe just list this as a possibility for this API in a //comment somewhere.
                             Note 2: I let it go through for 16 countdown cycles.
                                     It sent out texts 8 times, but numerous times, it would "Text attempted" without getting a "Text sent" callback.
                                         Nevertheless, it sent out the text each time.
                                         This is probably the issue that was occurring before.
                                     Probably best to just add a "//comment" about this somewhere, since it's probably just a quirk of the emulator.
                                         Although maybe check Snowcone [√] and Tiramisu [_] to see if waiting this many cycles has a similar effect (is the callback rate-limited?).
                             Note 3: Maybe this occurs when I do the routine, but then have the Android Studio window minimized for API 32!
                             Tiramisu runs into a similar issue as API 32!
                                BUT! It also won't play PLEASE CHECK INs or HELP! I NEED HELP! for some reason.
                                Therefore, it may be entirely an issue with ClassSound, and not the Confirmation Callbacks (i.e. text sent and text delivered from phone).
                                   Seems to only affect non-priority sounds.
                                As such, removed: "//Note: In The API 32 Emulator, If You Immediately Turn Off The Screen When You Begin The Main Part Of The App, Turn Off The Screen, Turn Down The Volume, And Wait For All The Sound Alerts To Conclude ("Volume too low!", "Please turn screen back on!", "Please return to app!", "Please plug in phone!"), Then Minimize Android Studio, And Then Wait One To Several Cycles, You Might See A "Text Attempted!" Without A "Text Sent!" Or "Text Delivered!" Pseudo-Toast. For Some Reason, This Sequence Of Events Reduces The Likelihood Of The Callback Confirmations In API 32, Even Though The SMS' Do Seem To Actually Go Through."
                                   Which was added on account of API 32 theory.
                                Note: When it stops working properly, it keeps adding more and more stuff to the Sound Buffer without removing any from the front end.
                                      So it keeps accumulating unplayed sounds.
                                NOTE: Attempted to rehaul ClassSound and have the store only the integer reference to the sound files,
                                      and then remake the MediaPlayer each time a new sound is added.
                                         This had no effect.
                                      Then I tried to remove all .release() commands.
                                         This had no effect
                                      Then I tried to remove the help_i_need_help function call.
                                         SEEMED TO WORK!!!
                                      Then I tried to unremove it and just change the soudn that plays!
                                         This had no effect.
                                      [_] It's possible we may need, instead of pausing and rewinding, completely releasing and removing the MediaPlayer each time we need to play a priority sound instead.
                                      [_] It's possible we may need to use a soundboard full of pre-populated sound clips, like how we do with priority sounds.
                                      [_] It's possible that the button color change might somehow be affecting this.
                                          Doesn't appear to be the case.
                             FIX!
                             FIX!!
                             FIX!!!
                                [√] .reset()'ing the MediaPlayer object, and then re-create.()'ing it, seems to work swimmingly!
          [√] 31«
          [√] 30«
          [√] 29« <-- I think this (Quince Tart) was the last version where turning off the screen wasn't treated as a defocus.
                     Oddly, it didn't detect when we plugged the device back in, nor when we unplugged it again.
                        This appears to be a quirk of the Quince Tart emulator, which doesn't detect the alteration in realtime, but instead requires a cold boot. Pistachio Ice Cream works fine.
          [√] 28«
          [√] 27«
          [√] 26
          [√] 25 <-- Note that here, sometimes I noticed after all the MEDIA sounds were done playing, adjusting volume wouldn't decrease the MEDIA volume but instead the RINGTONE volume (since I think the emulator seems to try to determine the "context" of the action, and the context when the app's not playing any MEDIA sounds is to assume we're reducing RINGTONE instead?)
                     Reducing RINGTONE volume does not have any effect on our MEDIA sounds, and also doesn't trigger any of the "Volume too low!" subroutines.
          [√] 24«
          [√] 23« <-- !!!!!!! MARSHMALLOW Very prone to overlap at first (Please plug in and Please check in, then when you turn screen off), but it does seem to then rewind and play everything in approx. order.
                              Is the overlap only ever one non-priority and one priority?
                                 Nope! Seems like it cab a mix or priorities with priorities or priorities with non-prorities (not sure about non-priorities with non-priorities though).
                              Maybe write this in a //comment somewhere.
                              Solution: Don't rewind audio before pausing, instead rewind before unpausing.
          [_] 22  <-- As elsewhere noted in this document, no audio/no callbacks for this emulator.
                      This might be overcomable if you Cold Boot BEFORE trying to open the app after a hard boot of the computer.
          [_] 21« <-- Weird little skips where it starts playing part of one before moving onto the next in the queue.
   [.] Note that if you get a "Please plug the device in" during ActivityBehestImportant,
       that runs on a different instance of ClassSound than in ActivitySettings...
       ... and therefore won't pause and release when we mmStopAllPendingSounds(true) in ActivitySettings.
       Note: Very low priority concern.
   [√] Alpha testing:
      [p] [√] Highest priority to volume.
          [√] Medium priority to other Power Management sounds.
      [√] Dad has Snowcone, but it would appear that "Swipe to exit" doesn't appear.
           It also won't swipe to exit, maybe because it's a Foreground Service notification.
           Perhaps we should change it to remove the "\n" and say "restart app to exit" instead.
           [√] Note that we have mtNotificationBody and mtHowToExitApp.
               When and how are each used?
               mtHowToExitApp only shows up for when we're already checked in, not when we need to check in.
                  Maybe fix this so it trails ALL notifications?
      [√] Similar to the above concern, should we have it so the "Click to check-in" notification also has a "restart app to exit" after it when activities dismissed, or instead just rely on the idea that someone clicking it will see the updated message upon checking in (or maybe something else entirely)?
      [_] Airplane mode toggling as well as (possibly) permissions might be detectable in realtime by looking for Intent.ACTION_* intents.
          Much less likely for a user to accidentally turn on airplane mode after the fact, unlike turning off screen/navigating away from app/turning down volume/etc.
      [p] MAJOR! On Dad's phone, if the screen is allowed to automatically turn off, the process appears to end.
          This may be because it's trying to do too many calculations too often.
          [_] Maybe change it so the runnable isn't delayed every minute, but every 15 minutes?
              [_] Note: ONce we've counted down though, it could still be too taxing. Maybe consider slowing it down to once every two minutes during check-in time.
              [_] Also, consider making it so the code is lighter weight? (maybe certain things can be checked on a staggered basis during check-in)
              [√] ^^^ Unlikely to have a considerable effect on power or process disruption.
          [p] Or, consider making it so:
              [√] ... the screen doesn't time out at all: https://stackoverflow.com/questions/5712849/how-do-i-keep-the-screen-on-in-my-app
                 (This one might be better as we don't have to change the architecture of how the app works, only that we just need to make some alterations to the way the user can use it)
                  [√] Add special attribute to XML file: https://stackoverflow.com/questions/5712849/how-do-i-keep-the-screen-on-in-my-app
              [√] Set up a derpy-warning that plays IFF it detects the screen is manually turned off: https://stackoverflow.com/questions/19350258/how-to-check-screen-on-off-status-in-onstop
                  "Please turn the screen back on or the app might shut down"
                  [.] Detection
                      (look for "mbScreenOff")
                      Detects in both realtime (in the runnable) and at every power-button press (in the Caregivee Activity)
                  [√] Audio Warning (during main Caregivee activity)?
                      ."Please turn screen back on".
                      ."Or app might timeout."
                  [√] This doesn't appear to conflict with the window defocused alert.
                      Since it only triggers if we turn the screen off when the app is focused, and we can't defocus the app when the screen is already off.
                  [√] There is the question of precedence of volume alert, should this or audio volume alert have precedence?
                      Probably this should have precedence.
                  [p] Detect if screen is turned off from a service?
                      [√] Detection.
                      [√] Audio.
                  [√] Remove the extra space at the beginning of screen turned off audio?
                  [√] The way it's currently working, when we turn off the screen, it prioritizes "Please return to the app as soon as possible" over "Please turn the screen back on"
                      [_] We could try delaying "screen off" with a runnable, that way, it would interrupt "please return to the app"
                      [√] Or, we could keep the first four slots in the queue always ready, and simply add to those spots. Then, whenever one is done, it is removed and we iterate through the list from the beginning to find our next... unless it's of such high priority that it needs to be interrupted.
              [p] Set up a new important warning if the plug isn't plugged in: https://stackoverflow.com/questions/5283491/check-if-device-is-plugged-in
                  [.] Detection (handled using ClassPluggedIn and ActivityBehestImportant)
                  [√] Audio Warning (during main Caregivee activity)?
                      ."Please plug in phone".
                      ."Or device might turn off."
                  [_] Also have a plug-in pseudo-pseudo toast (i.e. a visual warning similar to the "Volume Too Low!" overlay)?
                      Might be overreach.
              [p] Set up a pseudotoast warning if the volume is too low: https://stackoverflow.com/questions/6248154/how-to-check-programmatically-the-volume-of-phone-in-android
                  [√] Detection (handled using mvAudioManager, look for "mbVolumeLow")
                      Detects in both realtime (in the runnable) and at every power-button press (in the Caregivee Activity)
                  [√] Attempt to create a minimum volume.
                  [√] Warning (similar to SMS-to-short, so not a true pseudo-toast)
                      ("Volume too low!" Make this a vertically oriented warning? off to the side?)
                  [√] Send up a real toast IFF mvActivitiesDismissed == true
                  [√] Audio Warning? (during main Caregivee activity)?
                      ."Volume too low".
                  [√] Case 1: If a user turned down the volume too quickly and then defocused the window, the warning about the volume might be quickly overridden.
                              However, there is a visual cue on the volume as well when the app is reopened, so probably not a huge deal.
                  [√] Case 2: The vice versa case, where the app is defocused and then the volume is turned down, might be a different story!
                              When we do this, it doesn't detect the volume adjustment in realtime, so it doesn't override it.
                              When we refocus the app, it runs an non-SMS iteration, so we should get the warning anyhow once we return (at least visually).
                  [p] ^ Since this doesn't respond directly to button presses, there could be a huge I/O delay before the button registers a toast.
                      Is there a way in a foreground service to detect a button press?
                      [√] This one might give me the answer: https://stackoverflow.com/questions/7297242/any-way-to-detect-volume-key-presses-or-volume-changes-with-android-service
                          [√] Detection.
                          [√] Audio implementation?
                      [.] Also, could this theoretically detect when the screen is turned off when the app is defocused?
                          Doesn't appear to be the case.
                      [√] Remove the extra space at the beginning of audio?
                      [√] Make it so it only plays in the service IFF the derivative of the sound adjustment is negative (i.e. if the volume is being decreased).
                      [√] Make it so that it tries to detect if the screen is offline and/or the app has been navigated away... because low volume is highest priority, we should consider sending some followup audio immediately afterwards for the other states (either by creating new manual subroutines that add those warnings to the queue).
                  [√] The "Volume Too Low!" won't play until we're completely done clicking the volume buttons, because of all the StopAllPendingSounds() calls.
                      That means it might be too late for an audio alert if someone turns it all the way down too quickly.
                      Maybe have it start a Runnable that prevents any more broadcasts for a few seconds so it triggers the audio only on the first button press of volume down/mute?
                  [.] Is there a special mute state that would return a normal volume value, yet still mute all sounds... rendering the current method inaccurate?
                      Prolly not: https://www.quora.com/Can-you-mute-an-apps-sound-on-Android-Not-the-notifications-but-the-in-app-sound-I-dont-want-an-app-to-bother-me-with-its-ads
                                  https://stackoverflow.com/questions/2048427/how-can-i-detect-whether-the-android-phone-in-silent-mode-programmatically
              [√] We can probably simplify some of our detection now, considering that we can detect in realtime using both the ContentObserver for volume adjustments and the BroadcastReceiver for screen off.
                  So the other methods we use to detect those adjustments in realtime (by detecting physical button presses or using onPause/onResume, might now be superfluous and might be better commented out).
              [√] Set up a derpy-warning if removed from recents and we have foreground service:
                  [√] Detection. (Handled with onWindowFocusChanged(), look for "mbWindowDefocused")
                  [√] Or maybe if we defocus at all! This is actually probably the best way to do this.
                  [√] Audio Warning? (during main Caregivee activity)?
                     √"Please return to the app as soon as possible"
                     √"Or app might timeout."
          Actual Observations:
             On Jun 22:
                Set the app to 30m until check-in at ~6:15p.
                Screen turned off automatically around ~6:17p.
                Checked in at ~6:55p.
                ~7:03p 15 minutes until check-in.
                ~7:07p 10 minutes until check-in.
                ~7:12p 05 minutes until check-in.
                Therefore, it is my theory that the process ended around 6:22, about 7 minutes after being set and about 5 minutes after screen off.
      [√] Recheck the signal on screen press even if countdown > 0 so if a user moves the phone around and hten fiddles with it to try to remove the low signal.
      [√] When the countdown > 0, only check low signal on the on the fives (countdown % 5 == 0's).
      [√] Suggestion: where it says something like "to exit, reopen app." Have it specifically say "exit from THIS screen upon reopening app".
      [√] In the legal disclaimer, change the word "affirmation" to sound less decisive.
      [√] Change "Time to Check In" on the top of the Settings screen to "Minutes Until Check-In".
      [√] Make "15 minutes" to check in show up immediately by increasing the countdown to "15 minus 1" instead of 15?
          Actually, this was because the runnable had to wait up to a minute to repost, since it didn't immediately repost on check-in. This was solved by removing callbacks and reposting immediately.
      [√] Apparently, it does show "Swipe to Reopen" in the notification, but on Dad's phone, it shows the Notification Message where you expect hte title would be.
          Have the title and the message of the notification identical so there's no confusion?
          Or, it turns out I've been using the message as the title and the title as the message. Just try switiching them around?

   [_] Other app stuff:
       [_] Same DING for all successes in MachCall... LONG BUZZ for anything that doesn't go through.
       [_] MachCall still uses "!!"
       [.] Try changing Labyrinthine Jelly Beans to START_STICKY, maybe that'll help fix the background process disappaearing thing.
   [.] Check for ?!?!?!?!'s?
       [_] //?!?!?!?! Inform on stackoverflow about .reset() usage? https://stackoverflow.com/questions/12486951/how-to-stop-mediaplayer-stream-and-then-restart-it-android ?
       [√] //?!?!?!?! Also, update the audio player answer to suggest using .reset() if they run into any issues with timeouts, and also to rewind not just after pausing but instead just before starting to play anew.
       [_] Maybe add the "exit app via dedicated activity technique" here too: https://stackoverflow.com/questions/13385289/remove-app-from-recent-apps-programmatically
           (^ Probably not necessary)
       [√] Maybe update the signal strength stackexchange responses to reflect new technique (that shows "No Signal Information" if at default 1000000?)
   [√] Recheck side-by-side with previous version.
   [_] Assorted ideas from during testing in Dad's room.
       _ there's always a notification beep when the notification text changes
       _ reset to 1 each check in (suggestion from Dad)
       _ behestwarning don't use app by itself AND use med alert device... place closer to top? (Dad suggestion)
       _ timestamp that says how long it's been since yellow "please check in" started (suggestion from Dad)
   [√] Change low signal threshold down to -133dB, since we've seen it go through at that signal in Dad's room.
   [√] My flip phone, when brought into the same room, sometimes ran into delays on receiving (it, on its own network, might've been far below -133dB... event hough they were sent from the caregivee phone at -130 and -129-ish)
       It would say "Text Sent" but never "Text Delivered"... until maybe like, 15 minutes later when they all came at the same time.
       Maybe make it so there's a "Text Delayed?" warning if it takes 15 seconds for "Text Delivered" to appear.
          How To:
             Set up in the main runnable, when we receive "Text Sent" broadcast, start a 15s runnable and increment a Text Sent variable.
             When we receive a "Text Received" broadcast, decrement the Text Sent variable.
             When the 15s are up, if the Text Sent variable is still greater than 0 (just in case there's a queue of delayed texts and one fo the previous Text Delivereds appears before the 15s is over), then issue an audio warning "Text May Be Delayed...",
                with a "Text Delayed" pseudotoast (APPEND = true, DISMISS = false), just in case we get delivered soon.
                If the text is immeidately thereafter delivered, the delivery appended line will likely appear offscreen, but with an audio so it's not THAT big a deal.
   [√] √ behestwarning to user to always keep app open
       √ behest warning says plug in OR charge, remove latter
       √ behestwarning change permission "Settings (word this better)
   [√] Maybe adjust the dB threshold for when we post "Low Signal" based on the results we may see during physical device testing.
   [√] Longer dismissal time on 15 seconds to check in.
       May need to send a new intent extra (something like mbTimeAddend, which defaults to 0)
   [.]
       [.] Test -133 db to see if low signal is workign properly.
       [√] TESTING?:
              Open app:
                 wait for text sent
                 wait 10s for "text delayed?"
                 wait for it to dismiss on its own during a half-cycle

                 wait for text attemppted
                 wait 10s for "text delayed?"
                 long press caregivee button to send a "delivery notification" to dismiss it

              u? then try three long presses to see what happens.
              u? then wait a minute and then do the same four times
                    (to simulate multiple Delivered!'s in the queue)

                 wait for text attempted
                 before 10s "text delayed?" appears, long-press Settings Button
                                                            (set up so all the audios play in a row when long pressed)
                 then see if we still get a "text delayed" notification

                 U [√]
                 T [√]
                32 [√]
                 S [√]
                 R [√]
                 Q [√]
                 P [√]
                 ----
                 O [√]
                26 [√]
                25 [√]
                 N [√]
                 M [√]
                22 [√]
                 L [√]

                 Findings:
                    If you get text attempted, then immediately or shortly therafter (perhaps one of the two or both) go to Settings and then come back again... you get a Text Delayed? ... which is probably not a huge deal.

                 Then, remove/comment out all the ?!?!?!?! debug stuff.
   [√] Check code against most previous versions of code using WinMerge.
   [ ] Turn off automatic debug mode, update Caregivee "version number" in strings.xml, publish (generate signed APK with keystore "Desktop/mk_key_store.jks" and legacy password "Appl......").
   [ ] Many of the permissions in this app are not allowed by default by Google Play:
          Family/Device Locators are probably denied:
      [√] CALL_PHONE is probably denied:
             https://support.google.com/googleplay/android-developer/thread/232597558/playstore-rejected-my-app-because-i-used-send-sms-permission-in-my-app?hl=en
             https://support.google.com/googleplay/android-developer/answer/10208820?hl=en#zippy=%2Cpermitted-uses-of-the-sms-and-call-log-permissions%2Cexceptions%2Cinvalid-use-cases%2Calternatives-to-common-uses
             https://community.appinventor.mit.edu/t/i-have-problems-with-the-error-908-the-permission-call-phone/72256/11

      [ ] Video:
          Use the built-in screencap (at 25 Mbps and with no visible "taps")
          Uninstall and reinstall app.
          Record screen.
          Go in and choose the first three contacts:
             Amelia
             Booker
             Charlotte
             Delroy
             Emma
             Fabian
             Gisele
             Harold
             Indira
          Wait a full cycle, tap the screen and then stop recording.
          Note: Maybe in the future, also try navigating away from and back to the app.
                Also, maybe try going through all three contacts to show how the names change onscreen.
      [ ] How To Upload App: https://orangesoft.co/blog/how-to-publish-an-android-app-on-google-play-store
          Note, there is a one-time 25USD fee for a mandatory developer account: https://www.quora.com/Who-can-publish-my-app-on-the-Google-play-store-for-free-I-dont-have-a-developer-console-account
                                                                                 https://daily.dev/blog/android-app-publishing-checklist-google-play-2024
          Pros:
             tk                    tmi                    67d                         mi
             popular               2nd popular            secure
             trusted               2nd trusted            obscure
                                                          probably will be denied
                                                             regardless of acct
                                                             due to permissions
                                                             requested,
                                                             so this one seems
                                                             safest to use:
                                                                a) it's unclear
                                                                   who has 67d
                                                                b) it's the 2nd
                                                                   easiest
                                                                   acct to
                                                                   restart from
                                                                   scratch should
                                                                   it accidentally
                                                                   run afoul
                                                                   of TOC
                                                                c) 67Dot has
                                                                   a generic sound.
                                                                d) We can still
                                                                   post a video
                                                                   to try to
                                                                   exude profes-
                                                                   sionalism.
                                                          however, will prevent
                                                             easier distribution
                                                             on reddit/youtube
                                                             if limited to the
                                                             air gap acct
          Lean:
             [ ]                   [ ]                    [√]                    [ ]

         Important Notes:
         ===============
         67Dot currently chosen:
         Note: Health Apps need to be registered as an organization:
                https://support.google.com/googleplay/android-developer/answer/10788890?hl=en
                https://support.google.com/googleplay/android-developer/answer/13996367

         The most onerous requirement appears to be a D-U-N-S Number.
         Here's the applicable info it requested when I tried to apply for one:

            Before you apply for a D-U-N-S Number
            Here's a checklist of documents that can help speed up your D-U-N-S application review. During the application process, you can save your progress and return later to complete where you left off.

            You'll need two (2) of the following Required Documents
            Documents must be in your company's name and company address
            Providing the required documents will help reduce delays

            If you're a Sole Proprietor:
               EIN/TIN Confirmation Letter

               Business License

               Professional License

               DBA or Fictious Name Certificate

               Secretary of State Certificate of Filing

            You can also include any of the following, optionally:
               Proof of business phone service

               Proof of a utility in your company's name

            If you need assistance with LLC formation and registration, please  visit Tailor Brands.linkinfo

         So, it's probably not worht the effort for Google Play XD
            (Or, maybe figure out how to make it NOT an outright Medical App, which would be very difficult)

         Perhaps try one one of the following instead:
            Aptoide
            F-Droid
            Amazon Appstore
            Uptodown
            APKMirror

      [ ] It is possible if I water down functionality to only sending location data via text (ignoring phone calls altogether)...
      ... that I might be able to fill out the appropriate paperwork, make a video explainer, and get it approved on PlayStore:
            https://play.google.com/store/apps/details?id=com.sygic.familywhere.android&hl=en_US&gl=US

            Video Highlights:
               Have Caregivee's mascot narrate, but as a 3D character?
               Full user control.
               User is informed of major things via voice.
               The app adapts to the user's preferences.
               No in-app purchases.

            There are alternatives to Google Play that MIIIIIIGHT publish this:
               https://www.aptoide.com
               https://www.apkmirror.com/faq/

            We can assess the options available if the above methods don't bear fruit.
               https://www.wired.com/story/install-apps-outside-app-store-sideload/
               https://www.vmedtechnology.com/customers/Android_sideloading.html
               https://www.howtogeek.com/313433/how-to-sideload-apps-on-android/ (Recent)
   [.] Make it so when we open the app while the Foreground Service is still going that it ends any pending Foreground Processes and restarts the app.
          (Maybe using a broadcast).
       Seems to do it already automatically
   [.] What if someone actually is at 0, 0 coordinates?
       This is only something we check for in MachCall, it seems.
   [√] "mvLocationPermissionRequest?.___"
       ^ May not be that safe because this is precisely where we fork into either starting or stopping the app.
          Use this, like in Labyrinthine Jelly Beans:
             if (mvLocationPermissionRequest == null) mvStopper //<-- Stop The App If null For Some Reason
   [√] Regarding:
          mvSettings.mvPermissions = arrayListOf(false, mvPermissionsLocal[Manifest.permission.ACCESS_FINE_LOCATION] ?: false, mvPermissionsLocal[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false, mvPermissionsLocal[Manifest.permission.READ_CONTACTS] ?: false, mvPermissionsLocal[Manifest.permission.SEND_SMS] ?: false) //"?:" Safety Review: Should Be OK As We're Just Defaulting To False (If Null)
          The zeroth index might need to be true, since we check to see if ANY are false shortly thereafter.
   [√] Make all Refresh Page dialogues that appear on defocus delay about 400ms before appearing (so it doesn't look weird as we switch).
   [√] Add a "+" if the user also included phone.
   [.] Do more testing with extra-app-ular permissions changing?
       [.] First, when we come back from Phone Settings, sometimes it doesn't refresh onCreate!
           Maybe use onResume and then have it call onCreate?
              Remember, this might remove any contacts we've added, perhaps save the contacts right in the RecyclerView instead?
              Doesn't seem to be an issue :)
           Maybe start a relatively fast working runnable that only triggers when we long-press.
           Or perhaps use that more advanced API for detecting focusing/defocusing.
           Or... maybe just first start a new activity before we leave the app that, when we return, asks us to refresh the page.
              Then, we end up in Settings, but... like... fixed. :D
              But, it would be cool if we could instead just keep actively checking with a runnable just in case extra-appular adjustments were made without long-press.
                 But under those circumstances, onCreate just refreshes. So we might only need to call a dedicated runnable that we can removeCallbacks from.
       [√] Maybe just use a dialogue that prompts us to refresh?
           ^ CURRENT SOLUTION!
                      CURRENT SOLUTION!
                                 CURRENT SOLUTION!
       [√] If we extra-appularly change permissions from SIREN MODE to ANONYMOUS LOCATION MODE...
           ... and it doesn't refresh the page...
           ... and we press the BACK BUTTON, it doesn't fix the Contacts text color.
           Just have ClassContactsUpToDate change back to default text color if necessary.
   [.] Telephony deprecated: https://stackoverflow.com/questions/69571012/telephonymanager-deprecated-listen-call-state-ringing-on-android-12
   [.] Light Theme/Dim Theme Option?
       Probably not extremely necessary, as the light vs. dim colors are only in Settings.
       And this app is designed not to be looked at for a long time.
   [√] Change to a TextView for showing which mode we're in in the Settings screen.
       Long press to go into Phone Settings?
   [√] Move This Checklist To Its Own File
   [√] Version number as "toast"
   ===
   [√] Re-check contacts each time to make sure they're still valid?
       Shade them red if they are no longer active.
   ===
   [√] Make GPS a Google Map link?
       When $mvCoordinates.mvLink is inserted in a text message, it doesn't send for some reason.
       Maybe switch to MMS instead of SMS? Or maybe try sending the link as a separate message?
       Note:
          Seems that it's just a matter of if the text with the URL is too long, it won't send.
          Not sure if this is the case without the URL.
          Just shorten the SMS.
   ===
   [√] Move all strings to string resources in string.xml.
       Please check the XMLs too.
   [√] Make xml id's more my style of reference
       Use mx for a prefix?
   ===
   [√] Have the button that sends us to "Phone Settings" do so once.
       Then when we return, have it change to Try Again?
       Then, just redo the permissions call, and send back to "Go To Phone Settings".
   [√] Test if the above works with the other permissions?
   ===
   [√] Rename activities so they sort in a row on the left panel.
   ===
   [√] Place an invisible back button in the whitespace in settings.
   [√] Populate empty slots with existing choices if we run through three "please choose" messages.
       Patterns:
          100, 010, 001 -> 111
          120, 102, 012 -> 121
          Where 1 is the first contact populated and 2 is the second contact populated.
       Maybe just submit a toast recommending three UP-TO-DATE contacts, but if not... populate the rest automatically.
   [√] If you opt out of Contacts List Permissions, then you go back in and agree to permissions on a subsequent dialogue...
       ... that won't get carried forward to the next page because it pulls it out of disk memory.
       Simply override the permissions part of what we pull out of disk memory.
   [√] Automatic contacts slot population.
       Handle differently for outdateds? (like, keep the outdated's, but fill the nulls?)
          How it currently works: Any outdateds are overridden per the appropriate pattern 111 121 pattern.
          If all are null, or if all are outdated, it will go back to the previous screen.
       Make it so it records outdateds as "1"'s and "2"'s, and nulls as "0"'s.
          Then check for "000" when we press the back button, and prevent starting the app under that hypothetical.
          The reason is that outdated contacts are better to preserve, because it's still possible they might be valid numbers and increase the chances of getting in touch with someone.
   [√] Make the most common emergency number patterns completely inaccessible. 0**, 1**, and 9**
   ===
   [.] If you try to removeCallbacks on "onSaveState()", it will stop the countdown when we INPUT-OUTPUT shut down and turn back on the emulator, without resuming.
   ===
   ===
   ===
   [√] Ideas for potential future versions?
       Make it so fewer permissions are necessary?
          Sending communication ALWAYS seems to require permissions.
             Even were we to expand to email, that would requires internet permissions.
             BUUUUUUUT... we could revert to siren mode, where a loud siren plays if someone doesn't respond.
          But, we could forego contacts and have caregivers manually enter them (and then save them to memory) with toasts that recommend updating contacts.
          GPS location data isn't really necessary.


Previously Used ClassDialog:
===========================

package com.example.caregivee

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log

//Review Code For This Page [√]

class ClassDialog(mvActivity: Activity)
{
    //Countdown Variables
        private val mvHandler = Handler(Looper.getMainLooper())
        private lateinit var mvRunnable : Runnable

    //"Dialog" Prompt Stuff
        private val mvBuilder = AlertDialog.Builder(mvActivity)
        private val mvDialog: AlertDialog = mvBuilder.create()
        private var mvDialogBuildingPaused = false

    //Display The "Dialog" Prompt
        private fun mmDisplayDialog(mvTitle : String, mvMessage : String, mvIcon : Int, mvYesInclude : Boolean, mvMaybeInclude : Boolean, mvNoInclude : Boolean, mvYes : String, mvMaybe : String, mvNo : String, mmYes : ((ArrayList<Any?>) -> Unit)?, mmMaybe : ((ArrayList<Any?>?) -> Unit)?, mmNo : ((ArrayList<Any?>?) -> Unit)?, mvArguments : ArrayList<Any?>) : AlertDialog {
            //Dialog: (https://www.digitalocean.com/community/tutorials/android-alert-dialog-using-kotlin)
            ////Also: (https://stackoverflow.com/questions/20494542/using-dialoginterfaces-onclick-method)
                if (!mvDialog.isShowing) {
                    mvBuilder.setCancelable(false)
                    mvBuilder.setTitle(mvTitle)
                    mvBuilder.setIcon(mvIcon)
                    mvBuilder.setMessage(mvMessage)
                    if (mvYesInclude) {
                        mvBuilder.setPositiveButton(mvYes) { mvDialog, mvButtonIndex ->
                            if (mmYes != null) {
                                Log.d(Throwable().stackTrace[0].lineNumber.toString(), "$mvDialog $mvButtonIndex") //<-- This Line Is To Prevent A "Parameter ... Is Never Used" Error
                                //Call This Function For The "Yes" State
                                    mmYes(mvArguments)
                            }
                        }
                    }
                    if (mvNoInclude) {
                        mvBuilder.setNegativeButton(mvNo) { mvDialog, mvButtonIndex ->
                            if (mmNo != null) {
                                Log.d(Throwable().stackTrace[0].lineNumber.toString(), "$mvDialog $mvButtonIndex") //<-- This Line Is To Prevent A "Parameter ... Is Never Used" Error
                                //Call This Function For The "No" State
                                    mmNo(mvArguments)
                            }
                        }
                    }
                    if (mvMaybeInclude) {
                        mvBuilder.setNeutralButton(mvMaybe) { mvDialog, mvButtonIndex ->
                            if (mmMaybe != null) {
                                Log.d(Throwable().stackTrace[0].lineNumber.toString(), "$mvDialog $mvButtonIndex") //<-- This Line Is To Prevent A "Parameter ... Is Never Used" Error
                                //Call This Function For The "Maybe" State
                                    mmMaybe(mvArguments)
                            }
                        }
                    }
                    mvBuilder.show() //<-- Actually Show The Dialog
                }
                return mvDialog
            }

    //Page Refresh Dialog (A Specific, Commonly-Used "Dialogue" Prompt)
        fun mmRefreshDialog(mvActivity : Activity, mvClassEnum : ClassEnum, mvSettings : ClassSettingsState, mvCountdown : ClassCountdown?)
        {
            Log.d("Refresh Dialog ?!?!?!?!", mvActivity.toString() + "; Paused: " + mvDialogBuildingPaused.toString())
            //Is The Building Of Dialogs Paused?
                if (!mvDialogBuildingPaused)
                {
                    //Populate Arguments
                        val mvArgumentArray : ArrayList<Any?> = ArrayList(arrayListOf(false, mvActivity, mvClassEnum, mvSettings, mvCountdown))
                    //Create A New Thread
                        mvRunnable = Runnable { mmDisplayDialog("", mvActivity.getString(R.string.mtRefreshPagePlease), R.drawable.ic_baseline_lock_person_24,
                            mvYesInclude = true,
                            mvMaybeInclude = false,
                            mvNoInclude = true,
                            mvYes = mvActivity.getString(R.string.mtRefreshPage),
                            mvMaybe = "",
                            mvNo = "",
                            mmYes = ::mmRefreshYes,
                            mmMaybe = null,
                            mmNo = null,
                            mvArguments = mvArgumentArray
                        ) }

                    //Start the Countdown (It's Helpful To Wait Just A Bit To Display The "Dialog" Prompt So It Doesn't Appear BEFORE We Leave The App)
                        mvHandler.postDelayed(mvRunnable, 500)
                }
        }
        private fun mmRefreshYes(mvArguments : ArrayList<Any?>)
        {
            val mvRefreshActivity  : Activity            = mvArguments[1] as Activity
            val mvRefreshClassEnum : ClassEnum           = mvArguments[2] as ClassEnum
            val mvRefreshSettings  : ClassSettingsState  = mvArguments[3] as ClassSettingsState
            val mvRefreshCountdown : ClassCountdown?     = mvArguments[4] as ClassCountdown?

            //Refresh The Page
            //Stop The Countdown
                mvRefreshCountdown?.mmStop() //"?." Safety Check: This Will Only Run If There's A ClassCountdown Object, So It Should Be Safe.
            //Prepare To Switch Activities
                val mvIntent = Intent(mvRefreshActivity, mvRefreshActivity::class.java)
            //Referring Activity
                mvRefreshSettings.mvReferringActivity = mvRefreshClassEnum
            //Let's Dismiss Any Pending Dialogs To Prevent A "WindowLeaked" Error
                mmRemoveDialog()
            //Continue...
                mvIntent.putExtra("mvSettings", mvRefreshSettings)
                mvRefreshActivity.startActivity(mvIntent)
        }
    //Remove Dialog
        fun mmRemoveDialog()
        {
            try {
                mvDialog.dismiss()
                mvDialogBuildingPaused = true //<-- So When We Leave The "Activity" (By Pressing A Back Button For Example), It Won't (a) Trigger The Activity's onPause (b) Build A "Refresh Dialog" Somewhere In The Background (c) Lead To A Window Leak Error If We Defocus The App
            } catch (mvE : Exception) {
                mvE.printStackTrace()
            }
        }
}






PREVIOUSLY USED CLASSPERMISSIONS
================================
package com.example.caregivee

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import java.lang.ref.WeakReference

//Review Code For This Page [√]

class ClassPermissions
{
    //"Permissions Registering" Variables
        private val mvPermissions: ArrayList<String> = arrayListOf("", Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_CONTACTS, Manifest.permission.SEND_SMS).also{if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) it.addAll(listOf(Manifest.permission.POST_NOTIFICATIONS))}
        private var mvLocationPermissionRequest : ActivityResultLauncher<Array<String>>? = null

    //Initialize The Functions That Are Called If Permissions Are Approved/Denied
    //(Note: They're Normally The Same As Each Other In The Current Version Of The App
    // ..... Since We Just Throttle Functionality Depending On User Preferences)
        private lateinit var mvProceeder : () -> Unit
        private lateinit var mvStopper : () -> Unit

    //Public Variables
        private var mvSufficientPermissions : Boolean = true

    //Functions
        fun mmRegisterPermissions(mvWeakReference : WeakReference<ComponentActivity>) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mvLocationPermissionRequest = (mvWeakReference.get() as ComponentActivity).registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { mvPermissionsLocal ->
                    val mvPermissionsTemp =  arrayListOf(true /* <-- Since The Next Line Checks If ANY Are false */, mvPermissionsLocal[Manifest.permission.ACCESS_FINE_LOCATION] ?: false, mvPermissionsLocal[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false, mvPermissionsLocal[Manifest.permission.READ_CONTACTS] ?: false, mvPermissionsLocal[Manifest.permission.SEND_SMS] ?: false).also{if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) it.addAll(listOf(mvPermissionsLocal[Manifest.permission.POST_NOTIFICATIONS] ?: false))} //"?:" Safety Review: Should Be OK As We're Just Defaulting To False (If Null)
                    mvSufficientPermissions = if (mvPermissionsTemp.any{ !it }) {
                        //This Is The Function We WOULD Call If Any Of The Permissions Are Denied...
                        //But In The Current Version Of The App, We Just Throttle Functionality Depending On The Permissions Chosen.
                        //So, mvStopper() Is Usually The Same As mvProceeder() In The Current Version.
                            mvStopper()
                            false
                    } else  {
                        //Have All Permissions Been Accepted?
                            mvProceeder()
                            true
                    }
                }
            }
        }
        fun mmGetPermissions (mvProceed : () -> Unit, mvStop : () -> Unit) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //SET The Functions That Are Called If Permissions Are Approved/Denied
                //(Note: They're Normally The Same As Each Other In The Current Version Of The App
                // ..... Since We Just Throttle Functionality Depending On User Preferences)
                    mvProceeder = mvProceed
                    mvStopper = mvStop

                    if (mvLocationPermissionRequest == null) {
                        //Proceed To Settings If mvLocationPermissionRequest Is Null, Where We Should End Up In Siren Mode By Default
                            mvProceeder()
                    }
                    else {
                        //"Drop" The First Permission Since The Zeroth Slot Is Blank For Ease Of "Array Element" COUNT-ability
                            mvLocationPermissionRequest?.launch(mvPermissions.drop(1).toTypedArray()) //<-- "Safe (?.) Call" SHOULD Be Fine Here, Due To The Fact It's In The else Block Of A Null-Checking Conditional
                    }
            }
            else
            {
                //Before Android 6.0, We Ostensibly Wouldn't Even Be Able To Use The App Without
                //All Permissions Being Granted:
                    mvProceed()
            }
        }
}

PREVIOUSLY USED METHOD TO GET STRINGS BY NAME
================================
//Get The List Of "Warnings" From The Strings XML File: https://stackoverflow.com/questions/68967613/android-get-string-resource-id-from-value
    val mvTextColor : ArrayList<String> = arrayListOf("#000000",
                                                      "#FFFFFF",
                                                      "#AA0022",
                                                      "#AA5500",
                                                      "#AA8800",
                                                      "#00AA00",
                                                      "#0099BB",
                                                      "#0066CC",
                                                      "#9900CC",
                                                      "#BB00BB",
                                                      "#AA5500",
                                                      "#AA8800",
                                                      "#CCCCCC",
                                                      "#FFFFAA")
    val mvFields : Array<Field> = R.string::class.java.fields
    for (mvField in mvFields) {
        val mvResourceId = mvField.getInt(mvField)
        if (mvField.name.contains("mtSuggestion")) {
            val mvIndex = mvField.name.replace("mtSuggestion", "").toInt()
            mvWarningList.add(ClassLineItem())
            mvWarningList.last().mvImage = R.drawable.baseline_warning_amber_24
            mvWarningList.last().mvId    = mvIndex.toLong()
            mvWarningList.last().mvName  = getString(mvResourceId)
            mvWarningList.last().mvColor = Color.parseColor(mvTextColor[mvIndex])
        }
    }
    mvWarningList.sortBy {it.mvId}
    mvWarningList.first().mvImage = android.R.color.transparent //Override First Image (Might Have An Issue On VERY Early Android Versions) (https://stackoverflow.com/a/8243184/16118981)
    mvWarningList.last().mvImage  = R.drawable.baseline_lightbulb_24 //Override Last Image

PREVIOUS WAY TO TEST FOR NOTIFICATION TAP
=========================================
            //What Should We Do If The Notification Is "Tapped"?
                val mvIntent = Intent(this, ActivityTimeToExitApp::class.java) //If You Want To Send Intent Extras: https://stackoverflow.com/a/30461116/16118981
                val mvPendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, mvIntent, PendingIntent.FLAG_IMMUTABLE)

PREVIOUS WAY TO MAKE ADJUSTMENTS TO NOTIFICATION STUFF (like light color and enableLights)
================================================
    //Create Notification Channel
        private fun mvCreateNotificationChannel() {
            //Create The "Notification" Chanel
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val mvServiceChannel = NotificationChannel(mvChannelId, applicationContext.getString(R.string.mtNotificationChannelName), NotificationManager.IMPORTANCE_DEFAULT)
                        .apply {
                            //lightColor = Color.BLUE
                            //enableLights(true)
                        }
                    val mvManager = getSystemService(NotificationManager::class.java)
                    mvManager.createNotificationChannel(mvServiceChannel)
                }
        }

PREVIOUS onString() OVERRIDE FOR CLASSLINEITEM
==============================================
    //Display As String
        override fun toString(): String {
            return "$mvId) $mvImage $mvName, $mvMobile, $mvColor"
        }

PREVIOUS CONTACTS RETRIEVER (THAT INCLUDED EMAIL-ONLY QUERIES)
==============================================
package com.example.caregivee

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.text.TextUtils

//Review Code For This Page [√ ]

class ClassContactsRetriever (val mvContext : Context) {
    //1) Get The Contact List (https://stackoverflow.com/questions/70693889/get-contacts-in-android-without-duplicates)
    //2) Get It With Emails And Stuff Too (https://stackoverflow.com/questions/11860475/how-to-get-contact-id-email-phone-number-in-one-sqlite-query-contacts-androi/11862598#11862598)
    //3) Separate Home/Mobile/Fax Numbers Etc. (https://stackoverflow.com/questions/9636209/android-i-try-to-find-what-type-of-numberfrom-contact-is-mobile-home-work)
        fun mmContactsRetriever() : MutableList<ClassLineItem>  {
            val mvNames: MutableList<ClassLineItem> = arrayListOf()
            var mvCursor : Cursor? = null
            try {
                    val mvIncludeEmail = false  //Do We Want To Include Email Addresses? (I.E. Including "Contacts" That Don't Also Have A Phone Number)
                    mvCursor = mvContext.contentResolver.query(
                        ContactsContract.Data.CONTENT_URI,
                        null,
                        ContactsContract.Data.HAS_PHONE_NUMBER + ">" + (if (mvIncludeEmail) -1 else 0) + " AND (" + ContactsContract.Data.MIMETYPE + "=? OR " + ContactsContract.Data.MIMETYPE + "=?)",
                                arrayOf(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, Phone.CONTENT_ITEM_TYPE), //<-- These Two Fill The = ? Placeholders Above
                                ContactsContract.Data.CONTACT_ID) //<-- Sorted By Contact ID

                    if (mvCursor != null && mvCursor.count > 0) {
                        var mvIdPrev : Long = -1
                        var mvClassLineItem : ClassLineItem? = null
                        //Iterate Through The Results
                            while (mvCursor.moveToNext()) {
                                //Assign The Results To Local Variables
                                //Use .getColumnIndexOrThrow() Instead of .getColumnIndex() (https://stackoverflow.com/questions/71338033/android-sqlite-value-must-be-%E2%89%A5-0-getcolumnindex-kotlin?answertab=scoredesc)
                                    val mvId : Long = mvCursor.getLong(mvCursor.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID))
                                    val mvName : String = mvCursor.getString(mvCursor.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME))
                                    val mvData1 : String = mvCursor.getString(mvCursor.getColumnIndexOrThrow(ContactsContract.Data.DATA1))
                                //Accumulate The Phone Numbers Into A Single ClassLineItem() (By Assigning Only When We Move On To A Different ID)
                                    if (mvId != mvIdPrev)
                                    {
                                        if (mvClassLineItem != null) //<-- Don't Assign On The First Iteration, As We Haven't Accumulated Our Contact Info Yet!
                                        {
                                            if (mvClassLineItem.mvId != null) //<-- Make Sure We Accumulated At Least One Valid Datapoint To Populate A Contact
                                            {
                                                mvClassLineItem.mvImage = R.drawable.ic_baseline_phone_android_24
                                                mvNames.add(mvClassLineItem)
                                            }
                                        }
                                        mvClassLineItem = ClassLineItem()
                                    }
                                //What Kind Of Contact Do We Have?
                                    val mvPhoneType: Int = mvCursor.getInt(mvCursor.getColumnIndexOrThrow(Phone.TYPE))
                                    if (mvIncludeEmail && mmValidEmail(mvData1))
                                    {
                                        /*  Uncomment Below Code If You Want To Update App To Include Email-Only Contacts
                                            mvClassLineItem?.mvEmail = mvData1
                                            mvClassLineItem?.mvName = mvName
                                            mvClassLineItem?.mvId = mvId
                                        */
                                    }
                                    else {
                                        //Don't Include Home/Work Contacts For Now, Only Mobile Contacts
                                            if (mvPhoneType == Phone.TYPE_MOBILE) {
                                                mvClassLineItem?.mvMobile = mvData1
                                                mvClassLineItem?.mvName = mvName
                                                mvClassLineItem?.mvId = mvId
                                            }
                                    }
                                //Get Ready For The Next Iteration
                                    mvIdPrev = mvId
                            }
                        //Add That Last One That Doesn't Get Added Because We Always Add The Current ClassLineItem On The NEXT Iteration
                            if (mvClassLineItem != null)
                            {
                                mvClassLineItem.mvImage = R.drawable.ic_baseline_phone_android_24
                                mvNames.add(mvClassLineItem)
                            }
                    }
                    else
                    {
                        //No Contacts!
                        //No Current Need For Code In This Section
                    }
            }
            catch (mvEx : Exception)
            {
                mvEx.printStackTrace()
            }
            finally {
                mvCursor?.close() //"?." Safety Review: This Theoretically Works Better Than "mvCursor!!.close()" Because We Shouldn't Throw An Exception In The "Finally" Block... Meanwhile, If There IS An Exception, "mvNames" Should Default To An Empty List
            }
            return mvNames
        }

    //Check If A String Is In A Valid Email Format:
        private fun mmValidEmail(mvEmail : String): Boolean {
            return !TextUtils.isEmpty(mvEmail) && android.util.Patterns.EMAIL_ADDRESS.matcher(mvEmail).matches()
        }

}

PREVIOUS EXPLICIT MANNER OF DECLARING INTENT FILTER IN MANIFEST
===============================================================
    <!-- Broadcast Receiver -->
    <receiver
        android:name=".BeginForegroundServiceTransmissionReceiver"
        android:exported="true"
        android:permission="android.permission.POST_NOTIFICATIONS">
        <!-- Since We Set android:exported To true, This Means Other Apps Can Interact With Ours. Attribute android:permission Limits The Scope To Only Apps With The Specified Permission(s) -->
        <intent-filter>
            <action android:name="mbNotificationCancelled"/>
        </intent-filter>
    </receiver>
    <!-- Broadcast Receiver -->

LEANEST, BESTEST AUDIO CLASS:
    package com.example.caregivee

    import android.content.Context
    import android.media.MediaPlayer

    //Review Code For This Page [√√]

    //UPDATE STACK OVERFLOW QUESTION? ?!?!?!?!
    //Note: Only Instantiate Once Per Activity And Call "mmScheduleSound()" Whenever We Want To Play An Sound File
        class ClassSound (val mvContext : Context) {
            //Create A MutableList (To Act As A Queue) Slated To Comprise The Sounds We Want To Put In The Queue
                private var mvSoundBuffer: MutableList<MediaPlayer> = arrayListOf()

            //Add Our New Sound To Queue And Play It Immediately IF We Just Added To An EMPTY Queue (Otherwise, The Already Running onCompletionListener In mmPlayFirst() Ensures It Will Automatically Play The Next One In The Queue Once The Current Sound Is Finished)
                fun mmScheduleSound(mvSound: Int /* <- mvSound contains a reference like "R.raw.mvSound" */) {
                    mvSoundBuffer.also{it.add(MediaPlayer.create(mvContext, mvSound))}.also{if (mvSoundBuffer.size == 1) mmPlayFirst()}
                }
            //Play The First Sound In The Queue If It Exists And It's Not Playing, "Remove" It From The Queue onCompletion, Then Repeat The Process
                private fun mmPlayFirst() {
                    mvSoundBuffer.getOrNull(0)?.let{ if (it.isPlaying) null else it}?.also{it.start()}?.setOnCompletionListener{mvSoundBuffer.removeFirstOrNull()?.release().also{mmPlayFirst()}} //"?." Safety Check: In Any Situation Where We Return null, It Theoretically Just Shouldn't Play The Sound
                }
            //Pause All Pending Sounds, Release Allotted Resources, And "Clear" It From The Queue
                fun mmStopAllPendingSound() {
                    mvSoundBuffer.also{ it -> for (mvSound in it) {mvSound.also{it.pause()}.release()}}.clear()
                }
        }

PREVIOUS CLASSTEXTER


package com.example.caregivee

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast

//Review Code For This Page [√√]

class ClassSms(val mvContext : Context) {

    //Strings 'n' Things
        private var mvTextAttempted    = mvContext.getString(R.string.mtTextAttempted)
        private var mvTextForwarded    = mvContext.getString(R.string.mtTextForwarded)
        private var mvTextDelivered    = mvContext.getString(R.string.mtTextDelivered)
        private var mvTextGotException = mvContext.getString(R.string.mtTextGotException)
        private var mvTextNotForwarded = mvContext.getString(R.string.mtTextNotForwarded)
        private var mvTextNotDelivered = mvContext.getString(R.string.mtTextNotDelivered)

    //Dispatch A Text (SMS) And Check Delivery
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        @Suppress("DEPRECATION")
        fun mmSendSms(mvText: String, mvNumber: String,  mvName: String) : Boolean {
            //Note: Because The Following Section is wrapped in a try/catch...
            //... exceptions might be trickier to spot in the "Logcat" since they aren't highlighted red:
                try {
                    //Fetch The SMS Manager
                        val mvSmsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                            mvContext.getSystemService(SmsManager::class.java)
                                                        } else {
                                                            SmsManager.getDefault()
                                                        }

                    //Set These Broadcasters (PendingIntents) Up So They Might Send A Broadcast Back When The Text Is Either Sent Or Delivered
                        val mvForwardedIntent = PendingIntent.getBroadcast(mvContext, 0, Intent("mbTextSent"), PendingIntent.FLAG_IMMUTABLE)      //<-- PendingIntent Explained: https://developer.android.com/reference/android/app/PendingIntent
                        val mvDeliveredIntent = PendingIntent.getBroadcast(mvContext, 0, Intent("mbTextDelivered"), PendingIntent.FLAG_IMMUTABLE) //<-- PendingIntent Explained: https://developer.android.com/reference/android/app/PendingIntent

                    //Receive Broadcasts About the Success Of A Text
                        val mvConfirmForwardedBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                            override fun onReceive(mvContext: Context, mvIntent: Intent) {
                                if (resultCode == Activity.RESULT_OK) {
                                    //Inform User
                                        Toast.makeText(mvContext, String.format(mvTextForwarded, mvName), Toast.LENGTH_SHORT).show()
                                    //Unregister Receiver
                                        mvContext.unregisterReceiver(this) //<-- Otherwise, We'll Keep Displaying Previous Successes/Failures On Subsequent Texts
                                }
                                else {
                                    //Inform User
                                        Toast.makeText(mvContext, mvTextNotForwarded, Toast.LENGTH_SHORT).show()
                                    //Unregister Receiver
                                        mvContext.unregisterReceiver(this) //<-- Otherwise, We'll Keep Displaying Previous Successes/Failures On Subsequent Ones
                                }
                            }
                        }
                        val mvConfirmDeliveredBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                            override fun onReceive(mvContext: Context, mvIntent: Intent) {
                                if (resultCode == Activity.RESULT_OK) {
                                    //Inform User
                                        Toast.makeText(mvContext, mvTextDelivered, Toast.LENGTH_SHORT).show()
                                    //Unregister Receiver
                                        mvContext.unregisterReceiver(this) //<-- Otherwise, We'll Keep Displaying Previous Successes/Failures On Subsequent Ones
                                }
                                else
                                {
                                    //Inform User
                                        Toast.makeText(mvContext, mvTextNotDelivered, Toast.LENGTH_SHORT).show()
                                    //Unregister Receiver
                                        mvContext.unregisterReceiver(this) //<-- Otherwise, We'll Keep Displaying Previous Successes/Failures On Subsequent Ones
                                }
                            }
                        }

                    //Register The Above Broadcast Receivers And Have The Intent Filters Attempt To Capture Broadcasts Submitted By The Earlier Broadcasters
                    // Note: IntentFilter Explained: https://stackoverflow.com/a/21608620 -AND HERE- https://developer.android.com/reference/android/content/IntentFilter)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            //Register For More Recent Versions Of Android
                                mvContext.registerReceiver(mvConfirmForwardedBroadcastReceiver, IntentFilter("mbTextSent"), Context.RECEIVER_EXPORTED)
                                mvContext.registerReceiver(mvConfirmDeliveredBroadcastReceiver, IntentFilter("mbTextDelivered"), Context.RECEIVER_EXPORTED)
                        }
                        else {
                            //Register For Earlier Versions Of Android
                            //This Block Of Code Is Why We Have "@SuppressLint("UnspecifiedRegisterReceiverFlag")" Further Up The Page
                                mvContext.registerReceiver(mvConfirmForwardedBroadcastReceiver, IntentFilter("mbTextSent"))
                                mvContext.registerReceiver(mvConfirmDeliveredBroadcastReceiver, IntentFilter("mbTextDelivered"))
                        }

                    //Actually Send The Text
                        mvSmsManager.sendTextMessage(mvNumber.filter{it.isDigit()} /* <-- Remove Dashes And Parentheses And Stuff */, null, mvText, mvForwardedIntent, mvDeliveredIntent)
                        Toast.makeText(mvContext, mvTextAttempted, Toast.LENGTH_SHORT).show()

                    //We At Least Succeeded In ATTEMPTING The Text
                    //The Broadcasters (PendingIntents) Included With "sendTextMessage()" Should Tell Us If It Was Actually Dispatched And/Or Delivered
                        return true

                } catch (mvEx: Exception) {
                    //Attempt Failed
                        Toast.makeText(mvContext, mvTextGotException, Toast.LENGTH_LONG).show()
                        mvEx.printStackTrace()
                        return false
                }
        }
}

INTERMEDIATE CLASSTEXTER AND TOAST HANDLER:
(ISSUE HERE IS THAT IT ONLY EVER SHOWS THE TOAST FOR THE FIRST CONTACT, IT'S NOT UPDATING THE INTENT EXTRAS PROPERLY FOR SOME REASON)
=====================================================================================================================================
package com.example.caregivee

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast

//Review Code For This Page [√√]

class ClassSms(val mvContext : Context) {

    //Strings 'n' Things
        private var mvTextAttempted    = mvContext.getString(R.string.mtTextAttempted)
        private var mvTextGotException = mvContext.getString(R.string.mtTextGotException)

    /*
        private var mvTextForwarded    = mvContext.getString(R.string.mtTextForwarded)
        private var mvTextDelivered    = mvContext.getString(R.string.mtTextDelivered)
        private var mvTextNotForwarded = mvContext.getString(R.string.mtTextNotForwarded)
        private var mvTextNotDelivered = mvContext.getString(R.string.mtTextNotDelivered)

     */

    //Dispatch A Text (SMS) And Check Delivery
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        @Suppress("DEPRECATION")
        fun mmSendSms(mvText: String, mvNumber: String,  mvName: String) : Boolean {
            //Note: Because The Following Section is wrapped in a try/catch...
            //... exceptions might be trickier to spot in the "Logcat" since they aren't highlighted red:
                try {
                    //Fetch The SMS Manager
                        val mvSmsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                            mvContext.getSystemService(SmsManager::class.java)
                                                        } else {
                                                            SmsManager.getDefault()
                                                        }

                    //Set These Broadcasters (PendingIntents) Up So They Might Send A Broadcast Back When The Text Is Either Sent Or Delivered
                        Log.d("Tag", mvName)
                        val mvForwardedIntent = PendingIntent.getBroadcast(mvContext, 0, Intent("mbTextSent").also{it.putExtra("mvName", mvName)}, PendingIntent.FLAG_IMMUTABLE or Intent.FILL_IN_DATA /* <-- Intent.FILL_IN_DATA is so we can send extras: */) //<-- PendingIntent Explained: https://developer.android.com/reference/android/app/PendingIntent
                        val mvDeliveredIntent = PendingIntent.getBroadcast(mvContext, 0, Intent("mbTextDelivered"), PendingIntent.FLAG_IMMUTABLE) //<-- PendingIntent Explained: https://developer.android.com/reference/android/app/PendingIntent


                    /*
                    //Receive Broadcasts About the Success Of A Text
                        val mvConfirmForwardedBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                            override fun onReceive(mvContext: Context, mvIntent: Intent) {
                                if (resultCode == Activity.RESULT_OK) {
                                    //Inform User
                                        Toast.makeText(mvContext, String.format(mvTextForwarded, mvName), Toast.LENGTH_SHORT).show()
                                    //Unregister Receiver
                                        mvContext.unregisterReceiver(this) //<-- Otherwise, We'll Keep Displaying Previous Successes/Failures On Subsequent Texts
                                }
                                else {
                                    //Inform User
                                        Toast.makeText(mvContext, mvTextNotForwarded, Toast.LENGTH_SHORT).show()
                                    //Unregister Receiver
                                        mvContext.unregisterReceiver(this) //<-- Otherwise, We'll Keep Displaying Previous Successes/Failures On Subsequent Ones
                                }
                            }
                        }
                        val mvConfirmDeliveredBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                            override fun onReceive(mvContext: Context, mvIntent: Intent) {
                                if (resultCode == Activity.RESULT_OK) {
                                    //Inform User
                                        Toast.makeText(mvContext, mvTextDelivered, Toast.LENGTH_SHORT).show()
                                    //Unregister Receiver
                                        mvContext.unregisterReceiver(this) //<-- Otherwise, We'll Keep Displaying Previous Successes/Failures On Subsequent Ones
                                }
                                else
                                {
                                    //Inform User
                                        Toast.makeText(mvContext, mvTextNotDelivered, Toast.LENGTH_SHORT).show()
                                    //Unregister Receiver
                                        mvContext.unregisterReceiver(this) //<-- Otherwise, We'll Keep Displaying Previous Successes/Failures On Subsequent Ones
                                }
                            }
                        }
                     */
/*
                    //Register The Above Broadcast Receivers And Have The Intent Filters Attempt To Capture Broadcasts Submitted By The Earlier Broadcasters
                    // Note: IntentFilter Explained: https://stackoverflow.com/a/21608620 -AND HERE- https://developer.android.com/reference/android/content/IntentFilter)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            //Register For More Recent Versions Of Android
                                mvContext.registerReceiver(mvConfirmForwardedBroadcastReceiver, IntentFilter("mbTextSent"), Context.RECEIVER_EXPORTED)
                                mvContext.registerReceiver(mvConfirmDeliveredBroadcastReceiver, IntentFilter("mbTextDelivered"), Context.RECEIVER_EXPORTED)
                        }
                        else {
                            //Register For Earlier Versions Of Android
                            //This Block Of Code Is Why We Have "@SuppressLint("UnspecifiedRegisterReceiverFlag")" Further Up The Page
                                mvContext.registerReceiver(mvConfirmForwardedBroadcastReceiver, IntentFilter("mbTextSent"))
                                mvContext.registerReceiver(mvConfirmDeliveredBroadcastReceiver, IntentFilter("mbTextDelivered"))
                        }
 */

                    //Actually Send The Text
                        mvSmsManager.sendTextMessage(mvNumber.filter{it.isDigit()} /* <-- Remove Dashes And Parentheses And Stuff */, null, mvText, mvForwardedIntent, mvDeliveredIntent)
                        Toast.makeText(mvContext, mvTextAttempted, Toast.LENGTH_SHORT).show()

                    //We At Least Succeeded In ATTEMPTING The Text
                    //The Broadcasters (PendingIntents) Included With "sendTextMessage()" Should Tell Us If It Was Actually Dispatched And/Or Delivered
                        return true

                } catch (mvEx: Exception) {
                    //Attempt Failed
                        Toast.makeText(mvContext, mvTextGotException, Toast.LENGTH_LONG).show()
                        mvEx.printStackTrace()
                        return false
                }
        }
}
MEANWHILE IN BEGINFOREGROUNDSERVICERUNNABLE:
	//Handle Toasts All In One Convenient Location So There's Little Conflict
		private val mvBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
			//?!?!?!?! FIGURE OUT HOW TO ALSO DISPLAY THE CONTACT'S NAME FROM HERE!
			override fun onReceive(mvContext: Context, mvIntent: Intent) {
				(if (mvIntent.action == "mbTextSent")
					if (resultCode == Activity.RESULT_OK) String.format(mvTextForwarded, mvIntent.getStringExtra("mvName")) else mvTextNotForwarded
				else if (mvIntent.action == "mbTextDelivered")
					if (resultCode == Activity.RESULT_OK) mvTextDelivered else mvTextNotDelivered
				else
					null)?.also{Toast.makeText(mvContext, it, Toast.LENGTH_LONG).show()} //"?." Null Safety Check: It Simply Won't Show The Toast If It's null
			}
		}
	//Init
        init {
			//Register The Broadcast Receiver
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
					mvContext.registerReceiver(mvBroadcastReceiver, IntentFilter().also{it.addAction("mbTextSent")
																						it.addAction("mbTextDelivered")}, Context.RECEIVER_EXPORTED)
				else
					mvContext.registerReceiver(mvBroadcastReceiver, IntentFilter().also{it.addAction("mbTextSent")
																						it.addAction("mbTextDelivered")})

PREVIOUS METHOD OF COMMUNICATING TO THE RUNNABLE VIA THE FOREGROUND SERVICE
===========================================================================
            //Conditionals
                if (!this@ActivityCaregivee::mvBeginForegroundServiceRunnable.isInitialized && !mvClassPermissionsMode.mmForegroundServiceMode() || this@ActivityCaregivee::mvBeginForegroundServiceRunnable.isInitialized  && mvClassPermissionsMode.mmForegroundServiceMode()) {
                    //If Permissions Have Changed (On A Sudden)!
                    //I.E We Started A Foreground Service But Have Since Lost Permission, Or We Didn't Start A Foreground Service And Have Since Gained Permission
                        //Let's Just Restart The Activity In Realtime :)
                        //(Instead Of Waiting For Next Iteration Of mvBeginForegroundServiceRunnable)
                            mmRestartActivity()
                }
                else if (!this@ActivityCaregivee::mvBeginForegroundServiceRunnable.isInitialized) {
                    //Communicate With The Foreground Service To Reset The Countdown
                    //(Note: We Can Use "startForegroundService()" To Communicate With An Already-Running Foreground Service: https://stackoverflow.com/questions/43736714/how-to-pass-data-from-activity-to-running-service#comment74519790_43737298)
                        val mvIntent = Intent(applicationContext, BeginForegroundService::class.java)
                        mvIntent.putExtra("mvResetCountdown", true)
                        ContextCompat.startForegroundService(applicationContext, mvIntent)
                }
                else {
                    //Reset Countdown
                    //(Note: This else Condition Is Triggered Only When The Foreground Service Isn't Running)
                        if (mvBeginForegroundServiceRunnable.mvCountdown <= 0) {
                            mvBeginForegroundServiceRunnable.mvCountdown = mvBeginForegroundServiceRunnable.mvSettings.mvRefreshRate
                            mvBeginForegroundServiceRunnable.mmChangePleaseCheckInButtonProperties(ClassEnum.BUTTONCHECKEDIN.mvInt, ClassEnum.COLORGREEN.mvInt, true)
                        }
                }

        override fun onStartCommand(mvIntentParam: Intent, mvFlags: Int, mvStartId: Int): Int {
            //Is The Activity Just Trying To Send Flags Or Other Data To An Already-Running Foreground Service?
                if (mvIntentParam.hasExtra("mvResetCountdown"))
                {
                    //Reset Countdown
                        if (this@BeginForegroundService::mvBeginForegroundServiceRunnable.isInitialized && mvBeginForegroundServiceRunnable.mvCountdown <= 0) {
                            //Reset The Countdown And Change Button
                                mvBeginForegroundServiceRunnable.mvCountdown = mvBeginForegroundServiceRunnable.mvSettings.mvRefreshRate
                                mvBeginForegroundServiceRunnable.mmChangePleaseCheckInButtonProperties(ClassEnum.BUTTONCHECKEDIN.mvInt, ClassEnum.COLORGREEN.mvInt, true)
                        }
                }
                else if (mvIntentParam.hasExtra("mvCloseApp"))
                {
                    //Close App
                        if (this@BeginForegroundService::mvBeginForegroundServiceRunnable.isInitialized) {
                            //Close App From Within The Runnable
                                mvBeginForegroundServiceRunnable.mvCountdown = 1000000
                                mvBeginForegroundServiceRunnable.mvHandler.also{it.post(mvBeginForegroundServiceRunnable.mvRunnable)}
                        }
                }

            //Is The Activity Just Trying To Send Flags Or Other Data To An Already-Running Foreground Service?
                else if (mvIntentParam.hasExtra("mvNotificationUpdate")) {
                    //Reset Countdown
                        if (this@BeginForegroundService::mvBeginForegroundServiceRunnable.isInitialized) {
                            //Update The Notification Text
                            //Think Of Something Better Than "" For The Default Value ?!?!?!?!
                                mvNotificationBuilder.setContentTitle(mvIntentParam.getStringExtra("mvNotificationUpdate") ?: "") //"?:" Safety Check: It Should Just Show A Blank String In The Notification If This Value Is null
                                (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(1, mvNotificationBuilder.build())
                        }
                }



NOTES THAT PREVIOUSLY MADE SENSE WITH DIFFERENT CODE:
            //Note: If We Defocus The App And Change Permissions Extra-app-ularly In Such A Way That It (De Facto) Resets The App's "Process"
            //..... (As When We Turn Off GPS Location Permissions)...
            //..... mvSettings.mvReferringActivity Seems To Retain The Previous Value Instead Of The New Self-Referential Value It Should Have Acquired In onCreate()


PREVIOUS WAY OF DETERMINING WHICH CONTACT BUTTON WAS CLICKED WHEN USING android:onClick INSTEAD OF ADDING AN ONCLICK LISTENER IN THE KOTLIN CODE:
    //"Contact #" Button Clicked? Go To The RecyclerView Activity :)
        fun mmContactNumberClick(mvView: View) {
            //Figure Out Where To Go And What To Do
                if (!mvClassPermissionsMode.mmSirenMode()) { //<-- Buttons Aren't Active In "Siren Mode"
                    //Which Button Was Selected?
                        val mvViewName = mvView.resources.getResourceName(mvView.id).substringAfterLast("/")
                        mvWhichContact = when (mvViewName) {
                                              "mxContact0" -> 0
                                              "mxContact1" -> 1
                                                     else  -> 2
                                          }
                    //Check For Unpopulated Or Outdated Contacts And Make Them Red
                    //(Note: If We're In "Siren Mode", Let's Just Gray Out The Contacts)
                        val mvContactsListPermitted = !mvClassPermissionsMode.mmNoContactsMode()
                        val mvContactsCount         = mvClassContactsUpToDate.mmClassContactsUpToDate<Int>(mvSettings, mvContactButtons, mvContactsListPermitted, false, 2)
                        if (mvContactsListPermitted && mvContactsCount == 0) Toast.makeText(applicationContext, mvNoContactsInPhone, Toast.LENGTH_SHORT).show()
                    //Switch Activities
                        mmSwitchActivities(if (!mvContactsListPermitted || mvContactsCount == 0) ActivityTelephoneContactsManualInput::class.java else ActivityTelephoneContactsRecyclerView::class.java)
                }
        }

POWER BUTTON OFF DETECTION (NOW SUPERFLUOUS DUE TO BROADCASTRECEIVER)

        //Power Management (To Ensure The "Process" Doesn't End Early Due To Screen Being Turned Off, For Example)
            private lateinit var mvPowerManager : PowerManager

        //Power Management
            mvPowerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager

        override fun onResume()
        {
            super.onResume()

            //Power Management
                if (!mvScreenOn && mvPowerManager.isInteractive) {
                    mvScreenOn = true
                    println("Screen turned on!!!")
                }

        override fun onWindowFocusChanged(mvFocused: Boolean) {
            super.onWindowFocusChanged(mvFocused)
            if (!mvTrueWindowFocusChanged) {
                ...
            }
            else {
                if (!mvPowerManager.isInteractive) {
                    //Power Management (Realtime Power Button Detection)
                    //Is The Screen off? (https://stackoverflow.com/questions/6848518/detect-on-off-key-press-android)
                    //(Note: Previously In onStop(), But The onWindowFocusChanged() was taking precedence when the power button was pressed.)
                        mvScreenOn = false
                        sendBroadcast(Intent("mbScreenOff"))
                }

DETECTING VOLUME ADJUSTMENTS IN REALTIME (PREVIOUS, NOW SUPERFLUOUS DUE TO CONTENTOBSERVER
    //On Back Button Pressed
        override fun onKeyDown(mvKeyCode: Int, mvEvent: KeyEvent?): Boolean {
            if (mvKeyCode == KeyEvent.KEYCODE_VOLUME_UP || mvKeyCode == KeyEvent.KEYCODE_VOLUME_DOWN || mvKeyCode == KeyEvent.KEYCODE_VOLUME_MUTE)
            {
                //Power Management (Realtime Volume Detection)
                    val mvVolCur = mvAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                    val mvVolMax = mvAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    if (mvVolCur.toFloat()/mvVolMax.toFloat() <= .75) {
                        //Send Broadcast
                            if (mvKeyCode != KeyEvent.KEYCODE_VOLUME_UP) applicationContext.sendBroadcast(Intent("mbVolumeLow")) //<-- We Don't Trigger This On KeyEvent.KEYCODE_VOLUME_UP Because It Might Play The "Volume Too Low!" Audio While We're Ascending Out Of "Too Low" Territory
                    }
                    else
                    {
                        //Send Broadcast
                            applicationContext.sendBroadcast(Intent("mbVolumeOk"))
                    }
            }

GETTING SIGNAL STRENGTH (Prev WIP):
    package com.example.caregivee

    import android.annotation.SuppressLint
    import android.content.Context
    import android.os.Build
    import android.telephony.CellInfoGsm
    import android.telephony.CellInfoLte
    import android.telephony.PhoneStateListener
    import android.telephony.SignalStrength
    import android.telephony.TelephonyManager
    import android.util.Log

    class ClassSignalStrength (mvContext : Context) {
        val mvTelephonyManager = mvContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val mvCellInfoList = mvTelephonyManager.allCellInfo
        var mvSignalStrengthDbm = 0
        var mvSignalStrengthValue = ""

        init {
            //https://stackoverflow.com/a/77285142/16118981
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                //Is Cell Info Available?
                if (mvCellInfoList != null) {
                    Log.d("Signal List", mvCellInfoList.toString())
                    for (mvCellInfo in mvCellInfoList) {
                        //General Strength
                            Log.i("Signal (${mvCellInfo::class})", "------ General Signal Strength --> ${mvCellInfo.cellSignalStrength.dbm}" + if (mvCellInfo.cellSignalStrength.dbm < -100) " (Weak)" else "")
                        //Handle Specific Cell Info Types If Necessary (e.g., CellInfoWcdma, CellInfoCdma)
                            /*
                                when (mvCellInfo) {
                                    is CellInfoGsm -> {
                                        val mvCellSignalStrengthGsm = mvCellInfo.cellSignalStrength
                                        mvSignalStrengthDbm = mvCellSignalStrengthGsm.dbm
                                        Log.i("Signal (GSM)", "------ GSM Signal --> $mvSignalStrengthDbm")
                                        //Do Something With mvCellSignalStrengthGsm for GSM network
                                    }

                                    is CellInfoLte -> {
                                        val mvCellSignalStrengthLte = mvCellInfo.cellSignalStrength
                                        mvSignalStrengthDbm = mvCellSignalStrengthLte.dbm
                                        Log.i("Signal (LTE)", "------ LTE Signal --> $mvSignalStrengthDbm")
                                        //Do Something With mvCellSignalStrengthGsm for LTE network
                                    }
                                }
                            */
                    }
                } else {
                    mvTelephonyManager.listen(object : PhoneStateListener() {
                        override fun onSignalStrengthsChanged(mvSignalStrength: SignalStrength) {
                            super.onSignalStrengthsChanged(mvSignalStrength)

                            if (mvSignalStrength.isGsm) {
                                mvSignalStrengthDbm = 2 * mvSignalStrength.gsmSignalStrength - 113 //Convert To dBm
                            } else {
                                mvSignalStrengthDbm = mvSignalStrength.cdmaDbm //Already Converted to dBm
                            }
                        }
                    }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
                }
            }
        }
    }

PREVIOUS NON-CUSTOM SPINNER:
mvSpinner.adapter = ArrayAdapter(applicationContext, android.R.layout.simple_spinner_item, mvCountdownFromOptions) //"ArrayAdapter" Gives An Array To An "AdapterView", Such As A "Spinner" (Note: "An AdapterView is a view whose children are determined by an Adapter" — Source: https://developer.android.com/reference/kotlin/android/widget/AdapterView)

PREVIOUS GRADLE BUILD FILE DEPENDENCIES:
dependencies {
    implementation("androidx.recyclerview:recyclerview:1.2.0") //<-- This is for the RECYCLERVIEW
    implementation("androidx.recyclerview:recyclerview:1.3.2") //<-- This is for the RECYCLERVIEW
    implementation("androidx.cardview:cardview:1.0.0")         //<-- This is for the RECYCLERVIEW
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.preference:preference:1.2.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

NEW GRADLE BUILD FILE DEPENDENCIES:
dependencies {
    implementation("androidx.recyclerview:recyclerview:1.3.2") //<-- This is for the RECYCLERVIEW
    implementation("androidx.cardview:cardview:1.0.0")         //<-- This is for the RECYCLERVIEW
    implementation("androidx.core:core-ktx:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.preference:preference:1.2.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
     */
/*
    Previous Volume Too Low Checker:
        val mvVolCur = mvAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val mvVolMax = mvAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        if (mvVolCur.toFloat() / mvVolMax.toFloat() <= .75) {}

*/
    /*
SOUND MANAGER PRIOR TO PRIORITY SOUNDS
        package com.example.caregivee

        import android.content.Context
        import android.content.Intent
        import android.media.MediaPlayer
        import android.os.Handler
        import android.os.Looper
        import kotlin.system.exitProcess

        //Review Code For This Page [√√√]

            //Note: Only Instantiate Once Per Activity And Call "mmScheduleSound()" Whenever We Want To Play An Sound File
            @Suppress("UNUSED")
            class ClassSound (val mvContext : Context) {
                //Create A MutableList (To Act As A Queue) Slated To Comprise The Sounds We Want To Put In The Queue
                private var mvSoundBuffer: MutableList<Triple<MediaPlayer, Int, Int>> = arrayListOf()
                private var mvMuteAllSounds = false

                //Add Our New Sound To Queue And Play It Immediately IF We Just Added To An EMPTY Queue (Otherwise, The Already Running onCompletionListener In mmPlayFirst() Ensures It Will Automatically Play The Next One In The Queue Once The Current Sound Is Finished)
                fun mmScheduleSound(mvSound : Int /* <- mvSound contains a reference like "R.raw.mvSound" */, mvCallFwdCode : Int, mvCallBackCode : Int, mvPlaySound : Boolean, mvImmediateCallFwd : Boolean) {
                    if (mvPlaySound) mvSoundBuffer.also{it.add(Triple(MediaPlayer.create(mvContext, mvSound), if (!mvImmediateCallFwd) mvCallFwdCode else ClassEnum.CALLFWDNONE.mvInt, mvCallBackCode))}.also{if (mvSoundBuffer.size == 1) mmPlayFirst()}.also{ mmCallForward(if (mvImmediateCallFwd) mvCallFwdCode else ClassEnum.CALLFWDNONE.mvInt) } else mmCallForward(mvCallFwdCode) //<-- Sometimes, We Just Want To Skip The Sound Altogether And Go Right To The CallForward Function (Be It "Immediate CallForward" Or Otherwise), Like When We Do A Non-SMS Iteration In BeginForegroundServiceRunnable (I.E. mvSendSms = false).
                }
                //Play The First Sound In The Queue If It Exists And It's Not Playing, "Remove" It From The Queue onCompletion, Then Repeat The Process
                private fun mmPlayFirst() {
                    if (!mvMuteAllSounds) mvSoundBuffer.getOrNull(0)?.let{if (it.first.isPlaying) null else it}?.also{it.first.start()}?.also{mmCallForward(it.second)}?.also{it.first.setOnCompletionListener{mvSoundBuffer.removeFirstOrNull()?.also{mvIt -> mvIt.first.release()}?.also{mvIt -> mmCallback(mvIt.third)}}} //"?." Safety Check: In Any Situation Where We Return null, It Theoretically Just Shouldn't Play The Sound
                }
                //Pause All Pending Sounds, Release Allotted Resources, And "Clear" It From The Queue
                fun mmStopAllPendingSound() {
                    mvSoundBuffer.also{ it -> for (mvSound in it) {mvSound.also{it.first.pause()}.first.release()}}.clear()
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
                private var mvSmsDelivered              = mvContext.getString(R.string.mtSmsDelivered)
                private var mvSmsForwardedToContact     = mvContext.getString(R.string.mtSmsForwardedToContact)
                private var mvSmsGotExceptionToContact  = mvContext.getString(R.string.mtSmsGotExceptionToContact)
                private var mvSmsNotDelivered           = mvContext.getString(R.string.mtSmsNotDelivered)
                private var mvSmsNotForwardedToContact  = mvContext.getString(R.string.mtSmsNotForwardedToContact)

                //Special Callforwards
                private fun mmCallForward(mvCallFwdCode : Int)
                {
                    if (mvCallFwdCode != ClassEnum.CALLFWDNONE.mvInt)
                    {
                        //Default Values
                        mvPseudoToastMessage = ""
                        mvAppend             = true  //<-- Do We Append To An Existing Pseudo-Toast Block, Or Overwrite?
                        mvDismiss            = false //<-- Does The Pseudo-Toast Automatically Disappear?
                        mvWhichBlock         =  2    //<-- Bottommost Pseudo-Toast Block Takes Precedence
                        mvWhichContact       = -1    //<-- This Variable Is So We Can Forward Which Contact (Was Contacted) To The Broadcast Receiver

                        //Fetch The Appropriate Pseudo-Toast Message
                        when(mvCallFwdCode) {
                            ClassEnum.CALLFWDAIRPLANEMODE.mvInt      -> mvPseudoToastMessage = mvAirplaneMode.also{mvAppend = false}.also{mvWhichBlock = 1}
                            ClassEnum.CALLFWDGPSEXPIRED.mvInt        -> mvPseudoToastMessage = mvGpsExpired.also{mvAppend = false}.also{mvWhichBlock = 0}
                            ClassEnum.CALLFWDLOWSIGNAL.mvInt         -> mvPseudoToastMessage = mvLowSignal.also{mvAppend = false}.also{mvWhichBlock = 1}
                            ClassEnum.CALLFWDNOCONTACTS.mvInt        -> mvPseudoToastMessage = mvNoContactsInApp.also{mvAppend = false}.also{mvWhichBlock = 1}
                            ClassEnum.CALLFWDSIRENMODE.mvInt         -> mvPseudoToastMessage = mvSirenMode.also{mvAppend = false}.also{mvWhichBlock = 1}
                            ClassEnum.CALLFWDSMSDELIVERED.mvInt      -> mvPseudoToastMessage = mvSmsDelivered.also{mvDismiss = true}
                            ClassEnum.CALLFWDSMSDELIVERYFAILED.mvInt -> mvPseudoToastMessage = mvSmsNotDelivered.also{mvDismiss = true}
                            else                                     -> for (mvI in 0 until mvNumberOfContacts)
                            {
                                if (mvCallFwdCode == ClassEnum.CALLFWDSMSSENTTOCONTACT.mvInt + mvI)
                                    mvPseudoToastMessage = mvSmsForwardedToContact.also{mvWhichContact = mvI}
                                if (mvCallFwdCode == ClassEnum.CALLFWDSMSFAILEDTOCONTACT.mvInt + mvI)
                                    mvPseudoToastMessage = mvSmsNotForwardedToContact.also{mvWhichContact = mvI}
                                if (mvCallFwdCode == ClassEnum.CALLFWDSMSGOTEXCEPTIONTOCONTACT.mvInt + mvI)
                                    mvPseudoToastMessage = mvSmsGotExceptionToContact.also{mvAppend = false}.also{mvDismiss = true}.also{mvWhichContact = mvI}
                            }
                        }

                        //Signal The Pseudo-Toaster
                        if (mvPseudoToastMessage != "") mvContext.sendBroadcast(Intent("mbPseudoToast").putExtra("mvToastMessage", mvPseudoToastMessage).putExtra("mvAppend", mvAppend).putExtra("mvDismiss", mvDismiss).putExtra("mvWhichBlock", mvWhichBlock).putExtra("mvWhichContact", mvWhichContact))
                    }
                }
                //Special Callbacks
                private fun mmCallback(mvCallbackCode : Int)
                {
                    //Our Only Current Callback Is If We "Close App" From "Secondary Caregivee Button" (I.E. Notification Swipe)
                    when (mvCallbackCode)
                    {
                        ClassEnum.CALLBACKCLOSEAPP.mvInt -> {mmStopAllPendingSound()
                            mvMuteAllSounds = true
                            Handler(Looper.getMainLooper()).postDelayed({ exitProcess(-1) },3500)} //<-- Delay Exiting Until After The Toast (Which Is Handled In BeginForegroundServiceRunnable) Disappears: https://stackoverflow.com/a/7607614/16118981
                        else                             ->  mmPlayFirst() //<-- Under Normal Circumstances, We Want To Just Move Onto The Next Sound
                    }
                }
            }
    */


/* SOUND MANAGER PRIORITY SOUNDS V1
package com.example.caregivee

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import kotlin.system.exitProcess

//Review Code For This Page [√√√]

//Note: Only Instantiate Once Per Activity And Call "mmScheduleSound()" Whenever We Want To Play An Sound File
    @Suppress("UNUSED")
    class ClassSound (val mvContext : Context) {
        //Create A MutableList (To Act As A Queue) Slated To Comprise The Sounds We Want To Put In The Queue
            private var mvSoundBuffer: MutableList<Triple<MediaPlayer, Int, Int>> = arrayListOf()
            private var mvMuteAllSounds = false
            private var mvHighestPriority = 0

        //Add Our New Sound To Queue And Play It Immediately IF We Just Added To An EMPTY Queue (Otherwise, The Already Running onCompletionListener In mmPlayFirst() Ensures It Will Automatically Play The Next One In The Queue Once The Current Sound Is Finished)
            fun mmScheduleSound(mvSound : Int /* <- mvSound contains a reference like "R.raw.mvSound" */, mvCallFwdCode : Int, mvCallBackCode : Int, mvPlaySound : Boolean, mvImmediateCallFwd : Boolean) {
                if (mvPlaySound) mvSoundBuffer.also{ it.add(Triple(MediaPlayer.create(mvContext, mvSound), if (!mvImmediateCallFwd) mvCallFwdCode else ClassEnum.CALLFWDNONE.mvInt, mvCallBackCode)) }.also{ if (mvSoundBuffer.size == 1) mmPlayFirst() }.also{ mmCallForward(if (mvImmediateCallFwd) mvCallFwdCode else ClassEnum.CALLFWDNONE.mvInt) } else mmCallForward(mvCallFwdCode) //<-- Sometimes, We Just Want To Skip The Sound Altogether And Go Right To The CallForward Function (Be It "Immediate CallForward" Or Otherwise), Like When We Do A Non-SMS Iteration In BeginForegroundServiceRunnable (I.E. mvSendSms = false).
             }

        //Special Variant of mmScheduleSound Where We Stop And Rewind All Sounds, PREPEND The New Sound To The Queue, And Play Immediately
            @Suppress("UNUSED_PARAMETER")
            fun mmSchedulePrioritySound(mvIndex : Int, mvSound : Int /* <- mvSound contains a reference like "R.raw.mvSound" */, mvCallFwdCode : Int, mvCallBackCode : Int, mvPlaySound : Boolean, mvImmediateCallFwd : Boolean) {
                mvSoundBuffer.also{it.getOrNull(0)?.first?.also{mvIt -> mvIt.pause()}.also{mvIt -> mvIt?.seekTo(0)}}.also{ it.add(mvIndex + if(mvHighestPriority > 0) 1 else 0 /* <-- If mvHighestPriority Was Recently Incremented But Not Yet Decremented, That Means A HIGHEST Priority Sound Is Likely Playing... So Please Reduce Priority Of The New Sound */, Triple(MediaPlayer.create(mvContext, mvSound), if (!mvImmediateCallFwd) mvCallFwdCode else ClassEnum.CALLFWDNONE.mvInt, mvCallBackCode)) }.also{if (mvSound == R.raw.ma_volume_low) mvHighestPriority++}.also{ mmPlayFirst() }.also{ mmCallForward(if (mvImmediateCallFwd) mvCallFwdCode else ClassEnum.CALLFWDNONE.mvInt) }
            }
        //Play The First Sound In The Queue If It Exists And It's Not Playing, "Remove" It From The Queue onCompletion, Then Repeat The Process
            private fun mmPlayFirst() {
                if (!mvMuteAllSounds) mvSoundBuffer.getOrNull(0)?.let{if (it.first.isPlaying) null else it}?.also{it.first.start()}?.also{mmCallForward(it.second)}?.also{it.first.setOnCompletionListener{mvSoundBuffer.removeFirstOrNull()?.also{mvIt -> mvIt.first.release()}?.also{mvIt -> mmCallback(mvIt.third)}}} //"?." Safety Check: In Any Situation Where We Return null, It Theoretically Just Shouldn't Play The Sound
            }
        //Pause All Pending Sounds, Release Allotted Resources, And "Clear" It From The Queue
            fun mmStopAllPendingSound() {
                mvSoundBuffer.also{ mvIt -> for (mvSubSound in mvIt) {mvSubSound.also{it.first.pause()}.first.release()}}.clear()
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
            private var mvSmsDelivered              = mvContext.getString(R.string.mtSmsDelivered)
            private var mvSmsForwardedToContact     = mvContext.getString(R.string.mtSmsForwardedToContact)
            private var mvSmsGotExceptionToContact  = mvContext.getString(R.string.mtSmsGotExceptionToContact)
            private var mvSmsNotDelivered           = mvContext.getString(R.string.mtSmsNotDelivered)
            private var mvSmsNotForwardedToContact  = mvContext.getString(R.string.mtSmsNotForwardedToContact)

        //Special Callforwards
            private fun mmCallForward(mvCallFwdCode : Int) {
                if (mvCallFwdCode != ClassEnum.CALLFWDNONE.mvInt) {
                    //Default Values
                        mvPseudoToastMessage = ""
                        mvAppend             = true  //<-- Do We Append To An Existing Pseudo-Toast Block, Or Overwrite?
                        mvDismiss            = false //<-- Does The Pseudo-Toast Automatically Disappear?
                        mvWhichBlock         =  2    //<-- Bottommost Pseudo-Toast Block Takes Precedence
                        mvWhichContact       = -1    //<-- This Variable Is So We Can Forward Which Contact (Was Contacted) To The Broadcast Receiver

                    //Fetch The Appropriate Pseudo-Toast Message
                        when(mvCallFwdCode) {
                             ClassEnum.CALLFWDAIRPLANEMODE.mvInt      -> mvPseudoToastMessage = mvAirplaneMode.also{mvAppend = false}.also{mvWhichBlock = 1}
                             ClassEnum.CALLFWDGPSEXPIRED.mvInt        -> mvPseudoToastMessage = mvGpsExpired.also{mvAppend = false}.also{mvWhichBlock = 0}
                             ClassEnum.CALLFWDLOWSIGNAL.mvInt         -> mvPseudoToastMessage = mvLowSignal.also{mvAppend = false}.also{mvWhichBlock = 1}
                             ClassEnum.CALLFWDNOCONTACTS.mvInt        -> mvPseudoToastMessage = mvNoContactsInApp.also{mvAppend = false}.also{mvWhichBlock = 1}
                             ClassEnum.CALLFWDSIRENMODE.mvInt         -> mvPseudoToastMessage = mvSirenMode.also{mvAppend = false}.also{mvWhichBlock = 1}
                             ClassEnum.CALLFWDSMSDELIVERED.mvInt      -> mvPseudoToastMessage = mvSmsDelivered.also{mvDismiss = true}
                             ClassEnum.CALLFWDSMSDELIVERYFAILED.mvInt -> mvPseudoToastMessage = mvSmsNotDelivered.also{mvDismiss = true}
                             else                                     -> for (mvI in 0 until mvNumberOfContacts) {
                                                                             if (mvCallFwdCode == ClassEnum.CALLFWDSMSSENTTOCONTACT.mvInt + mvI)
                                                                                 mvPseudoToastMessage = mvSmsForwardedToContact.also{mvWhichContact = mvI}
                                                                             if (mvCallFwdCode == ClassEnum.CALLFWDSMSFAILEDTOCONTACT.mvInt + mvI)
                                                                                 mvPseudoToastMessage = mvSmsNotForwardedToContact.also{mvWhichContact = mvI}
                                                                             if (mvCallFwdCode == ClassEnum.CALLFWDSMSGOTEXCEPTIONTOCONTACT.mvInt + mvI)
                                                                                 mvPseudoToastMessage = mvSmsGotExceptionToContact.also{mvAppend = false}.also{mvDismiss = true}.also{mvWhichContact = mvI}
                                                                         }
                        }

                    //Signal The Pseudo-Toaster
                        if (mvPseudoToastMessage != "") mvContext.sendBroadcast(Intent("mbPseudoToast").putExtra("mvToastMessage", mvPseudoToastMessage).putExtra("mvAppend", mvAppend).putExtra("mvDismiss", mvDismiss).putExtra("mvWhichBlock", mvWhichBlock).putExtra("mvWhichContact", mvWhichContact))
                }
            }
        //Special Callbacks
            private fun mmCallback(mvCallbackCode : Int) {
                //Our Only Current Callback Is If We "Close App" From "Secondary Caregivee Button" (I.E. Notification Swipe)
                    when (mvCallbackCode) {
                        ClassEnum.CALLBACKCLOSEAPP.mvInt -> {mmStopAllPendingSound()
                                                             mvMuteAllSounds = true
                                                             Handler(Looper.getMainLooper()).postDelayed({exitProcess(-1)},3500)} //<-- Delay Exiting Until After The Toast (Which Is Handled In BeginForegroundServiceRunnable) Disappears: https://stackoverflow.com/a/7607614/16118981
                        else                             ->  (mvHighestPriority--).also{mmPlayFirst()} //<-- Under Normal Circumstances, We Want To Just Move Onto The Next Sound
                    }
            }
    }

 */

/* SOUND MANAGER PRIORITY SOUNDS V2

package com.example.caregivee

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import kotlin.system.exitProcess

//Review Code For This Page [√√√]

//Note: Only Instantiate Once Per Activity And Call "mmScheduleSound()" Whenever We Want To Play An Sound File
@Suppress("UNUSED")
class ClassSound (val mvContext : Context) {
    //Create A MutableList (To Act As A Queue) Slated To Comprise The Sounds We Want To Put In The Queue
    //Note: The First Four Slots Contain Our Highest Priority Sounds. The Earlier They Appear In The List, The More They Need To Take Precedence Over Others.
        private var mvSoundBuffer: MutableList<MutableList<Any>> = arrayListOf(arrayListOf(false, MediaPlayer.create(mvContext, R.raw.ma_volume_low),                 ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt),
                                                                               arrayListOf(false, MediaPlayer.create(mvContext, R.raw.ma_please_turn_screen_back_on), ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt),
                                                                               arrayListOf(false, MediaPlayer.create(mvContext, R.raw.ma_please_return_to_app),       ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt),
                                                                               arrayListOf(false, MediaPlayer.create(mvContext, R.raw.ma_please_plug_in),             ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt))
        private var mvIndexZero = 4
        private var mvMuteAllSounds = false
        private var mvHighestPriority = 0


    //When A Sound Buffer Entry's Zeroth Element Is Set To false, It Theoretically Is No Longer Usable
    //Let's Remove Any Unusable Ones From The End
    //NOTE: Due To The Asynchronous Nature Of The Way Sounds Are Added, It Might Be Beneficial To Only Call This When You're ABSOLUTELY Certain There Are No Other Sounds That Are Going To Play
        fun mmCleanupSounds()
        {
            //?!?!?!?! WHEN SHOULD THIS BE CALLED?
                while (mvSoundBuffer.size > mvIndexZero) { if (mvSoundBuffer.last()[0] == false) mvSoundBuffer.removeLast() else break }
        }
    //Add Our New Sound To Queue And Play It Immediately IF We Just Added To An EMPTY Queue (Otherwise, The Already Running onCompletionListener In mmPlayFirst() Ensures It Will Automatically Play The Next One In The Queue Once The Current Sound Is Finished)
        fun mmScheduleSound(mvSound : Int /* <- mvSound contains a reference like "R.raw.mvSound" */, mvCallFwdCode : Int, mvCallBackCode : Int, mvPlaySound : Boolean, mvImmediateCallFwd : Boolean) {
            if (mvPlaySound) { mvSoundBuffer.also{ it.add(arrayListOf(true, MediaPlayer.create(mvContext, mvSound), if (!mvImmediateCallFwd) mvCallFwdCode else ClassEnum.CALLFWDNONE.mvInt, mvCallBackCode)) }.also{mmPlayFirst()}.also{mmCallForward(if (mvImmediateCallFwd) mvCallFwdCode else ClassEnum.CALLFWDNONE.mvInt) } } else { mmCallForward(mvCallFwdCode) } //<-- Sometimes, We Just Want To Skip The Sound Altogether And Go Right To The CallForward Function (Be It "Immediate CallForward" Or Otherwise), Like When We Do A Non-SMS Iteration In BeginForegroundServiceRunnable (I.E. mvSendSms = false).
        }

    //Special Variant of mmScheduleSound Where We Stop And Rewind All Sounds, PREPEND The New Sound To The Queue, And Play Immediately
        fun mmSchedulePrioritySound(mvIndex : Int) {
            mvSoundBuffer.also{for (mvI in 0 until mvSoundBuffer.size) { if (mvI != mvIndex /* <-- ?!?!?!?! Don't Double Up!, Doesn't Seem To Currently Work, See Challenge 4.0 */ && it[mvI][0] == true && (it[mvI][1] as MediaPlayer).isPlaying) {it.also{mvIt -> (mvIt[mvI][1] as MediaPlayer).pause()}.also{mvIt -> (mvIt[mvI][1] as MediaPlayer).seekTo(0)}}}}.also{it[mvIndex][0] = true /* This Makes Our Desired Priority Sound Now Playable In The Queue */}.also{mmPlayFirst()}
        }
    //Play The First Sound In The Queue If It Exists And It's Not Playing, "Remove" It From The Queue onCompletion, Then Repeat The Process
        private fun mmPlayFirst() {
            //?!?!?!?! Combine the MIGHT TIME OUT or MIGHT TURN OFF with the appropriate priorities directly in AUDACITY
            //?!?!?!?! BACKUP THE PREVIOUS WAY WE DID ClassSound() :D
            //?!?!?!?! A few overlaps:
            //            Marshmallow: Turned off screen, then immediately tried to turn down volume. Got some overlap, but then when Volume Too Low was done, it did move on to "Please turn the screen back on".
            //            Marshmallow: Turned off the screen. After a bit, tried to turn down the volume, got some overlap over please check in and text sent to contact. (or it's possible that instead, it was that I turned off the screen at that point and got overlap with "turn t he screen back on" possibly from the next iteration, and then our reguarly scheduled "please check in" and "text sent")
            //            Result:      Maybe there needs to be an extra redundancy to keep PrioritySound from conflicting with PlaySound, like a HIGHPRIORITY == 1 or something?
            //            Upside Down: Challenge 4.0 -> Turn off screen, turn back on, then immediately check in (it repeats Please Turn the screen back on, probably due to the prioritysound iteration, shouldn't it just let it go through?)
                if (!mvMuteAllSounds) mvSoundBuffer.also{for (mvI in 0 until mvSoundBuffer.size) { if (it[mvI][0] == true) { (if ((it[mvI][1] as MediaPlayer).isPlaying) null else it)?.also{(it[mvI][1] as MediaPlayer).start()}?.also{mmCallForward((it[mvI][2] as Int))}?.also{(it[mvI][1] as MediaPlayer).setOnCompletionListener{mvSoundBuffer.also{it[mvI][0] = false}.also{if (mvI >= mvIndexZero) (it[mvI][1] as MediaPlayer).release()}.also{mmCallback(it[mvI][3] as Int)}}} ; break}}} //"?." Safety Check: In Any Situation Where We Return null, It Theoretically Just Shouldn't Play The Sound
        }
    //Pause All Pending Sounds, Release Allotted Resources, And "Clear" It From The Queue
    //Start After The High Priority Sounds, Just In Case We Want To Tell The User Some Important Priority Info
        fun mmStopAllPendingSound() {
            mvSoundBuffer.also{ for (mvI in mvIndexZero until mvSoundBuffer.size) if (it[mvI][0] == true) it.also{mvIt -> (mvIt[mvI][1] as MediaPlayer).pause()}.also{mvIt -> (mvIt[mvI][1] as MediaPlayer).release()}}
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
        private var mvSmsDelivered              = mvContext.getString(R.string.mtSmsDelivered)
        private var mvSmsForwardedToContact     = mvContext.getString(R.string.mtSmsForwardedToContact)
        private var mvSmsGotExceptionToContact  = mvContext.getString(R.string.mtSmsGotExceptionToContact)
        private var mvSmsNotDelivered           = mvContext.getString(R.string.mtSmsNotDelivered)
        private var mvSmsNotForwardedToContact  = mvContext.getString(R.string.mtSmsNotForwardedToContact)

    //Special Callforwards
        private fun mmCallForward(mvCallFwdCode : Int) {
            if (mvCallFwdCode != ClassEnum.CALLFWDNONE.mvInt) {
                //Default Values
                    mvPseudoToastMessage = ""
                    mvAppend             = true  //<-- Do We Append To An Existing Pseudo-Toast Block, Or Overwrite?
                    mvDismiss            = false //<-- Does The Pseudo-Toast Automatically Disappear?
                    mvWhichBlock         =  2    //<-- Bottommost Pseudo-Toast Block Takes Precedence
                    mvWhichContact       = -1    //<-- This Variable Is So We Can Forward Which Contact (Was Contacted) To The Broadcast Receiver

                //Fetch The Appropriate Pseudo-Toast Message
                    when(mvCallFwdCode) {
                         ClassEnum.CALLFWDAIRPLANEMODE.mvInt      -> mvPseudoToastMessage = mvAirplaneMode.also{mvAppend = false}.also{mvWhichBlock = 1}
                         ClassEnum.CALLFWDGPSEXPIRED.mvInt        -> mvPseudoToastMessage = mvGpsExpired.also{mvAppend = false}.also{mvWhichBlock = 0}
                         ClassEnum.CALLFWDLOWSIGNAL.mvInt         -> mvPseudoToastMessage = mvLowSignal.also{mvAppend = false}.also{mvWhichBlock = 1}
                         ClassEnum.CALLFWDNOCONTACTS.mvInt        -> mvPseudoToastMessage = mvNoContactsInApp.also{mvAppend = false}.also{mvWhichBlock = 1}
                         ClassEnum.CALLFWDSIRENMODE.mvInt         -> mvPseudoToastMessage = mvSirenMode.also{mvAppend = false}.also{mvWhichBlock = 1}
                         ClassEnum.CALLFWDSMSDELIVERED.mvInt      -> mvPseudoToastMessage = mvSmsDelivered.also{mvDismiss = true}
                         ClassEnum.CALLFWDSMSDELIVERYFAILED.mvInt -> mvPseudoToastMessage = mvSmsNotDelivered.also{mvDismiss = true}
                         else                                     -> for (mvI in 0 until mvNumberOfContacts) {
                                                                         if (mvCallFwdCode == ClassEnum.CALLFWDSMSSENTTOCONTACT.mvInt + mvI)
                                                                             mvPseudoToastMessage = mvSmsForwardedToContact.also{mvWhichContact = mvI}
                                                                         if (mvCallFwdCode == ClassEnum.CALLFWDSMSFAILEDTOCONTACT.mvInt + mvI)
                                                                             mvPseudoToastMessage = mvSmsNotForwardedToContact.also{mvWhichContact = mvI}
                                                                         if (mvCallFwdCode == ClassEnum.CALLFWDSMSGOTEXCEPTIONTOCONTACT.mvInt + mvI)
                                                                             mvPseudoToastMessage = mvSmsGotExceptionToContact.also{mvAppend = false}.also{mvDismiss = true}.also{mvWhichContact = mvI}
                                                                     }
                    }

                //Signal The Pseudo-Toaster
                    if (mvPseudoToastMessage != "") mvContext.sendBroadcast(Intent("mbPseudoToast").putExtra("mvToastMessage", mvPseudoToastMessage).putExtra("mvAppend", mvAppend).putExtra("mvDismiss", mvDismiss).putExtra("mvWhichBlock", mvWhichBlock).putExtra("mvWhichContact", mvWhichContact))
            }
        }
    //Special Callbacks
        private fun mmCallback(mvCallbackCode : Int) {
            //Our Only Current Callback Is If We "Close App" From "Secondary Caregivee Button" (I.E. Notification Swipe)
                when (mvCallbackCode) {
                    ClassEnum.CALLBACKCLOSEAPP.mvInt -> {mmStopAllPendingSound()
                                                         mvMuteAllSounds = true
                                                         Handler(Looper.getMainLooper()).postDelayed({exitProcess(-1)},3500)} //<-- Delay Exiting Until After The Toast (Which Is Handled In BeginForegroundServiceRunnable) Disappears: https://stackoverflow.com/a/7607614/16118981
                    else                             ->  (mvHighestPriority--).also{mmPlayFirst()} //<-- Under Normal Circumstances, We Want To Just Move Onto The Next Sound
                }
        }
}




 */

    /* SOUND MANAGER PRIORITY SOUNDS V3

    package com.example.caregivee

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import kotlin.system.exitProcess

//Review Code For This Page [√√√]

//Note: Only Instantiate Once Per Activity And Call "mmScheduleSound()" Whenever We Want To Play An Sound File
@Suppress("UNUSED")
class ClassSound (val mvContext : Context) {
    //Create A Special MutableList For The Most Important Sounds (These Should Take Priority And In Listed Order, Superseding Any That Are Playing After Them)
        private var mvPrioritySoundBoard: MutableList<ClassMediaPlayer> = arrayListOf(ClassMediaPlayer(0, R.raw.ma_volume_low, mvContext),
                                                                                      ClassMediaPlayer(1, R.raw.ma_please_turn_screen_back_on, mvContext),
                                                                                      ClassMediaPlayer(2, R.raw.ma_please_return_to_app, mvContext),
                                                                                      ClassMediaPlayer(3, R.raw.ma_please_plug_in, mvContext))
        private var mvPriority = false
    //Create A MutableList (To Act As A Queue) Slated To Comprise The Sounds We Want To Put In The Queue
        private var mvSoundBuffer: MutableList<Triple<MediaPlayer, Int, Int>> = arrayListOf()
        private var mvMuteAllSounds = false


    //Pause And Rewind All Current Non-Priority And Priority Sounds If Need Be, Then Schedule The Desired Priority Sound To Play
        fun mmSchedulePrioritySound(mvIndex : Int) {
            mvIndex.also{for (mvSound in mvSoundBuffer) {mvSound.also{it.first.pause()}.also{it.first.seekTo(0)}}}.also{for (mvSound in mvPrioritySoundBoard) {if (mvIndex < mvSound.mvInt && mvSound.mvMediaPlayer.isPlaying) mvSound.also{it.mvMediaPlayer.pause()}.also{it.mvMediaPlayer.seekTo(0)}}}.also{mvPrioritySoundBoard[mvIndex].mvScheduled = true}.also{mvPriority = true}.also{mmPlayFirstPrioritySoundInQueue()}
        }
    //Add Our New Sound To Queue And Play It Immediately IF We Just Added To An EMPTY Queue (Otherwise, The Already Running onCompletionListener In mmPlayFirstSoundInQueue() Ensures It Will Automatically Play The Next One In The Queue Once The Current Sound Is Finished)
        fun mmScheduleSound(mvSound : Int, mvCallFwdCode : Int, mvCallBackCode : Int, mvPlaySound : Boolean, mvImmediateCallFwd : Boolean) {
            if (mvPlaySound) mvSoundBuffer.also{it.add(Triple(MediaPlayer.create(mvContext, mvSound), if (!mvImmediateCallFwd) mvCallFwdCode else ClassEnum.CALLFWDNONE.mvInt, mvCallBackCode))}.also{if (mvSoundBuffer.size == 1) mmPlayFirstSoundInQueue()}.also{ mmCallForward(if (mvImmediateCallFwd) mvCallFwdCode else ClassEnum.CALLFWDNONE.mvInt) } else mmCallForward(mvCallFwdCode) //<-- Sometimes, We Just Want To Skip The Sound Altogether And Go Right To The CallForward Function (Be It "Immediate CallForward" Or Otherwise), Like When We Do A Non-SMS Iteration In BeginForegroundServiceRunnable (I.E. mvSendSms = false).
        }
    //Play Priority Sounds In Order Of Priority, Ignoring When One Is Already Playing, And Going Back To Playing Standard Sounds If There Are No More Scheduled Priority Sounds
        private fun mmPlayFirstPrioritySoundInQueue() {
            //Current Challenges:
            //1) Text sent to contact ___ (didn't add the number for some reason)
            //   ^ This only happened once, not sure why... not sure how to replicate. Might've just been a one-off.
            //2) There were briefly RED pause and seekTo errors in the console (may be related to the above issue... we may require some extra redundant checks to make sure it's pauseable and rewindable)
            //Theoretical Challenges:
            //?!?!?!?! mvPriority asynchroniicty concern at end of all priority sounds and at beginning of new priority sound
            //         So, let's say we're approaching the end of the last priority sound in the queue.
            //         A new priority sound clicks us into mvPriority == true, but then the last priority sound decrements us to mvPriority == false
            //         That makes this inaccessible.
            //         But what if we were to set mvPriority == true here! in this method instead at the beginning, remove the conditional here, and then continue?
            //         What if both fo the playfirst___ methods always check if the other one has something going?
            //            So, PlayFirstPriority would always cancel and rewind any pending Standard sounds.
            //            And instead of using mvPrioirty, PlayFirstStandard would just check to make sure there aren't any pending "true" values in the Priority sounds.
            if (!mvMuteAllSounds && mvPriority) mvPrioritySoundBoard.also{for (mvSound in it) { if (mvSound.mvScheduled && !mvSound.mvMediaPlayer.isPlaying) {mvSound.mvMediaPlayer.also{ mvIt -> mvIt.start()}.also{ mvIt -> mvIt.setOnCompletionListener { mvSound.also{mvItB -> mvItB.mvScheduled = false}.also{mmPlayFirstPrioritySoundInQueue()}} } ; break} else if (mvSound.mvMediaPlayer.isPlaying) { break } else if (mvSound.mvInt >= mvPrioritySoundBoard.size-1) { mvPrioritySoundBoard.also{mvPriority = false}.also{mmPlayFirstSoundInQueue()}} }}
        }
    //Play The First Sound In The Queue If It Exists And It's Not Playing, "Remove" It From The Queue onCompletion, Then Repeat The Process
        private fun mmPlayFirstSoundInQueue() {
            if (!mvMuteAllSounds && !mvPriority) mvSoundBuffer.getOrNull(0)?.let{if (it.first.isPlaying) null else it}?.also{it.first.start()}?.also{mmCallForward(it.second)}?.also{it.first.setOnCompletionListener{mvSoundBuffer.removeFirstOrNull()?.also{mvIt -> mvIt.first.release()}?.also{mvIt -> mmCallback(mvIt.third)}}} //"?." Safety Check: In Any Situation Where We Return null, It Theoretically Just Shouldn't Play The Sound
        }
    //Pause All Pending Sounds, Release Allotted Resources, And "Clear" It From The Queue
    //Note: Currently Doesn't Stop Any Pending Priority Sounds
        fun mmStopAllPendingSound() {
            mvSoundBuffer.also{for (mvSound in it) {mvSound.also{mvIt -> mvIt.first.pause()}.also{mvIt -> mvIt.first.release()}}}.clear()
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
        private var mvSmsDelivered              = mvContext.getString(R.string.mtSmsDelivered)
        private var mvSmsForwardedToContact     = mvContext.getString(R.string.mtSmsForwardedToContact)
        private var mvSmsGotExceptionToContact  = mvContext.getString(R.string.mtSmsGotExceptionToContact)
        private var mvSmsNotDelivered           = mvContext.getString(R.string.mtSmsNotDelivered)
        private var mvSmsNotForwardedToContact  = mvContext.getString(R.string.mtSmsNotForwardedToContact)

    //Special Callforwards
        private fun mmCallForward(mvCallFwdCode : Int)
        {
            if (mvCallFwdCode != ClassEnum.CALLFWDNONE.mvInt)
            {
                //Default Values
                    mvPseudoToastMessage = ""
                    mvAppend             = true  //<-- Do We Append To An Existing Pseudo-Toast Block, Or Overwrite?
                    mvDismiss            = false //<-- Does The Pseudo-Toast Automatically Disappear?
                    mvWhichBlock         =  2    //<-- Bottommost Pseudo-Toast Block Takes Precedence
                    mvWhichContact       = -1    //<-- This Variable Is So We Can Forward Which Contact (Was Contacted) To The Broadcast Receiver

                //Fetch The Appropriate Pseudo-Toast Message
                    when(mvCallFwdCode) {
                        ClassEnum.CALLFWDAIRPLANEMODE.mvInt      -> mvPseudoToastMessage = mvAirplaneMode.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDGPSEXPIRED.mvInt        -> mvPseudoToastMessage = mvGpsExpired.also{mvAppend = false}.also{mvWhichBlock = 0}
                        ClassEnum.CALLFWDLOWSIGNAL.mvInt         -> mvPseudoToastMessage = mvLowSignal.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDNOCONTACTS.mvInt        -> mvPseudoToastMessage = mvNoContactsInApp.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDSIRENMODE.mvInt         -> mvPseudoToastMessage = mvSirenMode.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDSMSDELIVERED.mvInt      -> mvPseudoToastMessage = mvSmsDelivered.also{mvDismiss = true}
                        ClassEnum.CALLFWDSMSDELIVERYFAILED.mvInt -> mvPseudoToastMessage = mvSmsNotDelivered.also{mvDismiss = true}
                        else                                     -> for (mvI in 0 until mvNumberOfContacts)
                        {
                            if (mvCallFwdCode == ClassEnum.CALLFWDSMSSENTTOCONTACT.mvInt + mvI)
                                mvPseudoToastMessage = mvSmsForwardedToContact.also{mvWhichContact = mvI}
                            if (mvCallFwdCode == ClassEnum.CALLFWDSMSFAILEDTOCONTACT.mvInt + mvI)
                                mvPseudoToastMessage = mvSmsNotForwardedToContact.also{mvWhichContact = mvI}
                            if (mvCallFwdCode == ClassEnum.CALLFWDSMSGOTEXCEPTIONTOCONTACT.mvInt + mvI)
                                mvPseudoToastMessage = mvSmsGotExceptionToContact.also{mvAppend = false}.also{mvDismiss = true}.also{mvWhichContact = mvI}
                        }
                    }

                //Signal The Pseudo-Toaster
                    if (mvPseudoToastMessage != "") mvContext.sendBroadcast(Intent("mbPseudoToast").putExtra("mvToastMessage", mvPseudoToastMessage).putExtra("mvAppend", mvAppend).putExtra("mvDismiss", mvDismiss).putExtra("mvWhichBlock", mvWhichBlock).putExtra("mvWhichContact", mvWhichContact))
            }
        }
        //Special Callbacks
            private fun mmCallback(mvCallbackCode : Int)
            {
                //Our Only Current Callback Is If We "Close App" From "Secondary Caregivee Button" (I.E. Notification Swipe)
                    when (mvCallbackCode)
                    {
                        ClassEnum.CALLBACKCLOSEAPP.mvInt -> {mmStopAllPendingSound()
                            mvMuteAllSounds = true
                            Handler(Looper.getMainLooper()).postDelayed({ exitProcess(-1) },3500)} //<-- Delay Exiting Until After The Toast (Which Is Handled In BeginForegroundServiceRunnable) Disappears: https://stackoverflow.com/a/7607614/16118981
                        else                             ->  mmPlayFirstSoundInQueue() //<-- Under Normal Circumstances, We Want To Just Move Onto The Next Sound
                    }
            }
}
     */
    /* SOUND MANAGER v 4

    package com.example.caregivee

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import kotlin.system.exitProcess

//Review Code For This Page [√√√]

//Note: Only Instantiate Once Per Activity And Call "mmScheduleSound()" Whenever We Want To Play An Sound File
@Suppress("UNUSED")
class ClassSound (val mvContext : Context) {
    //Create A Special MutableList For The Most Important Sounds (These Should Take Priority And In Listed Order, Superseding Any That Are Playing After Them)
        private var mvPrioritySoundBoard: MutableList<ClassMediaPlayer> = arrayListOf(ClassMediaPlayer(0, R.raw.ma_volume_low, mvContext),
                                                                                      ClassMediaPlayer(1, R.raw.ma_please_turn_screen_back_on, mvContext),
                                                                                      ClassMediaPlayer(2, R.raw.ma_please_return_to_app, mvContext),
                                                                                      ClassMediaPlayer(3, R.raw.ma_please_plug_in, mvContext))
        private val mvPrioritySoundBoardSize = mvPrioritySoundBoard.size
    //Create A MutableList (To Act As A Queue) Slated To Comprise The Sounds We Want To Put In The Queue
        private var mvSoundBuffer: MutableList<Triple<MediaPlayer, Int, Int>> = arrayListOf()
        private var mvMuteAllSounds = false


    //Pause And Rewind All Current Non-Priority And Priority Sounds If Need Be, Then Schedule The Desired Priority Sound To Play
        fun mmSchedulePrioritySound(mvIndex : Int) {
            mvIndex.also{mvPrioritySoundBoard[mvIndex].mvScheduled = true}.also{mmPlayFirstPrioritySoundInQueue()}
        }
    //Add Our New Sound To Queue And Play It Immediately IF We Just Added To An EMPTY Queue (Otherwise, The Already Running onCompletionListener In mmPlayFirstSoundInQueue() Ensures It Will Automatically Play The Next One In The Queue Once The Current Sound Is Finished)
        fun mmScheduleSound(mvSound : Int, mvCallFwdCode : Int, mvCallBackCode : Int, mvPlaySound : Boolean, mvImmediateCallFwd : Boolean) {
            if (mvPlaySound) mvSoundBuffer.also{it.add(Triple(MediaPlayer.create(mvContext, mvSound), if (!mvImmediateCallFwd) mvCallFwdCode else ClassEnum.CALLFWDNONE.mvInt, mvCallBackCode))}.also{if (mvSoundBuffer.size == 1) mmPlayFirstSoundInQueue()}.also{ mmCallForward(if (mvImmediateCallFwd) mvCallFwdCode else ClassEnum.CALLFWDNONE.mvInt) } else mmCallForward(mvCallFwdCode) //<-- Sometimes, We Just Want To Skip The Sound Altogether And Go Right To The CallForward Function (Be It "Immediate CallForward" Or Otherwise), Like When We Do A Non-SMS Iteration In BeginForegroundServiceRunnable (I.E. mvSendSms = false).
        }
    //Play Priority Sounds In Order Of Priority, Ignoring When One Is Already Playing, And Going Back To Playing Standard Sounds If There Are No More Scheduled Priority Sounds
        private fun mmPlayFirstPrioritySoundInQueue() {
            //Current Challenges:
            //0) Volume decrease doesn't seem to properly PAUSE "Please turn the screen back on"
            //1) Text sent to contact ___ (didn't add the number for some reason)
            //   ^ This only happened once, not sure why... not sure how to replicate. Might've just been a one-off.
            //2) There were briefly RED pause and seekTo errors in the console (may be related to the above issue... we may require some extra redundant checks to make sure it's pauseable and rewindable)
            //Theoretical Challenges:
            //What if the asynchronicity works like this:
            //1. Nothing is scheduled priority, so we begin the process of playing a sound.
            //2. Then a priority sound is scheduled and then immediately stops and rewinds all normal sounds.
            //3. Then the regular sound continues and plays.
            //4. Likelihood: Unlikely due to the amount of delay that would have to occur within the same "line" of code.
                if (!mvMuteAllSounds) (mvPrioritySoundBoard.firstOrNull { it.mvScheduled }?.mvInt ?: mvPrioritySoundBoardSize).also{for (mvSound in mvSoundBuffer) {mvSound.also{it.first.pause()}.also{it.first.seekTo(0)}}}.also{for (mvSound in mvPrioritySoundBoard) { if (it < mvSound.mvInt && mvSound.mvMediaPlayer.isPlaying) mvSound.also{ mvIt -> mvIt.mvMediaPlayer.pause()}.also{ mvIt -> mvIt.mvMediaPlayer.seekTo(0)} else if (mvSound.mvScheduled && !mvSound.mvMediaPlayer.isPlaying) {mvSound.mvMediaPlayer.also{ mvIt -> mvIt.start()}.also{ mvIt -> mvIt.setOnCompletionListener { mvSound.also{ mvS -> mvS.mvScheduled = false}.also{mmPlayFirstPrioritySoundInQueue()}}} ; break} else if (mvSound.mvMediaPlayer.isPlaying) { break } else if (mvSound.mvInt >= mvPrioritySoundBoardSize-1) { mvPrioritySoundBoard.also{mmPlayFirstSoundInQueue()}}}}
        }
    //Play The First Sound In The Queue If It Exists And It's Not Playing, "Remove" It From The Queue onCompletion, Then Repeat The Process
        private fun mmPlayFirstSoundInQueue() {
                if (!mvMuteAllSounds) (if (mvPrioritySoundBoard.any{it.mvScheduled}) null else true).also{mvSoundBuffer.getOrNull(0)?.let{if (it.first.isPlaying) null else it}?.also{it.first.start()}?.also{mmCallForward(it.second)}?.also{it.first.setOnCompletionListener{mvSoundBuffer.removeFirstOrNull()?.also{mvIt -> mvIt.first.release()}?.also{mvIt -> mmCallback(mvIt.third)}}}} //"?." Safety Check: In Any Situation Where We Return null, It Theoretically Just Shouldn't Play The Sound
        }
    //Pause All Pending Sounds, Release Allotted Resources, And "Clear" It From The Queue
    //Note: Currently Doesn't Stop Any Pending Priority Sounds
        fun mmStopAllPendingSound() {
            mvSoundBuffer.also{for (mvSound in it) {mvSound.also{mvIt -> mvIt.first.pause()}.also{mvIt -> mvIt.first.release()}}}.clear()
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
        private var mvSmsDelivered              = mvContext.getString(R.string.mtSmsDelivered)
        private var mvSmsForwardedToContact     = mvContext.getString(R.string.mtSmsForwardedToContact)
        private var mvSmsGotExceptionToContact  = mvContext.getString(R.string.mtSmsGotExceptionToContact)
        private var mvSmsNotDelivered           = mvContext.getString(R.string.mtSmsNotDelivered)
        private var mvSmsNotForwardedToContact  = mvContext.getString(R.string.mtSmsNotForwardedToContact)

    //Special Callforwards
        private fun mmCallForward(mvCallFwdCode : Int)
        {
            if (mvCallFwdCode != ClassEnum.CALLFWDNONE.mvInt)
            {
                //Default Values
                    mvPseudoToastMessage = ""
                    mvAppend             = true  //<-- Do We Append To An Existing Pseudo-Toast Block, Or Overwrite?
                    mvDismiss            = false //<-- Does The Pseudo-Toast Automatically Disappear?
                    mvWhichBlock         =  2    //<-- Bottommost Pseudo-Toast Block Takes Precedence
                    mvWhichContact       = -1    //<-- This Variable Is So We Can Forward Which Contact (Was Contacted) To The Broadcast Receiver

                //Fetch The Appropriate Pseudo-Toast Message
                    when(mvCallFwdCode) {
                        ClassEnum.CALLFWDAIRPLANEMODE.mvInt      -> mvPseudoToastMessage = mvAirplaneMode.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDGPSEXPIRED.mvInt        -> mvPseudoToastMessage = mvGpsExpired.also{mvAppend = false}.also{mvWhichBlock = 0}
                        ClassEnum.CALLFWDLOWSIGNAL.mvInt         -> mvPseudoToastMessage = mvLowSignal.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDNOCONTACTS.mvInt        -> mvPseudoToastMessage = mvNoContactsInApp.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDSIRENMODE.mvInt         -> mvPseudoToastMessage = mvSirenMode.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDSMSDELIVERED.mvInt      -> mvPseudoToastMessage = mvSmsDelivered.also{mvDismiss = true}
                        ClassEnum.CALLFWDSMSDELIVERYFAILED.mvInt -> mvPseudoToastMessage = mvSmsNotDelivered.also{mvDismiss = true}
                        else                                     -> for (mvI in 0 until mvNumberOfContacts)
                        {
                            if (mvCallFwdCode == ClassEnum.CALLFWDSMSSENTTOCONTACT.mvInt + mvI)
                                mvPseudoToastMessage = mvSmsForwardedToContact.also{mvWhichContact = mvI}
                            if (mvCallFwdCode == ClassEnum.CALLFWDSMSFAILEDTOCONTACT.mvInt + mvI)
                                mvPseudoToastMessage = mvSmsNotForwardedToContact.also{mvWhichContact = mvI}
                            if (mvCallFwdCode == ClassEnum.CALLFWDSMSGOTEXCEPTIONTOCONTACT.mvInt + mvI)
                                mvPseudoToastMessage = mvSmsGotExceptionToContact.also{mvAppend = false}.also{mvDismiss = true}.also{mvWhichContact = mvI}
                        }
                    }

                //Signal The Pseudo-Toaster
                    if (mvPseudoToastMessage != "") mvContext.sendBroadcast(Intent("mbPseudoToast").putExtra("mvToastMessage", mvPseudoToastMessage).putExtra("mvAppend", mvAppend).putExtra("mvDismiss", mvDismiss).putExtra("mvWhichBlock", mvWhichBlock).putExtra("mvWhichContact", mvWhichContact))
            }
        }
        //Special Callbacks
            private fun mmCallback(mvCallbackCode : Int)
            {
                //Our Only Current Callback Is If We "Close App" From "Secondary Caregivee Button" (I.E. Notification Swipe)
                    when (mvCallbackCode)
                    {
                        ClassEnum.CALLBACKCLOSEAPP.mvInt -> {mmStopAllPendingSound()
                            mvMuteAllSounds = true
                            Handler(Looper.getMainLooper()).postDelayed({ exitProcess(-1) },3500)} //<-- Delay Exiting Until After The Toast (Which Is Handled In BeginForegroundServiceRunnable) Disappears: https://stackoverflow.com/a/7607614/16118981
                        else                             ->  mmPlayFirstSoundInQueue() //<-- Under Normal Circumstances, We Want To Just Move Onto The Next Sound
                    }
            }
}
     */
    /*
    Sound MANAGER v5
    package com.example.caregivee

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import kotlin.system.exitProcess

//Review Code For This Page [√√√]

//Note: Only Instantiate Once Per Activity And Call "mmScheduleSound()" Whenever We Want To Play An Sound File
@Suppress("UNUSED")
class ClassSound (val mvContext : Context) {
    //Create A Special MutableList For The Most Important Sounds (These Should Take Priority And In Listed Order, Superseding Any That Are Playing After Them)
        private var mvPrioritySoundBoard: MutableList<ClassMediaPlayer> = arrayListOf(ClassMediaPlayer(0, R.raw.ma_volume_low, mvContext),
                                                                                      ClassMediaPlayer(1, R.raw.ma_please_turn_screen_back_on, mvContext),
                                                                                      ClassMediaPlayer(2, R.raw.ma_please_return_to_app, mvContext),
                                                                                      ClassMediaPlayer(3, R.raw.ma_please_plug_in, mvContext))
        private val mvPrioritySoundBoardSize = mvPrioritySoundBoard.size
    //Create A MutableList (To Act As A Queue) Slated To Comprise The Sounds We Want To Put In The Queue
        private var mvSoundBuffer: MutableList<Triple<MediaPlayer, Int, Int>> = arrayListOf()
        private var mvMuteAllSounds = false


    //Pause And Rewind All Current Non-Priority And Priority Sounds If Need Be, Then Schedule The Desired Priority Sound To Play
        fun mmSchedulePrioritySound(mvIndex : Int) {
            mvIndex.also{mvPrioritySoundBoard[mvIndex].mvScheduled = true}.also{mmPlayFirstPrioritySoundInQueue()}
        }
    //Add Our New Sound To Queue And Play It Immediately IF We Just Added To An EMPTY Queue (Otherwise, The Already Running onCompletionListener In mmPlayFirstSoundInQueue() Ensures It Will Automatically Play The Next One In The Queue Once The Current Sound Is Finished)
        fun mmScheduleSound(mvSound : Int, mvCallFwdCode : Int, mvCallBackCode : Int, mvPlaySound : Boolean, mvImmediateCallFwd : Boolean) {
            if (mvPlaySound) mvSoundBuffer.also{it.add(Triple(MediaPlayer.create(mvContext, mvSound), if (!mvImmediateCallFwd) mvCallFwdCode else ClassEnum.CALLFWDNONE.mvInt, mvCallBackCode))}.also{if (mvSoundBuffer.size == 1) mmPlayFirstSoundInQueue()}.also{ mmCallForward(if (mvImmediateCallFwd) mvCallFwdCode else ClassEnum.CALLFWDNONE.mvInt) } else mmCallForward(mvCallFwdCode) //<-- Sometimes, We Just Want To Skip The Sound Altogether And Go Right To The CallForward Function (Be It "Immediate CallForward" Or Otherwise), Like When We Do A Non-SMS Iteration In BeginForegroundServiceRunnable (I.E. mvSendSms = false).
        }
    //Play Priority Sounds In Order Of Priority, Ignoring When One Is Already Playing, And Going Back To Playing Standard Sounds If There Are No More Scheduled Priority Sounds
        private fun mmPlayFirstPrioritySoundInQueue() {
            //Current Challenges:
            //0) RELEASE AND CLEAR ALL PRIORITY SOUNDS ON APP EXIT... OR MAYBE EVEN ACTIVITY CHANGE!
            //0) VOLUME TOO LOW VISUAL KEEPS FLICKERING OFF... it should only flicker off when we start increasing?
            //0) Volume decrease doesn't seem to properly PAUSE "Please turn the screen back on"
            //1) Text sent to contact ___ (didn't add the number for some reason)
            //   ^ This only happened once, not sure why... not sure how to replicate. Might've just been a one-off.
            //2) There were briefly RED pause and seekTo errors in the console (may be related to the above issue... we may require some extra redundant checks to make sure it's pauseable and rewindable)
            //Theoretical Challenges:
            //What if the asynchronicity works like this:
            //1. Nothing is scheduled priority, so we begin the process of playing a sound.
            //2. Then a priority sound is scheduled and then immediately stops and rewinds all normal sounds.
            //3. Then the regular sound continues and plays.
            //4. Likelihood: Unlikely due to the amount of delay that would have to occur within the same "line" of code.
            //"mvPrioritySoundBoard.firstOrNull { it.mvScheduled }?.mvInt ?: mvPrioritySoundBoardSize" is identical to "mvPrioritySoundBoard.filter{ it.mvScheduled }.firstOrNull()?.mvInt ?: mvPrioritySoundBoardSize"
                if (!mvMuteAllSounds) (mvPrioritySoundBoard.firstOrNull { it.mvScheduled }?.mvInt ?: mvPrioritySoundBoardSize).also{println("?!?!?!?!" + it)}.also{for (mvSound in mvSoundBuffer) {mvSound.also{it.first.pause()}.also{it.first.seekTo(0)}}}.also{for (mvSound in mvPrioritySoundBoard) { if (it < mvSound.mvInt && mvSound.mvMediaPlayer.isPlaying) mvSound.also{ mvIt -> mvIt.mvMediaPlayer.pause()}.also{ mvIt -> mvIt.mvMediaPlayer.seekTo(0)} else if (mvSound.mvScheduled && !mvSound.mvMediaPlayer.isPlaying) {mvSound.mvMediaPlayer.also{ mvIt -> mvIt.start()}.also{ mvIt -> mvIt.setOnCompletionListener { mvSound.also{ mvS -> mvS.mvScheduled = false}.also{mmPlayFirstPrioritySoundInQueue()}}}} else if (mvSound.mvInt >= mvPrioritySoundBoardSize-1) { mvPrioritySoundBoard.also{mmPlayFirstSoundInQueue()}}}}
        }
    //Play The First Sound In The Queue If It Exists And It's Not Playing, "Remove" It From The Queue onCompletion, Then Repeat The Process
        private fun mmPlayFirstSoundInQueue() {
                if (!mvMuteAllSounds) (if (mvPrioritySoundBoard.any{it.mvScheduled}) null else true).also{mvSoundBuffer.getOrNull(0)?.let{if (it.first.isPlaying) null else it}?.also{it.first.start()}?.also{mmCallForward(it.second)}?.also{it.first.setOnCompletionListener{mvSoundBuffer.removeFirstOrNull()?.also{mvIt -> mvIt.first.release()}?.also{mvIt -> mmCallback(mvIt.third)}}}} //"?." Safety Check: In Any Situation Where We Return null, It Theoretically Just Shouldn't Play The Sound
        }
    //Pause All Pending Sounds, Release Allotted Resources, And "Clear" It From The Queue
    //Note: Currently Doesn't Stop Any Pending Priority Sounds
        fun mmStopAllPendingSound() {
            mvSoundBuffer.also{for (mvSound in it) {mvSound.also{mvIt -> mvIt.first.pause()}.also{mvIt -> mvIt.first.release()}}}.clear()
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
        private var mvSmsDelivered              = mvContext.getString(R.string.mtSmsDelivered)
        private var mvSmsForwardedToContact     = mvContext.getString(R.string.mtSmsForwardedToContact)
        private var mvSmsGotExceptionToContact  = mvContext.getString(R.string.mtSmsGotExceptionToContact)
        private var mvSmsNotDelivered           = mvContext.getString(R.string.mtSmsNotDelivered)
        private var mvSmsNotForwardedToContact  = mvContext.getString(R.string.mtSmsNotForwardedToContact)

    //Special Callforwards
        private fun mmCallForward(mvCallFwdCode : Int)
        {
            if (mvCallFwdCode != ClassEnum.CALLFWDNONE.mvInt)
            {
                //Default Values
                    mvPseudoToastMessage = ""
                    mvAppend             = true  //<-- Do We Append To An Existing Pseudo-Toast Block, Or Overwrite?
                    mvDismiss            = false //<-- Does The Pseudo-Toast Automatically Disappear?
                    mvWhichBlock         =  2    //<-- Bottommost Pseudo-Toast Block Takes Precedence
                    mvWhichContact       = -1    //<-- This Variable Is So We Can Forward Which Contact (Was Contacted) To The Broadcast Receiver

                //Fetch The Appropriate Pseudo-Toast Message
                    when(mvCallFwdCode) {
                        ClassEnum.CALLFWDAIRPLANEMODE.mvInt      -> mvPseudoToastMessage = mvAirplaneMode.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDGPSEXPIRED.mvInt        -> mvPseudoToastMessage = mvGpsExpired.also{mvAppend = false}.also{mvWhichBlock = 0}
                        ClassEnum.CALLFWDLOWSIGNAL.mvInt         -> mvPseudoToastMessage = mvLowSignal.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDNOCONTACTS.mvInt        -> mvPseudoToastMessage = mvNoContactsInApp.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDSIRENMODE.mvInt         -> mvPseudoToastMessage = mvSirenMode.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDSMSDELIVERED.mvInt      -> mvPseudoToastMessage = mvSmsDelivered.also{mvDismiss = true}
                        ClassEnum.CALLFWDSMSDELIVERYFAILED.mvInt -> mvPseudoToastMessage = mvSmsNotDelivered.also{mvDismiss = true}
                        else                                     -> for (mvI in 0 until mvNumberOfContacts)
                        {
                            if (mvCallFwdCode == ClassEnum.CALLFWDSMSSENTTOCONTACT.mvInt + mvI)
                                mvPseudoToastMessage = mvSmsForwardedToContact.also{mvWhichContact = mvI}
                            if (mvCallFwdCode == ClassEnum.CALLFWDSMSFAILEDTOCONTACT.mvInt + mvI)
                                mvPseudoToastMessage = mvSmsNotForwardedToContact.also{mvWhichContact = mvI}
                            if (mvCallFwdCode == ClassEnum.CALLFWDSMSGOTEXCEPTIONTOCONTACT.mvInt + mvI)
                                mvPseudoToastMessage = mvSmsGotExceptionToContact.also{mvAppend = false}.also{mvDismiss = true}.also{mvWhichContact = mvI}
                        }
                    }

                //Signal The Pseudo-Toaster
                    if (mvPseudoToastMessage != "") mvContext.sendBroadcast(Intent("mbPseudoToast").putExtra("mvToastMessage", mvPseudoToastMessage).putExtra("mvAppend", mvAppend).putExtra("mvDismiss", mvDismiss).putExtra("mvWhichBlock", mvWhichBlock).putExtra("mvWhichContact", mvWhichContact))
            }
        }
        //Special Callbacks
            private fun mmCallback(mvCallbackCode : Int)
            {
                //Our Only Current Callback Is If We "Close App" From "Secondary Caregivee Button" (I.E. Notification Swipe)
                    when (mvCallbackCode)
                    {
                        ClassEnum.CALLBACKCLOSEAPP.mvInt -> {mmStopAllPendingSound()
                            mvMuteAllSounds = true
                            Handler(Looper.getMainLooper()).postDelayed({ exitProcess(-1) },3500)} //<-- Delay Exiting Until After The Toast (Which Is Handled In BeginForegroundServiceRunnable) Disappears: https://stackoverflow.com/a/7607614/16118981
                        else                             ->  mmPlayFirstSoundInQueue() //<-- Under Normal Circumstances, We Want To Just Move Onto The Next Sound
                    }
            }
}
     */
/*
res/values/arrays.xml

<resources>
    <!-- Reply Preference -->
    <string-array name="reply_entries">
        <item>Reply</item>
        <item>Reply to all</item>
    </string-array>

    <string-array name="reply_values">
        <item>reply</item>
        <item>reply_all</item>
    </string-array>
</resources>
 */

/*
res/values/colors.xml

<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="green">#FF00FF00</color>
    <color name="yellow">#FFFFFF00</color>
    <color name="red">#FFFF0000</color>
</resources>
 */

/*
res/xml/root_preferences.xml

<PreferenceScreen xmlns:app="http://schemas.android.com/apk/res-auto">

    <PreferenceCategory app:title="@string/messages_header">

        <EditTextPreference
            app:key="signature"
            app:title="@string/signature_title"
            app:useSimpleSummaryProvider="true" />

        <ListPreference
            app:defaultValue="reply"
            app:entries="@array/reply_entries"
            app:entryValues="@array/reply_values"
            app:key="reply"
            app:title="@string/reply_title"
            app:useSimpleSummaryProvider="true" />

    </PreferenceCategory>

    <PreferenceCategory app:title="@string/sync_header">

        <SwitchPreferenceCompat
            app:key="sync"
            app:title="@string/sync_title" />

        <SwitchPreferenceCompat
            app:dependency="sync"
            app:key="attachment"
            app:summaryOff="@string/attachment_summary_off"
            app:summaryOn="@string/attachment_summary_on"
            app:title="@string/attachment_title" />

    </PreferenceCategory>
    <PreferenceCategory app:title="@string/messages_header">

        <EditTextPreference
            app:key="signature"
            app:title="@string/signature_title"
            app:useSimpleSummaryProvider="true" />

        <ListPreference
            app:defaultValue="reply"
            app:entries="@array/reply_entries"
            app:entryValues="@array/reply_values"
            app:key="reply"
            app:title="@string/reply_title"
            app:useSimpleSummaryProvider="true" />

    </PreferenceCategory>
    <PreferenceCategory app:title="@string/sync_header">

        <SwitchPreferenceCompat
            app:key="sync"
            app:title="@string/sync_title" />

        <SwitchPreferenceCompat
            app:dependency="sync"
            app:key="attachment"
            app:summaryOff="@string/attachment_summary_off"
            app:summaryOn="@string/attachment_summary_on"
            app:title="@string/attachment_title" />

    </PreferenceCategory>

</PreferenceScreen>
 */

/*
res/xml/backup_rules.xml

NOTE!!!
NOTE!!!
NOTE!!!
Note: Just replace the following in manifest if we remove this file: android:fullBackupContent="@xml/backup_rules" with android:fullBackupContent="true" (https://stackoverflow.com/a/50520882/16118981)

   <?xml version="1.0" encoding="utf-8"?><!--
   Sample backup rules file; uncomment and customize as necessary.
   See https://developer.android.com/guide/topics/data/autobackup
   for details.
   Note: This file is ignored for devices older that API 31
   See https://developer.android.com/about/versions/12/backup-restore
    -->
    <full-backup-content>
        <!--
       <include domain="sharedpref" path="."/>
       <exclude domain="sharedpref" path="device.xml"/>
    -->
    </full-backup-content>
 */

/*
res/xml/data_extraction_rules.xml

    <?xml version="1.0" encoding="utf-8"?><!--
       Sample data extraction rules file; uncomment and customize as necessary.
       See https://developer.android.com/about/versions/12/backup-restore#xml-changes
       for details.
    -->
    <data-extraction-rules>
        <cloud-backup>
            <!-- TODO: Use <include> and <exclude> to control what is backed up.
            <include .../>
            <exclude .../>
            -->
        </cloud-backup>
        <!--
        <device-transfer>
            <include .../>
            <exclude .../>
        </device-transfer>
        -->
    </data-extraction-rules>
 */
/*
res/values/strings.xml

    <!-- Miscellaneous -->
<!-- Preference Titles -->
<string name="messages_header">Messages</string>
<string name="sync_header">Sync</string>

<!-- Messages Preferences -->
<string name="signature_title">Your signature</string>
<string name="reply_title">Default reply action</string>

<!-- Sync Preferences -->
<string name="sync_title">Sync email periodically</string>
<string name="attachment_title">Download incoming attachments</string>
<string name="attachment_summary_on">Automatically download attachments for incoming emails
</string>
<string name="attachment_summary_off">Only download attachments when manually requested</string>
 */

    /* SIGNAL STRENGTH (before slight changes were made to legacy comments)
    @file:Suppress("DEPRECATION") //<-- Because Otherwise Some Deprecation Errors Appear, Despite The Fact That We Have "Responsive Design" Based On API Below
package com.example.caregivee

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.telephony.CellInfo
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager

//Review Code For This Page [√√√]

@SuppressLint("MissingPermission") //<-- We Handle Permissions Checking Elsewhere
class ClassSignalStrength (private val mvContext : Context, private val mvClassSound : ClassSound) {
    private val mvTelephonyManager = mvContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    private var mvTechnology = ""
    private var mvBigNumberAsReferenceValue = 1000000
    private var mvSignalStrengthDbm = 0
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
    fun mmGetSignalStrength(mvSendSms : Boolean, mvVerbal : Boolean)
    {
        //Get Signal Strength
        //(Source 1: https://stackoverflow.com/a/77285142/16118981)
        //(Source 2: https://stackoverflow.com/questions/61075598/what-is-proper-usage-of-requestcellinfoupdate/63370975#63370975)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                //Get Signal Strength Data (Snowcone+)
                //Note: The Following Requests An Up-To-Date Rundown Of The Cell Info
                //Previously Used The Following Line Of Code, But The Documentation Displayed When Hovering Over .allCellInfo Indicated The Ostensible Superiority Of Using requestCellInfoUpdate() Instead: "mvTelephonyManager.allCellInfo?.also{mmParseSignalStrength(it, false)} //<-- To Make Sure We Don't Send Two Subsequent Sound Alerts, Let's Only Try To Send Sound Alerts When We requestCellInfoUpdate(), So Please Set mvSendSms To false."
                    mvTelephonyManager.requestCellInfoUpdate(mvContext.mainExecutor, object : TelephonyManager.CellInfoCallback() {
                        override fun onCellInfo(mvCellInfoList: MutableList<CellInfo>) {
                            mmParseSignalStrength(mvCellInfoList, mvSendSms, mvVerbal)
                        }
                    })
            }
            else {
                //Alert User To The Status
                //(Explanation: Pre-Snowcone, The Listener Declared In The init Block Should Update This Info Automatically)
                    mmShowUser(mvSendSms, mvVerbal)
            }
    }
    private fun mmParseSignalStrength(mvCellInfoList : MutableList<CellInfo>, mvSendSms : Boolean, mvVerbal : Boolean)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            //Find The Minimum Signal Strength Value Of All Returned Instances
                mvSignalStrengthDbm = mvBigNumberAsReferenceValue //<-- When Notifications are off AND the Phone's Screen is off, the signal strength is NOT acquired and it shows the default 100000 strength for Snowcone+. It is unclear whether this permutation affects pre-Snowcone, as that's based on a listener (although there are hints that pre-Snowcone, it worked fine: https://stackoverflow.com/q/76807249/16118981).
                for (mvCellInfo in mvCellInfoList) {
                    if (mvCellInfo.cellSignalStrength.dbm < mvSignalStrengthDbm) {
                        mvSignalStrengthDbm = mvCellInfo.cellSignalStrength.dbm
                        mvTechnology = mvCellInfo.javaClass.kotlin.toString()
                    }
                }
            //Alert User As To The Status
                mmShowUser(mvSendSms, mvVerbal)
        }
    }
    private fun mmShowUser(mvSendSms : Boolean, mvVerbal : Boolean)
    {
        //Tell User Explicitly If We Detect A Weak Signal
            val mvDebugAddend = 0 //<-- For Testing "Low Signal" PseudoToasts If The Emulator Otherwise Shows Sufficient Signal Strength
            if (mvSignalStrengthDbm < -100+mvDebugAddend) mvClassSound.mmScheduleSound(R.raw.ma_low_signal, ClassEnum.CALLFWDLOWSIGNAL.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvPlaySound = mvSendSms && mvVerbal, mvImmediateCallFwd = true).also{mvClassSound.mmScheduleSound(R.raw.ma_please_move_the_phone_to_a_different_location, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvPlaySound = mvSendSms && mvVerbal, mvImmediateCallFwd = false)}
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

     */

    /*

    package com.example.caregivee

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import kotlin.system.exitProcess

//Review Code For This Page [√√√√]

//Note: Only Instantiate Once Per Activity And Call "mmScheduleSound()" Whenever We Want To Play An Sound File
class ClassSound (val mvContext : Context) {
    //Create A Special MutableList For The Most Important Sounds (These Should Take Priority In Descending Order, Superseding Any That Are Playing After Them)
        private var mvPrioritySoundBoard: MutableList<ClassMediaPlayer> = arrayListOf(ClassMediaPlayer(0, false, MediaPlayer.create(mvContext, R.raw.ma_volume_low)),
                                                                                      ClassMediaPlayer(1, false, MediaPlayer.create(mvContext, R.raw.ma_please_turn_screen_back_on)),
                                                                                      ClassMediaPlayer(2, false, MediaPlayer.create(mvContext, R.raw.ma_please_return_to_app)),
                                                                                      ClassMediaPlayer(3, false, MediaPlayer.create(mvContext, R.raw.ma_please_plug_in)))
        private var mvPrioritymostSound = mvPrioritySoundBoard.size
    //Create A MutableList (To Act As A Queue) Slated To Comprise The Sounds We Want To Put In The Queue
        private var mvSoundBuffer: MutableList<Triple<MediaPlayer, Int, Int>> = arrayListOf()
        private var mvMuteAllSounds = false

    //Schedule The Desired Priority Sound To Play, Then Pause And Rewind All Current Non-Priority (And Priority Sounds Of Lower Priority) If Need Be...
    //... Play Priority Sounds In Order Of Priority, Ignoring When One Is Already Playing, And Going Back To Playing Standard Sounds If There Are No More Scheduled Priority Sounds
        fun mmSchedulePrioritySound(mvIndex : Int?) { //<-- "Type?" Safety Review: This Allows Us To Reuse The Same Method For Automatically Calling The Next Sound In The Queue onCompletion
            //Is The App Closing (mvMuteAllSounds == true)? If So, Ignore This Section...
                if (!mvMuteAllSounds) {
                    //Let's Schedule A Priority Sound
                        mvIndex?.also{mvPrioritySoundBoard[it].mvScheduled = true} //<-- "?." Safety Review: This Allows Us To Skip Scheduling A New Sound If We're Just Playing The Next One In Rotation After onCompletion
                    //Pause And Rewind Any Currently Playing NON-Priority Sounds
                        for (mvSound in mvSoundBuffer) {
                            if (mvSound.first.isPlaying) {
                                mvSound.first.pause()
                                mvSound.first.seekTo(0)
                            }
                        }
                    //Find The First Priority Sound In The Queue That Is Scheduled
                    //Update This Value Each Time This Method Is Called To Better Ensure Synchronicity
                        mvPrioritymostSound = mvPrioritySoundBoard.firstOrNull { /* ... that matches: */ it.mvScheduled }?.mvInt ?: mvPrioritySoundBoard.size //?. and ?: Safety Review: Simply Return The Size Of The Priority Queue If None Are Scheduled
                    //Now Let's Focus On The Priority Sound Queue
                        for (mvSound in mvPrioritySoundBoard) {
                            //Have We Reached Our Scheduled Sound With The Most Priority?
                                if (mvSound.mvScheduled && mvPrioritymostSound == mvSound.mvInt && !mvSound.mvMediaPlayer.isPlaying) {
                                    mvSound.mvMediaPlayer.start()
                                    mvSound.mvMediaPlayer.setOnCompletionListener {
                                        mvSound.mvScheduled = false
                                        mmSchedulePrioritySound(null) //<-- Move To The Next Priority Sound
                                    }
                                }
                            //Pause And Rewind Any Currently Playing Priority Sounds If They Aren't Of Sufficient Priority Compared To Our Scheduled Sound With The Most Priority
                                else if (mvSound.mvInt > mvPrioritymostSound && mvSound.mvMediaPlayer.isPlaying) {
                                    mvSound.mvMediaPlayer.pause()
                                    mvSound.mvMediaPlayer.seekTo(0)
                                }
                            //If There Are No Remaining Priority Sounds, Let's Start Playing Any NON-Priority Sounds Again
                                else if (mvSound.mvInt >= mvPrioritySoundBoard.size - 1) {
                                    //?!?!?!?! PLEASE REMOVE AT EARLIEST
                                        println(mvSoundBuffer.toString())

                                    mmScheduleSound(0, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvPlaySound = false, mvImmediateCallFwd = false)
                                }
                        }
                }
        }
    //Add Our New Sound To Queue, Then Play The First Sound In The Queue If It Exists And It's Not Playing, "Remove" It From The Queue onCompletion, Then Repeat The Process (Without Adding A New Sound On Subsequent Recursive Calls)
        fun mmScheduleSound(mvSound : Int, mvCallFwdCode : Int, mvCallBackCode : Int, mvPlaySound : Boolean, mvImmediateCallFwd : Boolean) {
            //Schedule A New Sound At The End Of The Queue
                if (mvPlaySound) {
                    mvSoundBuffer.add(Triple(MediaPlayer.create(mvContext, mvSound).also{it.setOnCompletionListener{mvSoundBuffer.removeFirstOrNull()?.also{mvIt -> mvIt.first.release(); mmCallback(mvIt.third)}}}, //"?." Safety Check: In Any Situation Where We Return null, It Theoretically Shouldn't Continue To mmCallback() And Should (Therefore) Avoid A Subsequent Recursive Call
                                      if (mvImmediateCallFwd) ClassEnum.CALLFWDNONE.mvInt else mvCallFwdCode,
                                      mvCallBackCode))
                    if (mvImmediateCallFwd) mmCallForward(mvCallFwdCode)
                }
                else
                    mmCallForward(mvCallFwdCode) //<-- Sometimes, We Just Want To Skip The Sound Altogether And Go Right To The CallForward Function (Be It "Immediate CallForward" Or Otherwise), Like When We Do A Non-SMS Iteration In BeginForegroundServiceRunnable (I.E. mvSendSms = false).
            //Is The App Closing (mvMuteAllSounds == true)? If So, Ignore This Section...
                if (!mvMuteAllSounds) {
                    //Get The Next Sound In The Rotation
                        val mvFirstSound = mvSoundBuffer.getOrNull(0)

                        //?!?!?!?! PLEASE REMOVE AT EARLIEST
                            println("---mvFirstSound " + mvFirstSound)
                            println("---isntPlaying " + if (mvFirstSound != null) !mvFirstSound.first.isPlaying else "null")
                            println("---isntScheduled " + !mvPrioritySoundBoard.any{it.mvScheduled})

                        if (mvFirstSound != null) {
                            if (!mvFirstSound.first.isPlaying && !mvPrioritySoundBoard.any{it.mvScheduled}) { //<-- Are Any Priority Sounds Scheduled? If So, Ignore This Section...


                                //?!?!?!?! PLEASE REMOVE AT EARLIEST
                                //It does indeed get to this point on a non-mvFirstSound-playing iteration.
                                //So why doesn't it start.
                                //Maybe try detecting whether or not it's playing immediately thereafter, and if it's not playing simply replace and try again?
                                    println("---mvFirstSound.first.start()")

                                mvFirstSound.first.start()

                                //?!?!?!?! PLEASE REMOVE AT EARLIEST
                                //It does indeed get to this point on a non-mvFirstSound-playing iteration.
                                //So why doesn't it start.
                                //Maybe try detecting whether or not it's playing immediately thereafter, and if it's not playing simply replace and try again?
                                //... It does indeed seem to indicate the sound isPlaying! Yet it doesn't play... nor does it end up in a callback situation.
                                //1) Maybe try checking after mmCallForward (remembering it's async), if this isn't the solution.
                                //2) Maybe try commenting out mmCallForward.
                                //3) Maybe check isPlaying after a runnable.
                                //4) Maybe try just buffering by playing a runnable.
                                //5) Maybe add a println everywhere where audio stuff is .release()'d, to see if it's accidentally releasing it somewhere.
                                //6) One of the best solutions might be to just remake the MediaPlayer each time and only store the R.raw.___'s in the queue.
                                //// Then we can simply release and remove instead of rewinding.
                                //// But please backup the current ClassSound before doing so (if you decide to do so).
                                    println("---mvFirstSound.first.isPlaying " + mvFirstSound.first.isPlaying)

                                mmCallForward(mvFirstSound.second) //<--!?!?!?!?! THIS MIGHT BE BEST ONLY RUNNING IF mvPlaySound == true
                            }
                        }
                }
        }
    //Pause All Pending Sounds, Release Allotted Resources, And "Clear" It From The Queue
        fun mmStopAllPendingSound(mvBoth : Boolean) {
            mvSoundBuffer.also{for (mvSound in it) {mvSound.also{mvIt -> mvIt.first.pause()}.also{mvIt -> mvIt.first.release()}}}.clear()
            if (mvBoth) mvPrioritySoundBoard.also{for (mvSound in it) {mvSound.also{mvIt -> mvIt.mvMediaPlayer.pause()}.also{mvIt -> mvIt.mvMediaPlayer.release()}}}.clear()
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
        private var mvSmsDelivered              = mvContext.getString(R.string.mtSmsDelivered)
        private var mvSmsForwardedToContact     = mvContext.getString(R.string.mtSmsForwardedToContact)
        private var mvSmsGotExceptionToContact  = mvContext.getString(R.string.mtSmsGotExceptionToContact)
        private var mvSmsNotDelivered           = mvContext.getString(R.string.mtSmsNotDelivered)
        private var mvSmsNotForwardedToContact  = mvContext.getString(R.string.mtSmsNotForwardedToContact)

    //Special Callforwards
        private fun mmCallForward(mvCallFwdCode : Int)
        {
            if (mvCallFwdCode != ClassEnum.CALLFWDNONE.mvInt)
            {
                //Default Values
                    mvPseudoToastMessage = ""
                    mvAppend             = true  //<-- Do We Append To An Existing Pseudo-Toast Block, Or Overwrite?
                    mvDismiss            = false //<-- Does The Pseudo-Toast Automatically Disappear?
                    mvWhichBlock         =  2    //<-- Bottommost Pseudo-Toast Block Takes Precedence
                    mvWhichContact       = -1    //<-- This Variable Is So We Can Forward Which Contact (Was Contacted) To The Broadcast Receiver

                //Fetch The Appropriate Pseudo-Toast Message
                    when(mvCallFwdCode) {
                        ClassEnum.CALLFWDAIRPLANEMODE.mvInt      -> mvPseudoToastMessage = mvAirplaneMode.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDGPSEXPIRED.mvInt        -> mvPseudoToastMessage = mvGpsExpired.also{mvAppend = false}.also{mvWhichBlock = 0}
                        ClassEnum.CALLFWDLOWSIGNAL.mvInt         -> mvPseudoToastMessage = mvLowSignal.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDNOCONTACTS.mvInt        -> mvPseudoToastMessage = mvNoContactsInApp.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDSIRENMODE.mvInt         -> mvPseudoToastMessage = mvSirenMode.also{mvAppend = false}.also{mvWhichBlock = 1}
                        ClassEnum.CALLFWDSMSDELIVERED.mvInt      -> mvPseudoToastMessage = mvSmsDelivered.also{mvDismiss = true}
                        ClassEnum.CALLFWDSMSDELIVERYFAILED.mvInt -> mvPseudoToastMessage = mvSmsNotDelivered.also{mvDismiss = true}
                        else                                     -> for (mvI in 0 until mvNumberOfContacts) {
                                                                        if (mvCallFwdCode == ClassEnum.CALLFWDSMSSENTTOCONTACT.mvInt + mvI)
                                                                            mvPseudoToastMessage = mvSmsForwardedToContact.also{mvWhichContact = mvI}
                                                                        if (mvCallFwdCode == ClassEnum.CALLFWDSMSFAILEDTOCONTACT.mvInt + mvI)
                                                                            mvPseudoToastMessage = mvSmsNotForwardedToContact.also{mvWhichContact = mvI}
                                                                        if (mvCallFwdCode == ClassEnum.CALLFWDSMSGOTEXCEPTIONTOCONTACT.mvInt + mvI)
                                                                            mvPseudoToastMessage = mvSmsGotExceptionToContact.also{mvAppend = false}.also{mvDismiss = true}.also{mvWhichContact = mvI}
                        }
                    }

                //Signal The Pseudo-Toaster
                    if (mvPseudoToastMessage != "") mvContext.sendBroadcast(Intent("mbPseudoToast").putExtra("mvToastMessage", mvPseudoToastMessage).putExtra("mvAppend", mvAppend).putExtra("mvDismiss", mvDismiss).putExtra("mvWhichBlock", mvWhichBlock).putExtra("mvWhichContact", mvWhichContact))
            }
        }
        //Special Callbacks
            private fun mmCallback(mvCallbackCode : Int)
            {
                //Our Only Current Callback Is If We "Close App" From "Secondary Caregivee Button" (I.E. Notification Swipe) During A "True" Foreground Process
                    when (mvCallbackCode)
                    {
                        ClassEnum.CALLBACKCLOSEAPP.mvInt -> {mmStopAllPendingSound(true)
                                                             mvMuteAllSounds = true
                                                             Handler(Looper.getMainLooper()).postDelayed({ exitProcess(-1) },3500)} //<-- Delay Exiting Until After The Toast (Which Is Handled In BeginForegroundServiceRunnable) Disappears: https://stackoverflow.com/a/7607614/16118981
                        else                             ->  mmScheduleSound(0, ClassEnum.CALLFWDNONE.mvInt, ClassEnum.CALLBACKNONE.mvInt, mvPlaySound = false, mvImmediateCallFwd = false) //<-- Under Normal Circumstances, We Want To Just Move Onto The Next Sound In The Queue
                    }
            }
}

     */

    /*
    //?!?!?!?! JUST FOR STACKOVERFLOW PURPOSES? :D
//?!?!?!?! JUST FOR STACKOVERFLOW PURPOSES? :D
//?!?!?!?! JUST FOR STACKOVERFLOW PURPOSES? :D

package com.example.caregivee //?!?!?!?!

import android.content.Context
import android.media.MediaPlayer

//Note: Only Instantiate Once Per Activity And Call "mmScheduleSound()" Or "mmSchedulePrioritySound()" Whenever We Want To Play A Sound File
    class ClassSounds (val mvContext : Context) {
        //Create A Special MutableList For The Highest Priority Sounds (These Should Take Priority In Descending Order Of Index, I.E. Superseding Any Subsequent Sound In The List)
            private var mvPrioritySoundBoard: MutableList<ClassMediasPlayer> = arrayListOf(ClassMediasPlayer(ClassEnum.PRIORITYVOLUMELOW.mvInt, false, R.raw.ma_volume_low, mvContext),
                                                                                           ClassMediasPlayer(ClassEnum.PRIORITYSCREENOFF.mvInt, false, R.raw.ma_please_turn_screen_back_on, mvContext),
                                                                                           ClassMediasPlayer(ClassEnum.PRIORITYWINDOWDEFOCUSED.mvInt, false, R.raw.ma_please_return_to_app, mvContext),
                                                                                           ClassMediasPlayer(ClassEnum.PRIORITYUNPLUGGED.mvInt, false, R.raw.ma_please_plug_in, mvContext))
            private var mvPrioritymostSound = mvPrioritySoundBoard.size //<-- Set The Default Value To One Higher Than The Highest Index
        //Create A MutableList (To Act As A Queue) That Should Comprise The "References" To Non-Priority Sounds
            private var mvSoundBuffer : MutableList<ClassSoundsBuffer> = arrayListOf()
            private var mvSoundCurrent : ClassMediasPlayer = ClassMediasPlayer(0, false, R.raw.ma_null, mvContext) //<-- Holds The Actual Currently Playing Sound, Not Just A Reference (Note: In This Case, "R.raw.ma_null" Just References An Audio File With Silence)
        //Schedule The Desired Priority Sound To Play, Then Pause All Current Non-Priority (And Priority Sounds Of Lower Priority) If Need Be...
        //... Play Priority Sounds In Order Of Priority, Not Playing One Of Equal Or Lower Priority When One Is Already Playing, And Going Back To Playing Standard Sounds If There Are No More Scheduled Priority Sounds
            fun mmSchedulePrioritySound(mvIndex : Int?) { //<-- "Type?" Safety Review: This Allows Us To Reuse The Same Method For Automatically Calling The Next Sound — That Is In The Queue — Upon onCompletion() (By Inserting A null Value)
                //Let's Schedule A Priority Sound
                    mvIndex?.also{mvPrioritySoundBoard[it].mvScheduled = true} //<-- "?." Safety Review: This Allows Us To Skip Scheduling A New Sound If We're Just Playing The Next One In Rotation After onCompletion(), Which Is A Situation Where mvIndex Is Assigned A null Value
                //Pause Any Currently Playing NON-Priority Sounds
                    if (mvSoundCurrent.mmIsPlaying()) {
                        mvSoundCurrent.mmPause()
                        mvSoundCurrent.mmReset() //This resets the MediaPlayer to its initialized state. This seems to fix the glitch where audio (specifically non-priority audio) eventually stops playing when the Android Studio emulator is minimized in API 32 and Tiramisu. Note: The MediaPlayer needs to be re-.create()'d after each .reset().
                    }
                //Find The First Priority Sound In The Queue That Is Scheduled
                //Update This Value Each Time This Method Is Called To Better Ensure Synchronicity
                    mvPrioritymostSound = mvPrioritySoundBoard.firstOrNull { /* ... grab the first where the following is true: */ it.mvScheduled }?.mvInt ?: mvPrioritySoundBoard.size //?. and ?: Safety Review: Simply Return The Size Of The Priority Queue If No Sounds Are Scheduled, I.E. One More Than The Highest Index

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
                                mmScheduleSound(0, mvAddSoundToQueue = false)
                            }
                    }
            }
        //Add Our New Sound To Queue, Then Play The First Sound In The Queue If It Exists And It's Not Playing, "Remove" It From The Queue onCompletion, Then Repeat The Process To Move Onto The Next One In The Queue (Without Adding A New Sound On Subsequent Recursive Calls)
            fun mmScheduleSound(mvSound : Int, mvAddSoundToQueue : Boolean) {
                //Schedule A New Sound At The End Of The Queue
                    if (mvAddSoundToQueue)
                        mvSoundBuffer.add(ClassSoundsBuffer(mvSound))
                //Is The App Closing (mvClosingApp == true)? If So, Ignore This Section...
                    //Get The Next Sound In The Rotation
                        val mvFirstSound = mvSoundBuffer.getOrNull(0)
                    //If There Indeed Was A Next Sound In Rotation...
                        if (mvFirstSound != null) {
                            //Are Any Non-Priority Sounds Currently Playing -OR- Are Any Priority Sounds Scheduled? If So, Ignore This Section...
                                if (!mvSoundCurrent.mmIsPlaying() && !mvPrioritySoundBoard.any{it.mvScheduled}) {
                                        mvSoundCurrent.mmReplaceSound(mvFirstSound.mvSoundRef)
                                        mvSoundCurrent.mmRewind()
                                        mvSoundCurrent.mmStart()
                                        mvSoundCurrent.mvMediaPlayer.setOnCompletionListener{mvSoundBuffer.removeFirstOrNull()?.also{mvSoundCurrent.mmRelease()}} //"?." Safety Check: In Any Situation Where We Return null, It Theoretically Shouldn't Continue To mmCallback() And Should (Therefore) Avoid A Subsequent Recursive Call (Since The Queue Would Theoretically Be Empty)
                                }
                        }
            }
        //Pause All Pending Sounds, Release Allotted Resources, And "Clear" From The Queue
            fun mmStopAllPendingSound(mvBoth : Boolean) {
                mvSoundCurrent.also{it.mmPause(); it.mmRelease(); mvSoundBuffer.clear()}
                if (mvBoth) mvPrioritySoundBoard.also{for (mvSound in it) {mvSound.also{mvIt -> mvIt.mmPause() ; mvIt.mmRelease()}}}.clear()
            }

        //To Instantiate An Object That Holds The Reference To Each Sound. You Can Add Any Other Custom Fields Or Methods If You So Desire.
            inner class ClassSoundsBuffer (val mvSoundRef : Int)

        //Create A Custom Class To Hold The MediaPlayer Object And Some Handy Methods
            inner class ClassMediasPlayer(val mvInt: Int, var mvScheduled : Boolean, mvSoundReference : Int, val mvContext : Context) {

                //Manual Sound "State" Variables
                private var mvReleased = false

                //MediaPlayer Object
                var mvMediaPlayer: MediaPlayer = MediaPlayer.create(mvContext, mvSoundReference)

                //Methods
                fun mmIsPlaying() : Boolean {
                    return !mvReleased && mvMediaPlayer.isPlaying
                }
                fun mmPause() {
                    if (!mvReleased) mvMediaPlayer.pause()
                }
                fun mmRelease() {
                    mvReleased = true
                    mvMediaPlayer.release() //This Releases Resources For Memory Optimization
                }
                fun mmReplaceSound(mvSoundReference : Int) {
                    mvReleased = false
                    mvMediaPlayer = MediaPlayer.create(mvContext, mvSoundReference)
                }
                fun mmReset() {
                    if (!mvReleased) mvMediaPlayer.reset() //This resets the MediaPlayer to its initialized state. This seems to fix the glitch where audio (specifically non-priority audio) eventually stops playing when Android Studio itself is minimized in API 32 and Tiramisu. Note: The MediaPlayer needs to be re-.create()'d after each .reset().
                }
                fun mmRewind() {
                    if (!mvReleased) mvMediaPlayer.seekTo(0)
                }
                fun mmStart() {
                    if (!mvReleased) mvMediaPlayer.start()
                }
            }
}
     */
    /*
     //?!?!?!?! JUST FOR STACKOVERFLOW PURPOSES? :D
     //?!?!?!?! JUST FOR STACKOVERFLOW PURPOSES? :D
     //?!?!?!?! JUST FOR STACKOVERFLOW PURPOSES? :D

//HERE IS A MORE COMPLICATED WORKFLOW. THIS ALSO ALLOWS FOR A SOUNDBOARD OF PRIORITY SOUNDS:
//HERE IS A MORE COMPLICATED WORKFLOW. THIS ALSO ALLOWS FOR A SOUNDBOARD OF PRIORITY SOUNDS:
//HERE IS A MORE COMPLICATED WORKFLOW. THIS ALSO ALLOWS FOR A SOUNDBOARD OF PRIORITY SOUNDS:

    package com.example.caregivee //?!?!?!?!

    import android.content.Context
    import android.media.MediaPlayer

    //Note: Only Instantiate Once Per Activity And Call "mmScheduleSound()" Or "mmSchedulePrioritySound()" Whenever We Want To Play A Sound File
    class ClassSounds (val mvContext : Context) {
        //Create A Special MutableList For The Highest Priority Sounds (These Should Take Priority In Descending Order Of Index, I.E. Superseding Any Subsequent Sound In The List)
        private var mvPrioritySoundBoard: MutableList<ClassMediasPlayer> = arrayListOf(ClassMediasPlayer(ClassEnum.PRIORITYVOLUMELOW.mvInt, false, R.raw.ma_volume_low, mvContext),
            ClassMediasPlayer(ClassEnum.PRIORITYSCREENOFF.mvInt, false, R.raw.ma_please_turn_screen_back_on, mvContext),
            ClassMediasPlayer(ClassEnum.PRIORITYWINDOWDEFOCUSED.mvInt, false, R.raw.ma_please_return_to_app, mvContext),
            ClassMediasPlayer(ClassEnum.PRIORITYUNPLUGGED.mvInt, false, R.raw.ma_please_plug_in, mvContext))
        private var mvPrioritymostSound = mvPrioritySoundBoard.size //<-- Set The Default Value To One Higher Than The Highest Index
        //Create A MutableList (To Act As A Queue) That Should Comprise The "References" To Non-Priority Sounds
        private var mvSoundBuffer : MutableList<ClassSoundsBuffer> = arrayListOf()
        private var mvSoundCurrent : ClassMediasPlayer = ClassMediasPlayer(0, false, R.raw.ma_null, mvContext) //<-- Holds The Actual Currently Playing Sound, Not Just A Reference (Note: In This Case, "R.raw.ma_null" Just References An Audio File With Silence)
        //Schedule The Desired Priority Sound To Play, Then Pause All Current Non-Priority (And Priority Sounds Of Lower Priority) If Need Be...
        //... Play Priority Sounds In Order Of Priority, Not Playing One Of Equal Or Lower Priority When One Is Already Playing, And Going Back To Playing Standard Sounds If There Are No More Scheduled Priority Sounds
        fun mmSchedulePrioritySound(mvIndex : Int?) { //<-- "Type?" Safety Review: This Allows Us To Reuse The Same Method For Automatically Calling The Next Sound — That Is In The Queue — Upon onCompletion() (By Inserting A null Value)
            //Let's Schedule A Priority Sound
            mvIndex?.also{mvPrioritySoundBoard[it].mvScheduled = true} //<-- "?." Safety Review: This Allows Us To Skip Scheduling A New Sound If We're Just Playing The Next One In Rotation After onCompletion(), Which Is A Situation Where mvIndex Is Assigned A null Value
            //Pause Any Currently Playing NON-Priority Sounds
            if (mvSoundCurrent.mmIsPlaying()) {
                mvSoundCurrent.mmPause()
                mvSoundCurrent.mmReset() //This resets the MediaPlayer to its initialized state. This seems to fix the glitch where audio (specifically non-priority audio) eventually stops playing when the Android Studio emulator is minimized in API 32 and Tiramisu. Note: The MediaPlayer needs to be re-.create()'d after each .reset().
            }
            //Find The First Priority Sound In The Queue That Is Scheduled
            //Update This Value Each Time This Method Is Called To Better Ensure Synchronicity
            mvPrioritymostSound = mvPrioritySoundBoard.firstOrNull { /* ... grab the first where the following is true: */ it.mvScheduled }?.mvInt ?: mvPrioritySoundBoard.size //?. and ?: Safety Review: Simply Return The Size Of The Priority Queue If No Sounds Are Scheduled, I.E. One More Than The Highest Index

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
                    mvSound.mmPause() //<-- Do Not mmRewind() Immediately After Pausing, Instead, mmRewind() Just Before We Start Playing The Sound Again (Since In "Android Marshmallow" For Example, It Seems To mmRewind() And Overlap With Any Interrupting Higher Priority Sounds BEFORE It Successfully mmPause()'s The Lower Priority Sound)
                }
                //If There Are No Remaining Priority Sounds, Let's Start Playing Any NON-PRIORITY Sounds Again
                else if (mvSound.mvInt >= mvPrioritySoundBoard.size - 1) {
                    mmScheduleSound(0, mvAddSoundToQueue = false)
                }
            }
        }
        //Add Our New Sound To Queue, Then Play The First Sound In The Queue If It Exists And It's Not Playing, "Remove" It From The Queue onCompletion, Then Repeat The Process To Move Onto The Next One In The Queue (Without Adding A New Sound On Subsequent Recursive Calls)
        fun mmScheduleSound(mvSound : Int, mvAddSoundToQueue : Boolean) {
            //Schedule A New Sound At The End Of The Queue
            if (mvAddSoundToQueue)
                mvSoundBuffer.add(ClassSoundsBuffer(mvSound))
            //Get The Next Sound In The Rotation
            val mvFirstSound = mvSoundBuffer.getOrNull(0)
            //If There Indeed Was A Next Sound In Rotation...
            if (mvFirstSound != null) {
                //Are Any Non-Priority Sounds Currently Playing -OR- Are Any Priority Sounds Scheduled? If So, Ignore This Section...
                if (!mvSoundCurrent.mmIsPlaying() && !mvPrioritySoundBoard.any{it.mvScheduled}) {
                    mvSoundCurrent.mmReplaceSound(mvFirstSound.mvSoundRef)
                    mvSoundCurrent.mmRewind()
                    mvSoundCurrent.mmStart()
                    mvSoundCurrent.mvMediaPlayer.setOnCompletionListener{mvSoundBuffer.removeFirstOrNull()?.also{mvSoundCurrent.mmRelease()}} //"?." Safety Check: In Any Situation Where We Return null, It Theoretically Shouldn't Continue To mmCallback() And Should (Therefore) Avoid A Subsequent Recursive Call (Since The Queue Would Theoretically Be Empty)
                }
            }
        }
        //Pause All Pending Sounds, Release Allotted Resources, And "Clear" From The Queue
        fun mmStopAllPendingSound(mvBoth : Boolean) {
            mvSoundCurrent.also{it.mmPause(); it.mmRelease(); mvSoundBuffer.clear()}
            if (mvBoth) mvPrioritySoundBoard.also{for (mvSound in it) {mvSound.also{mvIt -> mvIt.mmPause() ; mvIt.mmRelease()}}}.clear()
        }

        //To Instantiate An Object That Holds The Reference To Each Sound. You Can Add Any Other Custom Fields Or Methods If You So Desire.
        inner class ClassSoundsBuffer (val mvSoundRef : Int)

        //Create A Custom Class To Hold The MediaPlayer Object And Some Handy Methods
        inner class ClassMediasPlayer(val mvInt: Int, var mvScheduled : Boolean, mvSoundReference : Int, val mvContext : Context) {

            //Manual Sound "State" Variables
            private var mvReleased = false

            //MediaPlayer Object
            var mvMediaPlayer: MediaPlayer = MediaPlayer.create(mvContext, mvSoundReference)

            //Methods
            fun mmIsPlaying() : Boolean {
                return !mvReleased && mvMediaPlayer.isPlaying
            }
            fun mmPause() {
                if (!mvReleased) mvMediaPlayer.pause()
            }
            fun mmRelease() {
                mvReleased = true
                mvMediaPlayer.release() //This Releases Resources For Memory Optimization
            }
            fun mmReplaceSound(mvSoundReference : Int) {
                mvReleased = false
                mvMediaPlayer = MediaPlayer.create(mvContext, mvSoundReference)
            }
            fun mmReset() {
                if (!mvReleased) mvMediaPlayer.reset() //This resets the MediaPlayer to its initialized state. This seems to fix the glitch where audio (specifically non-priority audio) eventually stops playing when Android Studio itself is minimized in API 32 and Tiramisu. Note: The MediaPlayer needs to be re-.create()'d after each .reset().
            }
            fun mmRewind() {
                if (!mvReleased) mvMediaPlayer.seekTo(0)
            }
            fun mmStart() {
                if (!mvReleased) mvMediaPlayer.start()
            }
        }
    }
            */
    /*
    //?!?!?!?! JUST FOR STACKOVERFLOW PURPOSES? :D
//?!?!?!?! JUST FOR STACKOVERFLOW PURPOSES? :D
//?!?!?!?! JUST FOR STACKOVERFLOW PURPOSES? :D

//Here's A More Readable Version, Hopefully Improved:
//Here's A More Readable Version, Hopefully Improved:
//Here's A More Readable Version, Hopefully Improved:
//Note, Current Method requires creating an audio file named ma_null.wav in order to initialize the MediaPlayer.

package com.example.caregivee //?!?!?!?!
import android.content.Context
import android.media.MediaPlayer

//Note: Only Instantiate Once Per Activity And Call "mmScheduleSound()" Whenever We Want To Play A Sound File
    class ClassSounds (val mvContext : Context) {
        //Create A MutableList (To Act As A Queue) Comprising "References" To Sound Files
            private var mvSoundBuffer : MutableList<Int> = arrayListOf()
            private var mvSoundCurrent : ClassMediasPlayer = ClassMediasPlayer(R.raw.ma_null) //<-- Holds The Actual Currently Playing Sound, Not Just A Reference (Note: In This Case, "R.raw.ma_null" Just References An Audio File With Silence)

        //Create A Custom Inner Class To Hold The MediaPlayer Object And Also Some Handy User-Friendly "Methods"
            inner class ClassMediasPlayer(mvSoundReference : Int) {
                //MediaPlayer Object
                    var mvMediaPlayer: MediaPlayer = MediaPlayer.create(mvContext, mvSoundReference)

                //Has It Been .release()'d? (Note: Keeping Track Of This Variable Is Helpful To Prevent Runtime Errors)
                    private var mvReleased = false

                //Methods
                    fun mmIsPlaying() : Boolean {
                        return !mvReleased && mvMediaPlayer.isPlaying
                    }
                    fun mmPause() {
                        if (!mvReleased) mvMediaPlayer.pause()
                    }
                    fun mmRelease() {
                        mvReleased = true
                        mvMediaPlayer.release() //This Releases Resources For Memory Optimization
                    }
                    fun mmReplaceSound(mvSoundReference : Int) {
                        mvReleased = false
                        mvMediaPlayer = MediaPlayer.create(mvContext, mvSoundReference)
                    }
                    fun mmReset() {
                        if (!mvReleased) mvMediaPlayer.reset() //This resets the MediaPlayer to its uninitialized state. This seems to fix the glitch where sound might eventually stop playing when the Android Studio emulator is minimized in API 32 and Tiramisu. Note: The MediaPlayer needs to be re-.create()'d after each .reset(), Which Is Accomplished Through The Custom "Method" mmReplaceSound().
                    }
                    fun mmRewind() {
                        if (!mvReleased) mvMediaPlayer.seekTo(0)
                    }
                    fun mmStart() {
                        if (!mvReleased) mvMediaPlayer.start()
                    }
            }

        //Add Our New Sound To Queue
        //Then Play The First Sound In The Queue If It Exists And It's Not Playing
        //Then "Remove" It From The Queue onCompletion
        //Then Repeat The Process To Move Onto The Next One In The Queue (Without Adding A New Sound On Subsequent Recursive Calls)
            fun mmScheduleSound(mvSoundReferenceToAddToQueue : Int, mvAddSoundToQueue : Boolean) {
                //Schedule A New Sound At The End Of The Queue
                    if (mvAddSoundToQueue) mvSoundBuffer.add(mvSoundReferenceToAddToQueue)
                //Get The Next Sound In The Rotation By Querying The Front Of The Queue
                    val mvFirstSound = mvSoundBuffer.getOrNull(0)
                //If There Indeed Was A Next Sound In Rotation...
                    if (mvFirstSound != null) {
                        //Are Sounds Currently Playing? If So, Ignore This Section...
                            if (!mvSoundCurrent.mmIsPlaying()) {
                                    mvSoundCurrent.mmPause()
                                    mvSoundCurrent.mmReset()
                                    mvSoundCurrent.mmReplaceSound(mvFirstSound)
                                    mvSoundCurrent.mmRewind()
                                    mvSoundCurrent.mmStart()
                                    mvSoundCurrent.mvMediaPlayer.setOnCompletionListener{mvSoundBuffer.removeFirstOrNull()?.also{mvSoundCurrent.mmRelease();
                                                                                                                                 mmScheduleSound(0, mvAddSoundToQueue = false)}} //"?." Safety Check: In Any Situation Where We Return null, It Theoretically Shouldn't Continue To mmScheduleSound() And Should (Therefore) Avoid A Subsequent Recursive Call (Since The Queue Would Theoretically Be Empty)
                            }
                    }
            }

        //Pause The Current Sound, Release Allotted Resources, And "Clear" All Pending Sounds From The Queue
            fun mmStopAllPendingSound() {
                mvSoundCurrent.mmPause()
                mvSoundCurrent.mmRelease()
                mvSoundBuffer.clear()
            }
}
     */

    /*

    //STACKOVERFLOW-READY VERSION OF CLASSSOUNDS
        //STACKOVERFLOW-READY VERSION OF CLASSSOUNDS
            //STACKOVERFLOW-READY VERSION OF CLASSSOUNDS

    package com.example.caregivee

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.Toast

//Review Code For This Page [√√√√√]

class ActivityBegin : Activity() {
    //Countdown Variables
        private var mvCountdown = ClassCountdown()

    private lateinit var mvClassSounds : ClassSounds //?!?!?!?!
    //OnCreate
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_begin)
            //?!?!?!?!
                mvClassSounds = ClassSounds(applicationContext)
                mvClassSounds.mmScheduleSound(R.raw.ma_volume_low, mvAddSoundToQueue = true)
                mvClassSounds.mmScheduleSound(R.raw.ma_please_turn_screen_back_on, mvAddSoundToQueue = true)
                mvClassSounds.mmScheduleSound(R.raw.ma_please_return_to_app, mvAddSoundToQueue = true)

            //If We're Completely Restarting The App, Let's Remove The Foreground Service Just In Case
                stopService(Intent(applicationContext, BeginForegroundService::class.java))

            //Main "Countdown" Thread (When We Reach *0*, We Jump To "ActivityBehestImportant")
            //Purpose Is To Treat The First Activity Like A Splash Screen (Though Clickable)
                mvCountdown.mmCountdownClock<Int, Long>(20000 /* ?!?!?!?! 2 */, 2000, ::mmScreenClick)

            //Show Version Number
                Toast.makeText(applicationContext, getString(R.string.mtVersion), Toast.LENGTH_SHORT).show()

            //Add onClickListeners (More Futureproof Than Using android:onClick In The XML Files: https://stackoverflow.com/a/44184111/16118981)
                findViewById<Button>(R.id.mxCaregiveeButton).setOnClickListener { mmScreenClick() }
        }
    //Save The State!
        public override fun onSaveInstanceState(savedInstanceState: Bundle) {
            super.onSaveInstanceState(savedInstanceState)
        }
    //Screen Click
        private fun mmScreenClick() {
            //Switch Activities
                mmSwitchActivities(ActivityBehestImportant::class.java)
                mvClassSounds.mmStopAllPendingSounds()//?!?!?!?!
        }
    //Switch Activities
        private fun mmSwitchActivities(mvClazz : Class<*>?) {
            //Stop The Countdown
                mvCountdown.mmStop()
            //Switch Activities
                startActivity(Intent(applicationContext, mvClazz))
        }
}

package com.example.caregivee
import android.content.Context
import android.media.MediaPlayer

//Note: Only Instantiate "ClassSounds" Once Per Activity
//Call "mmScheduleSound()" Whenever We Want To Play A Sound File
//Call "mmStopAllPendingSounds()" When Switching Activities
class ClassSounds (val mvContext : Context) {
    //Create A MutableList (To Act As A Queue) Comprising "References" To Sound Files
    private var mvSoundBuffer : MutableList<Int> = arrayListOf()
    private var mvSoundCurrent : ClassMediasPlayer = ClassMediasPlayer(R.raw.ma_null) //<-- Holds The Actual Currently Playing Sound, Not Just A Reference (Note: In This Case, "R.raw.audio_null" Just References An Audio File With Silence, Our Default Initial Value)

    //Create A Custom Inner Class To Hold The MediaPlayer Object And Also Some Handy User-Friendly "Methods"
    inner class ClassMediasPlayer(mvSoundReference : Int) {
        //MediaPlayer Object
        var mvMediaPlayer: MediaPlayer = MediaPlayer.create(mvContext, mvSoundReference)

        //Has The MediaPlayer Object Been .release()'d? (Note: Keeping Track Of This Variable Is Helpful To Prevent Runtime Errors)
        private var mvReleased = false

        //Methods
        fun mmIsPlaying() : Boolean {
            return !mvReleased && mvMediaPlayer.isPlaying
        }
        fun mmPause() {
            if (!mvReleased) mvMediaPlayer.pause()
        }
        fun mmRelease() {
            mvReleased = true
            mvMediaPlayer.release() //This Releases Resources For Memory Optimization
        }
        fun mmReplaceSound(mvSoundReference : Int) {
            mvReleased = false
            mvMediaPlayer = MediaPlayer.create(mvContext, mvSoundReference)
        }
        fun mmReset() {
            if (!mvReleased) mvMediaPlayer.reset() //This resets the MediaPlayer to its uninitialized state. This seems to fix the glitch where sound might eventually stop playing when the Android Studio emulator is minimized in some versions of Android. Note: The MediaPlayer needs to be re-.create()'d after each .reset(), Which Is Accomplished Through The Custom mmReplaceSound() Method.
        }
        fun mmRewind() {
            if (!mvReleased) mvMediaPlayer.seekTo(0)
        }
        fun mmStart() {
            if (!mvReleased) mvMediaPlayer.start()
        }
    }

    //Add Our New Sound To Queue
    //Then Play The First Sound In The Queue If It Exists And It's Not Playing
    //Then "Remove" It From The Queue onCompletion
    //Then Repeat The Process To Move Onto The Next One In The Queue (Without Adding A New Sound On Subsequent Recursive Calls)
    fun mmScheduleSound(mvSoundReferenceToAddToQueue : Int, mvAddSoundToQueue : Boolean) {
        //Schedule A New Sound At The End Of The Queue
        if (mvAddSoundToQueue) mvSoundBuffer.add(mvSoundReferenceToAddToQueue)
        //Get The Next Sound In The Rotation By Querying The Front Of The Queue
        val mvFirstSound = mvSoundBuffer.getOrNull(0)
        //If There Indeed Was A Next Sound In Rotation...
        if (mvFirstSound != null) {
            //Are Sounds Currently Playing? If So, Ignore This Section...
            if (!mvSoundCurrent.mmIsPlaying()) {
                mvSoundCurrent.mmPause()
                mvSoundCurrent.mmReset()
                mvSoundCurrent.mmReplaceSound(mvFirstSound)
                mvSoundCurrent.mmRewind()
                mvSoundCurrent.mmStart()
                mvSoundCurrent.mvMediaPlayer.setOnCompletionListener{mvSoundBuffer.removeFirstOrNull()?.also{mvSoundCurrent.mmRelease(); mmScheduleSound(0, mvAddSoundToQueue = false)}} //"?." Safety Check: In Any Situation Where We Return null, It Theoretically Shouldn't Continue To mmScheduleSound() And Should (Therefore) Avoid A Subsequent Recursive Call (Since The Queue Would Theoretically Be Empty)
            }
        }
    }

    //Pause The Current Sound, Release Allotted Resources, And "Clear" All Pending Sounds From The Queue
    fun mmStopAllPendingSounds() {
        mvSoundCurrent.mmPause()
        mvSoundCurrent.mmRelease()
        mvSoundBuffer.clear()
    }
}

     */
}