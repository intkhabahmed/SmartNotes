package com.intkhabahmed.smartnotes.fragments;

import android.arch.lifecycle.Observer;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.database.NoteRepository;
import com.intkhabahmed.smartnotes.models.ChecklistItem;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.ui.AddAndEditChecklist;
import com.intkhabahmed.smartnotes.utils.NoteUtils;
import com.intkhabahmed.smartnotes.utils.ViewUtils;

import java.util.List;

public class ChecklistNotesDetailFragment extends Fragment {
    private Note mNote;
    private int mNoteId;
    private static final String BUNDLE_DATA = "bundle-data";
    private TextView noteTitleTextView;
    private LinearLayout checklistContainer;
    private TextView noteCreatedDateTextView;
    private TextView noteModifiedDateTextView;
    private FloatingActionButton editButton;

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
        return inflater.inflate(R.layout.note_detail_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        noteTitleTextView = view.findViewById(R.id.tv_note_title);
        checklistContainer = view.findViewById(R.id.checklist_container);
        noteCreatedDateTextView = view.findViewById(R.id.tv_date_created);
        noteModifiedDateTextView = view.findViewById(R.id.tv_date_modified);
        editButton = view.findViewById(R.id.edit_note_button);
        setupNote();
    }

    private void setupNote() {
        NoteRepository.getInstance().getNoteById(mNoteId)
                .observe(this, new Observer<Note>() {
                    @Override
                    public void onChanged(@Nullable Note note) {
                        if (note != null) {
                            mNote = note;
                            if (mNote.getTrashed() == 1) {
                                setHasOptionsMenu(false);
                            }
                            checklistContainer.removeAllViews();
                            setupUI();
                        }
                    }
                });
    }

    private void setupUI() {
        checklistContainer.setVisibility(View.VISIBLE);
        noteTitleTextView.setText(mNote.getNoteTitle());
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                populateChecklistData();
            }
        }, 200);
        noteCreatedDateTextView.setText(NoteUtils.getFormattedTime(mNote.getDateCreated()));
        noteModifiedDateTextView.setText(NoteUtils.getFormattedTime(mNote.getDateModified()));
        if (mNote.getTrashed() == 1) {
            editButton.setVisibility(View.GONE);
        }
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AddAndEditChecklist.class);
                intent.putExtra(Intent.EXTRA_TEXT, mNote);
                startActivity(intent);
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
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
                    Toast.makeText(getActivity(), getString(R.string.moved_to_trash), Toast.LENGTH_LONG).show();
                    getActivity().getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            };
            ViewUtils.showDeleteConfirmationDialog(getContext(), deleteListener);
        }
        return super.onOptionsItemSelected(item);
    }

    public void populateChecklistData() {
        if (mNote != null) {
            List<ChecklistItem> checklistItems = new Gson().fromJson(mNote.getDescription(), new TypeToken<List<ChecklistItem>>() {
            }.getType());
            for (int i = 0; i < checklistItems.size(); i++) {
                TextView checklistItem = new TextView(getActivity());
                checklistItem.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                checklistItem.setTextSize(24);
                checklistItem.setTextColor(ViewUtils.getColorFromAttribute(getActivity(), R.attr.secondaryTextColor));
                checklistItem.setText(checklistItems.get(i).getTitle());
                if (checklistItems.get(i).isChecked()) {
                    checklistItem.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                }
                checklistContainer.addView(checklistItem);
            }
        }
    }
}
