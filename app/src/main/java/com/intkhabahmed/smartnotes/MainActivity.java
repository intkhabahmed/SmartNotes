package com.intkhabahmed.smartnotes;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
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

import com.intkhabahmed.smartnotes.fragments.HomePageFragment;
import com.intkhabahmed.smartnotes.fragments.SettingsFragment;
import com.intkhabahmed.smartnotes.fragments.TrashFragment;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private FragmentManager mFragmentManager;
    private Handler handler;

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
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mFragmentManager.beginTransaction()
                            .replace(R.id.fragment_layout, new HomePageFragment())
                            .commit();
                }
            }, 300);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        NavigationView navigationView = findViewById(R.id.navigation_view);
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
                                        .replace(R.id.fragment_layout, new HomePageFragment())
                                        .commit();
                                break;
                            case R.id.trash:
                                item.setChecked(true);
                                mFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_layout, new TrashFragment())
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case R.id.settings:
                                item.setChecked(true);
                                mFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_layout, new SettingsFragment())
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

    @Override
    public Resources.Theme getTheme() {
        final Resources.Theme theme = super.getTheme();
        boolean isDarkThemeEnabled = PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                .getBoolean(getString(R.string.dark_theme_key), false);
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
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("bundle-extra", 1);
    }
}
