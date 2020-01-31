package com.simplepathstudios.snowgloo.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.api.model.ArtistList;
import com.simplepathstudios.snowgloo.api.model.MusicArtist;
import com.simplepathstudios.snowgloo.api.model.MusicPlaylist;
import com.simplepathstudios.snowgloo.api.model.MusicPlaylistListItem;
import com.simplepathstudios.snowgloo.api.model.PlaylistList;
import com.simplepathstudios.snowgloo.viewmodel.ArtistListViewModel;
import com.simplepathstudios.snowgloo.viewmodel.PlaylistListViewModel;

public class PlaylistListFragment extends Fragment {
    private final String TAG = "PlaylistListFragment";
    private RecyclerView listElement;
    private Adapter adapter;
    private LinearLayoutManager layoutManager;
    private PlaylistListViewModel viewModel;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "fragment initiated");
        return inflater.inflate(R.layout.playlist_list_fragment, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listElement = view.findViewById(R.id.playlist_list);
        adapter = new Adapter();
        listElement.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getActivity());
        listElement.setLayoutManager(layoutManager);
        viewModel = new ViewModelProvider(this).get(PlaylistListViewModel.class);
        viewModel.Data.observe(getViewLifecycleOwner(), new Observer<PlaylistList>() {
            @Override
            public void onChanged(PlaylistList playlistList) {
                adapter.setData(playlistList);
                adapter.notifyDataSetChanged();
            }
        });
        viewModel.load();
    }
    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView textView;
        public MusicPlaylistListItem playlist;

        public ViewHolder(TextView textView) {
            super(textView);
            this.textView = textView;
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            NavController navController = Navigation.findNavController(getActivity(),R.id.nav_host_fragment);
            Bundle bundle = new Bundle();
            bundle.putString("PlaylistName", playlist.name);
            bundle.putString("PlaylistId", playlist.id);
            navController.navigate(R.id.playlist_view_fragment, bundle);
        }
    }
    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private PlaylistList data;
        public Adapter(){
            this.data = null;
        }

        public void setData(PlaylistList data){
            this.data = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView v = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.small_list_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            MusicPlaylistListItem playlist = this.data.list.get(position);
            holder.playlist = playlist;
            TextView view = holder.textView;
            view.setText(holder.playlist.name);
        }

        @Override
        public int getItemCount() {
            if(this.data == null || this.data.list == null){
                return 0;
            }
            return this.data.list.size();
        }
    }
}
