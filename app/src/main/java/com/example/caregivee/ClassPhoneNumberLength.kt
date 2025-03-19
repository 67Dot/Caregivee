package com.example.caregivee

//Review Code For This Page [√√√√√]

class ClassPhoneNumberLength {
    //Using Emergency Numbers Is Not Allowed
    //Originally, This Was Coded To Notify The User That It Was Not Recommendable, Then Allow Override
    //However, It's Probably Not Worth The Risk, Please Just Make It So The User Can't Choose Common Emergency Numbers At All.
    //Methodology:
    //===========
    //We Check If It's Three Digits And Begins With A Zero, One, or Nine... Which Represents Much Of The World's Emergency Numbers
    //These Patterns Could Pose An Issue In Samoa, Faroe Islands, the Cook Islands, and Niue (misspelled Nieu here https://worldpopulationreview.com/country-rankings/phone-number-length-by-country)
    //Where ALL Phone Number Lengths Are OSTENSIBLY Only *3* Digits Long
        //Current Research:
        //================
        //Samoa: No 0** or 1** patterns, but 9** are emergency services.
        //Faroe: No 0** or 9** patterns, but 1** are emergency services.
        //Cook:  No 0** or 1** or 9** patterns.
        //Nieu:  No 1** pattern, but 0** is operator/directory/weather and 9** are emergency services.
            fun <T1> mmEmergencyNumber (mvNumber : T1) : Boolean {
                //Three Digits And Begins With A "0", "1", Or "9"?
                //If So, Let's Prevent The User From Selecting It
                    val mvRawNumber = (mvNumber as String).filter{it.isDigit()} //<-- Remove Dashes And Whatnot
                    return mvRawNumber.length == 3 && listOf("0", "1", "9").contains(mvRawNumber[0].toString())
            }
            fun <T1> mmShortCode (mvNumber : T1) : Boolean {
                //Is A (Non-Blank) Field Not A Standard United States-length Phone Number?
                //If So, Let's Prompt The User To Review Their Choice
                    val mvRawNumber = (mvNumber as String).filter{it.isDigit()} //<-- Remove Dashes And Whatnot
                    return !(mvRawNumber.length == 10 || mvRawNumber.length == 11 && mvRawNumber[0].toString() == "1" || mvRawNumber.isEmpty() /* <-- Ignore If Blank Field, Warning About Blank Fields Is Handled Elsewhere */)
            }
}