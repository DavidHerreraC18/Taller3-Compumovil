package com.taller.myapplication.services.MapsServices;

import android.app.Activity;
import android.util.Log;

import com.taller.myapplication.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

public class PlacesService {

    public void findPlaces(Activity activeActivity, GoogleMap mMap, LatLng origin, String placeType, List<Marker> markers,float color){
        String uri = getRequestedUrl(activeActivity,origin,placeType);
        new TaskPlacesRequest(mMap,markers,color).execute(uri);
    }

    private String getRequestedUrl(Activity activeActivity, LatLng origin, String placeType) {
        String strOrigin = "location=" + origin.latitude + "," + origin.longitude;
        String radius = "radius=5000";
        String sensor = "sensor=true";
        String types = "type="+placeType;

        String param = strOrigin + "&" + radius + "&" + sensor + "&" + types;
        String output = "json";
        String APIKEY = activeActivity.getResources().getString(R.string.google_maps_key);

        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/" + output + "?" + param + "&key="+APIKEY;
        Log.d("TAG", url);
        return url;
    }
}
