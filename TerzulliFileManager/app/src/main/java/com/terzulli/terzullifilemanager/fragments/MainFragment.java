package com.terzulli.terzullifilemanager.fragments;

import android.app.Activity;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.activities.MainActivity;
import com.terzulli.terzullifilemanager.adapters.ItemsAdapter;
import com.terzulli.terzullifilemanager.fragments.data.MainFragmentViewModel;
import com.terzulli.terzullifilemanager.utils.Utils;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private static RecyclerView recyclerView;
    private static SwipeRefreshLayout swipeRefreshLayout;
    private static MainFragmentViewModel mainFragmentViewModel;
    private static View view;
    private static String currentPath;
    private static String sortBy;
    private static boolean ascending;
    private static String pathHome;

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

        //Toast.makeText(getContext(), Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();

        mainFragmentViewModel = new ViewModelProvider(this).get(MainFragmentViewModel.class);
        swipeRefreshLayout = view.findViewById(R.id.fragment_main_swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.fragment_main_list_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        swipeRefreshLayout.setOnRefreshListener(this);
        pathHome = Environment.getExternalStorageDirectory().getAbsolutePath();
        //currentPath = pathHome;
        if(currentPath == null)
            currentPath = pathHome;
        loadPath(currentPath);

        setActionBarTitle();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        setActionBarTitle();
    }

    private void setActionBarTitle() {
        String title = getString(R.string.drawer_menu_storage_internal);
        ActionBar supportActionBar = ((MainActivity) requireActivity()).getSupportActionBar();
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

        swipeRefreshLayout.setRefreshing(true);
        currentPath = path;

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Runnable backgroundRunnable = () -> {
            File rootFile = new File(path);
            File[] filesAndDirs = rootFile.listFiles();
            Utils.sortFileAndFoldersList(filesAndDirs, sortBy, ascending);

            initializeEmptyDirectoryLayout(filesAndDirs == null || filesAndDirs.length == 0);

            recyclerView.setAdapter(new ItemsAdapter(view.getContext(), filesAndDirs));

            swipeRefreshLayout.setRefreshing(false);
        };
        executor.execute(backgroundRunnable);








        /*File rootFile = new File(path);
        File[] filesAndDirs = rootFile.listFiles();
        Utils.sortFileAndFoldersList(filesAndDirs, sortBy, ascending);

        initializeEmptyDirectoryLayout(filesAndDirs == null || filesAndDirs.length == 0);

        // TODO gestione con thread
        recyclerView.setAdapter(new ItemsAdapter(view.getContext(), filesAndDirs));

        swipeRefreshLayout.setRefreshing(false);*/
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
        if (Objects.equals(currentPath, pathHome))
            return true;
        return false;
    }

    public static void setHomePath(String path) {
        pathHome = path;
    }

    @Override
    public void onRefresh() {
        updateList();
    }
}