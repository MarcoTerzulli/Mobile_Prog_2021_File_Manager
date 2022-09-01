package com.terzulli.terzullifilemanager.ui.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.ui.activities.MainActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainFragment extends Fragment {

    private static RecyclerView recyclerView;
    private static List<File> fileList;
    File storage; // TODO verificare se serve
    static View view;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActionBarTitle();
        setHasOptionsMenu(true);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);

        // TODO inizializzazione breadcrumb



        Toast.makeText(getContext(), Environment.getExternalStorageDirectory().getAbsolutePath(), Toast.LENGTH_SHORT).show();

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



}