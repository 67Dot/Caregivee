package com.example.caregivee

import android.os.Handler
import android.os.Looper

//Review Code For This Page [√√√√√]

class ClassCountdown {
    //Initialize Countdown Variables
        var mvCountdown = 0
        var mvRunnableRefresh = 0L
        val mvRefreshDebugSpeedFactor = 1f

    //Handlers & Runnables
        val mvHandler = Handler(Looper.getMainLooper())
        lateinit var mvRunnable: Runnable //<-- Needs To Be In The Same Scope As mvHandler (Source: https://stackoverflow.com/questions/60761647/kotlin-how-to-pass-a-runnable-as-this-in-handler)
        var mvPause = false

    //Countdown
    //Inline Functions THEORETICALLY Reduce Memory Load (Source: https://medium.com/huawei-developers/inline-functions-in-kotlin-8581a82ca530)
    //Crossinline Keyword Explanation:
    //   First, "non-local functions" can be explained like this (Source: https://medium.com/@ramadan123sayed/mastering-inline-functions-in-kotlin-deep-dive-into-inline-crossinline-and-noinline-with-generic-c135052a0ea4#:~:text=In%20the%20context%20of%20inline,also%20the%20entire%20calling%20function.)
    //      If you have a function that calls a lambda function:
    //         If the lambda function has a "return" keyword,
    //         it would be liable to be interpreted as wanting to return not from the lambda,
    //         but instead from the CALLING function.
    //      You can allow non-local returns to return from the calling function by using the "inline" keyword,
    //         or prevent them by using the "crossinline" keyword.
    //Though We Use The "crossline" Keyword Here For Our Higher-Order Function, It's May Be Superfluous:
        inline fun <T1, T2> mmCountdownClock (mvCountdownFrom : T1, mvRefresh : T2, crossinline mmCountedDown : () -> Unit) {
            //Set Initial Countdown Value
                mvCountdown = mvCountdownFrom as Int
                mvRunnableRefresh = ((mvRefresh as Long).toFloat() / mvRefreshDebugSpeedFactor).toLong()
            //Main "Countdown" Thread (When We Reach *0*, We Call A Function)
                mvRunnable = Runnable {
                    //Decrement mvCountdown
                        if (!mvPause) mvCountdown--
                    //Repost After An Interval
                        mvHandler.postDelayed(mvRunnable, mvRunnableRefresh)
                    //What To Do When The Countdown Is Complete?
                        if (mvCountdown <= 0) {
                            mmCountedDown()
                        }
                }
            //First Posting (I.E. Start The Countdown)
                mvHandler.postDelayed(mvRunnable, mvRunnableRefresh)
        }
    //Stop The Countdown
        fun mmStop() {
            //Has The Runnable Been Initialized? If So, Remove Callbacks:
                if (this::mvRunnable.isInitialized) mvHandler.removeCallbacks(mvRunnable)
        }
    //Pause The Countdown
        fun mmPause() {
            //Toggle mvPause Variable
                mvPause = true
        }
    //Unpause The Countdown
        fun mmUnpause() {
            //Toggle mvPause Variable
                mvPause = false
        }
    }
