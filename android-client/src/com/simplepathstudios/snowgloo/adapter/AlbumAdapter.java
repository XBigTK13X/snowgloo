package com.simplepathstudios.snowgloo.adapter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepathstudios.snowgloo.MainActivity;
import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.api.model.AlbumList;
import com.simplepathstudios.snowgloo.api.model.MusicAlbum;
import com.simplepathstudios.snowgloo.fragment.AlbumListFragment;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    private ArrayList<MusicAlbum> data;

    public AlbumAdapter() {
        this.data = null;
    }

    public void setData(ArrayList<MusicAlbum> data) {
        this.data = data;
    }

    @Override
    public AlbumAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.small_list_item, parent, false);
        return new AlbumAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AlbumAdapter.ViewHolder holder, int position) {
        holder.musicAlbum = this.data.get(position);
        TextView view = holder.textView;
        view.setText(holder.musicAlbum.Album + " - " + holder.musicAlbum.Artist);
    }

    @Override
    public int getItemCount() {
        if (this.data == null) {
            return 0;
        }
        return this.data.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView textView;
        public MusicAlbum musicAlbum;

        public ViewHolder(TextView textView) {
            super(textView);
            this.textView = textView;
            textView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            NavController navController = Navigation.findNavController(MainActivity.getInstance(), R.id.nav_host_fragment);
            Bundle bundle = new Bundle();
            bundle.putString("AlbumSlug", musicAlbum.AlbumSlug);
            bundle.putString("AlbumDisplay", musicAlbum.Album + " ("+musicAlbum.ReleaseYear+")");
            navController.navigate(R.id.album_view_fragment, bundle);
        }
    }
}


