package com.intkhabahmed.smartnotes.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intkhabahmed.smartnotes.R;
import com.intkhabahmed.smartnotes.models.ChecklistItem;
import com.intkhabahmed.smartnotes.models.Note;
import com.intkhabahmed.smartnotes.utils.NoteUtils;

import java.io.File;
import java.util.List;

/**
 * Created by INTKHAB on 23-03-2018.
 */

public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private List<Note> mNotes;
    private OnItemClickListener mOnItemClickListener;

    public NotesAdapter(Context context, OnItemClickListener clickListener) {
        mContext = context;
        mOnItemClickListener = clickListener;
    }

    public interface OnItemClickListener {
        void onMenuItemClick(View view, Note note);

        void onItemClick(int noteId, String noteType);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case 0:
                view = LayoutInflater.from(mContext).inflate(R.layout.note_item, parent, false);
                return new TextNotesViewHolder(view);
            case 1:
                view = LayoutInflater.from(mContext).inflate(R.layout.image_note_item, parent, false);
                return new ImageNotesViewHolder(view);
            default:
                throw new IllegalArgumentException("Unknown ViewType");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Note note = mNotes.get(holder.getAdapterPosition());
        switch (holder.getItemViewType()) {
            case 0:
                TextNotesViewHolder textNotesViewHolder = (TextNotesViewHolder) holder;
                if (note.getNoteType().equals(mContext.getString(R.string.simple_note))) {
                    textNotesViewHolder.noteDescriptionTextView.setText(note.getDescription());
                } else if (note.getNoteType().equals(mContext.getString(R.string.checklist))) {
                    List<ChecklistItem> checklistItems = new Gson().fromJson(note.getDescription(), new TypeToken<List<ChecklistItem>>() {
                    }.getType());
                    int noOfItems = checklistItems.size() >= 2 ? 2 : checklistItems.size();
                    textNotesViewHolder.noteDescriptionTextView.setText("");
                    for (int i = 0; i < noOfItems; i++) {
                        textNotesViewHolder.noteDescriptionTextView
                                .append(String.valueOf(checklistItems.get(i).getTitle()));
                        if (checklistItems.get(i).isChecked()) {
                            textNotesViewHolder.noteDescriptionTextView.append(" " +
                                    mContext.getString(R.string.checkmark_unicode));
                        }
                        if (i < noOfItems - 1) {
                            textNotesViewHolder.noteDescriptionTextView.append("\n");
                        }
                    }
                    if (checklistItems.size() > 2) {
                        textNotesViewHolder.noteDescriptionTextView.append(" ...");
                    }
                }
                textNotesViewHolder.noteTitleTextView.setText(note.getNoteTitle());
                textNotesViewHolder.noteCreateDateTextView.setText(NoteUtils.getFormattedTime(note.getDateCreated(),
                        System.currentTimeMillis()));
                break;
            case 1:
                ImageNotesViewHolder imageNotesViewHolder = (ImageNotesViewHolder) holder;
                File imageFile = new File(note.getDescription());
                if (imageFile.exists()) {
                    Glide.with(mContext).asDrawable().load(Uri.fromFile(imageFile)).into(imageNotesViewHolder.noteImageView);
                }
                imageNotesViewHolder.noteTitleTextView.setText(note.getNoteTitle());
                imageNotesViewHolder.noteCreateDateTextView.setText(NoteUtils.getFormattedTime(note.getDateCreated(),
                        System.currentTimeMillis()));
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (mNotes == null) {
            return 0;
        }
        return mNotes.size();
    }

    @Override
    public int getItemViewType(int position) {
        Note note = mNotes.get(position);
        if (note.getNoteType().equals(mContext.getString(R.string.simple_note))
                || note.getNoteType().equals(mContext.getString(R.string.checklist))) {
            return 0;
        }
        return 1;
    }

    public void setNotes(List<Note> notes) {
        mNotes = notes;
        notifyDataSetChanged();
    }

    public class TextNotesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView noteTitleTextView;
        TextView noteDescriptionTextView;
        TextView noteCreateDateTextView;
        ImageButton itemMenuButton;

        private TextNotesViewHolder(View itemView) {
            super(itemView);
            noteTitleTextView = itemView.findViewById(R.id.note_title);
            noteDescriptionTextView = itemView.findViewById(R.id.note_description);
            noteCreateDateTextView = itemView.findViewById(R.id.note_create_date);
            itemMenuButton = itemView.findViewById(R.id.item_menu_button);
            noteTitleTextView.setOnClickListener(this);
            noteDescriptionTextView.setOnClickListener(this);
            noteCreateDateTextView.setOnClickListener(this);
            itemMenuButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view instanceof ImageButton) {
                mOnItemClickListener.onMenuItemClick(view, mNotes.get(getAdapterPosition()));
            } else if (view instanceof TextView) {
                mOnItemClickListener.onItemClick(mNotes.get(getAdapterPosition()).getNoteId(), mNotes.get(getAdapterPosition()).getNoteType());
            }
        }
    }

    public class ImageNotesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView noteTitleTextView;
        ImageView noteImageView;
        TextView noteCreateDateTextView;
        ImageButton itemMenuButton;

        private ImageNotesViewHolder(View itemView) {
            super(itemView);
            noteTitleTextView = itemView.findViewById(R.id.tv_note_title);
            noteImageView = itemView.findViewById(R.id.note_image_view);
            noteCreateDateTextView = itemView.findViewById(R.id.tv_note_date_created);
            itemMenuButton = itemView.findViewById(R.id.note_menu_button);
            noteTitleTextView.setOnClickListener(this);
            noteImageView.setOnClickListener(this);
            noteCreateDateTextView.setOnClickListener(this);
            itemMenuButton.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (view instanceof ImageButton) {
                mOnItemClickListener.onMenuItemClick(view, mNotes.get(getAdapterPosition()));
            } else if (view instanceof TextView || view instanceof ImageView) {
                mOnItemClickListener.onItemClick(mNotes.get(getAdapterPosition()).getNoteId(), mNotes.get(getAdapterPosition()).getNoteType());
            }
        }
    }
}
