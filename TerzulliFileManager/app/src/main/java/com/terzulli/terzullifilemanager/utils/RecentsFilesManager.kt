package com.terzulli.terzullifilemanager.utils

import android.content.SharedPreferences
import com.terzulli.terzullifilemanager.utils.ObjectSerializer
import java.io.File
import java.io.IOException
import java.util.ArrayList

class RecentsFilesManager(private val sharedPreferences: SharedPreferences) {
    var recentsFilesList: ArrayList<File>? = null
        private set

    init {
        loadRecentsFilesListFromPreferences()
    }

    fun addFileToRecentsFilesList(file: File) {
        if (recentsFilesList == null)
            recentsFilesList = ArrayList()
        else {
            val MAX_SIZE = 20
            if (recentsFilesList!!.size == MAX_SIZE) recentsFilesList!!.removeAt(0)
            if (!recentsFilesList!!.contains(file)) recentsFilesList!!.add(file)
        }

        val editor = sharedPreferences.edit()
        try {
            editor.putString("recentsFileList", ObjectSerializer.serialize(recentsFilesList))
        } catch (e: IOException) {
            //e.printStackTrace();
        }
        editor.apply()
    }

    private fun loadRecentsFilesListFromPreferences() {
        val emptyList = ArrayList<File>()

        recentsFilesList = try {
            val prefStr = sharedPreferences.getString(
                "recentsFileList",
                ObjectSerializer.serialize(emptyList)
            )
            ObjectSerializer.deserialize(prefStr) as ArrayList<File>
        } catch (e: IOException) {
            ArrayList()
        } catch (e: ClassNotFoundException) {
            ArrayList()
        }
    }
}