package com.simplepathstudios.snowgloo.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.api.model.ArtistView;
import com.simplepathstudios.snowgloo.api.model.MusicAlbum;
import com.simplepathstudios.snowgloo.viewmodel.ArtistViewViewModel;

import java.util.ArrayList;

public class ArtistViewFragment extends Fragment {
    private final String TAG = "ArtistViewFragment";
    private ArtistViewViewModel viewModel;
    private String artistName;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "ArtistViewFragment initiated");
        artistName = getArguments().getString("Artist");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(artistName);

        viewModel = new ViewModelProvider(this).get(ArtistViewViewModel.class);
        viewModel.Data.observe(getViewLifecycleOwner(), new Observer<ArtistView>() {
            @Override
            public void onChanged(ArtistView artistView) {
                LinearLayout container = getView().findViewById(R.id.lists_container);
                for(String listKind : artistView.albums.listKinds){
                    if(artistView.albums.lists.get(listKind).size() > 0){
                        ArrayList<MusicAlbum> albums = new ArrayList<>();
                        for(String albumName : artistView.albums.lists.get(listKind)){
                            albums.add(artistView.albums.lookup.get(albumName));
                        }
                        View listView = getLayoutInflater().inflate(R.layout.album_list,container,false);
                        TextView listKindText = listView.findViewById(R.id.list_kind);
                        listKindText.setText(listKind);
                        RecyclerView listElement = listView.findViewById(R.id.album_list);
                        ArtistViewFragment.Adapter adapter = new ArtistViewFragment.Adapter();
                        listElement.setAdapter(adapter);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                        listElement.setLayoutManager(layoutManager);
                        adapter.setData(albums);
                        adapter.notifyDataSetChanged();
                        container.addView(listView);
                        Log.d(TAG, "Populated "+artistName + " "+listKind+" with "+albums.size()+" albums");
                    }
                }
            }
        });

        return inflater.inflate(R.layout.artist_view_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel.load(artistName);
    }

    private class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView textView;
        public MusicAlbum album;

        public ViewHolder(TextView textView) {
            super(textView);
            this.textView = textView;
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            NavController navController = Navigation.findNavController(getActivity(),R.id.nav_host_fragment);
            Bundle bundle = new Bundle();
            bundle.putString("AlbumSlug", album.AlbumSlug);
            bundle.putString("AlbumDisplay", album.Album + "("+album.ReleaseYear+")");
            navController.navigate(R.id.album_view_fragment, bundle);
        }
    }
    private class Adapter extends RecyclerView.Adapter<ArtistViewFragment.ViewHolder> {
        private ArrayList<MusicAlbum> data;
        public Adapter(){
            this.data = null;
        }

        public void setData(ArrayList<MusicAlbum> data){
            this.data = data;
        }

        @Override
        public ArtistViewFragment.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView v = (TextView) LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ArtistViewFragment.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ArtistViewFragment.ViewHolder holder, int position) {
            holder.album = this.data.get(position);
            TextView view = holder.textView;
            view.setText(holder.album.Album + "("+holder.album.ReleaseYear+")");
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
