package com.terzulli.terzullifilemanager.ui.fragments.images;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.terzulli.terzullifilemanager.R;
import com.terzulli.terzullifilemanager.ui.fragments.images.ImagesViewModel;

public class ImagesFragment extends Fragment {

    private ImagesViewModel mViewModel;

    public static ImagesFragment newInstance() {
        return new ImagesFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_images, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(com.terzulli.terzullifilemanager.ui.fragments.images.ImagesViewModel.class);
        // TODO: Use the ViewModel
    }

}