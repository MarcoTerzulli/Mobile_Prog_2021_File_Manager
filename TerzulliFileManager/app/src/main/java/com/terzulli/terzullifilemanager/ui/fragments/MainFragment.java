package com.terzulli.terzullifilemanager.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.ui.activities.MainActivity;
import com.terzulli.terzullifilemanager.ui.adapters.ItemsAdapter;
import com.terzulli.terzullifilemanager.ui.fragments.data.MainFragmentViewModel;
import com.terzulli.terzullifilemanager.utils.Utils;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class MainFragment extends Fragment {

    private static RecyclerView recyclerView;
    private View view;
    private SwipeRefreshLayout swipeRefreshLayout;
    private MainFragmentViewModel mainFragmentViewModel;
    private String sortBy;
    private boolean ascending;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarTitle();
        setHasOptionsMenu(true);

        sortBy = "NAME";
        ascending = true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);

        // TODO inizializzazione breadcrumb

        Toast.makeText(getContext(), Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();

        mainFragmentViewModel = new ViewModelProvider(this).get(MainFragmentViewModel.class);
        swipeRefreshLayout = view.findViewById(R.id.fragment_main_swipe_refresh_layout);
        recyclerView = view.findViewById(R.id.fragment_main_list_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        loadPath(Environment.getExternalStorageDirectory().getAbsolutePath());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        setActionBarTitle();
    }

    private void setActionBarTitle() {
        String title  = getString(R.string.drawer_menu_storage_internal);
        Objects.requireNonNull(((MainActivity) requireActivity()).getSupportActionBar()).setTitle(title);
    }

    public void initializeEmptyDirectoryLayout(boolean showEmptyLayout) {
        RelativeLayout itemsEmptyPlaceHolder = view.findViewById(R.id.items_empty_folder_placeholder);

        if (showEmptyLayout) {
            itemsEmptyPlaceHolder.setVisibility(View.VISIBLE);
        } else {
            itemsEmptyPlaceHolder.setVisibility(View.INVISIBLE);
        }
    }

    public void loadPath(final String path) {
        if (mainFragmentViewModel == null) {
            return;
        }

        swipeRefreshLayout.setRefreshing(true);

        // TODO caricamento elementi

        File rootFile = new File(path);
        File[] filesAndDirs = rootFile.listFiles();
        Utils.sortFileAndFoldersList(filesAndDirs, sortBy, true);

        if (filesAndDirs == null || filesAndDirs.length == 0) {
            initializeEmptyDirectoryLayout(true);
            return;
        }

        initializeEmptyDirectoryLayout(false);
        recyclerView.setAdapter(new ItemsAdapter(view.getContext(), filesAndDirs));



        swipeRefreshLayout.setRefreshing(false);
    }



}