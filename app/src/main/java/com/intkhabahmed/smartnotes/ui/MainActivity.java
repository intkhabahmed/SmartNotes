package com.intkhabahmed.smartnotes.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.google.firebase.analytics.FirebaseAnalytics;
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
    private PublisherInterstitialAd mInterstitialAd;
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
                launchRespectiveDetailFragment(note);
            }
        }
    }

    private void setupInterstitialAd() {
        mInterstitialAd = new PublisherInterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.release_admob_interstitial_adunit_id));
        mInterstitialAd.loadAd(new PublisherAdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
            }
        });
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
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(MainActivity.this);
                Bundle params = new Bundle();
                params.putLong(AppConstants.APP_OPEN_TIME, System.currentTimeMillis());
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, params);
            }
        }, 200);
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
        if (isAdTime) {
            if (mInterstitialAd.isLoaded()) {
                mInterstitialAd.show();
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
}
