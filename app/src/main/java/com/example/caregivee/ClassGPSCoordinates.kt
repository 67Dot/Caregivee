package com.example.caregivee

import android.content.Context
import java.io.Serializable
import kotlin.math.absoluteValue

//Review Code For This Page [√√√√√]

class ClassGPSCoordinates(val mvContext : Context) : Serializable {
    //Basic Variables
        private var mvLat = ""
        private var mvLon = ""

    //Public Variables
        var mvLatLon = ""
        var mvLatLonLink = ""

    //Update Variables
        fun mmUpdate(mvLatInp : String, mvLonInp : String) {
            //Normalize Formatting (Three Digits Before, Fifteen Digits After Decimal, With A Plus Or Minus Sign Regardless)
                mvLat = (if (mvLatInp.toDouble() >= 0) "+" else "-") + String.format("%019.15f", mvLatInp.toDouble().absoluteValue)
                mvLon = (if (mvLonInp.toDouble() >= 0) "+" else "-") + String.format("%019.15f", mvLonInp.toDouble().absoluteValue)
            //How Is The Information Displayed In The SMS
                mvLatLon     = String.format(mvContext.getString(R.string.mtLatLon), mvLat.replace("+", "0"), mvLon.replace("+", "0")) //<-- On At Least Some Flip Phones, A Preceding "+" Might Turn "Lat: +000.000000000000000" Into A Hyperlink... So Let's Replace The "+" With A "0"
                mvLatLonLink = String.format(mvContext.getString(R.string.mtGpsLink), mvLat, mvLon) //<-- "Google Maps" Hyperlink To Cartographically Check Location By Clicking A Link In The SMS
        }
}
