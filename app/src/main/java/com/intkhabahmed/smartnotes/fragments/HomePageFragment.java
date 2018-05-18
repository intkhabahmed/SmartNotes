package com.intkhabahmed.smartnotes.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.intkhabahmed.smartnotes.NotesFragmentPagerAdapter;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.notesdata.NotesContract;

public class HomePageFragment extends Fragment{

    public HomePageFragment() {
    }

    private NotesFragmentPagerAdapter mNotesFragmentPagerAdapter;
    private ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_page_layout, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressBar = view.findViewById(R.id.progress_bar);
        mNotesFragmentPagerAdapter = new NotesFragmentPagerAdapter(getChildFragmentManager(), getActivity());
        setHasOptionsMenu(true);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewPager viewPager = view.findViewById(R.id.view_pager);
                TabLayout tabLayout = view.findViewById(R.id.tab_layout);
                viewPager.setAdapter(mNotesFragmentPagerAdapter);
                tabLayout.setupWithViewPager(viewPager, true);
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        },100);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.main_menu, menu);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int subMenuOrder = PreferenceManager.getDefaultSharedPreferences(getActivity()).getInt(getString(R.string.sort_criteria_id), 4);
                menu.getItem(1).getSubMenu().getItem(subMenuOrder-1).setChecked(true);
            }
        }, 0);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.search_menu:
                getActivity().getSupportFragmentManager().beginTransaction()
                        .addToBackStack(HomePageFragment.class.getName()).replace(R.id.fragment_layout, new SearchFragment())
                        .commit();
                return true;
            case R.id.sort_date_created_acsending:
            case R.id.sort_date_created_descending:
            case R.id.sort_title_ascending:
            case R.id.sort_title_descending:
                if(item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                    changeSortCriteria(getCriteriaString(item.getOrder()), item.getOrder());
                }
                return true;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    private String getCriteriaString(int order){
        switch (order) {
            case 1:
                return NotesContract.NotesEntry.COLUMN_TITLE + " ASC";
            case 2:
                return NotesContract.NotesEntry.COLUMN_TITLE + " DESC";
            case 3:
                return NotesContract.NotesEntry.COLUMN_DATE_CREATED + " ASC";
            case 4:
                return NotesContract.NotesEntry.COLUMN_DATE_CREATED + " DESC";
            default:
                return null;
        }
    }

    private void changeSortCriteria(String criteria, int subMenuOrder){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putString(getString(R.string.sort_criteria), criteria);
        editor.putInt(getString(R.string.sort_criteria_id), subMenuOrder);
        editor.apply();
        mNotesFragmentPagerAdapter.notifyDataSetChanged();
    }
}
