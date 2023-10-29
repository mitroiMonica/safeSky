package com.example.hackathon;

import com.google.android.gms.maps.model.Marker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Utils {

    public static void clearMarkers(List<Marker> markers) {
        for (Marker marker : markers) {
            marker.remove();
        }
        markers.clear();
    }

//    public static String formatMyDate(Date date) {
//        // Create a SimpleDateFormat with the desired format
//        SimpleDateFormat sdf = new SimpleDateFormat("dd.mm.yyyy HH:mm", Locale.getDefault());
//
//        // Format the date as a string
////        String formattedDate = sdf.format(date);
//        String formattedDate = "";
//        return formattedDate;
//    }
}