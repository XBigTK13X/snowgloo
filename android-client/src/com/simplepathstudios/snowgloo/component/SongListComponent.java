package com.simplepathstudios.snowgloo.component;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.C;
import com.simplepathstudios.snowgloo.MainActivity;
import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.viewmodel.MusicQueueViewModel;


public class SongListComponent {
    private final static String TAG = "SongListComponent";

    private MediaQueueListAdapter mediaQueueListAdapter;
    private MainActivity mainActivity;
    private Fragment fragment;
    private MusicQueueViewModel musicQueueViewModel;
    private LinearLayoutManager layoutManager;
    private RecyclerView mediaQueueList;


    public SongListComponent(MainActivity activity, Fragment fragment){
        this.mainActivity = activity;
        this.fragment = fragment;

        ItemTouchHelper helper = new ItemTouchHelper(new RecyclerViewCallback());
        mediaQueueList = this.fragment.getView().findViewById(R.id.music_queue);
        helper.attachToRecyclerView(mediaQueueList);

        mediaQueueListAdapter = new MediaQueueListAdapter();
        mediaQueueList.setAdapter(mediaQueueListAdapter);
        layoutManager = new LinearLayoutManager(mainActivity);
        mediaQueueList.setLayoutManager(layoutManager);
        musicQueueViewModel = new ViewModelProvider(mainActivity).get(MusicQueueViewModel.class);
        musicQueueViewModel.Data.observe(this.fragment, new Observer<MusicQueue>() {
            @Override
            public void onChanged(MusicQueue musicQueue) {
                Log.d(TAG,"Music files have changed");
                mediaQueueListAdapter.setData(musicQueue);
                mediaQueueListAdapter.notifyDataSetChanged();
            }
        });
        this.musicQueueViewModel.load();
    }

    public void refresh(){
        if(mediaQueueList != null && mediaQueueListAdapter != null){
            mediaQueueList.setAdapter(mediaQueueListAdapter);
        }
    }

    public void clear(){
        if(mediaQueueListAdapter != null){
            mediaQueueListAdapter.notifyItemRangeRemoved(0, mediaQueueListAdapter.getItemCount());
        }
        if(mediaQueueList != null){
            mediaQueueList.setAdapter(null);
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
            musicQueueViewModel.setCurrentIndex(getAdapterPosition());
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
            musicQueueViewModel.removeItem(position);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            if (draggingFromPosition != C.INDEX_UNSET) {
                QueueItemViewHolder queueItemHolder = (QueueItemViewHolder) viewHolder;
                musicQueueViewModel.moveItem(queueItemHolder.item, draggingToPosition);
            }
            draggingFromPosition = C.INDEX_UNSET;
            draggingToPosition = C.INDEX_UNSET;
        }
    }


    private class MediaQueueListAdapter extends RecyclerView.Adapter<QueueItemViewHolder> {
        private MusicQueue data;
        public MediaQueueListAdapter(){
            this.data = MusicQueue.EMPTY;
        }

        public void setData(MusicQueue data){
            this.data = data;
        }

        @Override
        public QueueItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView v = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new QueueItemViewHolder(v);
        }

        @Override
        public void onBindViewHolder(QueueItemViewHolder holder, int position) {
            holder.item = this.data.songs.get(position);
            TextView view = holder.textView;
            view.setText(holder.item.Title);
            if(data.currentIndex != null){
                view.setTextColor(
                        ColorUtils.setAlphaComponent(
                                view.getCurrentTextColor(),
                                position == data.currentIndex ? 255 : 100));
            }
        }

        @Override
        public int getItemCount() {
            return this.data.songs.size();
        }
    }
}
