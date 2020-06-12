package com.intkhabahmed.smartnotes.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.databinding.NoteDetailLayoutBinding;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.ui.AddSimpleNote;
import com.intkhabahmed.smartnotes.utils.NoteUtils;
import com.intkhabahmed.smartnotes.utils.ViewUtils;
import com.intkhabahmed.smartnotes.viewmodels.NoteViewModel;
import com.intkhabahmed.smartnotes.viewmodels.NoteViewModelFactory;

public class SimpleNotesDetailFragment extends Fragment {

    private static final String BUNDLE_DATA = "bundle-data";
    private Note mNote;
    private int mNoteId;
    private NoteDetailLayoutBinding mDetailBinding;
    private AdView bannerAdView;


    public SimpleNotesDetailFragment() {
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
        NoteViewModel noteViewModel = new ViewModelProvider(this, factory).get(NoteViewModel.class);
        noteViewModel.getNote().observe(getViewLifecycleOwner(), new Observer<Note>() {
            @Override
            public void onChanged(@Nullable Note note) {
                if (note != null) {
                    mNote = note;
                    if (mNote.getTrashed() == 1) {
                        setHasOptionsMenu(false);
                    }
                    setupUI();
                }
            }
        });
    }

    private void setupUI() {
        mDetailBinding.tvNoteDescription.setVisibility(View.VISIBLE);
        mDetailBinding.tvNoteTitle.setText(mNote.getNoteTitle());
        mDetailBinding.tvNoteDescription.setText(mNote.getDescription());
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
                Intent intent = new Intent(getParentActivity(), AddSimpleNote.class);
                intent.putExtra(Intent.EXTRA_TEXT, mNote);
                startActivity(intent);
                getParentActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        bannerAdView = new AdView(getParentActivity(), getString(R.string.simple_detail_banner_placement_id), AdSize.BANNER_HEIGHT_50);

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
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
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
