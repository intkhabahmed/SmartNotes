package com.intkhabahmed.smartnotes.ui

import android.content.Intent
import android.content.res.Resources.Theme
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.facebook.ads.InterstitialAd
import com.google.android.material.navigation.NavigationView
import com.intkhabahmed.smartnotes.R
import com.intkhabahmed.smartnotes.databinding.ActivityMainBinding
import com.intkhabahmed.smartnotes.fragments.AboutFragment
import com.intkhabahmed.smartnotes.fragments.ChecklistNotesDetailFragment
import com.intkhabahmed.smartnotes.fragments.HelpFragment
import com.intkhabahmed.smartnotes.fragments.HomePageFragment
import com.intkhabahmed.smartnotes.fragments.ImageNotesDetailFragment
import com.intkhabahmed.smartnotes.fragments.SettingsFragment
import com.intkhabahmed.smartnotes.fragments.SimpleNotesDetailFragment
import com.intkhabahmed.smartnotes.fragments.TrashFragment
import com.intkhabahmed.smartnotes.models.Note
import com.intkhabahmed.smartnotes.utils.AppConstants
import com.intkhabahmed.smartnotes.utils.CurrentFragmentListener
import com.intkhabahmed.smartnotes.utils.Global

class MainActivity : AppCompatActivity(), CurrentFragmentListener {
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mToggle: ActionBarDrawerToggle
    private var mFragmentManager: FragmentManager? = null
    private var handler: Handler? = null
    private lateinit var navigationView: NavigationView
    private lateinit var mMainBinding: ActivityMainBinding
    private var mInterstitialAd: InterstitialAd? = null
    private var isAdTime = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mDrawerLayout = mMainBinding.drawerLayout
        mToggle = ActionBarDrawerToggle(this@MainActivity, mDrawerLayout, R.string.open, R.string.close)
        mDrawerLayout.addDrawerListener(mToggle)
        mToggle.syncState()
        val toolbar = mMainBinding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp)
        }
        mFragmentManager = supportFragmentManager
        handler = Handler()
        if (savedInstanceState == null) {
            mFragmentManager!!.beginTransaction()
                    .replace(R.id.fragment_layout, HomePageFragment(), HomePageFragment::class.java.simpleName)
                    .commit()
            val intent = intent
            if (intent != null && intent.hasExtra(AppConstants.NOTIFICATION_INTENT_EXTRA)) {
                val note: Note = intent.getParcelableExtra(AppConstants.NOTIFICATION_INTENT_EXTRA) as Note
                launchRespectiveDetailFragment(note)
            }
        }
    }

    private fun setupInterstitialAd() {
        mInterstitialAd = InterstitialAd(this, getString(R.string.interstitial_placement_id))
        mInterstitialAd!!.loadAd()
    }

    private fun launchRespectiveDetailFragment(note: Note) {
        val fragment: Fragment
        fragment = when (note.noteType) {
            getString(R.string.checklist) -> {
                val checklistNotesDetailFragment = ChecklistNotesDetailFragment()
                checklistNotesDetailFragment.setNoteId(note.noteId)
                checklistNotesDetailFragment
            }
            getString(R.string.image_note) -> {
                val imageNotesDetailFragment = ImageNotesDetailFragment()
                imageNotesDetailFragment.setNoteId(note.noteId)
                imageNotesDetailFragment
            }
            else -> {
                val simpleNotesDetailFragment = SimpleNotesDetailFragment()
                simpleNotesDetailFragment.setNoteId(note.noteId)
                simpleNotesDetailFragment
            }
        }
        supportFragmentManager.beginTransaction()
                .addToBackStack(null)
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_layout, fragment)
                .commit()
    }

    override fun onStart() {
        super.onStart()
        navigationView = mMainBinding.navigationView
        navigationView.menu.getItem(0).isChecked = true
        navigationView.setNavigationItemSelectedListener { item ->
            mDrawerLayout.closeDrawer(GravityCompat.START)
            handler!!.postDelayed({
                when (item.itemId) {
                    R.id.home_page -> {
                        item.isChecked = true
                        mFragmentManager?.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
                        mFragmentManager!!.beginTransaction()
                                .replace(R.id.fragment_layout, HomePageFragment(), HomePageFragment::class.java.simpleName)
                                .commit()
                    }
                    R.id.trash -> {
                        item.isChecked = true
                        mFragmentManager!!.beginTransaction()
                                .replace(R.id.fragment_layout, TrashFragment(), TrashFragment::class.java.simpleName)
                                .addToBackStack(null)
                                .commit()
                    }
                    R.id.settings -> {
                        item.isChecked = true
                        mFragmentManager!!.beginTransaction()
                                .replace(R.id.fragment_layout, SettingsFragment(), SettingsFragment::class.java.simpleName)
                                .addToBackStack(null)
                                .commit()
                    }
                    R.id.help -> {
                        item.isChecked = true
                        mFragmentManager!!.beginTransaction()
                                .replace(R.id.fragment_layout, HelpFragment(), HelpFragment::class.java.simpleName)
                                .addToBackStack(null)
                                .commit()
                    }
                    R.id.about -> {
                        item.isChecked = true
                        mFragmentManager!!.beginTransaction()
                                .replace(R.id.fragment_layout, AboutFragment(), AboutFragment::class.java.simpleName)
                                .addToBackStack(null)
                                .commit()
                    }
                    R.id.privacy_policy -> {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse(getString(R.string.privacy_policy_link))
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        }
                    }
                }
            }, 300)
            false
        }
        setupInterstitialAd()
    }

    val currentFragmentListener: CurrentFragmentListener
        get() = this

    override fun getTheme(): Theme {
        val theme = super.getTheme()
        val isDarkThemeEnabled = Global.getDarkThemeStatus()
        if (isDarkThemeEnabled) {
            theme.applyStyle(R.style.AppThemeDark, true)
        } else {
            theme.applyStyle(R.style.AppThemeLight, true)
        }
        return theme
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mToggle.onOptionsItemSelected(item)) {
            mDrawerLayout.openDrawer(GravityCompat.START)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START)
            return
        }
        val homePageFragment = mFragmentManager!!.findFragmentByTag(HomePageFragment::class.java.simpleName) as HomePageFragment?
        if (homePageFragment != null && homePageFragment.isVisible) {
            if (!homePageFragment.isFirstViewPagerPage) {
                homePageFragment.viewPager.currentItem = 0
                return
            }
            isAdTime = true
        }
        if (isAdTime && mInterstitialAd != null && mInterstitialAd!!.isAdLoaded) {
            mInterstitialAd!!.show()
        }
        super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(BUNDLE_EXTRA, 1)
    }

    override fun setCurrentFragment(fragmentName: String) {
        when (fragmentName) {
            HomePageFragment::class.java.simpleName -> {
                navigationView.menu.getItem(0).isChecked = true
                isAdTime = false
            }
            TrashFragment::class.java.simpleName -> {
                navigationView.menu.getItem(1).isChecked = true
            }
            SettingsFragment::class.java.simpleName -> {
                navigationView.menu.getItem(2).isChecked = true
            }
            HelpFragment::class.java.simpleName -> {
                navigationView.menu.getItem(3).isChecked = true
            }
            AboutFragment::class.java.simpleName -> {
                navigationView.menu.getItem(4).isChecked = true
            }
        }
    }

    override fun onDestroy() {
        if (mInterstitialAd != null) {
            mInterstitialAd!!.destroy()
        }
        super.onDestroy()
    }

    companion object {
        private const val BUNDLE_EXTRA = "bundle-extra"
    }
}