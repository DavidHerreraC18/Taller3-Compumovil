package com.taller.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Spinner;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.taller.myapplication.model.Usuario;
import com.taller.myapplication.services.permissionService.PermissionService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import lombok.NonNull;

public class RegistroActivity extends AppCompatActivity {

    private static final int IMAGE_PICKER_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int REQUEST_CHECK_SETTINGS = 3;
    private static final int REQUEST_STORAGE_SAVE    = 4;

    Bitmap bitmapImagenUsuario;

    private FirebaseAuth mAuth;
    private DatabaseReference myRef;
    private FirebaseDatabase database;
    private StorageReference mStorageRef;
    public static final String TAG = "Taller3";
    public static final String PATH_USERS="usuarios/";

    private String   latlng;
    private String   aux;
    private String   correo;
    private String   contra;
    private boolean  loadedImage;
    private TextView textUbi;
    private EditText textNombre;
    private EditText textCorreo;
    private EditText textContra;
    private EditText textIdenti;
    private EditText textApellido;
    private Spinner     spinnerTipoIdenti;
    private ImageView   imagenUsuario;
    private ImageButton btnSeleccionImagen;


    private PermissionService permissionService;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private String[] tiposIdenti = {"CC","TI"};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        mStorageRef = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance();
        permissionService = new PermissionService();
        loadedImage = false;
        latlng = "";
        mAuth = FirebaseAuth.getInstance();
        textApellido = findViewById(R.id.textApellidoReg);
        textNombre = findViewById(R.id.textNombreReg);
        textCorreo = findViewById(R.id.textCorreoReg);
        textContra = findViewById(R.id.textContraReg);
        textIdenti = findViewById(R.id.textIdentiReg);
        textUbi = findViewById(R.id.textUbiRegistradaReg);
        imagenUsuario = findViewById(R.id.editImagenMascota);
        btnSeleccionImagen = findViewById(R.id.botonSeleccionImagen);
        spinnerTipoIdenti = findViewById(R.id.spinnerIdeReg);
        spinnerTipoIdenti.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tiposIdenti));

        aux = textUbi.getText().toString();
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    latlng = location.getLatitude()+","+location.getLongitude();
                    textUbi.setText(aux+" "+latlng);
                }
            }
        };

        btnSeleccionImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(RegistroActivity.this, btnSeleccionImagen);
                popup.getMenuInflater().inflate(R.menu.menu_foto, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        if(id==R.id.seleccionar_galeria)
                        {
                            String[] permisos = {Manifest.permission.READ_EXTERNAL_STORAGE};
                            permissionService.requestPermission(RegistroActivity.this,permisos);
                            abrirGaleria();
                        }
                        else if(id==R.id.usar_camara)
                        {
                            String[] permisos = {Manifest.permission.CAMERA};
                            permissionService.requestPermission(RegistroActivity.this,permisos);
                            tomarFoto();
                        }
                        return true;
                    }
                });
                popup.show();
            }
        });
    }

    private void tomarFoto() {
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
        {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null)
            {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private void abrirGaleria() {
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            Intent pickImage = new Intent(Intent.ACTION_PICK);
            pickImage.setType("image/*");
            startActivityForResult(pickImage, IMAGE_PICKER_REQUEST);
        }
    }

    private void guardarFoto()
    {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        FirebaseUser user = mAuth.getCurrentUser();
        String path = guardarEnAlmacenamientoInterno(bitmapImagenUsuario);
        Uri file = Uri.fromFile(new File(directory,"fotoperfil.jpg"));
        StorageReference imageRef= mStorageRef.child("images/profile/"+user.getUid()+"/fotoperfil.jpg");
        imageRef.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        // Get a URL to the uploaded content
                        Log.i(TAG, "Succesfullyupload image");
                    }
                });
        imageRef.putFile(file)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception)
                    {

                    }
                });
    }



    private String guardarEnAlmacenamientoInterno(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"fotoperfil.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            loadedImage = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
    }

    public void toObtenerUbicacion(View v)
    {
        String[] permisos = {Manifest.permission.ACCESS_FINE_LOCATION};
        mLocationRequest = createLocationRequest();
        permissionService.requestPermission(this, permisos);
        checkSettings();
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
            public void onFailure(@androidx.annotation.NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(RegistroActivity.this,REQUEST_CHECK_SETTINGS);
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
            case IMAGE_PICKER_REQUEST: {
                if (resultCode == RESULT_OK) {
                    try {
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        bitmapImagenUsuario = selectedImage;
                        imagenUsuario.setImageBitmap(selectedImage);
                        loadedImage = true;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                return;
            }
            case REQUEST_IMAGE_CAPTURE: {
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    bitmapImagenUsuario = imageBitmap;
                    imagenUsuario.setImageBitmap(imageBitmap);
                    loadedImage = true;
                }
                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                checkSettings();
                return;
            case REQUEST_IMAGE_CAPTURE:
                tomarFoto();
                return;
            case IMAGE_PICKER_REQUEST:
                abrirGaleria();
                return;

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

    public void registrar(View v)
    {
        Usuario nuevo = validarRegistro();
        if( nuevo != null)
        {
            crearUsuario(nuevo);
        }
    }

    public void crearUsuario(Usuario nuevo)
    {
        mAuth.createUserWithEmailAndPassword(correo, contra)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            almacenarDatosUsuario(user, nuevo);
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getBaseContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        updateUI(user);
    }

    @Override
    protected void onResume(){
        super.onResume();
        FirebaseUser user = mAuth.getCurrentUser();
        updateUI(user);
    }

    private void updateUI(FirebaseUser user){
        if(user != null){
            startActivity(new Intent(this, HomeUsuarioActivity.class));
            finish();
        }else{

        }
    }

    public void almacenarDatosUsuario(FirebaseUser user, Usuario nuevo)
    {
        myRef=database.getReference(PATH_USERS+user.getUid());
        myRef.setValue(nuevo);
        if(loadedImage)
        {
            guardarFoto();
        }

    }

    public Usuario validarRegistro()
    {
        Usuario nuevo = new Usuario();
        String apellido, nombre, correo, contra, identi;
        apellido = textApellido.getText().toString();
        if(TextUtils.isEmpty(apellido))
        {
            textApellido.setError(null);
            return null;
        }
        nombre = textNombre.getText().toString();
        if(TextUtils.isEmpty(nombre))
        {
            textNombre.setError(null);
            return null;
        }
        correo = textCorreo.getText().toString();
        if(TextUtils.isEmpty(correo) || !isEmailValid(correo))
        {
            Toast.makeText(this, "Ingrese un correo válido por favor",Toast.LENGTH_LONG).show();
            textCorreo.setError(null);
            return null;
        }
        contra = textContra.getText().toString();
        if(TextUtils.isEmpty(contra) || !isPasswordValid(contra))
        {
            textContra.setError(null);
            textContra.setText("");
            Toast.makeText(this, "Ingrese una contraseña de más de 6 caracteres",Toast.LENGTH_LONG).show();
            return null;
        }
        identi = textIdenti.getText().toString();
        if(TextUtils.isEmpty(identi))
        {
            textIdenti.setError(null);
            return null;
        }
        if(TextUtils.isEmpty(latlng))
        {
            Toast.makeText(this,"Es necesario obtener su ubicacion", Toast.LENGTH_LONG);
            return null;
        }
        nuevo.setDisponible("false");
        nuevo.setNotificado("false");
        nuevo.setApellidoUsuario(apellido);
        nuevo.setNombreUsuario(nombre);
        nuevo.setIdentificacion((String) spinnerTipoIdenti.getSelectedItem() + identi);
        nuevo.setUbicacionActual(latlng);
        this.correo = correo;
        this.contra = contra;
        return nuevo;
    }

    private boolean isPasswordValid(String password)
    {
        if(password.length() < 6)
        {
            return false;
        }
        return true;
    }

    private boolean isEmailValid(String email) {
        if (!email.contains("@") ||
                !email.contains(".") ||
                email.length() < 5)
            return false;
        return true;
    }

    public void toAtras(View v)
    {
        finish();
    }
}