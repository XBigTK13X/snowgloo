package com.simplepathstudios.snowgloo.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepathstudios.snowgloo.MainActivity;
import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.Util;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicPlaylist;
import com.simplepathstudios.snowgloo.viewmodel.ObservableMusicQueue;
import com.simplepathstudios.snowgloo.viewmodel.PlaylistViewViewModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private MenuItem renamePlaylistButton;
    private MenuItem updatePlaylistButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.playlist_view_action_menu, menu);
        addToQueueButton = menu.findItem(R.id.add_to_queue_button);
        updatePlaylistButton = menu.findItem(R.id.update_playlist_button);
        renamePlaylistButton = menu.findItem(R.id.rename_playlist_button);
        UpdatePlaylistNameFragment dialogFragment = new UpdatePlaylistNameFragment(getLayoutInflater(), playlistViewModel);
        renamePlaylistButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                dialogFragment.show(getChildFragmentManager(),"rename-playlist-dialog");
                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        playlistId = getArguments().getString("PlaylistId");
        playlistName = getArguments().getString("PlaylistName");
        MainActivity.getInstance().setActionBarTitle(playlistName);
        MainActivity.getInstance().setActionBarSubtitle("Playlist");
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
                MainActivity.getInstance().setActionBarTitle(playlist.name);
                Util.confirmMenuAction(addToQueueButton, "Add " + playlist.songs.size() + " songs to queue?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        observableMusicQueue.addItems(playlist.songs);
                    }
                });
                Util.confirmMenuAction(updatePlaylistButton, "Save current queue as playlist '" + playlist.name + "'?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Call runUpdate = observableMusicQueue.updatePlaylistFromQueue(playlist.id, playlistName);
                        if(runUpdate != null){
                            runUpdate.enqueue(new Callback() {
                                @Override
                                public void onResponse(Call call, Response response) {
                                    playlistViewModel.load(playlist.id);
                                }

                                @Override
                                public void onFailure(Call call, Throwable t) {

                                }
                            });
                        }
                    }
                });
                adapter.setData(playlist);
                listElement.setAdapter(adapter);
            }
        });
        playlistViewModel.load(playlistId);
    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,View.OnCreateContextMenuListener {

        public final TextView textView;
        public MusicFile musicFile;

        public ViewHolder(TextView textView) {
            super(textView);
            this.textView = textView;
            textView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            Util.log(TAG, "Adding "+musicFile.Title + " to queue");
            observableMusicQueue.addItem(musicFile);
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
                    bundle.putString("AlbumDisplay", musicFile.Album + " ("+musicFile.ReleaseYear+")");
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
