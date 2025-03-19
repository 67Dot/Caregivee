package com.example.caregivee

import android.content.Context
import android.media.MediaPlayer

//Review Code For This Page [√√√√√]

class ClassMediaPlayer(val mvInt: Int, var mvScheduled : Boolean, mvSoundReference : Int, val mvContext : Context) {

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