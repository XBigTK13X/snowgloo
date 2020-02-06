package com.simplepathstudios.snowgloo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.material.navigation.NavigationView;
import com.simplepathstudios.snowgloo.api.ApiClient;
import com.simplepathstudios.snowgloo.api.model.MusicQueue;
import com.simplepathstudios.snowgloo.audio.AudioPlayer;
import com.simplepathstudios.snowgloo.viewmodel.ObservableMusicQueue;
import com.simplepathstudios.snowgloo.viewmodel.SettingsViewModel;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private static MainActivity __instance;

    public static MainActivity getInstance() {
        return __instance;
    }

    private SettingsViewModel settingsViewModel;
    private ObservableMusicQueue observableMusicQueue;
    private MusicQueue queue;

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ProgressBar loadingView;
    private Button previousButton;
    private Button playButton;
    private Button pauseButton;
    private Button nextButton;
    private SeekBar seekBar;
    private TextView seekTime;

    private AudioPlayer audioPlayer;
    private Handler seekHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        __instance = this;
        observableMusicQueue = ObservableMusicQueue.getInstance();

        // Getting the cast context later than onStart can cause device discovery not to take place.
        CastContext.getSharedInstance(this);
        MediaNotification.registerActivity(this);
        audioPlayer = AudioPlayer.getInstance();
        startService(new Intent(this, SnowglooService.class));

        this.settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        this.settingsViewModel.initialize(this.getSharedPreferences("Snowgloo", Context.MODE_PRIVATE));
        settingsViewModel.Data.observe(this, new Observer<SettingsViewModel.Settings>() {
            @Override
            public void onChanged(SettingsViewModel.Settings settings) {
                if(settings.Username == null){
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }
                else {
                    ApiClient.retarget(settings.ServerUrl, settings.Username);
                }
            }
        });
        SettingsViewModel.Settings settings = settingsViewModel.Data.getValue();
        ApiClient.retarget(settings.ServerUrl, settings.Username);

        setContentView(R.layout.main_activity);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        loadingView = findViewById(R.id.loading_indicator);
        LoadingIndicator.setProgressBar(loadingView);

        NavController navController = Navigation.findNavController(this,R.id.nav_host_fragment);
        drawerLayout = findViewById(R.id.main_activity_layout);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.queue_fragment,
                R.id.album_list_fragment,
                R.id.artist_list_fragment,
                R.id.search_fragment,
                R.id.options_fragment,
                R.id.artist_view_fragment,
                R.id.album_view_fragment,
                R.id.playlist_view_fragment,
                R.id.playlist_list_fragment,
                R.id.now_playing_fragment)
                .setDrawerLayout(drawerLayout)
                .build();
        navigationView = findViewById(R.id.nav_view);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                CharSequence name = destination.getLabel();
                toolbar.setTitle(name);
            }
        });
        NavigationUI.setupWithNavController(toolbar, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Hide the keyboard if touch event outside keyboard (better search experience)
        findViewById(R.id.main_activity_layout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if(imm != null){
                    View focus = getCurrentFocus();
                    if(focus != null){
                        imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
                    }
                }
                return false;
            }
        });

        playButton = findViewById(R.id.play_button);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.play();
            }
        });
        pauseButton = findViewById(R.id.pause_button);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.pause();
            }
        });
        nextButton = findViewById(R.id.next_button);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.next();
            }
        });
        previousButton = findViewById(R.id.previous_button);
        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioPlayer.previous();
            }
        });

        seekBar = findViewById(R.id.seek_bar);

        ObservableMusicQueue.getInstance().observe(new Observer<MusicQueue>() {
            @Override
            public void onChanged(MusicQueue musicQueue) {
                queue = musicQueue;
                playButton.setVisibility(musicQueue.isPlaying ? View.GONE : View.VISIBLE);
                pauseButton.setVisibility(musicQueue.isPlaying ? View.VISIBLE : View.GONE);
            }
        });

        seekHandler = new Handler();
        seekTime = findViewById(R.id.seek_time);
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(queue != null && queue.isPlaying && queue.currentIndex != null){
                    int position = audioPlayer.getSongPosition();
                    int duration = audioPlayer.getSongDuration();
                    int completionPercent = (int)(100*((float)position/duration));
                    seekBar.setProgress(completionPercent);
                    if(position == 0 && duration == 0){
                        seekTime.setText("Loading...");
                    } else {
                        seekTime.setText(String.format("%s / %s",Util.songPositionToTimestamp(position), Util.songPositionToTimestamp(duration)));
                    }

                }
                seekHandler.postDelayed(this, 1000); //Update every second
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(audioPlayer != null && fromUser){
                    audioPlayer.seekTo(progress);
                }
            }
        });

        observableMusicQueue.load();
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
        Log.d(TAG, "Resuming");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "Pausing");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.d(TAG, "Destroying");
    }

    private void showToast(int messageId) {
        Toast.makeText(getApplicationContext(), messageId, Toast.LENGTH_LONG).show();
    }
}