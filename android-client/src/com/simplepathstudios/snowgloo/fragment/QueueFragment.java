package com.simplepathstudios.snowgloo.fragment;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.Util;
import com.simplepathstudios.snowgloo.adapter.SongAdapter;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.audio.AudioPlayer;
import com.simplepathstudios.snowgloo.viewmodel.ObservableMusicQueue;

import static androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG;

public class QueueFragment extends Fragment {
    static final String TAG = "QueueFragment";
    static final int FIRST_SCREEN_ROWS = 8;
    static final int CENTER_BUFFER = 5;

    private SongAdapter adapter;
    private ObservableMusicQueue observableMusicQueue;
    private LinearLayoutManager layoutManager;
    private NestedScrollView scrollView;
    private RecyclerView listView;
    private MenuItem clearQueueButton;
    private MenuItem shuffleQueueButton;
    private AudioPlayer audioPlayer;
    private Integer requestedScrollPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.queue_action_menu, menu);

        audioPlayer = AudioPlayer.getInstance();

        clearQueueButton = menu.findItem(R.id.clear_queue_button);
        Util.confirmMenuAction(clearQueueButton, "Clear the queue?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                observableMusicQueue.clear();
                audioPlayer.stop();
            }
        });
        shuffleQueueButton = menu.findItem(R.id.shuffle_queue_button);
        Util.confirmMenuAction(shuffleQueueButton, "Shuffle the queue?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                observableMusicQueue.shuffle();
                audioPlayer.play();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.queue_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scrollView = view.findViewById(R.id.scroll_view);
        listView = view.findViewById(R.id.music_queue);

        adapter = new SongAdapter(listView);
        listView.setAdapter(adapter);
        layoutManager = new LinearLayoutManager(getActivity());
        listView.setLayoutManager(layoutManager);
        Bundle arguments = getArguments();
        if(arguments != null){
            requestedScrollPosition = arguments.getInt("ScrollToItemIndex");
        }
        observableMusicQueue = ObservableMusicQueue.getInstance();
        ObservableMusicQueue.getInstance().observe(new Observer<MusicQueue>() {
            @Override
            public void onChanged(MusicQueue musicQueue) {
                adapter.setData(musicQueue.songs);
                listView.setAdapter(adapter);
            }
        });

        if(requestedScrollPosition != null && adapter.getItemCount() > requestedScrollPosition) {
            // TODO This is a workaround to ensure the recyclerview is populated before scrolling.
            // There should be a better way, but this is the only way I could get it working.
            listView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(requestedScrollPosition > FIRST_SCREEN_ROWS){
                        requestedScrollPosition -= CENTER_BUFFER;
                    } else {
                        requestedScrollPosition = 0;
                    }
                    View child = listView.getChildAt(requestedScrollPosition);
                    scrollView.scrollTo(0, (int)child.getY());
                    requestedScrollPosition = null;
                }
            }, 0);
        }
    }
}
