package com.terzulli.terzullifilemanager.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import com.terzulli.terzullifilemanager.R;

import java.io.File;
import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Utils {

    // string constants
    public static final String strSortName = "NAME";
    public static final String strSortSize = "SIZE";
    public static final String strSortDate = "DATE";
    public static final String strFileDocument = "Document";
    public static final String strFileSpreadsheet = "Spreadsheet";
    public static final String strFileVideo = "Video";
    public static final String strFileImage = "Image";
    public static final String strFileAudio = "Audio";
    public static final String strFileArchive = "Archive";
    public static final String strFileDirectory = "Folder";
    public static final String strFilePresentation = "Presentation";
    public static final String strFileApplication = "Android application";
    public static final String strFileGeneric = "File";

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

    public static void sortByName(File[] files, final boolean ascending) {
        if (files == null)
            return;

        if (ascending) {
            Arrays.sort(files, (t, t1) -> {
                if (t.isDirectory() && t1.isFile())
                    return -1;
                else if (t.isFile() && t1.isDirectory())
                    return 1;

                // both files or both directories
                return t.getName().compareTo(t1.getName());
            });
        } else {
            Arrays.sort(files, (t, t1) -> {
                if (t.isDirectory() && t1.isFile())
                    return -1;
                else if (t.isFile() && t1.isDirectory())
                    return 1;

                // both files or both directories
                return t1.getName().compareTo(t.getName());
            });
        }
    }

    public static void sortByDate(File[] files, final boolean ascending) {
        if (files == null)
            return;

        if (ascending) {

            Arrays.sort(files, (t, t1) -> {
                if (t.isDirectory() && t1.isFile())
                    return -1;
                else if (t.isFile() && t1.isDirectory())
                    return 1;

                // both files or both directories
                return (int) (t.lastModified() - t1.lastModified());
            });
        } else {

            Arrays.sort(files, (t, t1) -> {
                if (t.isDirectory() && t1.isFile())
                    return -1;
                else if (t.isFile() && t1.isDirectory())
                    return 1;

                // both files or both directories
                return (int) (t1.lastModified() - t.lastModified());
            });
        }

    }

    public static void sortBySize(File[] files, final boolean ascending) {
        if (files == null)
            return;

        if (ascending) {
            Arrays.sort(files, (t, t1) -> {
                if (t.isDirectory() && t1.isFile())
                    return -1;
                else if (t.isFile() && t1.isDirectory())
                    return 1;

                // both files or both directories
                return (int) (t.length() - t1.length());
            });
        } else {
            Arrays.sort(files, (t, t1) -> {
                if (t.isDirectory() && t1.isFile())
                    return -1;
                else if (t.isFile() && t1.isDirectory())
                    return 1;

                // both files or both directories
                return (int) (t1.length() - t.length());
            });
        }
    }


    public static File[] sortFileAndFoldersList(File[] filesAndDirs, final String sortBy, final boolean ascending) {
        switch (sortBy) {
            case strSortName:
                Utils.sortByName(filesAndDirs, ascending);
                break;
            case strSortSize:
                Utils.sortBySize(filesAndDirs, ascending);
                break;
            case strSortDate:
                Utils.sortByDate(filesAndDirs, ascending);
                break;
            default:
                Utils.sortByName(filesAndDirs, ascending);
                break;
        }

        return filesAndDirs;
    }

    public static String formatFileDetails(File file) {
        String details;

        long fileSize = file.length();
        String formattedUsedSpace = humanReadableByteCountSI(fileSize);

        Date lastModified = new Date(file.lastModified());
        String formattedDateString = formatDateDetailsShort(lastModified);

        //String mimeType = getReadableMimeType(getMimeType(Uri.fromFile(file)));
        String mimeType = getReadableMimeTypeAndExtension(file);

        details = formattedDateString + ", " + formattedUsedSpace;
        if (!mimeType.equals(""))
            details +=  ", " + mimeType;
            //details +=  ", " + getFileType(file);
        else
            details +=  ", BIN file";

        return details;
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatDateDetailsShort(Date date) {
        Date todayDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yy");

        if (todayDate.getDate() == date.getDate()) {
            formatter = new SimpleDateFormat("HH:mm");
        } else if (todayDate.getYear() == date.getYear()) {
            formatter = new SimpleDateFormat("dd MMM");
        }

        return formatter.format(date);
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatDateDetailsFull(Date date) {
        Date todayDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yy, HH:mm");

        return formatter.format(date);
    }

    @SuppressLint("DefaultLocale")
    public static String humanReadableByteCountSI(long bytes) {
        if (-1000 < bytes && bytes < 1000) {
            return bytes + " B";
        }

        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }

    public static String getMimeType(Uri url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(String.valueOf(url));
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public static String getReadableMimeType(String mimeType) {
        if (mimeType == null)
            return "";

        if (mimeType.length() == 0 || mimeType.split("/").length != 2) {
            return mimeType;
        }

        String mediaType = mimeType.split("/")[0];
        mediaType = mediaType.substring(0, 1).toUpperCase() + mediaType.substring(1);

        String extension = mimeType.split("/")[1];
        extension = extension.toUpperCase();

        return extension + " " + mediaType;
    }

    public static String getReadableMimeTypeAndExtension(File file) {
        String mimeType = getMimeType(Uri.fromFile(file));

        if (mimeType == null)
            return "";

        if (mimeType.length() == 0 || mimeType.split("/").length != 2) {
            return mimeType;
        }

        String mediaType = mimeType.split("/")[0];
        mediaType = getFileType(file);

        String extension = mimeType.split("/")[1];
        extension = extension.toUpperCase();

        return extension + " " + mediaType;
    }

    public static boolean fileIsImage(File file) {
        switch (MimeTypeMap.getFileExtensionFromUrl(String.valueOf(Uri.fromFile(file)))) {
            case "png":
            case "jpg":
            case "jpeg":
            case "gif":
            case "bmp":
                return true;
            default:
                return false;
        }
    }

    public static int getFileTypeIcon(File file) {
        if (file.isDirectory())
            return R.drawable.ic_folder;

        switch(MimeTypeMap.getFileExtensionFromUrl(String.valueOf(Uri.fromFile(file)))) {
            case "png":
            case "jpg":
            case "jpeg":
            case "gif":
            case "bmp":
                return R.drawable.ic_file_image;

            case "mp3":
            case "wav":
            case "ogg":
            case "midi":
                return R.drawable.ic_file_audio;

            case "mp4":
            case "rmvb":
            case "avi":
            case "flv":
            case "3gp":
                return R.drawable.ic_file_video;

            case "jsp":
            case "html":
            case "htm":
            case "js":
            case "php":
            case "c":
            case "cpp":
            case "py":
            case "json":
                return R.drawable.ic_file_code;

            case "txt":
            case "xml":
            case "log":
                return R.drawable.ic_file_document;

            case "xls":
            case "xlsx":
                return R.drawable.ic_file_sheets;

            case "doc":
            case "docx":
                return R.drawable.ic_file_docs;

            case "ppt":
            case "pptx":
                return R.drawable.ic_file_slides;

            case "pdf":
                return R.drawable.ic_file_pdf;

            case "jar":
            case "zip":
            case "rar":
            case "gz":
            case "7z":
                return R.drawable.ic_file_folder_zip;

            case "apk":
                return R.drawable.ic_file_apk;

            default:
                return R.drawable.ic_file_generic;
        }
    }

    public static String getFileType(File file) {
        if (file.isDirectory())
            return strFileDirectory;

        switch(MimeTypeMap.getFileExtensionFromUrl(String.valueOf(Uri.fromFile(file)))) {
            case "png":
            case "jpg":
            case "jpeg":
            case "gif":
            case "bmp":
                return strFileImage;

            case "mp3":
            case "wav":
            case "ogg":
            case "midi":
                return strFileAudio;

            case "mp4":
            case "rmvb":
            case "avi":
            case "flv":
            case "3gp":
                return strFileVideo;

            case "jsp":
            case "html":
            case "htm":
            case "js":
            case "php":
            case "c":
            case "cpp":
            case "py":
            case "json":
                return strFileGeneric;

            case "txt":
            case "xml":
            case "log":
                return strFileDocument;

            case "xls":
            case "xlsx":
                return strFileSpreadsheet;

            case "doc":
            case "docx":
                return strFileDocument;

            case "ppt":
            case "pptx":
                return strFilePresentation;

            case "pdf":
                return strFileDocument;

            case "jar":
            case "zip":
            case "rar":
            case "gz":
            case "7z":
                return strFileArchive;

            case "apk":
                return strFileApplication;

            default:
                return strFileGeneric;
        }
    }

    public static String getFileExtension(File file) {
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());

        if (ext.length() > 0)
            return ext;
        else {
            int dotPosition = file.getName().lastIndexOf(".");
            if (dotPosition >= 0) {
                return file.getName().substring(dotPosition).replace(".", "");
            }
        }

        return "";
    }

    public static boolean isFileAZipArchive(File file) {
        if (file.isDirectory())
            return false;

        String extension = MimeTypeMap.getFileExtensionFromUrl(String.valueOf(Uri.fromFile(file)));

        if (extension == null)
            return false;

        return extension.equals("zip");
    }

    public static File[] removeHiddenFilesFromArray(File[] filesAndDirs) {
        if (filesAndDirs == null)
            return null;

        List<File> fileList = Arrays.asList(filesAndDirs);
        List<File> filetoRemoveList = new ArrayList<>();

        for(File file : fileList){
            if(file.isHidden()){
                filetoRemoveList.add(file);
            }
        }
        fileList.removeAll(filetoRemoveList);

        return fileList.toArray(new File[0]);
    }

    public static boolean validateDirectoryName(String name) {
        return name.matches("^.?[a-zA-Z0-9-_.()\\[\\] ]*$");
    }

    public static boolean validateFileName(String name) {
        return name.matches("^[a-zA-Z0-9-_()\\[\\] ]*(\\.[a-zA-Z0-9]+)+$");
    }

    /*public static String stringAddEscapeForRegex(String str) {
        str = str.replace("(", "\\(");
        str = str.replace(")", "\\)");
        str = str.replace("[", "\\[");
        str = str.replace("]", "\\]");

        return str;
    }

    public static String stringRemoveEscapeForRegex(String str) {
        str = str.replace("\\(", "(");
        str = str.replace("\\)", ")");
        str = str.replace("\\[", "[");
        str = str.replace("\\]", "]");

        return str;
    }*/

    public static boolean validateGenericFileName(File file, String name) {
        if (file.isDirectory())
            return validateDirectoryName(name);
        else
            return validateFileName(name);
    }
}
