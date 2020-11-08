package com.taller.myapplication.services.MapsServices;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class TaskParsePlaces extends AsyncTask<String, Void, JSONArray> {

    GoogleMap mMap;
    List<Marker> markers;
    float color;

    public TaskParsePlaces(GoogleMap map, List<Marker> markers,float color){
        this.mMap = map;
        this.markers = markers;
        this.color = color;
    }

    @Override
    protected JSONArray doInBackground(String... strings) {
        JSONArray results = null;
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(strings[0]);
            results = jsonObject.getJSONArray("results");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return results;
    }

    @Override
    protected void onPostExecute(JSONArray places) {
        super.onPostExecute(places);
        try{
            for(int i  = 0 ; i < places.length() ; i++){
                JSONObject aux = places.getJSONObject(i);
                Double lat = aux.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                Double lng = aux.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                String name = aux.getString("name");
                Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lng)).title(name)
                        .icon(BitmapDescriptorFactory.defaultMarker(this.color)));
                markers.add(m);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
