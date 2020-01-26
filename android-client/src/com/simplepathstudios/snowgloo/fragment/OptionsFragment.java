package com.simplepathstudios.snowgloo.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.SnowglooSettings;
import com.simplepathstudios.snowgloo.api.model.ServerInfo;
import com.simplepathstudios.snowgloo.viewmodel.ServerInfoViewModel;
import com.simplepathstudios.snowgloo.viewmodel.SettingsViewModel;

public class OptionsFragment extends Fragment {
    private static final String TAG = "OptionsFragment";
    private SettingsViewModel settingsViewModel;
    private ServerInfoViewModel serverInfoViewModel;
    private RadioButton prodRadio;
    private RadioButton devRadio;
    private RadioGroup serverUrlRadios;
    private TextView versionText;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "OptionsFragment initiated");
        return inflater.inflate(R.layout.options_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prodRadio = view.findViewById(R.id.prod_server_radio);
        devRadio = view.findViewById(R.id.dev_server_radio);

        settingsViewModel = new ViewModelProvider(getActivity()).get(SettingsViewModel.class);
        settingsViewModel.Data.observe(getViewLifecycleOwner(), new Observer<SettingsViewModel.Settings>() {
            @Override
            public void onChanged(SettingsViewModel.Settings settings) {
                if(settings.ServerUrl.equalsIgnoreCase(SnowglooSettings.DevServerUrl)){
                    prodRadio.setChecked(false);
                    devRadio.setChecked(true);
                } else if(settings.ServerUrl != null){
                    prodRadio.setChecked(true);
                    devRadio.setChecked(false);
                }
            }
        });

        serverInfoViewModel = new ViewModelProvider(getActivity()).get(ServerInfoViewModel.class);
        serverInfoViewModel.Data.observe(getViewLifecycleOwner(), new Observer<ServerInfo>() {
            @Override
            public void onChanged(ServerInfo serverInfo) {
                Log.d(TAG, "Loaded serverInfo");
                versionText.setText(String.format(
                        "Client Version: %s\nServer Version: %s\nClient Built: %s\nServer Built: %s",
                        SnowglooSettings.ClientVersion,
                        serverInfo.version,
                        SnowglooSettings.BuildDate,
                        serverInfo.buildDate
                ));
            }
        });

        String versionInfo = String.format("Client Version: %s\nServer Version: %s\nClient Built: %s\nServer Built: %s",SnowglooSettings.ClientVersion, "???",SnowglooSettings.BuildDate,"???");
        versionText = view.findViewById(R.id.version_text);
        versionText.setText(versionInfo);

        serverInfoViewModel.load();

        Button logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsViewModel.setUsername(null);
            }
        });

        serverUrlRadios = (RadioGroup) view.findViewById(R.id.server_url_radios);
        serverUrlRadios.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.dev_server_radio){
                    settingsViewModel.setServerUrl(SnowglooSettings.DevServerUrl);
                }
                if(checkedId == R.id.prod_server_radio){

                    settingsViewModel.setServerUrl(SnowglooSettings.ProdServerUrl);
                }
            }
        });
    }
}
