package com.intkhabahmed.smartnotes;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.intkhabahmed.smartnotes.notesdata.NotesContract;
import com.intkhabahmed.smartnotes.utils.BitmapUtils;
import com.intkhabahmed.smartnotes.utils.NotesDateUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Created by INTKHAB on 23-03-2018.
 */

public class NotesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context mContext;
    private Cursor mCursor;
    private OnItemClickListener mOnItemClickListener;
    private boolean mIsImageNote;

    public NotesAdapter(Context context, Cursor cursor, OnItemClickListener clickListener, boolean isImageNote) {
        mContext = context;
        mCursor = cursor;
        mOnItemClickListener = clickListener;
        mIsImageNote = isImageNote;
    }

    public interface OnItemClickListener {
        void onMenuItemClick(View view, long noteId);

        void onItemClick(int adapterPosition, Cursor cursor);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);
        switch (holder.getItemViewType()) {
            case 0:
                int trashed = mCursor.getInt(mCursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_TRASH));
                TextNotesViewHolder textNotesViewHolder = (TextNotesViewHolder) holder;
                String title = mCursor.getString(mCursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_TITLE));

                if(trashed == 0){
                    String noteType = mCursor.getString(mCursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_TYPE));
                    textNotesViewHolder.noteDescriptionTextView.setText("");
                    if (noteType.equals(mContext.getString(R.string.simple_note))) {
                        StringBuilder description = new StringBuilder(mCursor.getString(mCursor
                                .getColumnIndex(NotesContract.NotesEntry.COLUMN_DESCRIPTION)));
                        if (description.length() > 30) {
                            description.delete(30, description.length());
                            description.append(" ...");
                        }
                        textNotesViewHolder.noteDescriptionTextView.setText(description);
                    } else if (noteType.equals(mContext.getString(R.string.checklist))) {
                        String description = mCursor.getString(mCursor
                                .getColumnIndex(NotesContract.NotesEntry.COLUMN_DESCRIPTION));
                        try {
                            JSONObject checklistObjects = new JSONObject(description);
                            JSONArray jsonArrays = checklistObjects.getJSONArray(mContext.getString(R.string.checklist));
                            int noOfItems = jsonArrays.length() >= 2 ? 2 : 1;
                            for (int i = 0; i < noOfItems; i++) {
                                try {
                                    JSONObject jsonObject = jsonArrays.getJSONObject(i);
                                    String task = String.valueOf(jsonObject.get(AddAndEditChecklist.LIST_TITLE));
                                    boolean isCompleted = jsonObject.getBoolean(AddAndEditChecklist.IS_LIST_CHECKED);
                                    if (isCompleted) {
                                        textNotesViewHolder.noteDescriptionTextView.append(task);
                                        textNotesViewHolder.noteDescriptionTextView.append(" " +
                                                mContext.getString(R.string.checkmark_unicode));
                                    } else {
                                        textNotesViewHolder.noteDescriptionTextView.append(task);
                                    }
                                    textNotesViewHolder.noteDescriptionTextView.append("\n");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (jsonArrays.length() > 2) {
                                textNotesViewHolder.noteDescriptionTextView.append("...");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }else{
                    textNotesViewHolder.noteDescriptionTextView.setVisibility(View.GONE);
                    textNotesViewHolder.noteTitleTextView.setOnClickListener(null);
                    textNotesViewHolder.noteCreateDateTextView.setOnClickListener(null);
                }
                long time = mCursor.getLong(mCursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DATE_CREATED));
                textNotesViewHolder.noteTitleTextView.setText(title);
                textNotesViewHolder.noteCreateDateTextView.setText(NotesDateUtil.getFormattedTime(time, System.currentTimeMillis()));
                break;
            case 1:
                ImageNotesViewHolder imageNotesViewHolder = (ImageNotesViewHolder) holder;
                String imageNoteTitle = mCursor.getString(mCursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_TITLE));
                long imageNoteTime = mCursor.getLong(mCursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DATE_CREATED));
                String imagePath = mCursor.getString(mCursor.getColumnIndex(NotesContract.NotesEntry.COLUMN_DESCRIPTION));

                File imageFile = new File(imagePath);
                if(imageFile.exists()){
                    Glide.with(mContext).asDrawable().load(Uri.fromFile(imageFile)).into(imageNotesViewHolder.noteImageView);
                }
                imageNotesViewHolder.noteTitleTextView.setText(imageNoteTitle);
                imageNotesViewHolder.noteCreateDateTextView.setText(NotesDateUtil.getFormattedTime(imageNoteTime,
                        System.currentTimeMillis()));
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor == null || mCursor.getCount() == 0) return 0;
        return mCursor.getCount();
    }

    @Override
    public int getItemViewType(int position) {
        if (!mIsImageNote) {
            return 0;
        }
        return 1;
    }

    public void swapCursor(Cursor data) {
        if (data != null) {
            mCursor = data;
            notifyDataSetChanged();
        }
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
                mCursor.moveToPosition(getAdapterPosition());
                long noteId = mCursor.getLong(mCursor.getColumnIndex(NotesContract.NotesEntry._ID));
                mOnItemClickListener.onMenuItemClick(view, noteId);
            } else if (view instanceof TextView) {
                Cursor cursor = mCursor;
                mOnItemClickListener.onItemClick(getAdapterPosition(), cursor);
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
                mCursor.moveToPosition(getAdapterPosition());
                long noteId = mCursor.getLong(mCursor.getColumnIndex(NotesContract.NotesEntry._ID));
                mOnItemClickListener.onMenuItemClick(view, noteId);
            } else if (view instanceof TextView || view instanceof ImageView) {
                Cursor cursor = mCursor;
                mOnItemClickListener.onItemClick(getAdapterPosition(), cursor);
            }


        }

    }
}
