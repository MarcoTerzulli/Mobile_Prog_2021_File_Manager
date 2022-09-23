package com.terzulli.terzullifilemanager.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
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

import moe.feng.common.view.breadcrumbs.BreadcrumbsView;
import moe.feng.common.view.breadcrumbs.DefaultBreadcrumbsCallback;
import moe.feng.common.view.breadcrumbs.model.BreadcrumbItem;
import moe.feng.common.view.breadcrumbs.model.IBreadcrumbItem;

public class MainFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static RecyclerView recyclerView;
    @SuppressLint("StaticFieldLeak")
    private static SwipeRefreshLayout swipeRefreshLayout;
    private static MainFragmentViewModel mainFragmentViewModel;
    @SuppressLint("StaticFieldLeak")
    private static View view;
    private static String currentPath;
    private static String sortBy;
    private static boolean ascending;
    private static String pathHome;
    private static ActionBar supportActionBar;
    private static BreadcrumbsView breadcrumbsView;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        sortBy = "NAME";
        ascending = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);

        // TODO inizializzazione breadcrumb

        supportActionBar = ((MainActivity) requireActivity()).getSupportActionBar();
        mainFragmentViewModel = new ViewModelProvider(this).get(MainFragmentViewModel.class);
        swipeRefreshLayout = view.findViewById(R.id.fragment_main_swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.fragment_main_list_view);
        breadcrumbsView = view.findViewById(R.id.fragment_main_breadcrumbs);


        pathHome = Environment.getExternalStorageDirectory().getAbsolutePath();
        //currentPath = pathHome;
        if (currentPath == null)
            currentPath = pathHome;
        loadPath(currentPath, true);

        // inizializzazione layoyt
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        swipeRefreshLayout.setOnRefreshListener(this);
        setActionBarTitle(getCurrentDirectoryName());

        breadcrumbsView.setCallback(new DefaultBreadcrumbsCallback<BreadcrumbItem>() {
            @Override
            public void onNavigateBack(BreadcrumbItem item, int position) {
                loadPath(getSelectedBreadcrumbPath(position), false);
                //updateBreadCrumbList(currentPath, null);
            }

            @Override
            public void onNavigateNewLocation(BreadcrumbItem newItem, int changedPosition) {
                loadPath(getSelectedBreadcrumbPath(changedPosition - 1) + "/" + newItem.getSelectedItem(), false);
                //updateBreadCrumbList(currentPath, null);
            }
        });

        /*if (savedInstanceState == null) {
            updateBreadCrumbList(pathHome);
            loadPath(pathHome);
        }*/

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        setActionBarTitle(getCurrentDirectoryName());
    }

    private static void setActionBarTitle(String title) {
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

        if (!pathHome.equals(path) && pathHome.contains(path)) {
            // stiamo tentando di accedere a file di root
            updateBreadCrumbList(currentPath, null);
            Toast.makeText(recyclerView.getContext(), R.string.error_access_to_root_folder, Toast.LENGTH_SHORT).show();
            return;
        }

        String oldPath = currentPath;
        currentPath = path;
        setActionBarTitle(getCurrentDirectoryName());

        swipeRefreshLayout.setRefreshing(true);

        new Handler().postDelayed(() -> {
            File rootFile = new File(path);
            File[] filesAndDirs = rootFile.listFiles();
            Utils.sortFileAndFoldersList(filesAndDirs, sortBy, ascending);

            // se non ci sono file, imposto visibili gli elementi della schermata di default vuota
            initializeEmptyDirectoryLayout(filesAndDirs == null || filesAndDirs.length == 0);

            recyclerView.setAdapter(new ItemsAdapter(view.getContext(), filesAndDirs));

            if (updateBreadcrumb)
                updateBreadCrumbList(path, oldPath);

            swipeRefreshLayout.setRefreshing(false);
        }, 10);


        /*ExecutorService executor = Executors.newSingleThreadExecutor();
        Runnable backgroundRunnable = () -> {
            File rootFile = new File(path);
            File[] filesAndDirs = rootFile.listFiles();
            Utils.sortFileAndFoldersList(filesAndDirs, sortBy, ascending);

            // se non ci sono file, imposto visibili gli elementi della schermata di default vuota
            initializeEmptyDirectoryLayout(filesAndDirs == null || filesAndDirs.length == 0);

            recyclerView.setAdapter(new ItemsAdapter(view.getContext(), filesAndDirs));
            swipeRefreshLayout.setRefreshing(false);
        };
        executor.execute(backgroundRunnable);*/
    }

    private String getSelectedBreadcrumbPath(int depth) {
        if (depth == -1) depth = breadcrumbsView.getItems().size() - 1;

        //StringBuilder sb = new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath());
        StringBuilder sb = new StringBuilder("");

        for (int i = 0; i <= depth; i++) {
            sb.append("/").append(breadcrumbsView.getItems().get(i).getSelectedItem());
        }
        return sb.toString();
    }

    private static void updateBreadCrumbList(String newPath, String oldPath) {
        if (oldPath == null || oldPath.length() == 0 || breadcrumbsView.getItems().size() == 0) {// || (newPath.split("/").length != breadcrumbsView.getItems().size())) {

            // clean breadcrumb
            List<IBreadcrumbItem> currentItemsList = breadcrumbsView.getItems();
            int listSize = currentItemsList.size();

            for (int i = 0; i < listSize; i++) {
                breadcrumbsView.removeLastItem();
            }

            if (newPath == null || newPath.length() == 0) {
                List<String> pathList = Collections.singletonList(recyclerView.getContext().getResources().getString(R.string.drawer_menu_storage_internal));
                breadcrumbsView.addItem(new BreadcrumbItem(pathList));
            } else {
                String[] pathArr = newPath.split("/");
                if (pathArr.length > 0) {
                    for (int i = 1; i < pathArr.length; i++)
                        breadcrumbsView.addItem(new BreadcrumbItem(Collections.singletonList(pathArr[i])));
                }
            }
        } else {
            if (newPath == null || newPath.length() == 0) {
                List<String> pathList = Collections.singletonList(recyclerView.getContext().getResources().getString(R.string.drawer_menu_storage_internal));
                breadcrumbsView.addItem(new BreadcrumbItem(pathList));
            } else {
                if (newPath.equals(oldPath)) {
                    // non si fa niente
                } else if (newPath.contains(oldPath)) {
                    // se il nuovo path è una cartella "successiva" al vecchio, aggiungo solo la parte nuova del path
                    String difference = newPath.split(oldPath)[1];

                    String[] pathArr = difference.split("/");
                    if (pathArr.length > 0) {
                        for (int i = 1; i < pathArr.length; i++)
                            breadcrumbsView.addItem(new BreadcrumbItem(Collections.singletonList(pathArr[i])));
                    }
                } else if (oldPath.contains(newPath)) {
                    // se il nuovo path è una cartella "superiore" al vecchio, rimuovo parte del path
                    String difference = oldPath.split(newPath)[1];
                    String[] pathArr = difference.split("/");
                    if (pathArr.length > 0) {
                        for (int i = 1; i < pathArr.length; i++)
                            breadcrumbsView.removeLastItem();
                    }
                }
            }
        }

    }

    private static String getCurrentDirectoryName() {
        if (isInHomePath()) {
            return recyclerView.getContext().getResources().getString(R.string.drawer_menu_storage_internal);
        }

        if (currentPath == null || currentPath.length() == 0 || currentPath.split("/").length <= 1) {
            return "";
        } else if (currentPath.split("/").length > 1)
            return currentPath.split("/")[currentPath.split("/").length - 1];

        return "";
    }

    public static void updateList() {
        loadPath(currentPath, true);
    }

    public static void setCurrentPath(String path) {
        currentPath = path;
    }

    public static String getParentPath() {
        // se siamo già nella root
        if (Objects.equals(currentPath, pathHome))
            return pathHome;

        File file = new File(currentPath);

        return file.getParent();
    }

    public static boolean isInHomePath() {
        // se siamo già nella root
        return Objects.equals(currentPath, pathHome);
    }

    public static void setHomePath(String path) {
        pathHome = path;
    }

    @Override
    public void onRefresh() {
        updateList();
    }
}