package com.intkhabahmed.smartnotes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.intkhabahmed.smartnotes.BuildConfig;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.ui.MainActivity;
import com.intkhabahmed.smartnotes.utils.CurrentFragmentListener;

public class AboutFragment extends Fragment {
    public AboutFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getParentActivity().setTitle(R.string.about);
        TextView appVersion = view.findViewById(R.id.app_version);
        appVersion.setText(String.format("V. %s", BuildConfig.VERSION_NAME));
    }

    @Override
    public void onResume() {
        super.onResume();
        CurrentFragmentListener listener = ((MainActivity) getParentActivity()).getCurrentFragmentListener();
        listener.setCurrentFragment(AboutFragment.class.getSimpleName());
    }

    private FragmentActivity getParentActivity() {
        return getActivity();
    }
}
