package com.simplepathstudios.snowgloo;

import android.content.Context;

import android.util.Log;
import android.widget.Toast;

public class Util {
    private static final String TAG = "Util";
    public static String songPositionToTimestamp(int position){
        int seconds = (position/1000) % 60;
        int minutes = (position/(1000 *60)) % 60;
        return String.format("%02d:%02d",minutes,seconds);
    }
    private static Context __context;
    public static void setGlobalContext(Context context){
        __context = context;
    }
    public static Context getGlobalContext(){
        if(__context == null){
            Log.d(TAG,"Global context is null, it must be set before it is read");
        }
        return __context;
    }
    public static void toast(String message){
        Toast.makeText(getGlobalContext(), message, Toast.LENGTH_LONG).show();
    }
}
