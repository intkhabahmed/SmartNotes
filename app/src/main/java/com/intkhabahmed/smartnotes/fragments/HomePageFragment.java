package com.intkhabahmed.smartnotes.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.constraint.Group;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.transition.ChangeBounds;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ProgressBar;

import com.intkhabahmed.smartnotes.AddAndEditChecklist;
import com.intkhabahmed.smartnotes.AddImageNote;
import com.intkhabahmed.smartnotes.AddSimpleNote;
import com.intkhabahmed.smartnotes.NotesFragmentPagerAdapter;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.notesdata.NotesContract;

public class HomePageFragment extends Fragment{

    public HomePageFragment() {
    }

    private NotesFragmentPagerAdapter mNotesFragmentPagerAdapter;
    private Group buttonSubMenu;
    private FloatingActionButton mAddButton;
    private boolean isSubmenuShown;
    private SharedPreferences mSharedPreferences;
    private ProgressBar mProgressBar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home_page_layout, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mProgressBar = view.findViewById(R.id.progress_bar);
        buttonSubMenu = view.findViewById(R.id.button_sub_menu);
        mAddButton = view.findViewById(R.id.add_button);
        mNotesFragmentPagerAdapter = new NotesFragmentPagerAdapter(getChildFragmentManager(), getActivity());
        setHasOptionsMenu(true);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ViewPager viewPager = view.findViewById(R.id.view_pager);
                TabLayout tabLayout = view.findViewById(R.id.tab_layout);
                viewPager.setAdapter(mNotesFragmentPagerAdapter);
                tabLayout.setupWithViewPager(viewPager, true);
                final ConstraintSet constraintSet2 = new ConstraintSet();
                constraintSet2.clone(getActivity(),R.layout.button_sub_menu_1);
                final ConstraintLayout constraintLayout = view.findViewById(R.id.button_constraint_layout);
                final ConstraintSet constraintSet1 = new ConstraintSet();
                constraintSet1.clone(constraintLayout);

                mAddButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Transition transition = new ChangeBounds();
                        transition.setInterpolator(new OvershootInterpolator());
                        TransitionManager.beginDelayedTransition(constraintLayout, transition);
                        if(!isSubmenuShown){
                            isSubmenuShown = true;
                            constraintSet2.applyTo(constraintLayout);
                            mAddButton.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_clear_24dp));
                            buttonSubMenu.setVisibility(View.VISIBLE);
                        } else {
                            isSubmenuShown = false;
                            constraintSet1.applyTo(constraintLayout);
                            buttonSubMenu.setVisibility(View.GONE);
                            mAddButton.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_add_black_24dp));
                        }
                    }
                });
                view.findViewById(R.id.add_simple_note_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), AddSimpleNote.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                });
                view.findViewById(R.id.add_checklist_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), AddAndEditChecklist.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                });
                view.findViewById(R.id.add_image_note_button).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(), AddImageNote.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                });
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        },100);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        isSubmenuShown = false;
        mAddButton.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.ic_add_black_24dp));
        buttonSubMenu.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.main_menu, menu);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int subMenuOrder = mSharedPreferences.getInt(getString(R.string.sort_criteria_id), 4);
                menu.getItem(1).getSubMenu().getItem(subMenuOrder-1).setChecked(true);
            }
        }, 0);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.search_menu:
                getActivity().getSupportFragmentManager().beginTransaction()
                        .addToBackStack(HomePageFragment.class.getName()).replace(R.id.fragment_layout, new SearchFragment())
                        .commit();
                return true;
            case R.id.sort_date_created_acsending:
            case R.id.sort_date_created_descending:
            case R.id.sort_title_ascending:
            case R.id.sort_title_descending:
                if(item.isChecked()) {
                    item.setChecked(false);
                } else {
                    item.setChecked(true);
                    changeSortCriteria(getCriteriaString(item.getOrder()), item.getOrder());
                }
                return true;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    private String getCriteriaString(int order){
        switch (order) {
            case 1:
                return NotesContract.NotesEntry.COLUMN_TITLE + " ASC";
            case 2:
                return NotesContract.NotesEntry.COLUMN_TITLE + " DESC";
            case 3:
                return NotesContract.NotesEntry.COLUMN_DATE_CREATED + " ASC";
            case 4:
                return NotesContract.NotesEntry.COLUMN_DATE_CREATED + " DESC";
            default:
                return null;
        }
    }

    private void changeSortCriteria(String criteria, int subMenuOrder){
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(getString(R.string.sort_criteria), criteria);
        editor.putInt(getString(R.string.sort_criteria_id), subMenuOrder);
        editor.apply();
        mNotesFragmentPagerAdapter.notifyDataSetChanged();
    }
}
