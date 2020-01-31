package com.simplepathstudios.snowgloo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
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

import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.dynamite.DynamiteModule;
import com.google.android.material.navigation.NavigationView;
import com.simplepathstudios.snowgloo.api.ApiClient;
import com.simplepathstudios.snowgloo.viewmodel.SettingsViewModel;

public class MainActivity extends AppCompatActivity{

    private final String TAG = "MainActivity";

    private static MainActivity __instance;
    public static MainActivity getInstance(){
        return __instance;
    }

    private PlayerView localPlayerView;
    private PlayerControlView castControlView;
    private SettingsViewModel settingsViewModel;
    private PlayerManager playerManager;
    private Toolbar toolbar;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ProgressBar loadingView;

    private CastContext castContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        __instance = this;

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

        Intent intent = new Intent(this, CleanupService.class);
        startService(intent);

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

        localPlayerView = findViewById(R.id.local_player_view);
        localPlayerView.requestFocus();

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
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                return false;
            }
        });
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
        if(playerManager == null) {
            Log.d(TAG, "onResume => Create a new PlayerManager");
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
    }

    public void cleanup(){
        if(playerManager != null){
            playerManager.release();
        }
    }

    private void showToast(int messageId) {
        Toast.makeText(getApplicationContext(), messageId, Toast.LENGTH_LONG).show();
    }
}