package com.example.caregivee

import android.content.Context
import android.graphics.Color
import android.widget.Button

//Review Code For This Page [√√√√√]

class ClassContactsUpToDate(val mvContext : Context) {
    //Are The Contacts Up To Date?
        @Suppress("UNCHECKED_CAST") //<-- For The "as T" In The Return Section
        fun <T> mmClassContactsUpToDate(mvSettings : ClassSettingsState, mvContactButton : MutableList<Button>, mvContactsListPermitted : Boolean, mvSirenMode : Boolean, mvReturnMode : Int) : T {
            //Basic Variables
                var mvContactsAreCurrent = true
                var mvNonNullContact = 1
                var mvPattern = ""
            //Get The CURRENT Contacts
                val mvContactList = if (mvContactsListPermitted) ClassContactsRetriever(mvContext).mmContactsRetriever() else null
            //Loop Through Contacts
                for (mvIt in 0 until mvSettings.mvContacts.size) {
                    //Get Ready To Turn The Button For This Contact Red If Any Contacts Are Outdated
                        var mvRedContact = false
                    //Conditionals To Check If We Should Change Colors
                        if (mvSettings.mvContacts[mvIt].mvMobile == null) {
                            //Unpopulated Contacts Should Always Turn Red Regardless Of Mode...
                                mvRedContact = true
                                mvPattern += "0"
                        }
                        else {
                            //Otherwise, Update The "Pattern" To Reflect A Populated Contact
                                mvPattern += mvNonNullContact.toString()
                                mvNonNullContact++ //<-- The Value Increments With Each Populated Contact, E.G. The Pattern Becomes "123" If All Are Populated, Or "012" If Only The Last Two Are Populated
                        }
                        if (mvContactList != null && mvContactList.size > 0) {
                            //Is One Of Our Local Contacts Not Identical To Any In The Phone's Contacts List (I.E. Outdated)?
                            //(Note: This Conditional Is Ignored In "No Contacts List Mode")
                                mvRedContact = !mvContactList.any{ it.mmEquals(mvSettings.mvContacts[mvIt]) }
                        }
                    //Actually Begin Changing Colors
                    //==============================
                    //Red Contact?
                        if (mvRedContact) {
                            //Make The Contact Button Red If Needed
                                mvContactButton[mvIt].setBackgroundColor(Color.argb(255, 255, 0, 0))
                                mvContactsAreCurrent = false
                        }
                    //Siren Mode?
                        if (mvSirenMode) {
                            //Gray Out The Contact Button If We're In "Siren Mode"
                                mvContactButton[mvIt].setTextColor(Color.argb(255, 216, 216, 216))
                                mvContactButton[mvIt].setBackgroundColor(Color.argb(255, 172, 172, 172))
                                mvContactsAreCurrent = false
                        }
                        else {
                            //There Are Some Situations Where We May Need To Change The Text Color From "Grayed-Out" Back To Black
                            //Like If Permissions Change From "Siren Mode" To A Different Mode
                                mvContactButton[mvIt].setTextColor(Color.argb(255, 0, 0, 0))
                        }
                }
            //In Different Situations, We Need Different Return Types Of Objects
                return when (mvReturnMode) {
                   0    -> Unit as T //<-- No Return Value Needed In onCreate() Of "ActivitySettings"
                   1    -> Pair(mvContactsAreCurrent, mvPattern) as T //<-- Just These Two Variables Needed In mmBackButtonClick() Of "ActivitySettings"
                   else -> (mvContactList?.size ?: 0) as T  //"?." Safety Check: In Any Situation Where It's null, It Theoretically Should Theoretically Just Return 0 Due To The Elvis Operator "?:" (Note: This Value Needed In mmContactNumberClick() Of ActivitySettings)
                }
        }
}
