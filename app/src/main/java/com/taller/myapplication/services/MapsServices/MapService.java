package com.taller.myapplication.services.MapsServices;

import android.app.Activity;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapService {
    public enum Order {CLOSEST , FIFO}

    private GoogleMap mMap;
    private RouteService routeService;
    private PlacesService placesService;
    Map<String, Polyline> polylines;

    public MapService(GoogleMap mMap){
        this.mMap = mMap;
        this.polylines = new HashMap<>();
        this.routeService = new RouteService();
        this.placesService = new PlacesService();
    }

    public Marker addMarker(LatLng latLng, String title){
        return this.mMap.addMarker(new MarkerOptions().position(latLng).title(title));
    }

    public String getLatLngName(Activity context , LatLng latLng){
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());
        String address = null;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    public Float calculateDistance(LatLng pos1, LatLng pos2){
        float[] results = new float[1];
        Location.distanceBetween(pos1.latitude , pos1.longitude,
                pos2.latitude, pos2.longitude, results);
        return results[0];
    }

    public List<LatLng> makeRoute(Activity activeActivity, List<LatLng> points, Order order){
        if(points.size() < 2){
            return points;
        }
        List<LatLng> auxPoints = order.equals(Order.CLOSEST) ? sortPoints(points) : points;

        for(int i = 1 ; i < points.size() ; i++){
            this.routeService.makeRoute(activeActivity,this.mMap,points.get(i-1),points.get(i),this.polylines, (i-1)+"-"+i);
        }

        return auxPoints;
    }

    public LatLng getFromLocationName(Activity activity, String addressString){
        Geocoder mGeocoder = new Geocoder(activity);
        try{
            List<Address> addresses = mGeocoder.getFromLocationName(addressString, 2);
            if (addresses != null && !addresses.isEmpty()) {
                Address addressResult = addresses.get(0);
                LatLng position = new LatLng(addressResult.getLatitude(), addressResult.getLongitude());
                return position;
            }
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<LatLng> sortPoints(List<LatLng> points){
        List<LatLng> pointsCopy = new ArrayList<>(points);
        List<LatLng> sortedList = new ArrayList<>();
        sortedList.add(pointsCopy.get(0));
        pointsCopy.remove(0);
        while(!pointsCopy.isEmpty()){
            LatLng closestPoint = getClosestPoint(sortedList.get(sortedList.size()-1),pointsCopy);
            sortedList.add(closestPoint);
            pointsCopy.remove(closestPoint);
        }
        return sortedList;
    }

    private LatLng getClosestPoint(LatLng point, List<LatLng> points){
        LatLng closest = points.get(0);
        Float distance = calculateDistance(point,closest);
        for(LatLng x : points){
            Float auxDis = calculateDistance(point,x);
            if(auxDis < distance){
                distance = auxDis;
                closest = x;
            }
        }
        return closest;
    }

    public void makeRoute(Activity activeActivity, LatLng startPoint, LatLng endingPoint, String polylineKey){

        this.routeService.makeRoute(activeActivity,this.mMap,startPoint,endingPoint,this.polylines, polylineKey);
    }

    public void findPlaces(Activity activeActivity, LatLng origin,List<String> types, List<Marker> markers,List<String> hexColor){
        for(int i = 0 ; i < types.size() ; i++){
            this.placesService.findPlaces(activeActivity,this.mMap,origin,types.get(i),markers,getMarkerIcon(hexColor.get(i)));
        }
    }

    public float getMarkerIcon(String color) {
        float[] hsv = new float[3];
        Color.colorToHSV(Color.parseColor(color), hsv);
        return hsv[0];
    }

    public Polyline getPolyline(LatLng startPoint, LatLng endingPoint){
        String key = startPoint.latitude+"-"+startPoint.longitude+"-"+endingPoint.latitude+"-"+endingPoint.longitude;
        return this.polylines.get(key);
    }

    public void removePolylineFromMap(String key){
        Polyline polyline = this.polylines.get(key);
        System.out.println(polyline);
        if(polyline != null){
            polyline.remove();
        }
    }

    public void removePolylines(){
        polylines.values().stream().forEach(x -> x.remove());
        polylines.clear();
    }
}
