package com.terzulli.terzullifilemanager.fragments;

import static android.content.Context.MODE_PRIVATE;
import static com.terzulli.terzullifilemanager.activities.MainActivity.isSearchActive;
import static com.terzulli.terzullifilemanager.adapters.ItemsAdapter.clearFileToExtractSelection;
import static com.terzulli.terzullifilemanager.adapters.ItemsAdapter.clearSelection;
import static com.terzulli.terzullifilemanager.adapters.ItemsAdapter.clearSelectionFromCompress;
import static com.terzulli.terzullifilemanager.adapters.ItemsAdapter.clearSelectionFromCopyMove;
import static com.terzulli.terzullifilemanager.adapters.ItemsAdapter.executeCompressOperationOnThread;
import static com.terzulli.terzullifilemanager.adapters.ItemsAdapter.executeCopyMoveOperationOnThread;
import static com.terzulli.terzullifilemanager.adapters.ItemsAdapter.executeExtractOperationOnThread;
import static com.terzulli.terzullifilemanager.adapters.ItemsAdapter.isSelectionModeEnabled;
import static com.terzulli.terzullifilemanager.utils.Utils.formatDateDetailsFull;
import static com.terzulli.terzullifilemanager.utils.Utils.getFileType;
import static com.terzulli.terzullifilemanager.utils.Utils.humanReadableByteCountSI;
import static com.terzulli.terzullifilemanager.utils.Utils.removeHiddenFilesFromArray;
import static com.terzulli.terzullifilemanager.utils.Utils.strFileApplication;
import static com.terzulli.terzullifilemanager.utils.Utils.strFileDirectory;
import static com.terzulli.terzullifilemanager.utils.Utils.strLocationAudioFriendlyName;
import static com.terzulli.terzullifilemanager.utils.Utils.strLocationDownloadsFriendlyName;
import static com.terzulli.terzullifilemanager.utils.Utils.strLocationImagesFriendlyName;
import static com.terzulli.terzullifilemanager.utils.Utils.strLocationInternalFriendlyName;
import static com.terzulli.terzullifilemanager.utils.Utils.strLocationRecentsFriendlyName;
import static com.terzulli.terzullifilemanager.utils.Utils.strLocationVideosFriendlyName;
import static com.terzulli.terzullifilemanager.utils.Utils.strSortByDate;
import static com.terzulli.terzullifilemanager.utils.Utils.strSortByName;
import static com.terzulli.terzullifilemanager.utils.Utils.strSortBySize;
import static com.terzulli.terzullifilemanager.utils.Utils.validateDirectoryName;
import static com.terzulli.terzullifilemanager.utils.Utils.validateGenericFileName;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.activities.MainActivity;
import com.terzulli.terzullifilemanager.adapters.ItemsAdapter;
import com.terzulli.terzullifilemanager.utils.RecentsFilesManager;
import com.terzulli.terzullifilemanager.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import moe.feng.common.view.breadcrumbs.BreadcrumbsView;
import moe.feng.common.view.breadcrumbs.DefaultBreadcrumbsCallback;
import moe.feng.common.view.breadcrumbs.model.BreadcrumbItem;
import moe.feng.common.view.breadcrumbs.model.IBreadcrumbItem;

public class MainFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int backPressedInterval = 2000;
    private static RecyclerView recyclerView;
    @SuppressLint("StaticFieldLeak")
    private static RelativeLayout copyMoveExtractBar;
    @SuppressLint("StaticFieldLeak")
    private static SwipeRefreshLayout swipeRefreshLayout;
    @SuppressLint("StaticFieldLeak")
    private static View view;
    private static String currentPath;
    private static String pathHome;
    private static String pathRoot;
    private static String pathHomeFriendlyName;
    private static ActionBar supportActionBar;
    private static BreadcrumbsView breadcrumbsView;
    private static String lastActionBarTitle;
    private static long timeBackPressed;
    @SuppressLint("StaticFieldLeak")
    private static Activity activityReference;
    private static SharedPreferences sharedPreferences;
    private static long activeLoadingsCounter = 0;

    public MainFragment() {
        // Required empty public constructor
    }

    public static void setActionBarTitle(String title) {
        lastActionBarTitle = title;

        if (supportActionBar != null)
            Objects.requireNonNull(supportActionBar).setTitle(title);
    }

    public static void initializeEmptyDirectoryLayout(boolean showEmptyLayout) {
        RelativeLayout itemsEmptyPlaceHolder = view.findViewById(R.id.items_empty_directory_placeholder);

        if (showEmptyLayout) {
            itemsEmptyPlaceHolder.setVisibility(View.VISIBLE);
        } else {
            itemsEmptyPlaceHolder.setVisibility(View.INVISIBLE);
        }
    }

    private static void displayEmptyLayoutWhileWaiting(boolean hideBreadcrumb) {
        swipeRefreshLayout.setRefreshing(true);

        RelativeLayout itemsEmptyPlaceHolder = view.findViewById(R.id.items_empty_directory_placeholder);
        itemsEmptyPlaceHolder.setVisibility(View.INVISIBLE);

        new Handler().postDelayed(() -> {
            if (isACustomLocationDisplayed())
                updateBreadCrumbList(null, null);
            if (hideBreadcrumb)
                breadcrumbsView.setVisibility(View.GONE);
        }, 10);
    }

    public static void loadSelection(final File[] filesAndDirs, String newActionBarTitle) {

        if (newActionBarTitle != null && newActionBarTitle.length() != 0)
            setActionBarTitle(newActionBarTitle);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        long loadingTicket = ++activeLoadingsCounter;

        executor.execute(() -> {
            String sortBy = sharedPreferences.getString("sortBy", strSortByName);
            boolean sortOrderAscending = sharedPreferences.getBoolean("sortOrderAscending", true);

            Utils.sortFileAndDirectoriesList(filesAndDirs, sortBy, sortOrderAscending);

            handler.post(() -> {
                // se nel frattempo l'utente ha scelto di caricare un'altra schermata,
                // annullo questo caricamento

                if (activeLoadingsCounter <= loadingTicket) {
                    if (isACustomLocationDisplayed()){
                        updateBreadCrumbList(null, null);
                        breadcrumbsView.setVisibility(View.GONE);
                    } else
                        breadcrumbsView.setVisibility(View.VISIBLE);

                    // se non ci sono file, imposto visibili gli elementi della schermata di default vuota
                    initializeEmptyDirectoryLayout(filesAndDirs == null || filesAndDirs.length == 0);

                    recyclerView.setAdapter(new ItemsAdapter(view.getContext(), filesAndDirs));
                    swipeRefreshLayout.setRefreshing(false);

                    ItemsAdapter.recoverEventuallyActiveCopyMoveOperation();
                    ItemsAdapter.recoverEventuallyActiveExtractOperation();
                    ItemsAdapter.recoverEventuallyActiveCompressOperation();
                }
            });
        });

    }

    public static void loadPath(final String path, boolean updateBreadcrumb, boolean reloadBreadCrumb) {

        ItemsAdapter.clearCurrentFilesBeforeQuerySubmit();

        if (isPathProtected(path)) {
            // stiamo tentando di accedere a file di root
            breadcrumbsView.setVisibility(View.VISIBLE);
            updateBreadCrumbList(currentPath, null);
            Toast.makeText(view.getContext(), R.string.error_access_to_root_directory, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!ItemsAdapter.isSelectionModeEnabled()) {
            setActionBarTitle(getCurrentDirectoryName());
        } else {
            swipeRefreshLayout.setRefreshing(true);
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        long loadingTicket = ++activeLoadingsCounter;

        executor.execute(() -> {
            String oldPath = currentPath;
            currentPath = path;

            File rootFile = new File(path);
            File[] filesAndDirs = rootFile.listFiles();

            String sortBy = sharedPreferences.getString("sortBy", strSortByName);
            boolean sortOrderAscending = sharedPreferences.getBoolean("sortOrderAscending", true);

            Utils.sortFileAndDirectoriesList(filesAndDirs, sortBy, sortOrderAscending);

            // rimozione file nascosti (iniziano col .)
            if (!sharedPreferences.getBoolean("showHidden", false))
                filesAndDirs = removeHiddenFilesFromArray(filesAndDirs);

            File[] finalFilesAndDirs = filesAndDirs;
            handler.post(() -> {
                // se nel frattempo l'utente ha scelto di caricare un'altra schermata,
                // annullo questo caricamento

                if (activeLoadingsCounter <= loadingTicket) {
                    if (isACustomLocationDisplayed()){
                        updateBreadCrumbList(null, null);
                        breadcrumbsView.setVisibility(View.GONE);
                    } else
                        breadcrumbsView.setVisibility(View.VISIBLE);

                    // se non ci sono file, imposto visibili gli elementi della schermata di default vuota
                    initializeEmptyDirectoryLayout(finalFilesAndDirs == null || finalFilesAndDirs.length == 0);

                    recyclerView.setAdapter(new ItemsAdapter(view.getContext(), finalFilesAndDirs));

                    if (updateBreadcrumb)
                        updateBreadCrumbList(path, oldPath);
                    if (reloadBreadCrumb)
                        reloadBreadCrumb(path);

                    swipeRefreshLayout.setRefreshing(false);

                    ItemsAdapter.recoverEventuallyActiveCopyMoveOperation();
                    ItemsAdapter.recoverEventuallyActiveExtractOperation();
                    ItemsAdapter.recoverEventuallyActiveCompressOperation();
                }
            });
        });

    }

    public static void resetActionBarTitle() {
        setActionBarTitle(getCurrentDirectoryName());
    }

    private static void reloadBreadCrumb(String newPath) {
        if (breadcrumbsView != null) {
            emptyBreadcrumb();

            String[] pathArr = newPath.split("/");
            if (pathArr.length > 0) {
                for (int i = 1; i < pathArr.length; i++)
                    breadcrumbsView.addItem(new BreadcrumbItem(Collections.singletonList(pathArr[i])));
            }
        }
    }

    private static void updateBreadCrumbList(String newPath, String oldPath) {
        if (newPath == null || newPath.length() == 0) {
            List<String> pathList = Collections.singletonList(pathHome);
            breadcrumbsView.addItem(new BreadcrumbItem(pathList));
        } else {

            if (oldPath == null || oldPath.length() == 0 || breadcrumbsView.getItems().size() == 0) {// || (newPath.split("/").length != breadcrumbsView.getItems().size())) {
                reloadBreadCrumb(newPath);
            } else {

                if (!newPath.equals(oldPath)) {
                    if (newPath.contains(oldPath)) {
                        // se il nuovo path è una cartella "successiva" al vecchio, aggiungo solo la parte nuova del path
                        //String difference = newPath.split(oldPath)[1];
                        String difference = newPath.substring(oldPath.length());


                        String[] pathArr = difference.split("/");
                        if (pathArr.length > 0) {
                            for (int i = 1; i < pathArr.length; i++)
                                breadcrumbsView.addItem(new BreadcrumbItem(Collections.singletonList(pathArr[i])));
                        }
                    } else if (oldPath.contains(newPath)) {
                        // se il nuovo path è una cartella "superiore" al vecchio, rimuovo parte del path
                        //String difference = oldPath.split(newPath)[1];
                        String difference = oldPath.substring(newPath.length());

                        String[] pathArr = difference.split("/");
                        if (pathArr.length > 0) {
                            for (int i = 1; i < pathArr.length; i++)
                                breadcrumbsView.removeLastItem();
                        }
                    }
                }
            }
        }
    }

    private static void emptyBreadcrumb() {
        if (breadcrumbsView != null) {
            // clean breadcrumb
            List<IBreadcrumbItem> currentItemsList = breadcrumbsView.getItems();
            int listSize = currentItemsList.size();

            for (int i = 0; i < listSize; i++) {
                breadcrumbsView.removeLastItem();
            }
        }
    }

    private static boolean isPathProtected(String path) {
        return (!pathRoot.equals(path) && pathRoot.contains(path));
    }

    private static String getCurrentDirectoryName() {
        if (isInHomePath()) {
            return pathHomeFriendlyName;
        }

        if (currentPath == null || currentPath.length() == 0 || currentPath.split("/").length <= 1) {
            return "";
        } else if (currentPath.split("/").length > 1)
            return currentPath.split("/")[currentPath.split("/").length - 1];

        return "";
    }

    public static void refreshList() {

        switch (pathHomeFriendlyName) {
            case strLocationRecentsFriendlyName:
                displayRecentsFiles();
                break;
            case strLocationAudioFriendlyName:
                displayAudioFiles();
                break;
            case strLocationVideosFriendlyName:
                displayVideosFiles();
                break;
            case strLocationImagesFriendlyName:
                displayImagesFiles();
                break;
            /*case strLocationDownloadsFriendlyName:
                loadPath(currentPath, true);
                break;
            case strLocationInternalFriendlyName:
                loadPath(currentPath, true);
                break;*/
            default:
                loadPath(currentPath, true, false);
                break;
        }
    }

    public static String getParentPath() {
        // se siamo già nella root
        if (Objects.equals(currentPath, pathHome))
            return pathHome;

        File file = new File(currentPath);

        return file.getParent();
    }

    public static String getCurrentPath() {
        return currentPath;
    }

    public static String getInternalStoragePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    /*public static void setCurrentPath(String path) {
        currentPath = path;
    }*/

    public static boolean isInHomePath() {
        // se siamo già nella root
        return Objects.equals(currentPath, pathHome);
    }

    /*public static void setHomePath(String path) {
        pathHome = path;
    }*/

    public static boolean goBack() {

        if (isSelectionModeEnabled()) {
            clearSelection();
            //MainFragment.loadPath(MainFragment.getCurrentPath(), false);
            MainFragment.refreshList();
        } else {
            if (!MainFragment.isInHomePath()) {
                // se non siamo nella home, la gestione è quella classica nel tornare indietro nelle directory
                MainFragment.loadPath(MainFragment.getParentPath(), true, false);
            } else {
                if (timeBackPressed + backPressedInterval > System.currentTimeMillis())
                    return true; // la main activity deve invocare finish
                else {
                    timeBackPressed = System.currentTimeMillis();
                    Toast.makeText(view.getContext(), R.string.press_again_to_exit, Toast.LENGTH_SHORT).show();
                }
            }
        }

        return false;
    }

    private static void renameFile(File file, String newName) {
        if (file == null)
            return;

        File dir = file.getParentFile();
        if (dir != null && dir.exists()) {
            File from = new File(dir, file.getName());
            File to = new File(dir, newName);

            if (from.exists()) {
                from.renameTo(to);
                refreshList();
                ItemsAdapter.clearSelection();
            }
        }
    }

    public static void displayRenameDialog(File file) {
        if (file == null)
            return;

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(view.getContext());
        alertBuilder.setTitle(R.string.action_rename);

        final EditText editText = new EditText(view.getContext());
        editText.setText(file.getName());

        alertBuilder.setView(editText);

        alertBuilder.setPositiveButton(R.string.button_ok, (dialog, whichButton) -> renameFile(file, editText.getText().toString()));
        alertBuilder.setNegativeButton(R.string.button_cancel, (dialog, whichButton) -> {
        });

        AlertDialog alertDialog = alertBuilder.show();

        editText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                boolean alreadyExists = (new File(file.getParent(), s.toString()).exists());
                boolean isNameValid = validateGenericFileName(file, s.toString());

                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                if (button != null) {
                    button.setEnabled(isNameValid && !alreadyExists);

                    if (isNameValid && !alreadyExists)
                        editText.setTextColor(ContextCompat.getColor(view.getContext(), R.color.black));
                    else
                        editText.setTextColor(ContextCompat.getColor(view.getContext(), R.color.error_text_color));
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.toString().contains("\n")) {
                    String newName = editText.getText().toString().replace("\n", "");

                    if (validateGenericFileName(file, newName)) {
                        renameFile(file, newName);
                        alertDialog.cancel();
                    }
                }
            }
        });
    }

    private static void createDirectory(File currentDirectory, String newDirectoryName) {
        if (currentDirectory == null)
            return;

        File newDir = new File(currentDirectory, newDirectoryName);
        String originalName = newDirectoryName;
        int i, maxRetries = 10000;

        // gestione di omonimia, aggiunge " (i)" al nome (es. "Test (1)")
        for (i = 1; i < maxRetries; i++) {
            newDir = new File(currentDirectory, newDirectoryName);

            if (newDir.exists())
                newDirectoryName = originalName + " (" + i + ")";
            else
                break;
        }

        if (i == maxRetries) {
            Toast.makeText(view.getContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
        } else {
            if (!newDir.mkdirs())
                Toast.makeText(view.getContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
            else
                refreshList();
        }
    }

    public static void displayNewDirectoryDialog() {
        File currentDirectory = new File(currentPath);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(view.getContext());
        alertBuilder.setTitle(R.string.action_new_folder);

        final EditText editText = new EditText(view.getContext());
        editText.setText("");

        alertBuilder.setView(editText);

        alertBuilder.setPositiveButton(R.string.button_ok, (dialog, whichButton) -> createDirectory(currentDirectory, editText.getText().toString()));
        alertBuilder.setNegativeButton(R.string.button_cancel, (dialog, whichButton) -> {
        });

        AlertDialog alertDialog = alertBuilder.show();

        editText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                boolean isNameValid = validateDirectoryName(s.toString());

                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                if (button != null) {
                    button.setEnabled(isNameValid);

                    if (isNameValid)
                        editText.setTextColor(ContextCompat.getColor(view.getContext(), R.color.black));
                    else
                        editText.setTextColor(ContextCompat.getColor(view.getContext(), R.color.error_text_color));
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.toString().contains("\n")) {
                    String newName = editText.getText().toString().replace("\n", "");

                    if (validateDirectoryName(newName)) {
                        createDirectory(currentDirectory, newName);
                        alertDialog.cancel();
                    }
                }
            }
        });
    }


    /**
     * @param selectionType   tipologia di selezione:
     *                        - 1: singola directory
     *                        - 2: singolo file
     *                        - 3: multiple directory
     *                        - 4: multipli file
     *                        - 5: multipla generica
     * @param fileName        nome dell'eventuale file (solo per selezione singola)
     * @param selectedFilesQt numero di file selezionati
     */
    public static void displayDeleteSelectionDialog(int selectionType, String fileName, int selectedFilesQt) {
        if (fileName == null)
            return;

        String message;
        switch (selectionType) {
            case 1:
                message = view.getResources().getString(R.string.delete_single_dir_first_part) + " \"" + fileName +
                        "\" " + view.getResources().getString(R.string.delete_single_dir_second_part);
                break;
            case 2:
                message = view.getResources().getString(R.string.delete_single_file) + " \"" + fileName + "\"?";
                break;
            case 3:
                message = view.getResources().getString(R.string.delete_multiple_dirs_first_part) + " " + selectedFilesQt +
                        " " + view.getResources().getString(R.string.delete_multiple_dirs_second_part);
                break;
            case 4:
                message = view.getResources().getString(R.string.delete_multiple_files_first_part) + " " + selectedFilesQt +
                        " " + view.getResources().getString(R.string.delete_multiple_files_second_part);
                break;
            case 5:
                message = view.getResources().getString(R.string.delete_multiple_generic_first_part) + " " + selectedFilesQt +
                        " " + view.getResources().getString(R.string.delete_multiple_generic_second_part);
                break;
            default:
                return;
        }

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(view.getContext());
        alertBuilder.setMessage(message);

        alertBuilder.setPositiveButton(R.string.button_ok, (dialog, whichButton) -> executeDeleteOperationOnThread(selectedFilesQt));
        alertBuilder.setNegativeButton(R.string.button_cancel, (dialog, whichButton) -> {
        });

        alertBuilder.show();
    }

    private static void executeDeleteOperationOnThread(int selectedFilesQt) {
        activityReference.runOnUiThread(() -> {
            String toastMessage = view.getResources().getString(R.string.delete_toast_first_part) + " " + selectedFilesQt + " ";
            if (selectedFilesQt == 1)
                toastMessage += view.getResources().getString(R.string.delete_toast_single_second_part);
            else
                toastMessage += view.getResources().getString(R.string.delete_toast_multiple_second_part);

            Toast.makeText(view.getContext(), toastMessage, Toast.LENGTH_SHORT).show();
            ItemsAdapter.deleteSelectedFilesOperation(currentPath);
        });
    }

    public static void displayCopyMoveBar(boolean isCopy, int selectedItemsQt) {
        copyMoveExtractBar.setVisibility(View.VISIBLE);

        Button btnCancel = view.findViewById(R.id.items_copy_move_btn_cancel_operation);
        Button btnConfirm = view.findViewById(R.id.items_btn_confirm_operation);
        TextView txtOpDescr = view.findViewById(R.id.items_copy_move_operation_descr);

        String descr;

        if (isCopy)
            descr = view.getResources().getString(R.string.action_copy);
        else
            descr = view.getResources().getString(R.string.action_move);

        descr += " " + selectedItemsQt;

        if (selectedItemsQt == 1)
            descr += " " + view.getResources().getString(R.string.action_copy_move_item);
        else
            descr += " " + view.getResources().getString(R.string.action_copy_move_items);

        btnConfirm.setText(R.string.button_paste);
        txtOpDescr.setText(descr);

        btnCancel.setOnClickListener(view -> {
            if (ItemsAdapter.getOperationStartPath().equals(currentPath))
                ItemsAdapter.recoverSelectionFromCopyMove();
            else
                clearSelectionFromCopyMove();

            hideCopyMoveExtractBar();
            refreshList();
        });

        btnConfirm.setOnClickListener(view -> executeCopyMoveOperationOnThread(isCopy, currentPath));
    }

    public static void displayExtractToBar() {
        copyMoveExtractBar.setVisibility(View.VISIBLE);

        Button btnCancel = view.findViewById(R.id.items_copy_move_btn_cancel_operation);
        Button btnConfirm = view.findViewById(R.id.items_btn_confirm_operation);
        TextView txtOpDescr = view.findViewById(R.id.items_copy_move_operation_descr);

        btnConfirm.setText(R.string.button_extract);

        String descr = view.getResources().getString(R.string.action_extract_here);
        txtOpDescr.setText(descr);

        btnCancel.setOnClickListener(view -> {
            clearFileToExtractSelection();
            hideCopyMoveExtractBar();
            refreshList();
        });

        btnConfirm.setOnClickListener(view -> {
            hideCopyMoveExtractBar();
            executeExtractOperationOnThread(getCurrentPath());
        });
    }

    public static void displayCompressToBar(int selectedItemsQt) {
        copyMoveExtractBar.setVisibility(View.VISIBLE);

        Button btnCancel = view.findViewById(R.id.items_copy_move_btn_cancel_operation);
        Button btnConfirm = view.findViewById(R.id.items_btn_confirm_operation);
        TextView txtOpDescr = view.findViewById(R.id.items_copy_move_operation_descr);

        String descr = view.getResources().getString(R.string.action_compress) + " " + selectedItemsQt;

        if (selectedItemsQt == 1)
            descr += " " + view.getResources().getString(R.string.action_copy_move_item);
        else
            descr += " " + view.getResources().getString(R.string.action_copy_move_items);

        btnConfirm.setText(R.string.button_compress);
        txtOpDescr.setText(descr);

        btnCancel.setOnClickListener(view -> {
            if (ItemsAdapter.getOperationStartPath().equals(currentPath))
                ItemsAdapter.recoverSelectionFromCompress();
            else
                clearSelectionFromCompress();

            hideCopyMoveExtractBar();
            refreshList();
        });

        btnConfirm.setOnClickListener(view -> executeCompressOperationOnThread(currentPath));
    }

    public static void displaySortByDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(view.getContext());
        alertBuilder.setTitle(R.string.action_sort_by);

        AtomicReference<String> sortBy = new AtomicReference<>(sharedPreferences.getString("sortBy", strSortByName));
        SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();

        alertBuilder.setPositiveButton(R.string.sort_ascending, (dialog, which) -> {
            sharedPrefEditor.putString("sortBy", sortBy.get());
            sharedPrefEditor.putBoolean("sortOrderAscending", true);
            sharedPrefEditor.apply();

            refreshList();
        });

        alertBuilder.setNegativeButton(R.string.sort_descending, (dialog, which) -> {
            sharedPrefEditor.putString("sortBy", sortBy.get());
            sharedPrefEditor.putBoolean("sortOrderAscending", false);
            sharedPrefEditor.apply();

            refreshList();
        });

        String[] items = {activityReference.getResources().getString(R.string.sort_name),
                activityReference.getResources().getString(R.string.sort_size),
                activityReference.getResources().getString(R.string.sort_date_last_modified)};

        int checkedItem = 0;
        switch (sortBy.get()) {
            case strSortByName:
                break;
            case strSortBySize:
                checkedItem = 1;
                break;
            case strSortByDate:
                checkedItem = 2;
                break;
            default:
        }

        alertBuilder.setSingleChoiceItems(items, checkedItem, (dialog, which) -> {
            switch (which) {
                case 0:
                    sortBy.set(strSortByName);
                    break;
                case 1:
                    sortBy.set(strSortBySize);
                    break;
                case 2:
                    sortBy.set(strSortByDate);
                    break;
            }
        });

        alertBuilder.show();
    }

    public static void displayPropertiesDialog(File file) {

        // attributi file
        String fileType = getFileType(file);
        String fileLastModified = formatDateDetailsFull(new Date(file.lastModified()));
        String fileSize = humanReadableByteCountSI(file.length());
        String fileName = file.getName();

        String message = "<br><b>" + activityReference.getResources().getString(R.string.prop_name) + "</b>: " + fileName +
                "<br><br><b>" + activityReference.getResources().getString(R.string.prop_type) + "</b>: ";

        switch (fileType) {
            case strFileDirectory:
            case strFileApplication:
                break;
            default:
                message += MimeTypeMap.getFileExtensionFromUrl(String.valueOf(Uri.fromFile(file))).toUpperCase();
                break;
        }

        message += " " + fileType;

        if (!fileType.equals(strFileDirectory))
            message += "<br><br><b>" + activityReference.getResources().getString(R.string.sort_size) + "</b>: " + fileSize;

        message += "<br><br><b>" + activityReference.getResources().getString(R.string.sort_date_last_modified) + "</b>: " + fileLastModified;

        if (fileType.equals(strFileDirectory)) {
            int nItems = 0;
            File[] listFiles = file.listFiles();

            if (listFiles != null)
                nItems = listFiles.length;

            message += "<br><br><b>" + activityReference.getResources().getString(R.string.prop_n_items) + "</b>: " + nItems;
        }

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(view.getContext());
        alertBuilder.setTitle(R.string.prop_properties);
        alertBuilder.setMessage(Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY));

        alertBuilder.setPositiveButton(R.string.button_ok, (dialog, whichButton) -> {
            // non si fa niente
        });

        alertBuilder.show();
    }

    public static void hideCopyMoveExtractBar() {
        copyMoveExtractBar.setVisibility(View.GONE);
    }

    public static void loadPathDownload(boolean reloadBreadCrumb) {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        pathHomeFriendlyName = strLocationInternalFriendlyName;

        loadPath(path, false, reloadBreadCrumb);
        reloadBreadCrumb(path);
    }

    public static void loadPathInternal(boolean reloadBreadCrumb) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        pathHomeFriendlyName = strLocationInternalFriendlyName;

        loadPath(path, false, reloadBreadCrumb);
    }

    public static void displayVideosFiles() {
        currentPath = getInternalStoragePath();
        currentPath = getInternalStoragePath();

        displayEmptyLayoutWhileWaiting(true);
        ArrayList<File> searchedResults = new ArrayList<>();

        findVideosFiles(searchedResults, activityReference);

        File[] searchedResultsArr = new File[searchedResults.size()];
        int i = 0;
        for (File file : searchedResults)
            searchedResultsArr[i++] = file;

        pathHomeFriendlyName = strLocationVideosFriendlyName;
        loadSelection(searchedResultsArr, view.getResources().getString(R.string.drawer_menu_media_videos));
    }

    public static void displayAudioFiles() {
        currentPath = getInternalStoragePath();

        displayEmptyLayoutWhileWaiting(true);
        ArrayList<File> searchedResults = new ArrayList<>();

        findAudioFiles(searchedResults, activityReference);

        File[] searchedResultsArr = new File[searchedResults.size()];
        int i = 0;
        for (File file : searchedResults)
            searchedResultsArr[i++] = file;

        pathHomeFriendlyName = strLocationAudioFriendlyName;
        loadSelection(searchedResultsArr, view.getResources().getString(R.string.drawer_menu_media_audio));
    }

    public static void displayImagesFiles() {
        currentPath = getInternalStoragePath();

        displayEmptyLayoutWhileWaiting(true);
        ArrayList<File> searchedResults = new ArrayList<>();

        findImagesFiles(searchedResults, activityReference);

        File[] searchedResultsArr = new File[searchedResults.size()];
        int i = 0;
        for (File file : searchedResults)
            searchedResultsArr[i++] = file;

        pathHomeFriendlyName = strLocationImagesFriendlyName;
        loadSelection(searchedResultsArr, view.getResources().getString(R.string.drawer_menu_media_images));
    }

    public static void displayRecentsFiles() {
        currentPath = getInternalStoragePath();

        RecentsFilesManager recentsFilesManager = new RecentsFilesManager(sharedPreferences);
        ArrayList<File> recentsFiles = recentsFilesManager.getRecentsFilesList();

        File[] recentsFilesArr = new File[0];
        if (recentsFiles != null) {
            recentsFilesArr = new File[recentsFiles.size()];
            int i = 0;
            for (File file : recentsFiles)
                recentsFilesArr[i++] = file;
        }

        pathHomeFriendlyName = strLocationRecentsFriendlyName;
        loadSelection(recentsFilesArr, view.getResources().getString(R.string.drawer_menu_recent));
    }

    private static void findVideosFiles(ArrayList<File> fileList, Activity context) {
        ArrayList<String> urlList = getAllVideosUrls(context);

        for (String uri : urlList) {
            if (uri != null && uri.length() != 0)
                fileList.add(new File(uri));
        }
    }

    private static void findImagesFiles(ArrayList<File> fileList, Activity context) {
        ArrayList<String> urlList = getAllImagesUrls(context);

        for (String uri : urlList) {
            if (uri != null && uri.length() != 0)
                fileList.add(new File(uri));
        }
    }

    private static void findAudioFiles(ArrayList<File> fileList, Activity context) {
        ArrayList<String> urlList = getAllAudioUrls(context);

        for (String uri : urlList) {
            if (uri != null && uri.length() != 0)
                fileList.add(new File(uri));
        }
    }

    /**
     * Metodo che restituisce True se ci troviamo all'interno di una location "custom",
     * come la sezione "Images", "Audio" e "Recents". Queste schermate mostrano i risultati di una
     * query e non una location "canonica", e vanno gestiti in maniera diversa.
     *
     * @return True se ci troviamo all'interno di una location "custom", false altrimenti.
     */
    public static boolean isACustomLocationDisplayed() {
        return !pathHomeFriendlyName.equals(strLocationInternalFriendlyName)
                && !pathHomeFriendlyName.equals(strLocationDownloadsFriendlyName);
    }

    private static ArrayList<String> getAllImagesUrls(Activity context) {
        ArrayList<String> urlList;
        final String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};

        /*Cursor cursor = context.managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);*/

        Cursor cursor = context.managedQuery(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                MediaStore.Images.Media.DATA + " not like ? ",
                new String[]{MainFragment.getInternalStoragePath() + "/Android"}, null);

        urlList = new ArrayList<>();

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            int dataColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            urlList.add(cursor.getString(dataColumnIndex));

        }
        return urlList;
    }

    private static ArrayList<String> getAllVideosUrls(Activity context) {
        ArrayList<String> urlList;
        final String[] projection = {MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID};

        /*Cursor cursor = context.managedQuery(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);*/

        Cursor cursor = context.managedQuery(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection,
                MediaStore.Video.Media.DATA + " not like ? ",
                new String[]{MainFragment.getInternalStoragePath() + "/Android"}, null);

        urlList = new ArrayList<>();

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            int dataColumnIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
            urlList.add(cursor.getString(dataColumnIndex));

        }
        return urlList;
    }

    private static ArrayList<String> getAllAudioUrls(Activity context) {
        ArrayList<String> urlList;
        final String[] projection = {MediaStore.Audio.Media.DATA, MediaStore.Audio.Media._ID};

        /*Cursor cursor = context.managedQuery(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);*/

        Cursor cursor = context.managedQuery(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                MediaStore.Audio.Media.DATA + " not like ? ",
                new String[]{MainFragment.getInternalStoragePath() + "/Android"}, null);

        urlList = new ArrayList<>();

        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            int dataColumnIndex = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            urlList.add(cursor.getString(dataColumnIndex));

        }
        return urlList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);

        supportActionBar = ((MainActivity) requireActivity()).getSupportActionBar();
        swipeRefreshLayout = view.findViewById(R.id.fragment_main_swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.fragment_main_list_view);
        breadcrumbsView = view.findViewById(R.id.fragment_main_breadcrumbs);
        copyMoveExtractBar = view.findViewById(R.id.items_copy_move_bar);
        activityReference = requireActivity();
        sharedPreferences = activityReference.getSharedPreferences("TerzulliFileManager", MODE_PRIVATE);

        pathHome = Environment.getExternalStorageDirectory().getAbsolutePath();
        pathRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (pathHomeFriendlyName == null)
            pathHomeFriendlyName = strLocationInternalFriendlyName;

        if (currentPath == null)
            currentPath = pathHome;
        /*if (pathHomeFriendlyName == null)
            setPathRootFriendlyName(breadcrumbsView.getContext().getResources().getString(R.string.drawer_menu_storage_internal));*/
        lastActionBarTitle = "";

        // inizializzazione layoyt
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        swipeRefreshLayout.setOnRefreshListener(this);
        setActionBarTitle(getCurrentDirectoryName());
        copyMoveExtractBar.setVisibility(View.GONE);

        breadcrumbsView.setCallback(new DefaultBreadcrumbsCallback<BreadcrumbItem>() {
            @Override
            public void onNavigateBack(BreadcrumbItem item, int position) {
                clearSelection();
                loadPath(getSelectedBreadcrumbPath(position), false, false);
            }

            @Override
            public void onNavigateNewLocation(BreadcrumbItem newItem, int changedPosition) {
                clearSelection();
                loadPath(getSelectedBreadcrumbPath(changedPosition - 1) + "/" + newItem.getSelectedItem(), false, false);
            }
        });

        //loadPath(currentPath, true);
        refreshList();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (lastActionBarTitle.length() == 0)
            setActionBarTitle(getCurrentDirectoryName());
        else
            setActionBarTitle(lastActionBarTitle);

        activityReference = requireActivity();
        if (!isSearchActive())
            refreshList();
    }

    private String getSelectedBreadcrumbPath(int depth) {
        if (depth == -1)
            depth = breadcrumbsView.getItems().size() - 1;

        //StringBuilder sb = new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath());
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i <= depth; i++) {
            sb.append("/").append(breadcrumbsView.getItems().get(i).getSelectedItem());
        }
        return sb.toString();
    }

    @Override
    public void onRefresh() {
        refreshList();
    }
}