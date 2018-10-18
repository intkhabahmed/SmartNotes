package com.intkhabahmed.smartnotes.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.view.View;

import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.ui.MainActivity;
import com.intkhabahmed.smartnotes.utils.CurrentFragmentListener;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public SettingsFragment() {
    }

    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_fragment);
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        mSharedPreferences = preferenceScreen.getSharedPreferences();
        int count = preferenceScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference preference = preferenceScreen.getPreference(i);
            if (!(preference instanceof SwitchPreferenceCompat)) {
                preference.setSummary(mSharedPreferences.getString(preference.getKey(),
                        getActivity().getString(R.string.default_font_size)));
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        if (!(preference instanceof SwitchPreferenceCompat)) {
            preference.setSummary(sharedPreferences.getString(preference.getKey(),
                    getActivity().getString(R.string.default_font_size)));
        } else {
            getActivity().recreate();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(R.string.settings);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        CurrentFragmentListener listener = ((MainActivity) getActivity()).getCurrentFragmentListener();
        listener.setCurrentFragment(SettingsFragment.class.getSimpleName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
