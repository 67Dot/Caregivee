package com.example.caregivee

import android.os.Build
import android.text.Html
import android.text.Spanned

//Review Code For This Page [√√√√√]

class ClassFromHtml {
    //Format An HTML String Into Styled Text
    //(Source 1: https://developer.android.com/reference/android/text/Html.html#fromHtml(java.lang.String,%20int))
    //(Source 2: https://stackoverflow.com/questions/37904739/html-fromhtml-deprecated-in-android-n)
        @Suppress("DEPRECATION") //<-- Because Otherwise A Small Deprecation Error Appears, Despite The Fact That We Have "Responsive Design" Based On The Android API Below
        fun mmFromHtml (mvStringInput : String) : Spanned {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(mvStringInput, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(mvStringInput)
            }
        }
}