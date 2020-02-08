package com.simplepathstudios.snowgloo.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepathstudios.snowgloo.MainActivity;
import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.Util;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicPlaylist;
import com.simplepathstudios.snowgloo.viewmodel.ObservableMusicQueue;
import com.simplepathstudios.snowgloo.viewmodel.PlaylistViewViewModel;

public class PlaylistViewFragment extends Fragment {
    private final String TAG = "PlaylistViewFragment";

    private ObservableMusicQueue observableMusicQueue;
    private PlaylistViewViewModel playlistViewModel;
    private String playlistId;
    private String playlistName;
    private RecyclerView listElement;
    private PlaylistViewFragment.Adapter adapter;
    private LinearLayoutManager layoutManager;
    private MenuItem addToQueueButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.add_to_queue_action_menu, menu);
        addToQueueButton = menu.findItem(R.id.add_to_queue_button);
        addToQueueButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                observableMusicQueue.addItems(playlistViewModel.Data.getValue().songs);
                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        playlistId = getArguments().getString("PlaylistId");
        playlistName = getArguments().getString("PlaylistName");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(playlistName);
        return inflater.inflate(R.layout.playlist_view_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observableMusicQueue = ObservableMusicQueue.getInstance();
        listElement = view.findViewById(R.id.playlist_songs);
        adapter = new PlaylistViewFragment.Adapter();
        listElement.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getActivity());
        listElement.setLayoutManager(layoutManager);
        playlistViewModel = new ViewModelProvider(this).get(PlaylistViewViewModel.class);
        playlistViewModel.Data.observe(getViewLifecycleOwner(), new Observer<MusicPlaylist>() {
            @Override
            public void onChanged(MusicPlaylist playlist) {
                adapter.setData(playlist);
                adapter.notifyDataSetChanged();
            }
        });
        playlistViewModel.load(playlistId);
    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView textView;
        public MusicFile musicFile;

        public ViewHolder(TextView textView) {
            super(textView);
            this.textView = textView;
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Util.log(TAG, "Adding "+musicFile.Title + " to queue");
            observableMusicQueue.addItem(musicFile);
        }
    }
    private class Adapter extends RecyclerView.Adapter<PlaylistViewFragment.ViewHolder> {
        private MusicPlaylist data;
        public Adapter(){
            this.data = null;
        }

        public void setData(MusicPlaylist data){
            this.data = data;
        }

        @Override
        public PlaylistViewFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView v = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.small_list_item, parent, false);
            return new PlaylistViewFragment.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(PlaylistViewFragment.ViewHolder holder, int position) {
            holder.musicFile = this.data.songs.get(position);
            TextView view = holder.textView;
            view.setText(holder.musicFile.Title);
        }

        @Override
        public int getItemCount() {
            if(this.data == null || this.data.songs== null){
                return 0;
            }
            return this.data.songs.size();
        }
    }
}
