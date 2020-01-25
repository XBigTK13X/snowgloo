package com.simplepathstudios.snowgloo.viewmodel;

import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.simplepathstudios.snowgloo.SnowglooSettings;

public class SettingsViewModel extends ViewModel {
    public MutableLiveData<Settings> Data;
    public SettingsViewModel(){
        Data = new MutableLiveData<>();
    }
    public void initialize(SharedPreferences preferences){
        Settings settings = new Settings();
        settings.Preferences = preferences;
        settings.Username = settings.Preferences.getString("Username",null);
        settings.ServerUrl = settings.Preferences.getString("ServerUrl", SnowglooSettings.ServerUrl());
        Data.setValue(settings);
    }
    public class Settings {
        public String Username;
        public String ServerUrl;
        public SharedPreferences Preferences;
    }
}
