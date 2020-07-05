package com.intkhabahmed.smartnotes.adapters

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.intkhabahmed.smartnotes.R
import com.intkhabahmed.smartnotes.fragments.ChecklistFragment
import com.intkhabahmed.smartnotes.fragments.ImageNotesFragment
import com.intkhabahmed.smartnotes.fragments.SimpleNotesFragment

/**
 * Created by INTKHAB on 24-03-2018.
 */
class NotesFragmentPagerAdapter(fm: FragmentManager?, private val mContext: Context) : FragmentPagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> SimpleNotesFragment()
            1 -> ChecklistFragment()
            2 -> ImageNotesFragment()
            else -> throw IllegalArgumentException("Unknown Fragment")
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> mContext.getString(R.string.simple_note)
            1 -> mContext.getString(R.string.checklist)
            2 -> mContext.getString(R.string.image_note)
            else -> null
        }
    }

    override fun getItemPosition(`object`: Any): Int {
        when (`object`) {
            is SimpleNotesFragment -> {
                `object`.updateSimpleNotesFragment()
            }
            is ChecklistFragment -> {
                `object`.updateCheckListFragment()
            }
            is ImageNotesFragment -> {
                `object`.updateImageNotesFragment()
            }
        }
        return super.getItemPosition(`object`)
    }

}