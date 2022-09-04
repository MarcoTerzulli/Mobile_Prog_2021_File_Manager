package com.terzulli.terzullifilemanager.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.terzulli.terzullifilemanager.R;

import java.io.File;
import java.text.CharacterIterator;
import java.text.SimpleDateFormat;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

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

    public static void sortByName(File[] files, final boolean ascending) {
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

    public static void sortByDate(File[] files, final boolean ascending) {
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

    public static void sortBySize(File[] files, final boolean ascending) {
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

    public static String formatFileDetails(File file) {
        String details = "";

        long fileSize = file.length();
        String formattedUsedSpace = humanReadableByteCountSI(fileSize);

        Date lastModified = new Date(file.lastModified());
        String formattedDateString = formatDateDetails(lastModified);

        String mimeType = getReadableMimeType(getMimeType(Uri.fromFile(file)));

        details = formattedDateString + ", " + formattedUsedSpace;
        if (!mimeType.equals(""))
            details +=  ", " + mimeType;
        else
            details +=  ", BIN file";

        return details;
    }

    public static String formatDateDetails(Date date) {
        Date todayDate = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yy");;

        if (todayDate.getDate() == date.getDate()) {
            formatter = new SimpleDateFormat("HH:mm");
        } else if (todayDate.getYear() == date.getYear()) {
            formatter = new SimpleDateFormat("dd MMM");
        }

        return formatter.format(date);
    }

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
                // TODO preview immagine
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
}
