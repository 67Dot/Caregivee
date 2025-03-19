package com.example.caregivee

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone

//Review Code For This Page [√√√√√]

class ClassContactsRetriever (val mvContext : Context) {
    //Get The Contact List From The Phone (https://stackoverflow.com/questions/70693889/get-contacts-in-android-without-duplicates)
        fun mmContactsRetriever() : MutableList<ClassLineItem>  {
            val mvNames: MutableList<ClassLineItem> = arrayListOf()
            var mvCursor : Cursor? = null
            try {
                //Fetch Contacts
                    mvCursor = mvContext.contentResolver.query(ContactsContract.Data.CONTENT_URI, null,ContactsContract.Data.HAS_PHONE_NUMBER + "> 0 AND " + ContactsContract.Data.MIMETYPE + "=?", arrayOf(Phone.CONTENT_ITEM_TYPE) /* <-- This Populates The "=?" Placeholder */, ContactsContract.Data.CONTACT_ID /* <-- Sort By */)

                //Were Any Contacts Returned?
                    if (mvCursor != null && mvCursor.count > 0) {
                        //Iterate Through The Results
                            while (mvCursor.moveToNext()) {
                                //Don't Include Home/Work Contacts For Now, Only Mobile Contacts
                                //Use .getColumnIndexOrThrow() Instead of .getColumnIndex() (https://stackoverflow.com/questions/71338033/android-sqlite-value-must-be-%E2%89%A5-0-getcolumnindex-kotlin?answertab=scoredesc)
                                    if (mvCursor.getInt(mvCursor.getColumnIndexOrThrow(Phone.TYPE)) == Phone.TYPE_MOBILE) { //<-- Separate Home/Mobile/Fax Numbers Etc. (https://stackoverflow.com/questions/9636209/android-i-try-to-find-what-type-of-numberfrom-contact-is-mobile-home-work)
                                        val mvClassLineItem = ClassLineItem()
                                        mvClassLineItem.mvImage  = R.drawable.baseline_phone_android_24
                                        mvClassLineItem.mvId     = mvCursor.getLong(mvCursor.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID))
                                        mvClassLineItem.mvMobile = mvCursor.getString(mvCursor.getColumnIndexOrThrow(ContactsContract.Data.DATA1))
                                        mvClassLineItem.mvName   = mvCursor.getString(mvCursor.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME))
                                        mvNames.add(mvClassLineItem)
                                    }
                            }
                    }
            }
            catch (mvEx : Exception) {
                mvEx.printStackTrace()
            }
            finally {
                mvCursor?.close() //"?." Safety Review: This Theoretically Works Better Than "mvCursor!!.close()" Because We Shouldn't Throw An Exception In The "Finally" Block... Meanwhile, If There IS An Exception, "mvNames" Should Default To An Empty List
            }
            return mvNames
        }
}