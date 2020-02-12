package com.simplepathstudios.snowgloo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import androidx.recyclerview.widget.RecyclerView;

import com.simplepathstudios.snowgloo.MainActivity;
import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.api.model.ArtistView;
import com.simplepathstudios.snowgloo.api.model.MusicAlbum;
import com.simplepathstudios.snowgloo.viewmodel.ArtistViewViewModel;
import com.simplepathstudios.snowgloo.viewmodel.ObservableMusicQueue;

import java.util.ArrayList;

public class ArtistViewFragment extends Fragment {
    private final String TAG = "ArtistViewFragment";
    private ArtistViewViewModel artistViewViewModel;
    private ObservableMusicQueue observableMusicQueue;

    private String artistName;
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
                AddArtistToQueueDialogFragment dialogFragment = new AddArtistToQueueDialogFragment(artistViewViewModel, observableMusicQueue);
                dialogFragment.show(getChildFragmentManager(),"add-artist-to-queue-dialog");
                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        artistName = getArguments().getString("Artist");
        MainActivity.getInstance().setActionBarTitle(artistName);
        MainActivity.getInstance().setActionBarSubtitle("Artist");

        artistViewViewModel = new ViewModelProvider(this).get(ArtistViewViewModel.class);
        observableMusicQueue = ObservableMusicQueue.getInstance();
        artistViewViewModel.Data.observe(getViewLifecycleOwner(), new Observer<ArtistView>() {
            @Override
            public void onChanged(ArtistView artistView) {
                LinearLayout container = getView().findViewById(R.id.lists_container);
                container.removeAllViews();
                for(String listKind : artistView.albums.listKinds){
                    if(artistView.albums.lists.get(listKind).size() > 0){
                        ArrayList<MusicAlbum> albums = new ArrayList<>();
                        for(String albumName : artistView.albums.lists.get(listKind)){
                            albums.add(artistView.albums.lookup.get(albumName));
                        }
                        View listView = getLayoutInflater().inflate(R.layout.album_list,container,false);
                        TextView listKindText = listView.findViewById(R.id.list_kind);
                        listKindText.setText(String.format("%s (%d)",listKind,artistView.albums.lists.get(listKind).size()));
                        RecyclerView listElement = listView.findViewById(R.id.album_list);
                        ArtistViewFragment.Adapter adapter = new ArtistViewFragment.Adapter();
                        listElement.setAdapter(adapter);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
                        listElement.setLayoutManager(layoutManager);
                        adapter.setData(albums);
                        adapter.notifyDataSetChanged();
                        container.addView(listView);
                    }
                }
            }
        });

        return inflater.inflate(R.layout.artist_view_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        artistViewViewModel.load(artistName);
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
            bundle.putString("AlbumDisplay", album.Album + " ("+album.ReleaseYear+")");
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
                    .inflate(R.layout.small_list_item, parent, false);
            return new ArtistViewFragment.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ArtistViewFragment.ViewHolder holder, int position) {
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
}
