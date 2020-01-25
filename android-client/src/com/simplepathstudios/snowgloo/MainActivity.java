package com.simplepathstudios.snowgloo;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.dynamite.DynamiteModule;
import com.google.android.material.navigation.NavigationView;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.viewmodel.InterDestinationViewModel;
import com.simplepathstudios.snowgloo.viewmodel.MusicQueueViewModel;

/**
 * An activity that plays video using {@link SimpleExoPlayer} and supports casting using ExoPlayer's
 * Cast extension.
 */
public class MainActivity extends AppCompatActivity{

    private final String TAG = "MainActivity";

    private PlayerView localPlayerView;
    private PlayerControlView castControlView;
    private PlayerManager playerManager;
    private Toolbar toolbar;
    private TextView trackMetadataView;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MusicQueueViewModel musicQueueViewModel;
    private ProgressBar loadingView;

    private CastContext castContext;


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

        this.musicQueueViewModel = new ViewModelProvider(this).get(MusicQueueViewModel.class);
        musicQueueViewModel.Data.observe(this, new Observer<MusicQueue>() {
            @Override
            public void onChanged(MusicQueue musicQueue) {
                trackMetadataView.setText(musicQueue.getCurrent().getMetadata());
            }
        });

        setContentView(R.layout.main_activity);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        loadingView = findViewById(R.id.loading_indicator);
        LoadingIndicator.setProgressBar(loadingView);

        localPlayerView = findViewById(R.id.local_player_view);
        localPlayerView.requestFocus();

        trackMetadataView = findViewById(R.id.track_metadata);

        castControlView = findViewById(R.id.cast_control_view);

        if(playerManager == null) {
            playerManager = new PlayerManager(
                    this,
                    localPlayerView,
                    castControlView,
                    this,
                    castContext);
        }

        NavController navController = Navigation.findNavController(this,R.id.nav_host_fragment);
        drawerLayout = findViewById(R.id.drawer_layout);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph())
                .setDrawerLayout(drawerLayout)
                .build();
        navigationView = findViewById(R.id.nav_view);
        NavigationUI.setupWithNavController(toolbar, navController,appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
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
        if(playerManager == null) {
            playerManager = new PlayerManager(
                    this,
                    localPlayerView,
                    castControlView,
                    this,
                    castContext);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (castContext == null) {
            //Nothing to release.
            return;
        }
    }

    // Activity input.

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // If the event was not handled then see if the player view can handle it.
        return super.dispatchKeyEvent(event) || playerManager.dispatchKeyEvent(event);
    }

    private void showToast(int messageId) {
        Toast.makeText(getApplicationContext(), messageId, Toast.LENGTH_LONG).show();
    }


}