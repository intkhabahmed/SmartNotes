package com.intkhabahmed.smartnotes.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.facebook.ads.InterstitialAd;
import com.google.android.material.navigation.NavigationView;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.databinding.ActivityMainBinding;
import com.intkhabahmed.smartnotes.fragments.AboutFragment;
import com.intkhabahmed.smartnotes.fragments.ChecklistNotesDetailFragment;
import com.intkhabahmed.smartnotes.fragments.HelpFragment;
import com.intkhabahmed.smartnotes.fragments.HomePageFragment;
import com.intkhabahmed.smartnotes.fragments.ImageNotesDetailFragment;
import com.intkhabahmed.smartnotes.fragments.SettingsFragment;
import com.intkhabahmed.smartnotes.fragments.SimpleNotesDetailFragment;
import com.intkhabahmed.smartnotes.fragments.TrashFragment;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.utils.AppConstants;
import com.intkhabahmed.smartnotes.utils.CurrentFragmentListener;
import com.intkhabahmed.smartnotes.utils.Global;

public class MainActivity extends AppCompatActivity implements CurrentFragmentListener {

    private static final String BUNDLE_EXTRA = "bundle-extra";
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggle;
    private FragmentManager mFragmentManager;
    private Handler handler;
    private NavigationView navigationView;
    private ActivityMainBinding mMainBinding;
    private InterstitialAd mInterstitialAd;
    private boolean isAdTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mDrawerLayout = mMainBinding.drawerLayout;
        mToggle = new ActionBarDrawerToggle(MainActivity.this, mDrawerLayout, R.string.open, R.string.close);
        mDrawerLayout.addDrawerListener(mToggle);
        mToggle.syncState();
        Toolbar toolbar = mMainBinding.toolbar;
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
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra(AppConstants.NOTIFICATION_INTENT_EXTRA)) {
                Note note = intent.getParcelableExtra(AppConstants.NOTIFICATION_INTENT_EXTRA);
                if (note != null) {
                    launchRespectiveDetailFragment(note);
                }
            }
        }
    }

    private void setupInterstitialAd() {
        mInterstitialAd = new InterstitialAd(this, getString(R.string.interstitial_placement_id));
        mInterstitialAd.loadAd();
    }

    private void launchRespectiveDetailFragment(Note note) {
        Fragment fragment;
        if (note.getNoteType().equals(getString(R.string.checklist))) {
            ChecklistNotesDetailFragment checklistNotesDetailFragment = new ChecklistNotesDetailFragment();
            checklistNotesDetailFragment.setNoteId(note.getNoteId());
            fragment = checklistNotesDetailFragment;
        } else if (note.getNoteType().equals(getString(R.string.image_note))) {
            ImageNotesDetailFragment imageNotesDetailFragment = new ImageNotesDetailFragment();
            imageNotesDetailFragment.setNoteId(note.getNoteId());
            fragment = imageNotesDetailFragment;
        } else {
            SimpleNotesDetailFragment simpleNotesDetailFragment = new SimpleNotesDetailFragment();
            simpleNotesDetailFragment.setNoteId(note.getNoteId());
            fragment = simpleNotesDetailFragment;
        }
        getSupportFragmentManager().beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_layout, fragment)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        navigationView = mMainBinding.navigationView;
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
                            case R.id.help:
                                item.setChecked(true);
                                mFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_layout, new HelpFragment(), HelpFragment.class.getSimpleName())
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case R.id.about:
                                item.setChecked(true);
                                mFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_layout, new AboutFragment(), AboutFragment.class.getSimpleName())
                                        .addToBackStack(null)
                                        .commit();
                                break;
                            case R.id.privacy_policy:
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse(getString(R.string.privacy_policy_link)));
                                if (intent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(intent);
                                }
                        }
                    }
                }, 300);
                return false;
            }
        });
        setupInterstitialAd();
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
            isAdTime = true;
        }
        if (isAdTime && mInterstitialAd != null && mInterstitialAd.isAdLoaded()) {
            mInterstitialAd.show();
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
            isAdTime = false;
        } else if (fragmentName.equals(TrashFragment.class.getSimpleName())) {
            navigationView.getMenu().getItem(1).setChecked(true);
        } else if (fragmentName.equals(SettingsFragment.class.getSimpleName())) {
            navigationView.getMenu().getItem(2).setChecked(true);
        } else if (fragmentName.equals(HelpFragment.class.getSimpleName())) {
            navigationView.getMenu().getItem(3).setChecked(true);
        } else if (fragmentName.equals(AboutFragment.class.getSimpleName())) {
            navigationView.getMenu().getItem(4).setChecked(true);
        }
    }

    @Override
    protected void onDestroy() {
        if (mInterstitialAd != null) {
            mInterstitialAd.destroy();
        }
        super.onDestroy();
    }
}
