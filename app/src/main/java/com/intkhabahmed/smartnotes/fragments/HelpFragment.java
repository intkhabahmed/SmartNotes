package com.intkhabahmed.smartnotes.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.ui.MainActivity;
import com.intkhabahmed.smartnotes.utils.CurrentFragmentListener;

public class HelpFragment extends Fragment {
    public HelpFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getParentActivity().setTitle(R.string.help);
    }

    @Override
    public void onResume() {
        super.onResume();
        CurrentFragmentListener listener = ((MainActivity) getParentActivity()).getCurrentFragmentListener();
        listener.setCurrentFragment(HelpFragment.class.getSimpleName());
    }

    private FragmentActivity getParentActivity() {
        return getActivity();
    }
}
