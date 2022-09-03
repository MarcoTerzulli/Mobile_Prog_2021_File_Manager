package com.terzulli.terzullifilemanager.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Utils {

    public static boolean isDeviceInLandscapeOrientation(Activity activity) {
        return activity.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public static void disableScreenRotation(@NonNull Activity activity) {
        int screenOrientation = activity.getResources().getConfiguration().orientation;

        if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    public static void enableScreenRotation(@NonNull Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

    public static void sortByName(File[] files, final boolean ascending){
        if (ascending) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File t, File t1) {
                    if (t.isDirectory() && t1.isFile())
                        return -1;
                    else if (t.isFile() && t1.isDirectory())
                        return 1;

                    // both files or both directories
                    return t.getName().compareTo(t1.getName());
                }
            });
        } else {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File t, File t1) {
                    if (t.isDirectory() && t1.isFile())
                        return -1;
                    else if (t.isFile() && t1.isDirectory())
                        return 1;

                    // both files or both directories
                    return t1.getName().compareTo(t.getName());
                }
            });
        }
    }

    public static void sortByDate(File[] files, final boolean ascending){
        if (ascending) {

            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File t, File t1) {
                    if (t.isDirectory() && t1.isFile())
                        return -1;
                    else if (t.isFile() && t1.isDirectory())
                        return 1;

                    // both files or both directories
                    return (int) (t.lastModified() - t1.lastModified());
                }
            });
        } else {

            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File t, File t1) {
                    if (t.isDirectory() && t1.isFile())
                        return -1;
                    else if (t.isFile() && t1.isDirectory())
                        return 1;

                    // both files or both directories
                    return (int) (t1.lastModified() - t.lastModified());
                }
            });
        }

    }

    public static void sortBySize(File[] files, final boolean ascending){
        if (ascending) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File t, File t1) {
                    if (t.isDirectory() && t1.isFile())
                        return -1;
                    else if (t.isFile() && t1.isDirectory())
                        return 1;

                    // both files or both directories
                    return (int) (t.length() - t1.length());
                }
            });
        } else {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File t, File t1) {
                    if (t.isDirectory() && t1.isFile())
                        return -1;
                    else if (t.isFile() && t1.isDirectory())
                        return 1;

                    // both files or both directories
                    return (int) (t1.length() - t.length());
                }
            });
        }
    }



    public static File[] sortFileAndFoldersList(File[] filesAndDirs, final String sortBy, final boolean ascending) {
        switch (sortBy) {
            case "NAME":
                Utils.sortByName(filesAndDirs, ascending);
                break;
            case "SIZE":
                Utils.sortBySize(filesAndDirs, ascending);
                break;
            case "DATE":
                Utils.sortByDate(filesAndDirs, ascending);
                break;
            default:
                Utils.sortByName(filesAndDirs, ascending);
                break;
        }


        return filesAndDirs;
    }



}
