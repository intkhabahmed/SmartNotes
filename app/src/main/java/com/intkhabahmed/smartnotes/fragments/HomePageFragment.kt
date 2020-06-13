package com.intkhabahmed.smartnotes.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.intkhabahmed.smartnotes.R
import com.intkhabahmed.smartnotes.adapters.NotesFragmentPagerAdapter
import com.intkhabahmed.smartnotes.databinding.HomePageLayoutBinding
import com.intkhabahmed.smartnotes.ui.MainActivity
import com.intkhabahmed.smartnotes.utils.AppConstants
import com.intkhabahmed.smartnotes.utils.Global

class HomePageFragment : Fragment() {

    var isFirstViewPagerPage: Boolean = false

    private lateinit var mHomeBinding: HomePageLayoutBinding
    private lateinit var bannerAdView: AdView
    private lateinit var mNotesFragmentPagerAdapter: NotesFragmentPagerAdapter

    val viewPager: ViewPager
        get() = mHomeBinding.viewPager

    private val parentActivity: FragmentActivity?
        get() = activity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mHomeBinding = DataBindingUtil.inflate(inflater, R.layout.home_page_layout, container, false)
        return mHomeBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mNotesFragmentPagerAdapter = NotesFragmentPagerAdapter(childFragmentManager, parentActivity!!)
        setHasOptionsMenu(true)
        val handler = Handler()
        mHomeBinding.run {
            handler.postDelayed({
                viewPager.adapter = mNotesFragmentPagerAdapter
                viewPager.offscreenPageLimit = 2
                tabLayout.setupWithViewPager(mHomeBinding.viewPager, true)
                viewPager.addOnPageChangeListener(object : OnPageChangeListener {
                    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                        isFirstViewPagerPage = position == 0
                    }

                    override fun onPageSelected(position: Int) {}
                    override fun onPageScrollStateChanged(state: Int) {}
                })
                progressBar.visibility = View.INVISIBLE
            }, 100)
            parentActivity!!.setTitle(R.string.app_name)
            bannerAdView = AdView(parentActivity, getString(R.string.banner_placement_id), AdSize.BANNER_HEIGHT_50)

            bannerAdView.run {
                // Add the ad view to your activity layout
                adView.addView(this)

                // Request an ad
                loadAd()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val listener = (parentActivity as MainActivity?)!!.currentFragmentListener
        listener.setCurrentFragment(HomePageFragment::class.java.simpleName)
    }

    override fun onDestroy() {
        if (::bannerAdView.isInitialized) {
            bannerAdView.destroy()
            mHomeBinding.adView.removeView(bannerAdView)
        }
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        parentActivity!!.menuInflater.inflate(R.menu.main_menu, menu)
        menu.getItem(1).subMenu.getItem(Global.getSortId() - 1).isChecked = true
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search_menu -> {
                parentActivity!!.supportFragmentManager.beginTransaction()
                        .addToBackStack(HomePageFragment::class.java.name).replace(R.id.fragment_layout, SearchFragment())
                        .commit()
                true
            }
            R.id.sort_date_created_acsending, R.id.sort_date_created_descending, R.id.sort_title_ascending, R.id.sort_title_descending -> {
                if (item.isChecked) {
                    item.isChecked = false
                } else {
                    item.isChecked = true
                    Global.setSortCriteriaAndSortId(getCriteriaString(item.order), item.order)
                    mNotesFragmentPagerAdapter.notifyDataSetChanged()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getCriteriaString(order: Int): String? {
        return when (order) {
            1 -> AppConstants.COLUMN_TITLE_ASC
            2 -> AppConstants.COLUMN_TITLE_DESC
            3 -> AppConstants.COLUMN_DATE_CREATED_ASC
            4 -> AppConstants.COLUMN_DATE_CREATED_DESC
            else -> null
        }
    }
}