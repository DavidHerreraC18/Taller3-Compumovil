package com.taller.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.taller.myapplication.model.Usuario;
import com.taller.myapplication.services.MapsServices.MapService;
import com.taller.myapplication.services.permissionService.PermissionService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class SeguimientoUsuarioDisponible extends AppCompatActivity implements OnMapReadyCallback {

    private static final double RADIUS_OF_EARTH_KM = 6371;

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private DatabaseReference myRefSeguimiento;
    private FirebaseDatabase database;
    private StorageReference mStorageRef;
    public static final String TAG = "Taller3";
    public static final String PATH_USERS="usuarios/";


    private static final int MAP_PERMISSION = 11;
    private static final int REQUEST_CHECK_SETTINGS = 3;

    private PermissionService permissionService;

    private GoogleMap mMap;
    private Marker mMarkerPosActual;
    private Marker mMarkerPosActualSeguimiento;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private Geocoder mGeocoder;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorEventListener lightSensorListener;
    private MapService mapService;
    private LatLng UA;
    private LatLng UASeguimiento;

    private static Usuario usuario;
    private static Usuario usuarioSeguimiento;

    private TextView  nombreUsuarioSeg;
    private ImageView imagenUsuarioSeg;
    private TextView  distanciaUsuarioSeg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seguimiento_usuario_disponible);
        nombreUsuarioSeg = findViewById(R.id.nombreUsuarioDisp);
        distanciaUsuarioSeg = findViewById(R.id.distanciaUsuarioDisp);
        imagenUsuarioSeg = findViewById(R.id.imagenUsuarioDisp);


        Intent inti = getIntent();
        String id = inti.getStringExtra("id_usuario_presionado");
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        permissionService = new PermissionService();
        FirebaseUser user = mAuth.getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        myRef = database.getReference(PATH_USERS+user.getUid());
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                usuario = dataSnapshot.getValue(Usuario.class);
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.w(TAG, "error en la consulta", databaseError.toException());
            }
        });

        myRefSeguimiento = database.getReference(PATH_USERS+id);
        myRefSeguimiento.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                usuarioSeguimiento = dataSnapshot.getValue(Usuario.class);
                nombreUsuarioSeg.setText(usuarioSeguimiento.getNombreUsuario() + " " + usuarioSeguimiento.getApellidoUsuario());
                if(UASeguimiento != null && UA != null)
                {
                    calcularDistanciaUsuarios();
                }
                try {
                    downloadFile(dataSnapshot.getKey());
                } catch (IOException e) {
                    Log.w(TAG,"Problemas con lectura archivos");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.w(TAG, "error en la consulta", databaseError.toException());
            }
        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapSegUsuDisp);
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
                    mMarkerPosActual = mMap.addMarker(new MarkerOptions().position(ubicacionactual).title("Ubicacion Actual").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    if(!ubicacionactual.equals(UA))
                    {

                        String locationS = location.getLatitude() +","+location.getLongitude();
                        myRef.child("ubicacionActual").setValue(locationS);
                        if(UASeguimiento != null)
                        {
                            UA = ubicacionactual;
                            calcularDistanciaUsuarios();
                        }

                    }
                    UA = ubicacionactual;
                }
                if (usuarioSeguimiento != null  && location != null)
                {
                    String str = usuarioSeguimiento.getUbicacionActual();
                    String[] arrOfStr = str.split(",");
                    double latSeg  = Double.parseDouble(arrOfStr[0]);
                    double longSeg = Double.parseDouble(arrOfStr[1]);
                    LatLng ubiActualSeguimiento = new LatLng(latSeg,longSeg);
                    if(mMarkerPosActualSeguimiento != null)
                    {
                        mMarkerPosActualSeguimiento.remove();
                    }
                    mMarkerPosActual = mMap.addMarker(new MarkerOptions().position(ubiActualSeguimiento).title(usuarioSeguimiento.getNombreUsuario()+" "+usuarioSeguimiento.getApellidoUsuario()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                    if(!ubiActualSeguimiento.equals(UASeguimiento))
                    {
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(ubiActualSeguimiento));
                        UASeguimiento = ubiActualSeguimiento;
                        calcularDistanciaUsuarios();
                    }
                    UASeguimiento = ubiActualSeguimiento;
                }
            }
        };
        String[] permisos = {Manifest.permission.ACCESS_FINE_LOCATION};
        mLocationRequest = createLocationRequest();
        permissionService.requestPermission(this, permisos);
        checkSettings();

    }

    public void calcularDistanciaUsuarios()
    {
        double distancia = distance(UA.latitude,UA.longitude,UASeguimiento.latitude,UASeguimiento.longitude);
        distanciaUsuarioSeg.setText("Distancia: " + distancia + " KM");

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
                            resolvable.startResolutionForResult(SeguimientoUsuarioDisponible.this,REQUEST_CHECK_SETTINGS);
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
    }

    private void updateUI(FirebaseUser user){
        if(user != null){
            if(usuario != null)
            {

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
    }

    public double distance(double lat1, double long1, double lat2, double long2) {
        double latDistance = Math.toRadians(lat1 - lat2);
        double lngDistance = Math.toRadians(long1 - long2);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double result = RADIUS_OF_EARTH_KM * c;
        return Math.round(result*100.0)/100.0;
    }

    private void downloadFile(String id) throws IOException {
        File localFile = File.createTempFile("images", "jpg");
        StorageReference imageRef = mStorageRef.child("images/profile/"+id+"/fotoperfil.jpg");
        imageRef.getFile(localFile)
                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        try {
                            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(localFile));
                            imagenUsuarioSeg.setImageBitmap(b);

                        }
                        catch (FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                    }
                });
        imageRef.getFile(localFile)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception)
                    {

                    }
                });
    }

    public void toAtras(View v)
    {
        finish();
    }
}