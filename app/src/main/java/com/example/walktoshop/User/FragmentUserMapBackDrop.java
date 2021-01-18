package com.example.walktoshop.User;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;

import com.example.walktoshop.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class FragmentUserMapBackDrop extends Fragment {

    BottomSheetBehavior sheetBehavior;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View coordinatorLayout = (CoordinatorLayout)inflater.inflate(R.layout.fragment_map_backdrop, container, false);

        ImageView filterIcon = coordinatorLayout.findViewById(R.id.filterIcon);
        LinearLayout contentLayout = coordinatorLayout.findViewById(R.id.contentLayout);

        sheetBehavior = BottomSheetBehavior.from(contentLayout);
        sheetBehavior.setFitToContents(false);
        sheetBehavior.setHideable(false);//evita che il backdrop sia completamente oscurato
        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);//inizialmente il  backdrop parte esteso

        filterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFilters();
            }
        });
        return coordinatorLayout;
    }

    private void toggleFilters() {
        if (sheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior.setHideable(true);
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            sheetBehavior.setHideable(true);
            sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }
}

