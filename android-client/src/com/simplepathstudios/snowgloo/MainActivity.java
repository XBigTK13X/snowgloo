package com.simplepathstudios.snowgloo;

import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.cast.MediaItem;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.dynamite.DynamiteModule;
import com.simplepathstudios.snowgloo.api.model.MusicFile;
import com.simplepathstudios.snowgloo.browser.VideoItemLoader;

import java.util.List;

/**
 * An activity that plays video using {@link SimpleExoPlayer} and supports casting using ExoPlayer's
 * Cast extension.
 */
public class MainActivity extends AppCompatActivity
        implements PlayerManager.AudioListener {

    private final String TAG = "MainActivity";

    private PlayerView localPlayerView;
    private PlayerControlView castControlView;
    private PlayerManager playerManager;
    private MainFragment queueFragment;
    private Toolbar toolbar;

    private CastContext castContext;

    // Activity lifecycle methods.

    public PlayerManager getPlayerManager(){
        return playerManager;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Getting the cast context later than onStart can cause device discovery not to take place.
        try {
            castContext = CastContext.getSharedInstance(this);
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            while (cause != null) {
                if (cause instanceof DynamiteModule.LoadingException) {
                    setContentView(R.layout.cast_context_error);
                    return;
                }
                cause = cause.getCause();
            }
            // Unknown error. We propagate it.
            throw e;
        }

        setContentView(R.layout.main_activity);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        localPlayerView = findViewById(R.id.local_player_view);
        localPlayerView.requestFocus();

        castControlView = findViewById(R.id.cast_control_view);

        queueFragment = new MainFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, queueFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        Log.d(TAG, "Inflating chromecast menu");
        getMenuInflater().inflate(R.menu.menu, menu);
        CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (castContext == null) {
            // There is no Cast context to work with. Do nothing.
            return;
        }
        playerManager =
                new PlayerManager(
                        /* listener= */ this,
                        queueFragment,
                        localPlayerView,
                        castControlView,
                        /* context= */ this,
                        castContext);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (castContext == null) {
            // Nothing to release.
            return;
        }
        playerManager.release();
        playerManager = null;
    }

    // Activity input.

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // If the event was not handled then see if the player view can handle it.
        return super.dispatchKeyEvent(event) || playerManager.dispatchKeyEvent(event);
    }


    // PlayerManager.Listener implementation.

    @Override
    public void onUnsupportedTrack(int trackType) {
        if (trackType == C.TRACK_TYPE_AUDIO) {
            showToast(R.string.error_unsupported_audio);
        } else if (trackType == C.TRACK_TYPE_VIDEO) {
            showToast(R.string.error_unsupported_video);
        } else {
            // Do nothing.
        }
    }

    // Internal methods.

    private void showToast(int messageId) {
        Toast.makeText(getApplicationContext(), messageId, Toast.LENGTH_LONG).show();
    }


}