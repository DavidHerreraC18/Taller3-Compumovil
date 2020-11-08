package com.taller.myapplication.services.MapsServices;

import android.graphics.Color;
import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskParseDirection extends AsyncTask<String, Void, List<List<HashMap<String, String>>>> {

    protected GoogleMap mMap;
    Map<String, Polyline> polylines;
    String polylineKey;

    public TaskParseDirection(GoogleMap mMap, Map<String, Polyline> polylines, String polylineKey){
        this.mMap = mMap;
        this.polylines = polylines;
        this.polylineKey = polylineKey;
    }

    @Override
    protected List<List<HashMap<String, String>>> doInBackground(String... jsonString) {
        List<List<HashMap<String, String>>> routes = null;
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(jsonString[0]);
            DirectionParser parser = new DirectionParser();
            routes = parser.parse(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return routes;
    }

    @Override
    protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
        super.onPostExecute(lists);
        ArrayList points = null;
        PolylineOptions polylineOptions = null;

        for (List<HashMap<String, String>> path : lists) {
            points = new ArrayList();
            polylineOptions = new PolylineOptions();

            for (HashMap<String, String> point : path) {
                double lat = Double.parseDouble(point.get("lat"));
                double lon = Double.parseDouble(point.get("lng"));

                points.add(new LatLng(lat, lon));
            }
            polylineOptions.addAll(points);
            polylineOptions.width(15f);
            polylineOptions.color(Color.MAGENTA);
            polylineOptions.geodesic(true);
        }
        if (polylineOptions != null) {
            Polyline polyline = mMap.addPolyline(polylineOptions);
            if(this.polylineKey != null)
                this.polylines.put(this.polylineKey,polyline);
        }
    }
}
