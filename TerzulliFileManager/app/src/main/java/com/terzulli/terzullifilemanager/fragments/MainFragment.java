package com.terzulli.terzullifilemanager.fragments;

import static com.terzulli.terzullifilemanager.adapters.ItemsAdapter.clearSelection;
import static com.terzulli.terzullifilemanager.adapters.ItemsAdapter.isSelectionModeEnabled;
import static com.terzulli.terzullifilemanager.utils.Utils.validateDirectoryName;
import static com.terzulli.terzullifilemanager.utils.Utils.validateGenericFileName;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.activities.MainActivity;
import com.terzulli.terzullifilemanager.adapters.ItemsAdapter;
import com.terzulli.terzullifilemanager.fragments.data.MainFragmentViewModel;
import com.terzulli.terzullifilemanager.utils.Utils;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import moe.feng.common.view.breadcrumbs.BreadcrumbsView;
import moe.feng.common.view.breadcrumbs.DefaultBreadcrumbsCallback;
import moe.feng.common.view.breadcrumbs.model.BreadcrumbItem;
import moe.feng.common.view.breadcrumbs.model.IBreadcrumbItem;

public class MainFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int backPressedInterval = 2000;
    private static RecyclerView recyclerView;
    private static RelativeLayout copyMoveBar;
    @SuppressLint("StaticFieldLeak")
    private static SwipeRefreshLayout swipeRefreshLayout;
    private static MainFragmentViewModel mainFragmentViewModel;
    @SuppressLint("StaticFieldLeak")
    private static View view;
    private static String currentPath;
    private static String sortBy;
    private static boolean sortAscending;
    private static String pathHome;
    private static String pathRoot;
    private static String pathHomeFriendlyName;
    private static ActionBar supportActionBar;
    private static BreadcrumbsView breadcrumbsView;
    private static String lastActionBarTitle;
    private static long timeBackPressed;
    private static Activity activityReference;

    public MainFragment() {
        // Required empty public constructor
    }

    public static void setActionBarTitle(String title) {
        lastActionBarTitle = title;

        if (supportActionBar != null)
            Objects.requireNonNull(supportActionBar).setTitle(title);
    }

    public static void initializeEmptyDirectoryLayout(boolean showEmptyLayout) {
        RelativeLayout itemsEmptyPlaceHolder = view.findViewById(R.id.items_empty_folder_placeholder);

        if (showEmptyLayout) {
            itemsEmptyPlaceHolder.setVisibility(View.VISIBLE);
        } else {
            itemsEmptyPlaceHolder.setVisibility(View.INVISIBLE);
        }
    }

    public static void loadPath(final String path, boolean updateBreadcrumb) {
        if (mainFragmentViewModel == null) {
            return;
        }

        if (isPathProtected(path)) {
            // stiamo tentando di accedere a file di root
            updateBreadCrumbList(currentPath, null);
            Toast.makeText(view.getContext(), R.string.error_access_to_root_folder, Toast.LENGTH_SHORT).show();
            return;
        }

        String oldPath = currentPath;
        currentPath = path;

        if (!ItemsAdapter.isSelectionModeEnabled())
            setActionBarTitle(getCurrentDirectoryName());
        else
            swipeRefreshLayout.setRefreshing(true);

        new Handler().postDelayed(() -> {
            File rootFile = new File(path);
            File[] filesAndDirs = rootFile.listFiles();
            Utils.sortFileAndFoldersList(filesAndDirs, sortBy, sortAscending);

            // todo rimozione file nascosti se si è scelto di non visualizzarli (iniziano col .)
            // todo controllare nelle shared preferences cosa è stato scelto
            /*if (....)
                filesAndDirs = removeHiddenFilesFromArray(filesAndDirs);*/

            // se non ci sono file, imposto visibili gli elementi della schermata di default vuota
            initializeEmptyDirectoryLayout(filesAndDirs == null || filesAndDirs.length == 0);

            // TODO gestione apertura file zip
            boolean isCurrentDirAnArchive = false;

            recyclerView.setAdapter(new ItemsAdapter(view.getContext(), filesAndDirs, isCurrentDirAnArchive));

            if (updateBreadcrumb)
                updateBreadCrumbList(path, oldPath);

            swipeRefreshLayout.setRefreshing(false);
        }, 10);

    }

    public static void resetActionBarTitle() {
        setActionBarTitle(getCurrentDirectoryName());
    }

    private static void updateBreadCrumbList(String newPath, String oldPath) {
        if (newPath == null || newPath.length() == 0) {
            List<String> pathList = Collections.singletonList(pathHome);
            breadcrumbsView.addItem(new BreadcrumbItem(pathList));
        } else {

            if (oldPath == null || oldPath.length() == 0 || breadcrumbsView.getItems().size() == 0) {// || (newPath.split("/").length != breadcrumbsView.getItems().size())) {

                emptyBreadcrumb();

                String[] pathArr = newPath.split("/");
                if (pathArr.length > 0) {
                    for (int i = 1; i < pathArr.length; i++)
                        breadcrumbsView.addItem(new BreadcrumbItem(Collections.singletonList(pathArr[i])));
                }
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
        // clean breadcrumb
        List<IBreadcrumbItem> currentItemsList = breadcrumbsView.getItems();
        int listSize = currentItemsList.size();

        for (int i = 0; i < listSize; i++) {
            breadcrumbsView.removeLastItem();
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
        loadPath(currentPath, true);
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

    public static void setCurrentPath(String path) {
        currentPath = path;
    }

    public static boolean isInHomePath() {
        // se siamo già nella root
        return Objects.equals(currentPath, pathHome);
    }

    public static void setHomePath(String path) {
        pathHome = path;
    }

    public static boolean goBack() {

        if (isSelectionModeEnabled()) {
            clearSelection();
            MainFragment.loadPath(MainFragment.getCurrentPath(), false);
        } else {
            if (!MainFragment.isInHomePath()) {
                // se non siamo nella home, la gestione è quella classica nel tornare indietro nelle directory
                MainFragment.loadPath(MainFragment.getParentPath(), true);
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
        if(dir != null && dir.exists()){
            File from = new File(dir, file.getName());
            File to = new File(dir, newName);

            if(from.exists()) {
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
        alertBuilder.setNegativeButton(R.string.button_cancel, (dialog, whichButton) -> {});

        AlertDialog alertDialog = alertBuilder.show();

        editText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                boolean alreadyExists = (new File(file.getParent(), s.toString()).exists());
                boolean isNameValid = validateGenericFileName(file, s.toString());

                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                if (button != null) {
                    button.setEnabled(isNameValid && !alreadyExists);

                    if(isNameValid && !alreadyExists)
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

            if(newDir.exists())
                newDirectoryName = originalName + " (" + i + ")";
            else
                break;
        }

        if (i == maxRetries) {
            Toast.makeText(view.getContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
        } else {
            newDir.mkdirs();
            refreshList();
        }
    }

    public static void displayNewFolderDialog() {
        File currentDirectory = new File(currentPath);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(view.getContext());
        alertBuilder.setTitle(R.string.action_new_folder);

        final EditText editText = new EditText(view.getContext());
        editText.setText("");

        alertBuilder.setView(editText);

        alertBuilder.setPositiveButton(R.string.button_ok, (dialog, whichButton) -> createDirectory(currentDirectory, editText.getText().toString()));
        alertBuilder.setNegativeButton(R.string.button_cancel, (dialog, whichButton) -> {});

        AlertDialog alertDialog = alertBuilder.show();

        editText.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                boolean isNameValid = validateDirectoryName(s.toString());

                Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                if (button != null) {
                    button.setEnabled(isNameValid);

                    if(isNameValid)
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
     *
     * @param selectionType tipologia di selezione:
     *                      - 1: singola directory
     *                      - 2: singolo file
     *                      - 3: multiple directory
     *                      - 4: multipli file
     *                      - 5: multipla generica
     *
     * @param fileName nome dell'eventuale file (solo per selezione singola)
     * @param selectedFilesQt numero di file selezionati
     *
     */
    public static void displayDeleteSelectionDialog(int selectionType, String fileName, int selectedFilesQt) {
        if (fileName == null)
            return;

        String message = "";
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

        alertBuilder.setPositiveButton(R.string.button_ok, (dialog, whichButton) -> {
            new Handler().postDelayed(() -> {
                String toastMessage = view.getResources().getString(R.string.delete_toast_first_part) + " " + selectedFilesQt + " ";
                if (selectedFilesQt == 1)
                    toastMessage += view.getResources().getString(R.string.delete_toast_single_second_part);
                else
                    toastMessage += view.getResources().getString(R.string.delete_toast_multiple_second_part);

                Toast.makeText(view.getContext(), toastMessage, Toast.LENGTH_SHORT).show();
                ItemsAdapter.deleteSelectedFilesOperation();
            }, 10);
        });
        alertBuilder.setNegativeButton(R.string.button_cancel, (dialog, whichButton) -> {});

        alertBuilder.show();
    }

    public static void displayCopyMoveBar(boolean isCopy, int selectedItemsQt) {
        copyMoveBar.setVisibility(View.VISIBLE);

        Button btnCancel = view.findViewById(R.id.items_btn_cancel_operation);
        Button btnPaste = view.findViewById(R.id.items_btn_paste);
        TextView txtOpDescr = view.findViewById(R.id.items_operation_descr);

        String descr = "";

        if(isCopy)
            descr = view.getResources().getString(R.string.action_copy);
        else
            descr = view.getResources().getString(R.string.action_move);

        descr += " " + selectedItemsQt;

        if(selectedItemsQt == 1)
            descr += " " + view.getResources().getString(R.string.action_copy_move_item);
        else
            descr += " " + view.getResources().getString(R.string.action_copy_move_items);

        txtOpDescr.setText(descr);

        btnCancel.setOnClickListener(view -> {
            hideCopyMoveBar();
            ItemsAdapter.recoverSelectionFromCopyMove();
            refreshList();
        });

        btnPaste.setOnClickListener(view -> {
            executeCopyMoveOperationOnThread(isCopy);

            /*new Handler().postDelayed(() -> {
                ItemsAdapter.copyMoveSelectionOperation(isCopy, currentPath);
            }, 10);*/
        });
    }

    public static void displaySortByDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(view.getContext());
        alertBuilder.setTitle(R.string.action_sort_by);

        AtomicBoolean noItemCliked = new AtomicBoolean(true);

        alertBuilder.setPositiveButton(R.string.sort_ascending, (dialog, whichButton) -> {
            sortAscending = true;
            if (noItemCliked.get())
                sortBy = "NAME";
            refreshList();
        });
        alertBuilder.setNegativeButton(R.string.sort_descending, (dialog, whichButton) -> {
            sortAscending = false;
            if (noItemCliked.get())
                sortBy = "NAME";
            refreshList();
        });

        String[] items = {recyclerView.getContext().getResources().getString(R.string.sort_name),
                recyclerView.getContext().getResources().getString(R.string.sort_size),
                recyclerView.getContext().getResources().getString(R.string.sort_date_last_modified)};
        int checkedItem = 0;
        alertBuilder.setSingleChoiceItems(items, checkedItem, (dialog, which) -> {
            switch (which) {
                case 0:
                    sortBy = "NAME";
                    noItemCliked.set(false);
                    break;
                case 1:
                    sortBy = "SIZE";
                    noItemCliked.set(false);
                    break;
                case 2:
                    sortBy = "DATE";
                    noItemCliked.set(false);
                    break;
            }
        });

        alertBuilder.show();
    }

    private static void executeCopyMoveOperationOnThread(boolean isCopy) {
        activityReference.runOnUiThread(() -> {
            ItemsAdapter.copyMoveSelectionOperation(isCopy, currentPath, ItemsAdapter.getSelectedFilestoCopyMove());
        });
    }

    public static void hideCopyMoveBar() {
        copyMoveBar.setVisibility(View.GONE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        sortBy = "NAME";
        sortAscending = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);

        supportActionBar = ((MainActivity) requireActivity()).getSupportActionBar();
        mainFragmentViewModel = new ViewModelProvider(this).get(MainFragmentViewModel.class);
        swipeRefreshLayout = view.findViewById(R.id.fragment_main_swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.fragment_main_list_view);
        breadcrumbsView = view.findViewById(R.id.fragment_main_breadcrumbs);
        copyMoveBar = view.findViewById(R.id.items_copy_move_bar);
        activityReference = requireActivity();

        pathHome = Environment.getExternalStorageDirectory().getAbsolutePath();
        pathRoot = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (currentPath == null)
            currentPath = pathHome;
        if (pathHomeFriendlyName == null)
            setPathRootFriendlyName(getContext().getResources().getString(R.string.drawer_menu_storage_internal));
        lastActionBarTitle = "";

        // inizializzazione layoyt
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        swipeRefreshLayout.setOnRefreshListener(this);
        setActionBarTitle(getCurrentDirectoryName());
        copyMoveBar.setVisibility(View.GONE);

        breadcrumbsView.setCallback(new DefaultBreadcrumbsCallback<BreadcrumbItem>() {
            @Override
            public void onNavigateBack(BreadcrumbItem item, int position) {
                clearSelection();
                loadPath(getSelectedBreadcrumbPath(position), false);
                //updateBreadCrumbList(currentPath, null);
            }

            @Override
            public void onNavigateNewLocation(BreadcrumbItem newItem, int changedPosition) {
                clearSelection();
                loadPath(getSelectedBreadcrumbPath(changedPosition - 1) + "/" + newItem.getSelectedItem(), false);
                //updateBreadCrumbList(currentPath, null);
            }
        });

        /*if (savedInstanceState == null) {
            updateBreadCrumbList(pathHome);
            loadPath(pathHome);
        }*/

        loadPath(currentPath, true);

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

    public void setPathRootFriendlyName(String friendlyName) {
        pathHomeFriendlyName = friendlyName;
    }

    @Override
    public void onRefresh() {
        refreshList();
    }
}