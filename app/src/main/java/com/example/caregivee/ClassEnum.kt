package com.example.caregivee

import android.graphics.Color
import java.io.Serializable

//Review Code For This Page [√√√√√]

//If You Want The Fetch The mvInt Value Specifically, Reference Something Like ClassEnum.GPSACTIVE.mvInt
    enum class ClassEnum(var mvInt : Int) : Serializable {
        //GPS Expired Flag
            GPSNEVERSTARTED(0),
            GPSACTIVE(1),
            GPSEXPIRED(2),

        //Caregivee Button
            BUTTONCHECKIN(0),
            BUTTONCHECKEDIN(1),
            BUTTONRESTARTAPP(2),

        //Callback Codes
            CALLBACKNONE(0),
            CALLBACKCLOSEAPP(1),

        //CallForward Codes
            CALLFWDNONE(0),
            CALLFWDAIRPLANEMODE(1),
            CALLFWDSIRENMODE(2),
            CALLFWDGPSEXPIRED(3),
            CALLFWDLOWSIGNAL(4),
            CALLFWDNOCONTACTS(5),
            CALLFWDSMSDELAYED(6),
            CALLFWDSMSDELIVERED(7),
            CALLFWDSMSDELIVERYFAILED(8),
            CALLFWDSMSSENTTOCONTACT(100),
            CALLFWDSMSFAILEDTOCONTACT(150),
            CALLFWDSMSGOTEXCEPTIONTOCONTACT(200),

        //Colors
            COLORRED(Color.argb(255, 255, 0, 0)),
            COLORORANGE(Color.argb(255, 255, 128, 0)),
            COLORYELLOW(Color.argb(255, 255, 255, 0)),
            COLORGREEN(Color.argb(255, 0, 255, 0)),

        //Priority Sounds
            PRIORITYVOLUMELOW(0),
            PRIORITYSCREENOFF(1),
            PRIORITYWINDOWDEFOCUSED(2),
            PRIORITYUNPLUGGED(3)
    }