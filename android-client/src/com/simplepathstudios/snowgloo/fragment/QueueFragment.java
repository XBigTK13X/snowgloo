package com.simplepathstudios.snowgloo.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.C;
import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.viewmodel.MusicQueueViewModel;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG;

public class QueueFragment extends Fragment {
    static final String TAG = "QueueFragment";

    private QueueFragment.Adapter adapter;
    private MusicQueueViewModel viewModel;
    private LinearLayoutManager layoutManager;
    private RecyclerView listView;
    private ItemTouchHelper itemTouchHelper;
    private MenuItem clearQueueButton;
    private MenuItem shuffleQueueButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.queue_action_menu, menu);
        clearQueueButton = menu.findItem(R.id.clear_queue_button);
        clearQueueButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                viewModel.clear();
                return false;
            }
        });
        shuffleQueueButton = menu.findItem(R.id.shuffle_queue_button);
        shuffleQueueButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                viewModel.shuffle();
                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "QueueFragment initiated");
        return inflater.inflate(R.layout.queue_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listView = view.findViewById(R.id.music_queue);
        itemTouchHelper= new ItemTouchHelper(new RecyclerViewCallback());
        itemTouchHelper.attachToRecyclerView(listView);

        adapter = new QueueFragment.Adapter();
        listView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(layoutManager);
        viewModel = new ViewModelProvider(getActivity()).get(MusicQueueViewModel.class);
        viewModel.Data.observe(getViewLifecycleOwner(), new Observer<MusicQueue>() {
            @Override
            public void onChanged(MusicQueue musicQueue) {
                Log.d(TAG,"Music files have changed "+musicQueue.songs.size());
                adapter.setData(musicQueue);
                int scrollTarget = layoutManager.findFirstCompletelyVisibleItemPosition();
                listView.setAdapter(adapter);
                if(musicQueue.updateReason == MusicQueue.UpdateReason.ITEM_MOVED){
                    listView.scrollToPosition(scrollTarget);
                }
            }
        });

        this.viewModel.load();
    }

    public void onResume(){
        super.onResume();
        if(listView != null && adapter != null){
            listView.setAdapter(adapter);
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if(adapter != null){
            adapter.notifyItemRangeRemoved(0, adapter.getItemCount());
        }
        if(listView != null){
            listView.setAdapter(null);
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
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            if (actionState == ACTION_STATE_DRAG) {
                viewHolder.itemView.setAlpha(0.5f);
            }
        }

        @Override
        public boolean onMove(RecyclerView list, RecyclerView.ViewHolder origin, RecyclerView.ViewHolder target) {
            int fromPosition = origin.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            if (draggingFromPosition == C.INDEX_UNSET) {
                // A drag has started, but changes to the media queue will be reflected in clearView().
                draggingFromPosition = fromPosition;
            }
            draggingToPosition = toPosition;
            adapter.notifyItemMoved(fromPosition, toPosition);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            viewModel.removeItem(position);
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            viewHolder.itemView.setAlpha(1.0f);
            if (draggingFromPosition != C.INDEX_UNSET) {
                ViewHolder holder = (ViewHolder) viewHolder;
                viewModel.moveItem(holder.musicFile,draggingFromPosition, draggingToPosition);
            }
            draggingFromPosition = C.INDEX_UNSET;
            draggingToPosition = C.INDEX_UNSET;
        }

        @Override
        public boolean isLongPressDragEnabled(){
            return false;
        }
    }


    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener, View.OnClickListener {

        public final TextView textView;
        public final ImageView dragHandle;
        public MusicFile musicFile;
        public ViewHolder self;

        @SuppressLint("ClickableViewAccessibility")
        public ViewHolder(LinearLayout layout) {
            super(layout);
            this.textView = (TextView)layout.getChildAt(0);
            this.dragHandle = (ImageView)layout.getChildAt(1);
            itemView.setOnClickListener(this);
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

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem viewAlbumAction = menu.add("View Album");
            viewAlbumAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    NavController navController = Navigation.findNavController(getActivity(),R.id.nav_host_fragment);
                    Bundle bundle = new Bundle();
                    bundle.putString("AlbumSlug", musicFile.AlbumSlug);
                    bundle.putString("AlbumDisplay", musicFile.Album + "("+musicFile.ReleaseYear+")");
                    navController.navigate(R.id.album_view_fragment, bundle);
                    return false;
                }
            });
            MenuItem viewArtistAction = menu.add("View Artist");
            viewArtistAction.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    NavController navController = Navigation.findNavController(getActivity(),R.id.nav_host_fragment);
                    Bundle bundle = new Bundle();
                    bundle.putString("Artist", musicFile.Artist);
                    navController.navigate(R.id.artist_view_fragment, bundle);
                    return false;
                }
            });
        }

        @Override
        public void onClick(View v) {
            viewModel.setCurrentIndex(getAdapterPosition());
        }
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private MusicQueue data;
        public Adapter(){
            this.data = MusicQueue.EMPTY;
        }

        public void setData(MusicQueue data){
            this.data = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.song_list_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.musicFile = this.data.songs.get(position);
            TextView view = holder.textView;
            view.setText(String.format("%s - %s - %s",holder.musicFile.Title,holder.musicFile.DisplayAlbum,holder.musicFile.DisplayArtist));
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
