package com.intkhabahmed.smartnotes.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.android.material.tabs.TabLayout;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.adapters.NotesFragmentPagerAdapter;
import com.intkhabahmed.smartnotes.databinding.HomePageLayoutBinding;
import com.intkhabahmed.smartnotes.ui.MainActivity;
import com.intkhabahmed.smartnotes.utils.AppConstants;
import com.intkhabahmed.smartnotes.utils.CurrentFragmentListener;
import com.intkhabahmed.smartnotes.utils.Global;

public class HomePageFragment extends Fragment {

    private NotesFragmentPagerAdapter mNotesFragmentPagerAdapter;
    private boolean isFirstViewPagerPage;
    private HomePageLayoutBinding mHomeBinding;
    private AdView bannerAdView;

    public HomePageFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mHomeBinding = DataBindingUtil.inflate(inflater, R.layout.home_page_layout, container, false);
        return mHomeBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNotesFragmentPagerAdapter = new NotesFragmentPagerAdapter(getChildFragmentManager(), getParentActivity());
        setHasOptionsMenu(true);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TabLayout tabLayout = mHomeBinding.tabLayout;
                mHomeBinding.viewPager.setAdapter(mNotesFragmentPagerAdapter);
                mHomeBinding.viewPager.setOffscreenPageLimit(2);
                tabLayout.setupWithViewPager(mHomeBinding.viewPager, true);
                mHomeBinding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
                mHomeBinding.progressBar.setVisibility(View.INVISIBLE);
            }
        }, 100);
        getParentActivity().setTitle(R.string.app_name);
        bannerAdView = new AdView(getParentActivity(), getString(R.string.banner_placement_id), AdSize.BANNER_HEIGHT_50);

        // Add the ad view to your activity layout
        mHomeBinding.adView.addView(bannerAdView);

        // Request an ad
        bannerAdView.loadAd();
    }

    @Override
    public void onResume() {
        super.onResume();
        CurrentFragmentListener listener = ((MainActivity) getParentActivity()).getCurrentFragmentListener();
        listener.setCurrentFragment(HomePageFragment.class.getSimpleName());
    }

    @Override
    public void onDestroy() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
            mHomeBinding.adView.removeView(bannerAdView);
        }
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull final Menu menu, @NonNull MenuInflater inflater) {
        getParentActivity().getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.getItem(1).getSubMenu().getItem(Global.getSortId() - 1).setChecked(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.search_menu:
                getParentActivity().getSupportFragmentManager().beginTransaction()
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
        return mHomeBinding.viewPager;
    }

    private FragmentActivity getParentActivity() {
        return getActivity();
    }
}
