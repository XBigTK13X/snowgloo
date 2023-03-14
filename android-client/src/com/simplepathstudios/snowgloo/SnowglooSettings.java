package com.simplepathstudios.snowgloo;


import android.net.Uri;

public class SnowglooSettings {
    public static final String BuildDate = "March 14, 2023";
    public static final String ClientVersion = "1.6.7";
    public static final String DevServerUrl = "http://192.168.1.20:5051";
    public static final String ProdServerUrl = "http://9914.us:5051";
    public static boolean EnableDebugLog = false;
    public static double InternalMediaVolume = 1.0;
    public static boolean EnableSimpleUIMode = false;
    public static Uri UpdateSnowglooUrl = Uri.parse("http://9914.us/software/android/snowgloo.apk");
    public static int QueuePopulatedDelayMilliseconds = 200;
    public static float SongDurationMinimumSeconds = 10f;
    public static boolean DebugResourceLeaks = false;
}
