package com.terzulli.terzullifilemanager.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.webkit.MimeTypeMap;

import com.terzulli.terzullifilemanager.R;

import java.io.File;
import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public abstract class Utils {

    // string constants
    public static final String STR_SORT_BY_NAME = "NAME";
    public static final String STR_SORT_BY_SIZE = "SIZE";
    public static final String STR_SORT_BY_DATE = "DATE";
    public static final String STR_FILE_DOCUMENT = "Document";
    public static final String STR_FILE_SPREADSHEET = "Spreadsheet";
    public static final String STR_FILE_VIDEO = "Video";
    public static final String STR_FILE_IMAGE = "Image";
    public static final String STR_FILE_AUDIO = "Audio";
    public static final String STR_FILE_ARCHIVE = "Archive";
    public static final String STR_FILE_DIRECTORY = "Folder";
    public static final String STR_FILE_PRESENTATION = "Presentation";
    public static final String STR_FILE_APPLICATION = "Android application";
    public static final String STR_FILE_GENERIC = "File";
    public static final String STR_LOCATION_INTERNAL_FRIENDLY_NAME = "Internal Storage";
    public static final String STR_LOCATION_RECENTS_FRIENDLY_NAME = "Recents";
    public static final String STR_LOCATION_AUDIO_FRIENDLY_NAME = "Audio";
    public static final String STR_LOCATION_VIDEOS_FRIENDLY_NAME = "Videos";
    public static final String STR_LOCATION_IMAGES_FRIENDLY_NAME = "Images";
    public static final String STR_LOCATION_DOWNLOADS_FRIENDLY_NAME = "Downloads";
    public static final String STR_LOCATION_LOGS_FRIENDLY_NAME = "Logs";

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
                return t.getName().toLowerCase().compareTo(t1.getName().toLowerCase()); // aggiunto lower
            });
        } else {
            Arrays.sort(files, (t, t1) -> {
                if (t.isDirectory() && t1.isFile())
                    return -1;
                else if (t.isFile() && t1.isDirectory())
                    return 1;

                // both files or both directories
                return t1.getName().toLowerCase().compareTo(t.getName().toLowerCase());
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
                return (new Date(t.lastModified())).compareTo(new Date(t1.lastModified()));
            });
        } else {

            Arrays.sort(files, (t, t1) -> {
                if (t.isDirectory() && t1.isFile())
                    return -1;
                else if (t.isFile() && t1.isDirectory())
                    return 1;

                // both files or both directories
                return (new Date(t1.lastModified())).compareTo(new Date(t.lastModified()));
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


    public static void sortFileAndDirectoriesList(File[] filesAndDirs, final String sortBy, final boolean ascending) {
        switch (sortBy) {
            case STR_SORT_BY_SIZE:
                Utils.sortBySize(filesAndDirs, ascending);
                break;
            case STR_SORT_BY_DATE:
                Utils.sortByDate(filesAndDirs, ascending);
                break;
            default:
                Utils.sortByName(filesAndDirs, ascending);
                break;
        }

        //return filesAndDirs;
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
        if (!mimeType.equals("") && !mimeType.equals(STR_FILE_GENERIC))
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
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yy, HH:mm");

        return formatter.format(date);
    }

    @SuppressLint("SimpleDateFormat")
    public static String formatDateDetailsFullWithMilliseconds(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss.SSS");

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

        String mediaType = getFileType(file);

        String extension = mimeType.split("/")[1];
        extension = extension.toUpperCase();

        if(extension.contains("VND"))
            return MimeTypeMap.getFileExtensionFromUrl(String.valueOf(Uri.fromFile(file))).toUpperCase()
                    + " " + mediaType;
        else
            return extension + " " + mediaType;
    }

    public static boolean fileIsImage(File file) {
        switch (MimeTypeMap.getFileExtensionFromUrl(String.valueOf(Uri.fromFile(file))).toLowerCase()) {
            case "png":
            case "jpg":
            case "jpeg":
            case "gif":
            case "bmp":
            case "heic":
                return true;
            default:
                return false;
        }
    }

    public static int getFileTypeIcon(File file) {
        if (file.isDirectory())
            return R.drawable.ic_folder;

        switch(MimeTypeMap.getFileExtensionFromUrl(String.valueOf(Uri.fromFile(file))).toLowerCase()) {
            case "png":
            case "jpg":
            case "jpeg":
            case "gif":
            case "bmp":
            case "heic":
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

            case "java":
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
            return STR_FILE_DIRECTORY;

        switch(MimeTypeMap.getFileExtensionFromUrl(String.valueOf(Uri.fromFile(file))).toLowerCase()) {
            case "png":
            case "jpg":
            case "jpeg":
            case "gif":
            case "bmp":
            case "heic":
                return STR_FILE_IMAGE;

            case "mp3":
            case "wav":
            case "ogg":
            case "midi":
                return STR_FILE_AUDIO;

            case "mp4":
            case "rmvb":
            case "avi":
            case "flv":
            case "3gp":
                return STR_FILE_VIDEO;

            case "java":
            case "cpp":
            case "py":
            case "json":
            case "c":
            case "htm":
            case "html":
            case "js":
            case "txt":
            case "xml":
            case "log":
            case "doc":
            case "docx":
            case "pdf":
                return STR_FILE_DOCUMENT;

            case "xls":
            case "xlsx":
                return STR_FILE_SPREADSHEET;

            case "ppt":
            case "pptx":
                return STR_FILE_PRESENTATION;

            case "jar":
            case "zip":
            case "rar":
            case "gz":
            case "7z":
                return STR_FILE_ARCHIVE;

            case "apk":
                return STR_FILE_APPLICATION;

            default:
                return STR_FILE_GENERIC;
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

        List<File> fileList = new LinkedList<>(Arrays.asList(filesAndDirs));
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
        return name.matches("^[a-zA-Z0-9-_()\\[\\] ]*(\\.[a-zA-Z0-9_]+)+$");
    }

    public static boolean validateGenericFileName(File file, String name) {
        if (file.isDirectory())
            return validateDirectoryName(name);
        else
            return validateFileName(name);
    }

    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}
