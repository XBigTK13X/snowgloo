package com.simplepathstudios.snowgloo.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.simplepathstudios.snowgloo.R;
import com.simplepathstudios.snowgloo.SnowglooSettings;
import com.simplepathstudios.snowgloo.viewmodel.SettingsViewModel;

public class OptionsFragment extends Fragment {
    private static final String TAG = "OptionsFragment";
    private SettingsViewModel viewModel;
    private RadioButton prodRadio;
    private RadioButton devRadio;
    private RadioGroup serverUrlRadios;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "AlbumListFragment initiated");
        return inflater.inflate(R.layout.options_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prodRadio = view.findViewById(R.id.prod_server_radio);
        devRadio = view.findViewById(R.id.dev_server_radio);

        viewModel = new ViewModelProvider(getActivity()).get(SettingsViewModel.class);
        viewModel.Data.observe(getViewLifecycleOwner(), new Observer<SettingsViewModel.Settings>() {
            @Override
            public void onChanged(SettingsViewModel.Settings settings) {
                if(settings.ServerUrl.equalsIgnoreCase("http://192.168.1.20:5051")){
                    Log.d(TAG, "Dev server selected");
                    prodRadio.setChecked(false);
                    devRadio.setChecked(true);
                } else if(settings.ServerUrl != null){
                    Log.d(TAG, "Prod server selected");
                    prodRadio.setChecked(true);
                    devRadio.setChecked(false);
                }
            }
        });
        Button logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.setUsername(null);
            }
        });

        serverUrlRadios = (RadioGroup) view.findViewById(R.id.server_url_radios);
        serverUrlRadios.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.dev_server_radio){
                    viewModel.setServerUrl(SnowglooSettings.DevServerUrl());
                }
                if(checkedId == R.id.prod_server_radio){
                    viewModel.setServerUrl(SnowglooSettings.ProdServerUrl());
                }
            }
        });
    }
}
