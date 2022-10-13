package com.terzulli.terzullifilemanager.utils;

import android.content.SharedPreferences;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RecentsFilesManager {
    private final SharedPreferences sharedPreferences;
    private ArrayList<File> recentsFilesList;

    public RecentsFilesManager(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        loadRecentsFilesListFromPreferences();
    }

    public void addFileToRecentsFilesList(File file) {
        if (recentsFilesList == null)
            recentsFilesList = new ArrayList<>();
        else {
            int MAX_SIZE = 20;
            if (recentsFilesList.size() == MAX_SIZE)
                recentsFilesList.remove(0);

            if (!recentsFilesList.contains(file))
                recentsFilesList.add(file);
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        try {
            editor.putString("recentsFileList", ObjectSerializer.serialize(recentsFilesList));
        } catch (IOException e) {
            //e.printStackTrace();
        }
        editor.apply();
    }

    public ArrayList<File> getRecentsFilesList() {
        return recentsFilesList;
    }

    private void loadRecentsFilesListFromPreferences() {
        ArrayList<File> emptyList = new ArrayList<>();

        try {
            String prefStr = sharedPreferences.getString("recentsFileList", ObjectSerializer.serialize(emptyList));
            recentsFilesList = (ArrayList<File>) ObjectSerializer.deserialize(prefStr);
        } catch (IOException | ClassNotFoundException e) {
            recentsFilesList = new ArrayList<>();
        }
    }

}
