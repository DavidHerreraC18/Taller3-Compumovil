package com.taller.myapplication.services.FirebaseServices;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.taller.myapplication.HomeUsuarioActivity;
import com.taller.myapplication.InicioSesionActivity;
import com.taller.myapplication.SeguimientoUsuarioDisponible;
import com.taller.myapplication.model.Usuario;


public class FirebaseListenerService extends JobIntentService {
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth firebaseAuth;
    DatabaseReference myRef;
    DatabaseReference estadoRef;
    Query estadoQuery;

    private static final int JOB_ID = 12;
    public static final String TAG = "FIREBASE_SERVICE";

    public static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, FirebaseListenerService.class, JOB_ID, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        myRef = firebaseDatabase.getReference(HomeUsuarioActivity.PATH_USERS);
        //estadoQuery = myRef.child("disponible");
        //estadoRef = ;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onHandleWork(@NonNull Intent intent) {


        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Usuario usuario = dataSnapshot.getValue(Usuario.class);
                if(!dataSnapshot.getKey().equals(firebaseAuth.getCurrentUser().getUid())) {
                    if (usuario.getDisponible().equals("true")) {
                        buildAndShowNotification("Nuevo Usuario Activo", "El usuario "
                                + usuario.getNombreUsuario() + " " + usuario.getApellidoUsuario() + " se encuentra activo", dataSnapshot.getKey());
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void buildAndShowNotification(String title, String message, String id){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, HomeUsuarioActivity.CHANNEL_ID);
        mBuilder.setSmallIcon(android.R.drawable.star_on);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(message);
        mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent;
        if(firebaseAuth.getCurrentUser()!= null){
            intent = new Intent(this, SeguimientoUsuarioDisponible.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            Log.w(TAG, id);
            intent.putExtra("id_usuario_presionado",id);
        }else{
            intent = new Intent(this, InicioSesionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true);

        int notificationId = 001;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId es un entero unico definido para cada notificacion que se lanza
        notificationManager.notify(notificationId, mBuilder.build());
    }
}