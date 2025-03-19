package com.example.caregivee

import android.content.Context
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

//Review Code For This Page [√√√√√]

//Note: Here, mvContext always refers to the APPLICATION's Context, never the Activity's Context.
//(Source: https://stackoverflow.com/a/33896724/16118981)

class ClassFileManager (val mvContext : Context) {
    //Save Object To File
        fun <T : Serializable> mmSaveSerializable(mvObjectToSave : T, mvFileName : String) : Boolean {
            //Return true If We Succeed In Saving Object To File
                return try {
                            val mvFileOutput = mvContext.openFileOutput(mvFileName, Context.MODE_PRIVATE)
                            val mvObjectOutputStream = ObjectOutputStream(mvFileOutput)
                            mvObjectOutputStream.writeObject(mvObjectToSave)
                            mvObjectOutputStream.close()
                            mvFileOutput.close()
                            true
                       } catch (mvE: IOException) {
                            mvE.printStackTrace()
                            false
                       }
        }
    //Retrieve Object From File
        @Suppress("UNCHECKED_CAST")
        fun <T : Serializable> mmReadSerializable(mvFileName : String, mvDefault : T) : T {
            //Return Object From File
                return try {
                            val mvFileInput = mvContext.openFileInput(mvFileName)
                            val mvObjectInputStream = ObjectInputStream(mvFileInput)
                            val mvObjectToReturn = mvObjectInputStream.readObject() as T
                            mvObjectInputStream.close()
                            mvFileInput.close()
                            mvObjectToReturn
                       } catch (mvE: IOException) {
                            mvE.printStackTrace()
                            mvDefault
                       } catch (mvE: ClassNotFoundException) {
                            mvE.printStackTrace()
                            mvDefault
                       }
        }
    //Remove File
        @Suppress("UNUSED") //<-- We Suppress The "Unused Method" Warning Because We Currently Don't Use This Method Anywhere ¯\_(ツ)_/¯
        fun mmRemoveFile(mvFileName: String) {
            mvContext.deleteFile(mvFileName)
        }
}