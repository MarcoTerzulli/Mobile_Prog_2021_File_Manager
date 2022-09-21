package com.terzulli.terzullifilemanager.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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
import java.util.Objects;

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

        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        swipeRefreshLayout.setOnRefreshListener(this);

        pathHome = Environment.getExternalStorageDirectory().getAbsolutePath();
        //currentPath = pathHome;
        if (currentPath == null)
            currentPath = pathHome;
        loadPath(currentPath);

        setActionBarTitle(getCurrentDirectoryName());

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

    public static void loadPath(final String path) {
        if (mainFragmentViewModel == null) {
            return;
        }

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
        loadPath(currentPath);
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