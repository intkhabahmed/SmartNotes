package com.intkhabahmed.smartnotes.ui;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.fragments.HomePageFragment;
import com.intkhabahmed.smartnotes.fragments.SettingsFragment;
import com.intkhabahmed.smartnotes.fragments.TrashFragment;
import com.intkhabahmed.smartnotes.utils.CurrentFragmentListener;
import com.intkhabahmed.smartnotes.utils.Global;

public class MainActivity extends AppCompatActivity implements CurrentFragmentListener {

    private static final String BUNDLE_EXTRA = "bundle-extra";
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private FragmentManager mFragmentManager;
    private Handler handler;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(MainActivity.this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        }
        mFragmentManager = getSupportFragmentManager();
        handler = new Handler();
        if (savedInstanceState == null) {
            mFragmentManager.beginTransaction()
                    .replace(R.id.fragment_layout, new HomePageFragment(), HomePageFragment.class.getSimpleName())
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        navigationView = findViewById(R.id.navigation_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int id = item.getItemId();

                        switch (id) {
                            case R.id.home_page:
                                item.setChecked(true);
                                mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                mFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_layout, new HomePageFragment(), HomePageFragment.class.getSimpleName())
                                        .commit();
                                break;
                            case R.id.trash:
                                item.setChecked(true);
                                mFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_layout, new TrashFragment(), TrashFragment.class.getSimpleName())
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case R.id.settings:
                                item.setChecked(true);
                                mFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_layout, new SettingsFragment(), SettingsFragment.class.getSimpleName())
                                        .addToBackStack(null)
                                        .commit();
                                break;
                        }
                    }
                }, 300);
                return false;
            }
        });
    }

    public CurrentFragmentListener getCurrentFragmentListener() {
        return this;
    }

    @Override
    public Resources.Theme getTheme() {
        final Resources.Theme theme = super.getTheme();
        boolean isDarkThemeEnabled = Global.getDarkThemeStatus();
        if (isDarkThemeEnabled) {
            theme.applyStyle(R.style.AppThemeDark, true);
        } else {
            theme.applyStyle(R.style.AppThemeLight, true);
        }
        return theme;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mToggle.onOptionsItemSelected(item)) {
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        HomePageFragment homePageFragment = (HomePageFragment) mFragmentManager.findFragmentByTag(HomePageFragment.class.getSimpleName());
        if (homePageFragment != null && homePageFragment.isVisible()) {
            if (!homePageFragment.isFirstViewPagerPage()) {
                homePageFragment.getViewPager().setCurrentItem(0);
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_EXTRA, 1);
    }

    @Override
    public void setCurrentFragment(String fragmentName) {
        if (fragmentName.equals(HomePageFragment.class.getSimpleName())) {
            navigationView.getMenu().getItem(0).setChecked(true);
        } else if (fragmentName.equals(TrashFragment.class.getSimpleName())) {
            navigationView.getMenu().getItem(1).setChecked(true);
        } else if (fragmentName.equals(SettingsFragment.class.getSimpleName())) {
            navigationView.getMenu().getItem(2).setChecked(true);
        }
    }
}
