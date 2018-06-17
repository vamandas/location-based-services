package com.iskconbaroda.utils;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;

public class GoogleStaticMapsAPIServices {
    private static final double EARTH_RADIUS_KM = 6371;

    private static String GOOGLE_STATIC_MAPS_API_KEY = "AIzaSyDNdYwjHNyeuvsPwPLrKGKMDMfgcNKMKio";

    public static String getStaticMapURL(Location location, int radiusMeters) {
        String pathString = "";
        if (radiusMeters > 0) {
            // Add radius path
            ArrayList<LatLng> circlePoints = getCircleAsPolyline(location, radiusMeters);

            if (circlePoints.size() > 0) {
                String encodedPathLocations = PolyUtil.encode(circlePoints);
                pathString = "&path=color:0xff0000ff|weight:1|fillcolor:0xff999980|enc:" + encodedPathLocations;
            }
        }

        String staticMapURL = "https://maps.googleapis.com/maps/api/staticmap?size=640x320&markers=color:red|" +
                location.getLatitude() + "," + location.getLongitude() +
                pathString +
                "&key=" + GOOGLE_STATIC_MAPS_API_KEY;

        return staticMapURL;
    }

    private static ArrayList<LatLng> getCircleAsPolyline(Location center, int radiusMeters) {
        ArrayList<LatLng> path = new ArrayList<>();

        double latitudeRadians = center.getLatitude() * Math.PI / 180.0;
        double longitudeRadians = center.getLongitude() * Math.PI / 180.0;
        double radiusRadians = radiusMeters / 1000.0 / EARTH_RADIUS_KM;

        double calcLatPrefix = Math.sin(latitudeRadians) * Math.cos(radiusRadians);
        double calcLatSuffix = Math.cos(latitudeRadians) * Math.sin(radiusRadians);

        for (int angle = 0; angle < 361; angle += 10) {
            double angleRadians = angle * Math.PI / 180.0;

            double latitude = Math.asin(calcLatPrefix + calcLatSuffix * Math.cos(angleRadians));
            double longitude = ((longitudeRadians + Math.atan2(Math.sin(angleRadians) * Math.sin(radiusRadians) * Math.cos(latitudeRadians), Math.cos(radiusRadians) - Math.sin(latitudeRadians) * Math.sin(latitude))) * 180) / Math.PI;
            latitude = latitude * 180.0 / Math.PI;

            path.add(new LatLng(latitude, longitude));
        }

        return path;
    }
}