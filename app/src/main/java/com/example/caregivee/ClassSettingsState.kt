package com.example.caregivee

import java.io.Serializable

//Review Code For This Page [√√√√√]

//"mvSettings"
//(Note: This — And Any Subclasses — Need To Be Serializable In Order To Save To Disk)
    data class ClassSettingsState(var mvContacts : ArrayList<ClassLineItem>, var mvRefreshRate : Int) : Serializable