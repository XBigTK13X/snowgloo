package com.simplepathstudios.snowgloo;


import android.net.Uri;

public class SnowglooSettings {
    public static final String BuildDate = "December 20, 2024";
    public static final String ClientVersion = "1.7.4";
    public static final String DevServerUrl = "http://192.168.101.10:5051";
    public static final String ProdServerUrl = "https://snowgloo-api.9914.us";
    public static boolean EnableDebugLog = false;
    public static double InternalMediaVolume = 1.0;
    public static boolean EnableSimpleUIMode = false;
    public static Uri UpdateSnowglooUrl = Uri.parse("https://android.9914.us/snowgloo.apk");
    public static int QueuePopulatedDelayMilliseconds = 200;
    public static float SongDurationMinimumSeconds = 10f;
    public static boolean DebugResourceLeaks = false;
}
