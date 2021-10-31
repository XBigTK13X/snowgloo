package com.simplepathstudios.snowgloo;


import android.net.Uri;

public class SnowglooSettings {
    public static final String BuildDate = "October 31, 2021";
    public static final String ClientVersion = "1.4.12";
    public static final String DevServerUrl = "http://192.168.1.20:5051";
    public static final String ProdServerUrl = "http://9914.us:5051";
    public static boolean EnableDebugLog = false;
    public static double InternalMediaVolume = 1.0;
    public static boolean EnableSimpleUIMode = false;
    public static Uri UpdateSnowglooUrl = Uri.parse("http://9914.us/software/android/snowgloo.apk");
}
