package com.simplepathstudios.snowgloo;

public class Util {
    public static String songPositionToTimestamp(int position){
        int seconds = (position/1000) % 60;
        int minutes = (position/(1000 *60)) % 60;
        return String.format("%02d:%02d",minutes,seconds);
    }
}
