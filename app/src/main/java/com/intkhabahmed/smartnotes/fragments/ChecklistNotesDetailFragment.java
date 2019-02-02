package com.intkhabahmed.smartnotes.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.databinding.DataBindingUtil;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.CompoundButtonCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.databinding.NoteDetailLayoutBinding;
import com.intkhabahmed.smartnotes.models.ChecklistItem;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.services.NoteService;
import com.intkhabahmed.smartnotes.ui.AddAndEditChecklist;
import com.intkhabahmed.smartnotes.utils.AppExecutors;
import com.intkhabahmed.smartnotes.utils.NoteUtils;
import com.intkhabahmed.smartnotes.utils.ViewUtils;
import com.intkhabahmed.smartnotes.viewmodels.NoteViewModel;
import com.intkhabahmed.smartnotes.viewmodels.NoteViewModelFactory;

import java.util.List;
import java.util.TreeMap;

public class ChecklistNotesDetailFragment extends Fragment {
    private Note mNote;
    private int mNoteId;
    private static final String BUNDLE_DATA = "bundle-data";
    private NoteDetailLayoutBinding mDetailBinding;
    private TreeMap<String, ChecklistItem> mItems;
    private boolean isChecklistPressed;
    private AdView bannerAdView;

    public ChecklistNotesDetailFragment() {
    }

    public void setNoteId(int noteId) {
        mNoteId = noteId;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mNoteId = savedInstanceState.getInt(BUNDLE_DATA);
        }
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDetailBinding = DataBindingUtil.inflate(inflater, R.layout.note_detail_layout, container, false);
        return mDetailBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupNoteViewModel();
    }

    private void setupNoteViewModel() {
        NoteViewModelFactory factory = new NoteViewModelFactory(mNoteId);
        NoteViewModel noteViewModel = ViewModelProviders.of(this, factory).get(NoteViewModel.class);
        noteViewModel.getNote().observe(this, new Observer<Note>() {
            @Override
            public void onChanged(@Nullable Note note) {
                if (note != null) {
                    mNote = note;
                    if (mNote.getTrashed() == 1) {
                        setHasOptionsMenu(false);
                    }
                    if (!isChecklistPressed) {
                        mItems = new TreeMap<>();
                        mDetailBinding.checklistContainer.removeAllViews();
                        setupUI();
                    }
                }
            }
        });
    }

    private void setupUI() {
        mDetailBinding.checklistContainer.setVisibility(View.VISIBLE);
        mDetailBinding.tvNoteTitle.setText(mNote.getNoteTitle());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                populateChecklistData();
            }
        }, 200);
        mDetailBinding.tvDateCreated.setText(NoteUtils.getFormattedTime(mNote.getDateCreated()));
        mDetailBinding.tvDateModified.setText(mNote.getDateModified() != 0 ? NoteUtils.getFormattedTime(mNote.getDateModified()) : "-");
        mDetailBinding.tvNotification.setText(mNote.getReminderDateTime() != null ? NoteUtils.getFormattedTime
                (NoteUtils.getRelativeTimeFromNow(mNote.getReminderDateTime()) * 1000 + System.currentTimeMillis(),
                        System.currentTimeMillis()) : getString(R.string.notification_not_set));
        if (mNote.getTrashed() == 1) {
            mDetailBinding.editNoteButton.hide();
        }
        mDetailBinding.editNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getParentActivity(), AddAndEditChecklist.class);
                intent.putExtra(Intent.EXTRA_TEXT, mNote);
                startActivity(intent);
                getParentActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        bannerAdView = new AdView(getParentActivity(), getString(R.string.checklist_detail_banner_placement_id), AdSize.BANNER_HEIGHT_50);

        // Add the ad view to your activity layout
        mDetailBinding.adView2.addView(bannerAdView);

        // Request an ad
        bannerAdView.loadAd();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(BUNDLE_DATA, mNoteId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.detail_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.delete_menu) {
            DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    NoteRepository.getInstance().moveNoteToTrash(mNote);
                    Toast.makeText(getParentActivity(), getString(R.string.moved_to_trash), Toast.LENGTH_LONG).show();
                    getParentActivity().getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            };
            ViewUtils.showDeleteConfirmationDialog(getContext(), deleteListener, getString(R.string.delete_dialog_message));
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateChecklistData() {
        if (mNote != null) {
            List<ChecklistItem> checklistItems = new Gson().fromJson(mNote.getDescription(), new TypeToken<List<ChecklistItem>>() {
            }.getType());
            for (final ChecklistItem item : checklistItems) {
                final CheckBox checkBox = new CheckBox(getParentActivity());
                checkBox.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                checkBox.setText(item.getTitle());
                checkBox.setChecked(item.isChecked());
                if (item.isChecked()) {
                    checkBox.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                }
                mDetailBinding.checklistContainer.addView(checkBox);
                if (Build.VERSION.SDK_INT >= 23) {
                    checkBox.setTextAppearance(R.style.TextAppearance_AppCompat_Large);
                }
                ColorStateList colorStateList = new ColorStateList(
                        new int[][]{
                                new int[]{-android.R.attr.state_checked}, // unchecked
                                new int[]{android.R.attr.state_checked}, // checked
                        },
                        new int[]{
                                ViewUtils.getColorFromAttribute(getParentActivity(), R.attr.primaryTextColor),
                                ViewUtils.getColorFromAttribute(getParentActivity(), R.attr.colorAccent),
                        }
                );
                CompoundButtonCompat.setButtonTintList(checkBox, colorStateList);
                checkBox.setTextColor(ViewUtils.getColorFromAttribute(getParentActivity(), R.attr.primaryTextColor));
                mItems.put(item.getTitle(), new ChecklistItem(item.getTitle(), checkBox.isChecked()));
                if (mNote.getTrashed() == 0) {
                    checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                            if (b) {
                                checkBox.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                            } else {
                                checkBox.setPaintFlags(0);
                            }
                            isChecklistPressed = true;
                            mItems.get(item.getTitle()).setChecked(b);
                            mNote.setDescription(new Gson().toJson(mItems.values()));
                            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                @Override
                                public void run() {
                                    if (NoteRepository.getInstance().updateNote(mNote) > 0) {
                                        NoteService.startActionUpdateWidget(getParentActivity());
                                    }
                                }
                            });
                        }
                    });
                } else {
                    checkBox.setEnabled(false);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        if (bannerAdView != null) {
            bannerAdView.destroy();
            mDetailBinding.adView2.removeView(bannerAdView);
        }
        super.onDestroy();
    }

    private FragmentActivity getParentActivity() {
        return getActivity();
    }
}
