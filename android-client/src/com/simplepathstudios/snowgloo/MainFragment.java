package com.simplepathstudios.snowgloo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.C;
import com.simplepathstudios.snowgloo.api.model.MusicFile;

import java.util.List;

public class MainFragment extends Fragment implements PlayerManager.QueueListener,LoaderManager.LoaderCallbacks<List<MusicFile>>{
    static final String TAG = "MainFragment";

    private RecyclerView mediaQueueList;
    private MediaQueueListAdapter mediaQueueListAdapter;
    private LinearLayoutManager layoutManager;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ItemTouchHelper helper = new ItemTouchHelper(new RecyclerViewCallback());
        mediaQueueList = getView().findViewById(R.id.music_queue);
        helper.attachToRecyclerView(mediaQueueList);
        layoutManager = new LinearLayoutManager(getActivity());
        mediaQueueList.setLayoutManager(layoutManager);
        mediaQueueListAdapter = new MediaQueueListAdapter();
        mediaQueueList.setAdapter(mediaQueueListAdapter);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG,"onStart");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "MainFragment initiated");
        return inflater.inflate(R.layout.main_fragment, container, false);
    }
    @NonNull
    @Override
    public Loader<List<MusicFile>> onCreateLoader(int id, Bundle args) {
        Log.d(TAG,"Loader created");
        return new SnowglooLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<MusicFile>> loader, List<MusicFile> data) {
        Log.d(TAG,"Load complete");
        for(MusicFile music : data){
            Log.d(TAG, music.LocalFilePath);
            ((MainActivity)getActivity()).getPlayerManager().addItem(music);
        }
        mediaQueueListAdapter.notifyItemInserted(data.size() - 1);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(mediaQueueList != null && mediaQueueListAdapter != null){
            mediaQueueList.setAdapter(mediaQueueListAdapter);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if(mediaQueueListAdapter != null){
            mediaQueueListAdapter.notifyItemRangeRemoved(0, mediaQueueListAdapter.getItemCount());
        }
        if(mediaQueueList != null){
            mediaQueueList.setAdapter(null);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<MusicFile>> loader) {
        Log.d(TAG,"Loader reset");
        ((MainActivity)getActivity()).getPlayerManager().release();
    }

    // PlayerManager.Listener implementation.

    @Override
    public void onQueuePositionChanged(int previousIndex, int newIndex) {
        if (previousIndex != C.INDEX_UNSET) {
            mediaQueueListAdapter.notifyItemChanged(previousIndex);
        }
        if (newIndex != C.INDEX_UNSET) {
            mediaQueueListAdapter.notifyItemChanged(newIndex);
        }
    }

    private class QueueItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView textView;
        public MusicFile item;

        public QueueItemViewHolder(TextView textView) {
            super(textView);
            this.textView = textView;
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ((MainActivity)getActivity()).getPlayerManager().selectQueueItem(getAdapterPosition());
        }
    }

    private class RecyclerViewCallback extends ItemTouchHelper.SimpleCallback {

        private int draggingFromPosition;
        private int draggingToPosition;

        public RecyclerViewCallback() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END);
            draggingFromPosition = C.INDEX_UNSET;
            draggingToPosition = C.INDEX_UNSET;
        }

        @Override
        public boolean onMove(RecyclerView list, RecyclerView.ViewHolder origin,
                              RecyclerView.ViewHolder target) {
            int fromPosition = origin.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            if (draggingFromPosition == C.INDEX_UNSET) {
                // A drag has started, but changes to the media queue will be reflected in clearView().
                draggingFromPosition = fromPosition;
            }
            draggingToPosition = toPosition;
            mediaQueueListAdapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            QueueItemViewHolder queueItemHolder = (QueueItemViewHolder) viewHolder;
            if (((MainActivity)getActivity()).getPlayerManager().removeItem(queueItemHolder.item)) {
                mediaQueueListAdapter.notifyItemRemoved(position);
                // Update whichever item took its place, in case it became the new selected item.
                mediaQueueListAdapter.notifyItemChanged(position);
            }
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            if (draggingFromPosition != C.INDEX_UNSET) {
                QueueItemViewHolder queueItemHolder = (QueueItemViewHolder) viewHolder;
                // A drag has ended. We reflect the media queue change in the player.
                if (!((MainActivity)getActivity()).getPlayerManager().moveItem(queueItemHolder.item, draggingToPosition)) {
                    // The move failed. The entire sequence of onMove calls since the drag started needs to be
                    // invalidated.
                    mediaQueueListAdapter.notifyDataSetChanged();
                }
            }
            draggingFromPosition = C.INDEX_UNSET;
            draggingToPosition = C.INDEX_UNSET;
        }
    }
    private class MediaQueueListAdapter extends RecyclerView.Adapter<QueueItemViewHolder> {

        @Override
        public QueueItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView v = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new QueueItemViewHolder(v);
        }

        @Override
        public void onBindViewHolder(QueueItemViewHolder holder, int position) {
            holder.item = new MusicFile(((MainActivity)getActivity()).getPlayerManager().getItem(position));
            TextView view = holder.textView;
            view.setText(holder.item.Title);
            // TODO: Solve coloring using the theme's ColorStateList.
            view.setTextColor(
                    ColorUtils.setAlphaComponent(
                            view.getCurrentTextColor(),
                            position == ((MainActivity)getActivity()).getPlayerManager().getCurrentItemIndex() ? 255 : 100));
        }

        @Override
        public int getItemCount() {
            return ((MainActivity)getActivity()).getPlayerManager().getMediaQueueSize();
        }
    }
}
