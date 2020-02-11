package com.simplepathstudios.snowgloo.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
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

import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.api.model.ArtistList;
import com.simplepathstudios.snowgloo.api.model.MusicArtist;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.viewmodel.ArtistListViewModel;
import com.simplepathstudios.snowgloo.viewmodel.InterDestinationViewModel;

public class ArtistListFragment extends Fragment {
    private final String TAG = "ArtistListFragment";
    private RecyclerView listElement;
    private Adapter adapter;
    private LinearLayoutManager layoutManager;
    private ArtistListViewModel viewModel;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.artist_list_fragment, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        listElement = view.findViewById(R.id.artist_list);
        adapter = new Adapter();
        listElement.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getActivity());
        listElement.setLayoutManager(layoutManager);
        viewModel = new ViewModelProvider(this).get(ArtistListViewModel.class);
        viewModel.Data.observe(getViewLifecycleOwner(), new Observer<ArtistList>() {
            @Override
            public void onChanged(ArtistList artistList) {
                adapter.setData(artistList);
                adapter.notifyDataSetChanged();
            }
        });
        Bundle arguments = getArguments();
        if(arguments != null){
            viewModel.load(arguments.getString("Category"));
        }
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
            NavController navController = Navigation.findNavController(getActivity(),R.id.nav_host_fragment);
            Bundle bundle = new Bundle();
            bundle.putString("Artist", musicArtist.Artist);
            navController.navigate(R.id.artist_view_fragment, bundle);
        }
    }
    private class Adapter extends RecyclerView.Adapter<ViewHolder> {
        private ArtistList data;
        public Adapter(){
            this.data = null;
        }

        public void setData(ArtistList data){
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
        }
    }
}
