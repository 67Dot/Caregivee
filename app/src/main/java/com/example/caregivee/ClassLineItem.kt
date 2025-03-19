package com.example.caregivee

import android.graphics.Color
import java.io.Serializable

//Review Code For This Page [√√√√√]

//Object Containing Data For Each "Contact"
    class ClassLineItem : Serializable {
        //Properties
            var mvImage = R.drawable.baseline_phone_android_24
            var mvId : Long? = null
            var mvName : String? = null //<-- Name Of Contact Goes In This Variable
            var mvMobile : String? = null //<-- Phone Number Of Contact Goes In This Variable
            var mvColorImg = Color.parseColor("#000000")
            var mvColorText = Color.parseColor("#000000")

        //Copy The Object
            fun mmCopy() : ClassLineItem {
                val mvCopy = ClassLineItem()
                mvCopy.mvImage = mvImage
                mvCopy.mvId = mvId
                mvCopy.mvName = mvName
                mvCopy.mvMobile = mvMobile
                mvCopy.mvColorImg = mvColorImg
                mvCopy.mvColorText = mvColorText
                return mvCopy
            }

        //Equality Check
            fun mmEquals(mvClassLineItem : ClassLineItem) : Boolean {
                return mvName == mvClassLineItem.mvName && mvMobile == mvClassLineItem.mvMobile
            }
    }
