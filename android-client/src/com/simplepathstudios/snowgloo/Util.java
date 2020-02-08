package com.simplepathstudios.snowgloo;

import android.content.Context;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.Api;
import com.simplepathstudios.snowgloo.api.ApiClient;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    public static void log(String tag, String message){
        if(!SnowglooSettings.EnableDebugLog){
            return;
        }
        try{
            String timestamp = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date());
            String logEntry = String.format("%s - %s - %s : %s",System.currentTimeMillis(), timestamp,tag,message);
            Log.d(tag, logEntry);
            if (ApiClient.getInstance().getCurrentUser() != null) {
                ApiClient.getInstance().log(logEntry).enqueue(new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) { }
                    @Override
                    public void onFailure(Call call, Throwable t) {
                        Log.e(TAG, "Unable to send log",t);
                    }
                });
            }
        } catch(Exception e){
            Log.d(TAG, "An error occurred while logging",e);
        }

    }

    public static void toast(String message){
        Toast.makeText(getGlobalContext(), message, Toast.LENGTH_LONG).show();
    }
}
