package com.intkhabahmed.smartnotes;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.intkhabahmed.smartnotes.fragments.HomePageFragment;
import com.intkhabahmed.smartnotes.fragments.SettingsFragment;
import com.intkhabahmed.smartnotes.fragments.TrashFragment;

public class MainActivity extends AppCompatActivity {

    private static final String BUNDLE_EXTRA = "bundle-extra";
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private static int mFragmentNumber = 1;
    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean isDarkThemeEnabled = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(getString(R.string.dark_theme_key), false);
        if(isDarkThemeEnabled){
            this.setTheme(R.style.ActivityTheme_Primary_Base_Dark);
            getWindow().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.dark_theme_window_background));
        } else {
            this.setTheme(R.style.ActivityTheme_Primary_Base_Light);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();

        Toolbar toolbar = findViewById(R.id.toolbar);
        //toolbar.setTitleTextColor(getResources().getColor(R.color.iconFillColor));
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        }
        mFragmentManager = getSupportFragmentManager();
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.getMenu().getItem(0).setChecked(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.home_page:
                        actionBar.setTitle(getString(R.string.app_name));
                        item.setChecked(true);
                        HomePageFragment homePageFragment = new HomePageFragment();
                        mFragmentManager.beginTransaction()
                                .replace(R.id.fragment_layout, homePageFragment)
                                .commit();
                        break;
                    case R.id.trash:
                        actionBar.setTitle(getString(R.string.trash));
                        item.setChecked(true);
                        TrashFragment trashFragment = new TrashFragment();
                        mFragmentManager.beginTransaction()
                                .replace(R.id.fragment_layout, trashFragment)
                                .commit();
                        break;
                    case R.id.settings:
                        actionBar.setTitle(getString(R.string.settings));
                        item.setChecked(true);
                        SettingsFragment settingsFragment = new SettingsFragment();
                        mFragmentManager.beginTransaction()
                                .replace(R.id.fragment_layout, settingsFragment)
                                .commit();
                        break;

                }
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return false;
            }
        });
        if(savedInstanceState == null){
            HomePageFragment homePageFragment = new HomePageFragment();
            mFragmentManager.beginTransaction()
                    .replace(R.id.fragment_layout, homePageFragment)
                    .commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggle.onOptionsItemSelected(item)){
            mDrawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(BUNDLE_EXTRA, mFragmentNumber);
    }
}
