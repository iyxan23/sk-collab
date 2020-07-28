package com.ihsan.sketch.collab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MenuBottomSheetDialogFragment extends BottomSheetDialogFragment {

    public static MenuBottomSheetDialogFragment newInstance() {
        return new MenuBottomSheetDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_menu, container,false);

        

        return view;
    }
}

