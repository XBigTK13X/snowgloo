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

import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.api.model.AlbumView;
import com.simplepathstudios.snowgloo.api.model.MusicAlbum;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.viewmodel.AlbumViewViewModel;
import com.simplepathstudios.snowgloo.viewmodel.MusicQueueViewModel;

public class AlbumViewFragment extends Fragment {
    private final String TAG = "AlbumViewFragment";

    private MusicQueueViewModel queueViewModel;
    private AlbumViewViewModel albumViewModel;
    private String albumSlug;
    private String albumDisplay;
    private RecyclerView listElement;
    private AlbumViewFragment.Adapter adapter;
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
                queueViewModel.addItems(albumViewModel.Data.getValue().album.Songs);
                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "AlbumViewFragment initiated");
        albumSlug = getArguments().getString("AlbumSlug");
        albumDisplay = getArguments().getString("AlbumDisplay");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(albumDisplay);
        return inflater.inflate(R.layout.album_view_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        queueViewModel = new ViewModelProvider(getActivity()).get(MusicQueueViewModel.class);
        listElement = view.findViewById(R.id.album_songs);
        adapter = new AlbumViewFragment.Adapter();
        listElement.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getActivity());
        listElement.setLayoutManager(layoutManager);
        albumViewModel = new ViewModelProvider(this).get(AlbumViewViewModel.class);
        albumViewModel.Data.observe(getViewLifecycleOwner(), new Observer<AlbumView>() {
            @Override
            public void onChanged(AlbumView album) {
                Log.d(TAG,"Loaded album");
                if(album.album != null && album.album.Songs != null){
                    Log.d(TAG, "Found "+album.album.Songs.size()+" songs");
                }
                adapter.setData(album.album);
                adapter.notifyDataSetChanged();
            }
        });
        albumViewModel.load(albumSlug);
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
            Log.d(TAG, "Adding "+musicFile.Title + " to queue");
            queueViewModel.addItem(musicFile);
        }
    }
    private class Adapter extends RecyclerView.Adapter<AlbumViewFragment.ViewHolder> {
        private MusicAlbum data;
        public Adapter(){
            this.data = null;
        }

        public void setData(MusicAlbum data){
            this.data = data;
        }

        @Override
        public AlbumViewFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView v = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new AlbumViewFragment.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(AlbumViewFragment.ViewHolder holder, int position) {
            holder.musicFile = this.data.Songs.get(position);
            TextView view = holder.textView;
            view.setText(holder.musicFile.Title);
        }

        @Override
        public int getItemCount() {
            if(this.data == null || this.data.Songs == null){
                return 0;
            }
            return this.data.Songs.size();
        }
    }
}
