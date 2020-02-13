package com.simplepathstudios.snowgloo.adapter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepathstudios.snowgloo.MainActivity;
import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.api.model.MusicAlbum;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.audio.AudioPlayer;
import com.simplepathstudios.snowgloo.fragment.QueueFragment;
import com.simplepathstudios.snowgloo.viewmodel.ObservableMusicQueue;

import java.util.ArrayList;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {
    public enum Kind {
        QUEUE,
        TRACKS
    }


    private ArrayList<MusicFile> songs;
    private Kind kind;
    private ItemTouchHelper itemTouchHelper;
    private RecyclerView reorderableListView;
    public SongAdapter(RecyclerView reorderableListView){
        this.kind = Kind.QUEUE;
        this.reorderableListView = reorderableListView;
        itemTouchHelper= new ItemTouchHelper(new RecyclerViewCallback());
        itemTouchHelper.attachToRecyclerView(reorderableListView);
    }
    public SongAdapter(){
        this.kind = Kind.TRACKS;
        this.songs = null;
    }

    public void setData(ArrayList<MusicFile> songs){
        this.songs = songs;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(kind == Kind.TRACKS){
            TextView v = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.small_list_item, parent, false);
            return new ViewHolder(v);
        }
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.queue_list_item, parent, false);
        return new ViewHolder(v);

    }

    @Override
    public void onBindViewHolder(SongAdapter.ViewHolder holder, int position) {
        holder.musicFile = this.songs.get(position);
        TextView view = holder.textView;
        view.setText(String.format("%s - %s - %s",holder.musicFile.Title,holder.musicFile.DisplayAlbum,holder.musicFile.DisplayArtist));
        if(kind == Kind.QUEUE){
            if(ObservableMusicQueue.getInstance().getQueue().currentIndex != null){
                view.setTextColor(
                        ColorUtils.setAlphaComponent(
                                view.getCurrentTextColor(),
                                position == ObservableMusicQueue.getInstance().getQueue().currentIndex ? 255 : 100));
            }
        }
    }

    @Override
    public int getItemCount() {
        if(this.songs == null){
            return 0;
        }
        return this.songs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        public ViewHolder self;
        public MusicFile musicFile;
        public final TextView textView;
        public ImageView dragHandle;

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(LinearLayout layout){
            super(layout);
            this.textView = (TextView)layout.getChildAt(0);
            this.dragHandle = (ImageView)layout.getChildAt(1);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ObservableMusicQueue.getInstance().setCurrentIndex(getAdapterPosition());
                            AudioPlayer.getInstance().play();
                        }
                    });
                }
            });
            itemView.setOnCreateContextMenuListener(this);
            self = this;

            this.dragHandle.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getActionMasked()== MotionEvent.ACTION_DOWN) {
                        itemTouchHelper.startDrag(self);
                    }
                    return true;
                }
            });
        }

        public ViewHolder(TextView textView) {
            super(textView);
            this.textView = textView;
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ObservableMusicQueue.getInstance().addItem(musicFile);
                }
            });

            itemView.setOnCreateContextMenuListener(this);
        }

        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem viewAlbumAction = menu.add("View Album");
            viewAlbumAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    NavController navController = Navigation.findNavController(MainActivity.getInstance(), R.id.nav_host_fragment);
                    Bundle bundle = new Bundle();
                    bundle.putString("AlbumSlug", musicFile.AlbumSlug);
                    bundle.putString("AlbumDisplay", musicFile.Album + " ("+musicFile.ReleaseYear+")");
                    navController.navigate(R.id.album_view_fragment, bundle);
                    return false;
                }
            });
            MenuItem viewArtistAction = menu.add("View Artist");
            viewArtistAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    NavController navController = Navigation.findNavController(MainActivity.getInstance(),R.id.nav_host_fragment);
                    Bundle bundle = new Bundle();
                    bundle.putString("Artist", musicFile.Artist);
                    navController.navigate(R.id.artist_view_fragment, bundle);
                    return false;
                }
            });
        }
    }

    private class RecyclerViewCallback extends ItemTouchHelper.SimpleCallback {

        private int draggingFromPosition;
        private int draggingToPosition;

        public RecyclerViewCallback() {
            super(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.START | ItemTouchHelper.END);
            draggingFromPosition = -1;
            draggingToPosition = -1;
        }

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState == ACTION_STATE_DRAG) {
                viewHolder.itemView.setAlpha(0.5f);
            }
        }

        @Override
        public boolean onMove(RecyclerView list, RecyclerView.ViewHolder origin, RecyclerView.ViewHolder target) {
            int fromPosition = origin.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            if (draggingFromPosition == -1) {
                // A drag has started, but changes to the media queue will be reflected in clearView().
                draggingFromPosition = fromPosition;
            }
            draggingToPosition = toPosition;
            notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            ObservableMusicQueue.getInstance().removeItem(position);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setAlpha(1.0f);
            if (draggingFromPosition != -1) {
                ViewHolder holder = (ViewHolder) viewHolder;
                ObservableMusicQueue.getInstance().moveItem(holder.musicFile,draggingFromPosition, draggingToPosition);
            }
            draggingFromPosition = -1;
            draggingToPosition = -1;
        }

        @Override
        public boolean isLongPressDragEnabled(){
            return false;
        }
    }
}
