package com.terzulli.terzullifilemanager.ui.fragments.recents;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.ui.fragments.recents.RecentsViewModel;

public class RecentsFragment extends Fragment {

    private RecentsViewModel mViewModel;

    public static RecentsFragment newInstance() {
        return new RecentsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recents, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(com.terzulli.terzullifilemanager.ui.fragments.recents.RecentsViewModel.class);
        // TODO: Use the ViewModel
    }

}