package com.simplepathstudios.snowgloo.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
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
    private static final int TAB_COUNT = 3;
    private ViewPagerAdapter viewPagerAdapter;
    private SongResultsFragment songResultsFragment;
    private ArtistResultsFragment artistResultsFragment;
    private AlbumResultsFragment albumResultsFragment;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private SearchResultsViewModel searchResultsViewModel;
    private Button searchButton;
    private EditText searchQuery;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Fragment initiated");
        return inflater.inflate(R.layout.search_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchResultsViewModel = new ViewModelProvider(this).get(SearchResultsViewModel.class);
        searchResultsViewModel.Data.observe(getViewLifecycleOwner(), new Observer<SearchResults>() {
            @Override
            public void onChanged(SearchResults searchResults) {
                songResultsFragment.setResults(searchResults.Songs);
                artistResultsFragment.setResults(searchResults.Artists);
                albumResultsFragment.setResults(searchResults.Albums);
                viewPager.setAdapter(viewPagerAdapter);
                viewPagerAdapter.notifyDataSetChanged();
            }
        });
        searchButton = view.findViewById(R.id.search_button);
        searchQuery = view.findViewById(R.id.search_query);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchQuery.getText().toString();
                searchResultsViewModel.load(query);
            }
        });
        songResultsFragment = new SongResultsFragment();
        albumResultsFragment = new AlbumResultsFragment();
        artistResultsFragment = new ArtistResultsFragment();
        viewPagerAdapter = new ViewPagerAdapter(getChildFragmentManager(),FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager = view.findViewById(R.id.search_result_tab_views);
        viewPager.setOffscreenPageLimit(TAB_COUNT);
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout = view.findViewById(R.id.search_result_tab_container);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition(),false);
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                tabLayout.setScrollPosition(position,0f,true);
                viewPager.setCurrentItem(position,false);
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        String query = searchQuery.getText().toString();
        if(!query.isEmpty()){
            searchResultsViewModel.load(query);
        }
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
            super(fm, behavior);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    return artistResultsFragment;
                case 1:
                    return albumResultsFragment;
                case 2:
                    return songResultsFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }
    }
    public static class SongResultsFragment extends Fragment {
        private RecyclerView listElement;
        private Adapter adapter;
        private RecyclerView.LayoutManager layoutManager;
        private MusicQueueViewModel queueViewModel;

        public SongResultsFragment(){
            super();
            adapter = new Adapter();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            Log.d(TAG, "SongResultsFragment initiated");
            return inflater.inflate(R.layout.song_results_fragment, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            queueViewModel = new ViewModelProvider(this).get(MusicQueueViewModel.class);
            super.onViewCreated(view, savedInstanceState);
            listElement = view.findViewById(R.id.song_list);
            listElement.setAdapter(adapter);
            layoutManager = new LinearLayoutManager(getActivity());
            listElement.setLayoutManager(layoutManager);
        }

        public void setResults(ArrayList<MusicFile> songs){
            adapter.setData(songs);
            adapter.notifyDataSetChanged();
        }

        private class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener, View.OnClickListener {

            public final TextView textView;
            public MusicFile musicFile;

            public ViewHolder(TextView textView) {
                super(textView);
                this.textView = textView;
                itemView.setOnClickListener(this);
                itemView.setOnCreateContextMenuListener(this);
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
                queueViewModel.addItem(musicFile);
            }
        }

        private class Adapter extends RecyclerView.Adapter<ViewHolder> {
            private ArrayList<MusicFile> data;

            public void setData(ArrayList<MusicFile> data){
                this.data = data;
            }

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                TextView v = (TextView) LayoutInflater.from(parent.getContext())
                        .inflate(android.R.layout.simple_list_item_1, parent, false);
                return new ViewHolder(v);
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                MusicFile item = this.data.get(position);
                holder.musicFile = item;
                TextView view = holder.textView;
                view.setText(String.format("%s - %s - %s",holder.musicFile.Title,holder.musicFile.DisplayAlbum,holder.musicFile.DisplayArtist));
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
    public static class AlbumResultsFragment extends Fragment {
        private RecyclerView listElement;
        private Adapter adapter;
        private RecyclerView.LayoutManager layoutManager;

        public AlbumResultsFragment(){
            super();
            adapter = new Adapter();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            Log.d(TAG, "AlbumResultsFragment initiated");
            return inflater.inflate(R.layout.album_results_fragment, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            listElement = view.findViewById(R.id.album_list);
            listElement.setAdapter(adapter);
            layoutManager = new LinearLayoutManager(getActivity());
            listElement.setLayoutManager(layoutManager);
        }

        public void setResults(ArrayList<MusicAlbum> albums){
            adapter.setData(albums);
            adapter.notifyDataSetChanged();
        }

        private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public final TextView textView;
            public MusicAlbum item;

            public ViewHolder(TextView textView) {
                super(textView);
                this.textView = textView;
                textView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                Bundle bundle = new Bundle();
                bundle.putString("AlbumSlug", item.AlbumSlug);
                bundle.putString("AlbumDisplay", item.Album + "("+item.ReleaseYear+")");
                navController.navigate(R.id.album_view_fragment, bundle);
            }
        }

        private class Adapter extends RecyclerView.Adapter<AlbumResultsFragment.ViewHolder> {
            private ArrayList<MusicAlbum> data;

            public void setData(ArrayList<MusicAlbum> data){
                this.data = data;
            }

            @Override
            public AlbumResultsFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                TextView v = (TextView) LayoutInflater.from(parent.getContext())
                        .inflate(android.R.layout.simple_list_item_1, parent, false);
                return new AlbumResultsFragment.ViewHolder(v);
            }

            @Override
            public void onBindViewHolder(AlbumResultsFragment.ViewHolder holder, int position) {
                MusicAlbum item = this.data.get(position);
                holder.item = item;
                TextView view = holder.textView;
                view.setText(holder.item.Album + " - " + holder.item.Artist);
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
    public static class ArtistResultsFragment extends Fragment {
        private RecyclerView listElement;
        private Adapter adapter;
        private RecyclerView.LayoutManager layoutManager;

        public ArtistResultsFragment(){
            super();
            adapter = new Adapter();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {
            Log.d(TAG, "ArtistResultsFragment initiated");
            return inflater.inflate(R.layout.artist_results_fragment, container, false);
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            listElement = view.findViewById(R.id.artist_list);
            listElement.setAdapter(adapter);
            layoutManager = new LinearLayoutManager(getActivity());
            listElement.setLayoutManager(layoutManager);
        }

        public void setResults(ArrayList<MusicArtist> artists){
            adapter.setData(artists);
            adapter.notifyDataSetChanged();
        }

        private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            public final TextView textView;
            public MusicArtist item;

            public ViewHolder(TextView textView) {
                super(textView);
                this.textView = textView;
                textView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(getActivity(),R.id.nav_host_fragment);
                Bundle bundle = new Bundle();
                bundle.putString("Artist", item.Artist);
                navController.navigate(R.id.artist_view_fragment, bundle);
            }
        }

        private class Adapter extends RecyclerView.Adapter<ArtistResultsFragment.ViewHolder> {
            private ArrayList<MusicArtist> data;

            public void setData(ArrayList<MusicArtist> data){
                this.data = data;
            }

            @Override
            public ArtistResultsFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                TextView v = (TextView) LayoutInflater.from(parent.getContext())
                        .inflate(android.R.layout.simple_list_item_1, parent, false);
                return new ArtistResultsFragment.ViewHolder(v);
            }

            @Override
            public void onBindViewHolder(ArtistResultsFragment.ViewHolder holder, int position) {
                MusicArtist item = this.data.get(position);
                holder.item = item;
                TextView view = holder.textView;
                view.setText(holder.item.Artist);
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
}