package com.intkhabahmed.smartnotes;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.intkhabahmed.smartnotes.fragments.ChecklistFragment;
import com.intkhabahmed.smartnotes.fragments.ImageNotesFragment;
import com.intkhabahmed.smartnotes.fragments.SimpleNotesFragment;

/**
 * Created by INTKHAB on 24-03-2018.
 */

public class NotesFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    private String mSearchText;

    public NotesFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    public void setSearchText(String searchText) {
        mSearchText = searchText;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new SimpleNotesFragment();
            case 1:
                return new ChecklistFragment();
            case 2:
                return new ImageNotesFragment();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.simple_note);
            case 1:
                return mContext.getString(R.string.checklist);
            case 2:
                return mContext.getString(R.string.image_note);
            default:
                return null;
        }
    }

    @Override
    public int getItemPosition(Object object) {
        if(object instanceof SimpleNotesFragment){
            ((SimpleNotesFragment)object).updateSimpleNotesFragment(mSearchText);
        } else if(object instanceof ChecklistFragment){
            ((ChecklistFragment)object).updateCheckListFragment(mSearchText);
        } else if (object instanceof ImageNotesFragment){
            ((ImageNotesFragment)object).updateImageNotesFragment(mSearchText);
        }
        return super.getItemPosition(object);
    }
}
