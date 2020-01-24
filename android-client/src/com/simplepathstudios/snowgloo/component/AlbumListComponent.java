package com.simplepathstudios.snowgloo.component;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.api.model.ArtistList;
import com.simplepathstudios.snowgloo.api.model.MusicArtist;
import com.simplepathstudios.snowgloo.fragment.ArtistListFragment;
import com.simplepathstudios.snowgloo.viewmodel.ArtistListViewModel;

public class AlbumListComponent {
    private final String TAG = "ArtistListFragment";
    private RecyclerView listElement;
    //private AlbumListComponent.Adapter adapter;
    private LinearLayoutManager layoutManager;
    private ArtistListViewModel viewModel;
    private ArtistList model;

    /*public void onViewCreated(View view) {
        listElement = view.findViewById(R.id.artist_list);
        adapter = new ArtistListFragment.Adapter();
        listElement.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getActivity());
        listElement.setLayoutManager(layoutManager);
        viewModel = new ViewModelProvider(this).get(ArtistListViewModel.class);
        viewModel.Data.observe(getViewLifecycleOwner(), new Observer<ArtistList>() {
            @Override
            public void onChanged(ArtistList artistList) {
                model = artistList;
                adapter.setData(model);
                adapter.notifyDataSetChanged();
            }
        });
        viewModel.load();
    }
    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView textView;
        public MusicArtist musicArtist;

        public ViewHolder(TextView textView) {
            super(textView);
            this.textView = textView;
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //musicQueueViewModel.setCurrentIndex(getAdapterPosition());
        }
    }
    private class Adapter extends RecyclerView.Adapter<AlbumListComponent.ViewHolder> {
        private AlbumListComponent data;
        public Adapter(){
            this.data = null;
        }

        public void setData(AlbumListComponent data){
            this.data = data;
        }

        @Override
        public AlbumListComponent.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView v = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new AlbumListComponent.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(AlbumListComponent.ViewHolder holder, int position) {
            String artistSlug = this.data.list.get(position);
            holder.musicArtist = this.data.lookup.get(artistSlug);
            TextView view = holder.textView;
            view.setText(holder.musicArtist.Artist);
        }

        @Override
        public int getItemCount() {
            if(this.data == null || this.data.list == null){
                return 0;
            }
            return this.data.list.size();
        }*/
    //}
}
