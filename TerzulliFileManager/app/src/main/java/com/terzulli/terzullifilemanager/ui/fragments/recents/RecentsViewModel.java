package com.terzulli.terzullifilemanager.ui.fragments.recents;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RecentsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public RecentsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}