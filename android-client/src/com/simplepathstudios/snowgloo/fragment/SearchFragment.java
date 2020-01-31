package com.simplepathstudios.snowgloo.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.simplepathstudios.snowgloo.api.model.MusicAlbum;
import com.simplepathstudios.snowgloo.api.model.MusicArtist;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.SearchResults;
import com.simplepathstudios.snowgloo.viewmodel.MusicQueueViewModel;
import com.simplepathstudios.snowgloo.viewmodel.SearchResultsViewModel;

import java.util.ArrayList;

public class SearchFragment extends Fragment {
    private static final String TAG = "SearchFragment";

    private SearchResultsViewModel searchResultsViewModel;
    private MusicQueueViewModel queueViewModel;
    private EditText searchQuery;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Fragment initiated");
        return inflater.inflate(R.layout.search_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchQuery = view.findViewById(R.id.search_query);
        searchQuery.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId== EditorInfo.IME_ACTION_DONE || (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)){
                    String query = searchQuery.getText().toString();
                    searchResultsViewModel.load(query);
                }
                return false;
            }
        });

        queueViewModel = new ViewModelProvider(getActivity()).get(MusicQueueViewModel.class);
        searchResultsViewModel = new ViewModelProvider(getActivity()).get(SearchResultsViewModel.class);
        searchResultsViewModel.Data.observe(getViewLifecycleOwner(), new Observer<SearchResults>() {
            @Override
            public void onChanged(SearchResults searchResults) {
                LinearLayout container = getView().findViewById(R.id.lists_container);
                container.removeAllViews();
                if(searchResults.Artists.size() > 0){
                    View listView = getLayoutInflater().inflate(R.layout.search_result_list,container,false);
                    TextView resultKindText = listView.findViewById(R.id.result_kind);
                    resultKindText.setText("Artists (" + searchResults.Artists.size() +")");
                    RecyclerView listElement = listView.findViewById(R.id.result_list);
                    ArtistAdapter adapter = new ArtistAdapter();
                    listElement.setAdapter(adapter);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                    listElement.setLayoutManager(layoutManager);
                    adapter.setData(searchResults.Artists);
                    adapter.notifyDataSetChanged();
                    container.addView(listView);
                }
                if(searchResults.Albums.size() > 0){
                    View listView = getLayoutInflater().inflate(R.layout.search_result_list,container,false);
                    TextView resultKindText = listView.findViewById(R.id.result_kind);
                    resultKindText.setText("Albums (" + searchResults.Albums.size() +")");
                    RecyclerView listElement = listView.findViewById(R.id.result_list);
                    AlbumAdapter adapter = new AlbumAdapter();
                    listElement.setAdapter(adapter);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                    listElement.setLayoutManager(layoutManager);
                    adapter.setData(searchResults.Albums);
                    adapter.notifyDataSetChanged();
                    container.addView(listView);
                }
                if(searchResults.Songs.size() > 0){
                    View listView = getLayoutInflater().inflate(R.layout.search_result_list,container,false);
                    TextView resultKindText = listView.findViewById(R.id.result_kind);
                    resultKindText.setText("Songs  (" + searchResults.Songs.size() +")");
                    RecyclerView listElement = listView.findViewById(R.id.result_list);
                    SongAdapter adapter = new SongAdapter();
                    listElement.setAdapter(adapter);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                    listElement.setLayoutManager(layoutManager);
                    adapter.setData(searchResults.Songs);
                    adapter.notifyDataSetChanged();
                    container.addView(listView);
                }
            }
        });
    }

    private class AlbumViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView textView;
        public MusicAlbum album;

        public AlbumViewHolder(TextView textView) {
            super(textView);
            this.textView = textView;
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            NavController navController = Navigation.findNavController(getActivity(),R.id.nav_host_fragment);
            Bundle bundle = new Bundle();
            bundle.putString("AlbumSlug", album.AlbumSlug);
            bundle.putString("AlbumDisplay", album.Album + " ("+album.ReleaseYear+")");
            navController.navigate(R.id.album_view_fragment, bundle);
        }
    }

    private class AlbumAdapter extends RecyclerView.Adapter<AlbumViewHolder> {
        private ArrayList<MusicAlbum> data;
        public AlbumAdapter(){
            this.data = null;
        }

        public void setData(ArrayList<MusicAlbum> data){
            this.data = data;
        }

        @Override
        public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView v = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.small_list_item, parent, false);
            return new AlbumViewHolder(v);
        }

        @Override
        public void onBindViewHolder(AlbumViewHolder holder, int position) {
            holder.album = this.data.get(position);
            TextView view = holder.textView;
            view.setText(holder.album.Album + " ("+holder.album.ReleaseYear+")");
        }

        @Override
        public int getItemCount() {
            if(this.data == null){
                return 0;
            }
            return this.data.size();
        }
    }

    private class ArtistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView textView;
        public MusicArtist artist;

        public ArtistViewHolder(TextView textView) {
            super(textView);
            this.textView = textView;
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            NavController navController = Navigation.findNavController(getActivity(),R.id.nav_host_fragment);
            Bundle bundle = new Bundle();
            bundle.putString("Artist", artist.Artist);
            navController.navigate(R.id.artist_view_fragment, bundle);
        }
    }

    private class ArtistAdapter extends RecyclerView.Adapter<ArtistViewHolder> {
        private ArrayList<MusicArtist> data;
        public ArtistAdapter(){
            this.data = null;
        }

        public void setData(ArrayList<MusicArtist> data){
            this.data = data;
        }

        @Override
        public ArtistViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView v = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.small_list_item, parent, false);
            return new ArtistViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ArtistViewHolder holder, int position) {
            holder.artist = this.data.get(position);
            TextView view = holder.textView;
            view.setText(holder.artist.Artist);
        }

        @Override
        public int getItemCount() {
            if(this.data == null){
                return 0;
            }
            return this.data.size();
        }
    }

    private class SongViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener,View.OnClickListener {

        public final TextView textView;
        public MusicFile song;

        public SongViewHolder(TextView textView) {
            super(textView);
            this.textView = textView;
            textView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            queueViewModel.addItem(song);
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
                    bundle.putString("AlbumSlug", song.AlbumSlug);
                    bundle.putString("AlbumDisplay", song.Album + " ("+song.ReleaseYear+")");
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
                    bundle.putString("Artist", song.Artist);
                    navController.navigate(R.id.artist_view_fragment, bundle);
                    return false;
                }
            });
        }
    }

    private class SongAdapter extends RecyclerView.Adapter<SongViewHolder> {
        private ArrayList<MusicFile> data;
        public SongAdapter(){
            this.data = null;
        }

        public void setData(ArrayList<MusicFile> data){
            this.data = data;
        }

        @Override
        public SongViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView v = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.small_list_item, parent, false);
            return new SongViewHolder(v);
        }

        @Override
        public void onBindViewHolder(SongViewHolder holder, int position) {
            holder.song = this.data.get(position);
            TextView view = holder.textView;
            view.setText(holder.song.Title);
        }

        @Override
        public int getItemCount() {
            if(this.data == null){
                return 0;
            }
            return this.data.size();
        }
    }

}
