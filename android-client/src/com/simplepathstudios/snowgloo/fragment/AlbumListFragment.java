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
import com.simplepathstudios.snowgloo.api.model.AlbumList;
import com.simplepathstudios.snowgloo.api.model.MusicAlbum;
import com.simplepathstudios.snowgloo.viewmodel.AlbumListViewModel;

public class AlbumListFragment  extends Fragment {
    private final String TAG = "AlbumListFragment";
    private RecyclerView listElement;
    private AlbumListFragment.Adapter adapter;
    private LinearLayoutManager layoutManager;
    private AlbumListViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.album_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listElement = view.findViewById(R.id.album_list);
        adapter = new AlbumListFragment.Adapter();
        listElement.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getActivity());
        listElement.setLayoutManager(layoutManager);
        viewModel = new ViewModelProvider(this).get(AlbumListViewModel.class);
        viewModel.Data.observe(getViewLifecycleOwner(), new Observer<AlbumList>() {
            @Override
            public void onChanged(AlbumList albumList) {
                adapter.setData(albumList);
                adapter.notifyDataSetChanged();
            }
        });
        viewModel.load();
    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView textView;
        public MusicAlbum musicAlbum;

        public ViewHolder(TextView textView) {
            super(textView);
            this.textView = textView;
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
            Bundle bundle = new Bundle();
            bundle.putString("AlbumSlug", musicAlbum.AlbumSlug);
            bundle.putString("AlbumDisplay", musicAlbum.Album + " ("+musicAlbum.ReleaseYear+")");
            navController.navigate(R.id.album_view_fragment, bundle);
        }
    }

    private class Adapter extends RecyclerView.Adapter<AlbumListFragment.ViewHolder> {
        private AlbumList data;

        public Adapter() {
            this.data = null;
        }

        public void setData(AlbumList data) {
            this.data = data;
        }

        @Override
        public AlbumListFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView v = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.small_list_item, parent, false);
            return new AlbumListFragment.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(AlbumListFragment.ViewHolder holder, int position) {
            holder.musicAlbum = this.data.albums.lookup.get(this.data.albums.list.get(position));
            TextView view = holder.textView;
            view.setText(holder.musicAlbum.Album + " - " + holder.musicAlbum.Artist);
        }

        @Override
        public int getItemCount() {
            if (this.data == null || this.data.albums == null || this.data.albums.list == null) {
                return 0;
            }
            return this.data.albums.list.size();
        }
    }
}