package com.taller.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.taller.myapplication.model.Usuario;
import com.taller.myapplication.services.FirebaseServices.FirebaseListenerService;
import com.taller.myapplication.services.MapsServices.MapService;
import com.taller.myapplication.services.permissionService.PermissionService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeUsuarioActivity extends AppCompatActivity implements OnMapReadyCallback {

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    public static final String TAG = "Taller3";
    public static final String PATH_USERS="usuarios/";
    public static String CHANNEL_ID = "FIREBASE_NOTIF_CHANNEL";

    private Menu myMenu;


    private AssetManager mngr;
    private static final int MAP_PERMISSION = 11;
    private static final int REQUEST_CHECK_SETTINGS = 3;

    private PermissionService permissionService;

    private GoogleMap mMap;
    private Marker mMarkerPosActual;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Geocoder mGeocoder;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorEventListener lightSensorListener;
    private MapService mapService;
    private Toolbar toolbar;
    private LatLng UA;

    private static Usuario usuario;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_usuario);
        createNotificationChannel();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth = FirebaseAuth.getInstance();
        mngr = this.getAssets();
        database = FirebaseDatabase.getInstance();
        permissionService = new PermissionService();
        FirebaseUser user = mAuth.getCurrentUser();
        myRef = database.getReference(PATH_USERS + user.getUid());
        myRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                        usuario = dataSnapshot.getValue(Usuario.class);
                        updateUI(user);
                }
                @Override
                public void onCancelled(DatabaseError databaseError)
                {
                    Log.w(TAG, "error en la consulta", databaseError.toException());
                }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapHomeUsuario);
        mapFragment.getMapAsync(this);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        lightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (mMap != null) {
                    if (sensorEvent.values[0] < 5000) {
                        Log.i("MAPS", "DARK MAP " + sensorEvent.values[0]);
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getBaseContext(), R.raw.dark_style_map));
                    } else {
                        Log.i("MAPS", "LIGHT MAP " + sensorEvent.values[0]);
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getBaseContext(), R.raw.light_style_map));
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
        mGeocoder = new Geocoder(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    //mMap.clear();
                    LatLng ubicacionactual = new LatLng(location.getLatitude(),location.getLongitude());
                    if(mMarkerPosActual != null)
                    {
                        mMarkerPosActual.remove();
                    }
                    mMarkerPosActual = mapService.addMarker(ubicacionactual,"Ubicacion Actual");
                    if(!ubicacionactual.equals(UA))
                    {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicacionactual));
                        String locationS = location.getLatitude() +","+location.getLongitude();
                        myRef.child("ubicacionActual").setValue(locationS);
                    }
                    UA = ubicacionactual;
                }
            }
        };
        String[] permisos = {Manifest.permission.ACCESS_FINE_LOCATION};
        mLocationRequest = createLocationRequest();
        permissionService.requestPermission(this, permisos);
        checkSettings();
        updateUI(user);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_logout)
        {
            mAuth.signOut();
            Intent intent = new Intent(HomeUsuarioActivity.this, InicioSesionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
        if(id == R.id.menu_disponible)
        {
            if(usuario.getDisponible().equals("true"))
            {
                myRef.child("disponible").setValue("false");
                updateMenuTitles(false);
            }
            else
            {
                myRef.child("disponible").setValue("true");
                updateMenuTitles(true);
                //NotificarOtrosUsuarios
            }
        }
        if(id == R.id.menu_listar_disponibles)
        {
            startActivity(new Intent(this,ListarUsuariosDisponiblesActivity.class));
        }
        return true;
    }

    private void updateMenuTitles(boolean valor) {
        if(myMenu != null)
        {
            MenuItem menuItem = myMenu.findItem(R.id.menu_disponible);
            if (valor) {
                menuItem.setTitle(getString(R.string.menu_establecer_no_disponible));
            } else {
                menuItem.setTitle(getString(R.string.menu_establecer_disponible));
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        myMenu = menu;
        return true;
    }

    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000); //tasa de refresco en milisegundos
        locationRequest.setFastestInterval(5000); //máxima tasa de refresco
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    private void checkSettings(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //currentLocation();
                startLocationUpdates(); //Todas las condiciones para recibir localizaciones
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(HomeUsuarioActivity.this,REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                        } break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS: {
                if (resultCode == RESULT_OK) {
                    //currentLocation();
                    startLocationUpdates(); //Se encendió la localización!!!
                } else {
                    Toast.makeText(this, "Sin acceso a localización, hardware deshabilitado!", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Map<String, Boolean> responses = permissionService.managePermissionResponse(this,requestCode,permissions,grantResults);
        if(responses.get(Manifest.permission.ACCESS_FINE_LOCATION))
        {
            checkSettings();
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }
    private void stopLocationUpdates(){
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        updateUI(user);
        loadFirebaseBackgroundService();
    }

    private void loadFirebaseBackgroundService() {
        Intent intent = new Intent(this, FirebaseListenerService.class);
        FirebaseListenerService.enqueueWork(this,intent);
    }

    private void updateUI(FirebaseUser user){
        if(user != null){
            if(usuario != null)
            {
                Log.w(TAG,"Tengo usuario");
                if(usuario.getDisponible().equals("true"))
                {
                    updateMenuTitles(true);
                }
                else
                {
                    updateMenuTitles(false);
                }
            }
        }else{
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
        FirebaseUser user = mAuth.getCurrentUser();
        updateUI(user);
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mapService = new MapService(mMap);
        ubicarPuntosInteres();
    }


    public String loadJSONLocationsFromAsset()
    {
        String json = null;
        try{
            InputStream is = mngr.open("locations.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        }
        catch ( IOException ex)
        {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public void ubicarPuntosInteres(){
        try{
            JSONObject json= new JSONObject(loadJSONLocationsFromAsset());
            JSONArray locationsJsonArray= json.getJSONArray("locationsArray");
            for(int i=0; i<locationsJsonArray.length(); i++)
            {
                JSONObject jsonObject= locationsJsonArray.getJSONObject(i);
                String latitude = jsonObject.getString("latitude");
                String longitude = jsonObject.getString("longitude");
                String nombre = jsonObject.getString("name");
                double latD = Double.parseDouble(latitude);
                double longD = Double.parseDouble(longitude);
                LatLng punto = new LatLng(latD,longD);
                mMap.addMarker(new MarkerOptions().position(punto).title(nombre).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
            }
        }
        catch (JSONException jsonex)
        {
            jsonex.printStackTrace();
        }
    }

    private void createNotificationChannel() {
// Create the NotificationChannel, but only on API 26+ because
// the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel";
            String description = "channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//IMPORTANCE_MAX MUESTRA LA NOTIFICACIÓN ANIMADA
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
// Register the channel with the system; you can't change the importance
// or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}