package com.intkhabahmed.smartnotes.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
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

import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.adapters.NotesFragmentPagerAdapter;
import com.intkhabahmed.smartnotes.ui.MainActivity;
import com.intkhabahmed.smartnotes.utils.AppConstants;
import com.intkhabahmed.smartnotes.utils.CurrentFragmentListener;
import com.intkhabahmed.smartnotes.utils.Global;

public class HomePageFragment extends Fragment {

    private ViewPager viewPager;

    public HomePageFragment() {
    }

    private NotesFragmentPagerAdapter mNotesFragmentPagerAdapter;
    private ProgressBar mProgressBar;
    private boolean isFirstViewPagerPage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_page_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressBar = view.findViewById(R.id.progress_bar);
        mNotesFragmentPagerAdapter = new NotesFragmentPagerAdapter(getChildFragmentManager(), getActivity());
        setHasOptionsMenu(true);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                viewPager = view.findViewById(R.id.view_pager);
                TabLayout tabLayout = view.findViewById(R.id.tab_layout);
                viewPager.setAdapter(mNotesFragmentPagerAdapter);
                viewPager.setOffscreenPageLimit(2);
                tabLayout.setupWithViewPager(viewPager, true);
                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                        isFirstViewPagerPage = position == 0;
                    }

                    @Override
                    public void onPageSelected(int position) {

                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        }, 100);
        getActivity().setTitle(R.string.app_name);
    }

    @Override
    public void onResume() {
        super.onResume();
        CurrentFragmentListener listener = ((MainActivity) getActivity()).getCurrentFragmentListener();
        listener.setCurrentFragment(HomePageFragment.class.getSimpleName());
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.getItem(1).getSubMenu().getItem(Global.getSortId() - 1).setChecked(true);
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
                if (item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                    Global.setSortCriteriaAndSortId(getCriteriaString(item.getOrder()), item.getOrder());
                    mNotesFragmentPagerAdapter.notifyDataSetChanged();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String getCriteriaString(int order) {
        switch (order) {
            case 1:
                return AppConstants.COLUMN_TITLE_ASC;
            case 2:
                return AppConstants.COLUMN_TITLE_DESC;
            case 3:
                return AppConstants.COLUMN_DATE_CREATED_ASC;
            case 4:
                return AppConstants.COLUMN_DATE_CREATED_DESC;
            default:
                return null;
        }
    }

    public boolean isFirstViewPagerPage() {
        return isFirstViewPagerPage;
    }

    public ViewPager getViewPager() {
        return viewPager;
    }
}
